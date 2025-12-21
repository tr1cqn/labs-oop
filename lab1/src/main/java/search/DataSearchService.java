package search;

import entity.User;
import entity.Function;
import entity.Point;
import entity.Result;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;
import java.lang.reflect.Field;

/**
 * Сервис для поиска данных с поддержкой различных алгоритмов поиска
 */
public class DataSearchService {
    private static final Logger logger = LogManager.getLogger(DataSearchService.class);
    
    /**
     * Поиск в глубину (Depth-First Search)
     * Обходит иерархию: User -> Function -> Points/Results
     */
    public List<Object> searchDFS(List<HierarchicalDataNode> nodes, SearchCriteria criteria) {
        logger.info("Начало поиска в глубину (DFS) с критериями: {}", criteria);
        long startTime = System.currentTimeMillis();
        
        List<Object> results = new ArrayList<>();
        Set<Long> visitedUsers = new HashSet<>();
        Set<Long> visitedFunctions = new HashSet<>();
        
        for (HierarchicalDataNode node : nodes) {
            if (node.getUser() != null && !visitedUsers.contains(node.getUser().getId())) {
                dfsTraversal(node, criteria, results, visitedUsers, visitedFunctions);
            }
        }
        
        // Применяем сортировку если указана
        if (criteria.getSortField() != null) {
            results = sortResults(results, criteria);
            logger.debug("Результаты отсортированы по полю: {}, направление: {}", 
                criteria.getSortField(), criteria.getSortDirection());
        }
        
        long endTime = System.currentTimeMillis();
        logger.info("Поиск в глубину завершен. Найдено результатов: {}, время выполнения: {} мс", 
            results.size(), (endTime - startTime));
        
        return results;
    }
    
    /**
     * Рекурсивный обход в глубину
     */
    private void dfsTraversal(HierarchicalDataNode node, SearchCriteria criteria, 
                             List<Object> results, Set<Long> visitedUsers, Set<Long> visitedFunctions) {
        if (node.getUser() != null) {
            visitedUsers.add(node.getUser().getId());
            
            // Проверяем критерии для пользователя
            if (matchesCriteria(node.getUser(), criteria)) {
                results.add(node.getUser());
                logger.debug("Найден пользователь по критериям: id={}, login={}", 
                    node.getUser().getId(), node.getUser().getLogin());
            }
            
            // Рекурсивно обходим функции
            for (HierarchicalDataNode.FunctionNode funcNode : node.getFunctions()) {
                if (funcNode.getFunction() != null && 
                    !visitedFunctions.contains(funcNode.getFunction().getId())) {
                    visitedFunctions.add(funcNode.getFunction().getId());
                    
                    // Проверяем критерии для функции
                    if (matchesCriteria(funcNode.getFunction(), criteria)) {
                        results.add(funcNode.getFunction());
                        logger.debug("Найдена функция по критериям: id={}, name={}", 
                            funcNode.getFunction().getId(), funcNode.getFunction().getName());
                    }
                    
                    // Проверяем точки
                    for (Point point : funcNode.getPoints()) {
                        if (matchesCriteria(point, criteria)) {
                            results.add(point);
                            logger.debug("Найдена точка по критериям: id={}, x={}, y={}", 
                                point.getId(), point.getXValue(), point.getYValue());
                        }
                    }
                    
                    // Проверяем результаты
                    for (Result result : funcNode.getResults()) {
                        if (matchesCriteria(result, criteria)) {
                            results.add(result);
                            logger.debug("Найден результат по критериям: id={}, resultId={}", 
                                result.getId(), result.getFunction() != null ? result.getFunction().getId() : null);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Поиск в ширину (Breadth-First Search)
     * Обходит иерархию по уровням: все Users -> все Functions -> все Points/Results
     */
    public List<Object> searchBFS(List<HierarchicalDataNode> nodes, SearchCriteria criteria) {
        logger.info("Начало поиска в ширину (BFS) с критериями: {}", criteria);
        long startTime = System.currentTimeMillis();
        
        List<Object> results = new ArrayList<>();
        Queue<HierarchicalDataNode> queue = new LinkedList<>();
        Set<Long> visitedUsers = new HashSet<>();
        Set<Long> visitedFunctions = new HashSet<>();
        
        // Добавляем все корневые узлы в очередь
        for (HierarchicalDataNode node : nodes) {
            if (node.getUser() != null && !visitedUsers.contains(node.getUser().getId())) {
                queue.offer(node);
                visitedUsers.add(node.getUser().getId());
            }
        }
        
        // Обходим уровень Users
        int levelSize = queue.size();
        while (!queue.isEmpty()) {
            HierarchicalDataNode current = queue.poll();
            
            if (current.getUser() != null && matchesCriteria(current.getUser(), criteria)) {
                results.add(current.getUser());
                logger.debug("Найден пользователь (BFS): id={}, login={}", 
                    current.getUser().getId(), current.getUser().getLogin());
            }
            
            levelSize--;
            if (levelSize == 0) {
                // Переходим к следующему уровню - Functions
                levelSize = queue.size();
                for (HierarchicalDataNode node : new ArrayList<>(queue)) {
                    for (HierarchicalDataNode.FunctionNode funcNode : node.getFunctions()) {
                        if (funcNode.getFunction() != null && 
                            !visitedFunctions.contains(funcNode.getFunction().getId())) {
                            visitedFunctions.add(funcNode.getFunction().getId());
                            
                            if (matchesCriteria(funcNode.getFunction(), criteria)) {
                                results.add(funcNode.getFunction());
                                logger.debug("Найдена функция (BFS): id={}, name={}", 
                                    funcNode.getFunction().getId(), funcNode.getFunction().getName());
                            }
                            
                            // Проверяем Points и Results на этом уровне
                            for (Point point : funcNode.getPoints()) {
                                if (matchesCriteria(point, criteria)) {
                                    results.add(point);
                                    logger.debug("Найдена точка (BFS): id={}, x={}, y={}", 
                                        point.getId(), point.getXValue(), point.getYValue());
                                }
                            }
                            
                            for (Result result : funcNode.getResults()) {
                                if (matchesCriteria(result, criteria)) {
                                    results.add(result);
                                    logger.debug("Найден результат (BFS): id={}, resultId={}", 
                                        result.getId(), result.getFunction() != null ? result.getFunction().getId() : null);
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Применяем сортировку если указана
        if (criteria.getSortField() != null) {
            results = sortResults(results, criteria);
            logger.debug("Результаты отсортированы по полю: {}, направление: {}", 
                criteria.getSortField(), criteria.getSortDirection());
        }
        
        long endTime = System.currentTimeMillis();
        logger.info("Поиск в ширину завершен. Найдено результатов: {}, время выполнения: {} мс", 
            results.size(), (endTime - startTime));
        
        return results;
    }
    
    /**
     * Поиск по иерархии
     * Возвращает полную иерархическую структуру с найденными элементами
     */
    public List<HierarchicalDataNode> searchHierarchical(List<HierarchicalDataNode> nodes, SearchCriteria criteria) {
        logger.info("Начало иерархического поиска с критериями: {}", criteria);
        long startTime = System.currentTimeMillis();
        
        List<HierarchicalDataNode> results = new ArrayList<>();
        
        for (HierarchicalDataNode node : nodes) {
            HierarchicalDataNode resultNode = null;
            
            // Проверяем пользователя
            if (node.getUser() != null && matchesCriteria(node.getUser(), criteria)) {
                resultNode = new HierarchicalDataNode(node.getUser());
                logger.debug("Пользователь соответствует критериям: id={}, login={}", 
                    node.getUser().getId(), node.getUser().getLogin());
            }
            
            // Проверяем функции и их дочерние элементы
            for (HierarchicalDataNode.FunctionNode funcNode : node.getFunctions()) {
                boolean functionMatches = funcNode.getFunction() != null && 
                    matchesCriteria(funcNode.getFunction(), criteria);
                boolean hasMatchingChildren = false;
                
                HierarchicalDataNode.FunctionNode resultFuncNode = null;
                
                if (functionMatches) {
                    resultFuncNode = new HierarchicalDataNode.FunctionNode(funcNode.getFunction());
                    hasMatchingChildren = true;
                    logger.debug("Функция соответствует критериям: id={}, name={}", 
                        funcNode.getFunction().getId(), funcNode.getFunction().getName());
                } else {
                    // Проверяем дочерние элементы
                    for (Point point : funcNode.getPoints()) {
                        if (matchesCriteria(point, criteria)) {
                            if (resultFuncNode == null) {
                                resultFuncNode = new HierarchicalDataNode.FunctionNode(funcNode.getFunction());
                            }
                            resultFuncNode.addPoint(point);
                            hasMatchingChildren = true;
                            logger.debug("Точка соответствует критериям: id={}, x={}, y={}", 
                                point.getId(), point.getXValue(), point.getYValue());
                        }
                    }
                    
                    for (Result result : funcNode.getResults()) {
                        if (matchesCriteria(result, criteria)) {
                            if (resultFuncNode == null) {
                                resultFuncNode = new HierarchicalDataNode.FunctionNode(funcNode.getFunction());
                            }
                            resultFuncNode.addResult(result);
                            hasMatchingChildren = true;
                            logger.debug("Результат соответствует критериям: id={}, resultId={}", 
                                result.getId(), result.getFunction() != null ? result.getFunction().getId() : null);
                        }
                    }
                }
                
                // Если функция или её дочерние элементы соответствуют критериям
                if (hasMatchingChildren && resultFuncNode != null) {
                    if (resultNode == null) {
                        resultNode = new HierarchicalDataNode(node.getUser());
                    }
                    resultNode.addFunction(resultFuncNode);
                }
            }
            
            if (resultNode != null && !resultNode.getFunctions().isEmpty()) {
                results.add(resultNode);
            }
        }
        
        long endTime = System.currentTimeMillis();
        logger.info("Иерархический поиск завершен. Найдено узлов: {}, время выполнения: {} мс", 
            results.size(), (endTime - startTime));
        
        return results;
    }
    
    /**
     * Одиночный поиск - поиск по одному критерию
     */
    public <T> List<T> searchSingle(List<T> data, SearchCriteria criteria) {
        logger.info("Начало одиночного поиска по полю: {}, оператор: {}, значение: {}", 
            criteria.getFieldName(), criteria.getOperator(), criteria.getValue());
        long startTime = System.currentTimeMillis();
        
        if (criteria.isMultiple()) {
            logger.warn("Использован метод одиночного поиска с множественными критериями. " +
                "Рекомендуется использовать searchMultiple");
        }
        
        List<T> results = data.stream()
            .filter(item -> matchesCriteria(item, criteria))
            .collect(Collectors.toList());
        
        // Применяем сортировку если указана
        if (criteria.getSortField() != null) {
            Comparator<T> comparator = criteria.createComparator();
            if (comparator != null) {
                results.sort(comparator);
                logger.debug("Результаты отсортированы по полю: {}, направление: {}", 
                    criteria.getSortField(), criteria.getSortDirection());
            }
        }
        
        long endTime = System.currentTimeMillis();
        logger.info("Одиночный поиск завершен. Найдено результатов: {}, время выполнения: {} мс", 
            results.size(), (endTime - startTime));
        
        return results;
    }
    
    /**
     * Множественный поиск - поиск по нескольким критериям (AND логика)
     */
    public <T> List<T> searchMultiple(List<T> data, SearchCriteria criteria) {
        logger.info("Начало множественного поиска с количеством критериев: {}", 
            criteria.getMultipleCriteria().size());
        long startTime = System.currentTimeMillis();
        
        if (!criteria.isMultiple()) {
            logger.warn("Использован метод множественного поиска с одиночным критерием. " +
                "Рекомендуется использовать searchSingle");
        }
        
        List<T> results = data.stream()
            .filter(item -> {
                // Все критерии должны выполняться (AND)
                for (SearchCriteria singleCriteria : criteria.getMultipleCriteria()) {
                    if (!matchesCriteria(item, singleCriteria)) {
                        return false;
                    }
                }
                return true;
            })
            .collect(Collectors.toList());
        
        // Применяем сортировку если указана
        if (criteria.getSortField() != null) {
            Comparator<T> comparator = criteria.createComparator();
            if (comparator != null) {
                results.sort(comparator);
                logger.debug("Результаты отсортированы по полю: {}, направление: {}", 
                    criteria.getSortField(), criteria.getSortDirection());
            }
        }
        
        long endTime = System.currentTimeMillis();
        logger.info("Множественный поиск завершен. Найдено результатов: {}, время выполнения: {} мс", 
            results.size(), (endTime - startTime));
        
        return results;
    }
    
    /**
     * Сортировка результатов по указанному полю
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> sortResults(List<T> results, SearchCriteria criteria) {
        if (criteria.getSortField() == null || results.isEmpty()) {
            return results;
        }
        
        logger.debug("Сортировка {} результатов по полю: {}, направление: {}", 
            results.size(), criteria.getSortField(), criteria.getSortDirection());
        
        Comparator<T> comparator = criteria.createComparator();
        if (comparator != null) {
            results.sort(comparator);
            logger.debug("Сортировка завершена успешно");
        } else {
            logger.warn("Не удалось создать компаратор для поля: {}", criteria.getSortField());
        }
        
        return results;
    }
    
    /**
     * Проверяет, соответствует ли объект критериям поиска
     */
    @SuppressWarnings("unchecked")
    private boolean matchesCriteria(Object obj, SearchCriteria criteria) {
        if (obj == null || criteria == null) {
            return false;
        }
        
        // Если множественные критерии, проверяем каждый
        if (criteria.isMultiple()) {
            for (SearchCriteria singleCriteria : criteria.getMultipleCriteria()) {
                if (!matchesSingleCriteria(obj, singleCriteria)) {
                    return false;
                }
            }
            return true;
        }
        
        return matchesSingleCriteria(obj, criteria);
    }
    
    /**
     * Проверяет соответствие одному критерию
     */
    @SuppressWarnings("unchecked")
    private boolean matchesSingleCriteria(Object obj, SearchCriteria criteria) {
        if (criteria.getFieldName() == null || criteria.getValue() == null) {
            return false;
        }
        
        try {
            Field field = getField(obj.getClass(), criteria.getFieldName());
            if (field == null) {
                logger.debug("Поле {} не найдено в классе {}", 
                    criteria.getFieldName(), obj.getClass().getSimpleName());
                return false;
            }
            
            field.setAccessible(true);
            Object fieldValue = field.get(obj);
            
            if (fieldValue == null) {
                return false;
            }
            
            boolean matches = false;
            
            switch (criteria.getOperator()) {
                case EQUALS:
                    matches = fieldValue.equals(criteria.getValue());
                    break;
                case CONTAINS:
                    if (fieldValue instanceof String && criteria.getValue() instanceof String) {
                        matches = ((String) fieldValue).toLowerCase()
                            .contains(((String) criteria.getValue()).toLowerCase());
                    }
                    break;
                case STARTS_WITH:
                    if (fieldValue instanceof String && criteria.getValue() instanceof String) {
                        matches = ((String) fieldValue).toLowerCase()
                            .startsWith(((String) criteria.getValue()).toLowerCase());
                    }
                    break;
                case ENDS_WITH:
                    if (fieldValue instanceof String && criteria.getValue() instanceof String) {
                        matches = ((String) fieldValue).toLowerCase()
                            .endsWith(((String) criteria.getValue()).toLowerCase());
                    }
                    break;
                case GREATER_THAN:
                    if (fieldValue instanceof Comparable && criteria.getValue() instanceof Comparable) {
                        matches = ((Comparable<Object>) fieldValue)
                            .compareTo(criteria.getValue()) > 0;
                    }
                    break;
                case LESS_THAN:
                    if (fieldValue instanceof Comparable && criteria.getValue() instanceof Comparable) {
                        matches = ((Comparable<Object>) fieldValue)
                            .compareTo(criteria.getValue()) < 0;
                    }
                    break;
                case GREATER_OR_EQUAL:
                    if (fieldValue instanceof Comparable && criteria.getValue() instanceof Comparable) {
                        matches = ((Comparable<Object>) fieldValue)
                            .compareTo(criteria.getValue()) >= 0;
                    }
                    break;
                case LESS_OR_EQUAL:
                    if (fieldValue instanceof Comparable && criteria.getValue() instanceof Comparable) {
                        matches = ((Comparable<Object>) fieldValue)
                            .compareTo(criteria.getValue()) <= 0;
                    }
                    break;
            }
            
            return matches;
            
        } catch (Exception e) {
            logger.error("Ошибка при проверке критериев для объекта класса {}: {}", 
                obj.getClass().getSimpleName(), e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Рекурсивно ищет поле в классе и его суперклассах
     */
    private Field getField(Class<?> clazz, String fieldName) {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }
}

