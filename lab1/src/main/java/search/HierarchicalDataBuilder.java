package search;

import entity.User;
import entity.Function;
import entity.Point;
import entity.Result;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Вспомогательный класс для построения иерархической структуры данных
 */
@Service
public class HierarchicalDataBuilder {
    private static final Logger logger = LogManager.getLogger(HierarchicalDataBuilder.class);
    
    /**
     * Строит иерархическую структуру из списков Entity
     * 
     * @param users список пользователей
     * @param functions список функций
     * @param points список точек
     * @param results список результатов
     * @return список иерархических узлов
     */
    public List<HierarchicalDataNode> buildHierarchy(
            List<User> users,
            List<Function> functions,
            List<Point> points,
            List<Result> results) {
        
        logger.info("Начало построения иерархической структуры. " +
            "Users: {}, Functions: {}, Points: {}, Results: {}", 
            users.size(), functions.size(), points.size(), results.size());
        
        long startTime = System.currentTimeMillis();
        
        // Создаем карты для быстрого доступа
        Map<Long, HierarchicalDataNode> userNodes = new HashMap<>();
        Map<Long, List<Function>> functionsByUserId = new HashMap<>();
        Map<Long, List<Point>> pointsByFunctionId = new HashMap<>();
        Map<Long, List<Result>> resultsByFunctionId = new HashMap<>();
        
        // Группируем функции по пользователям
        for (Function function : functions) {
            if (function.getUser() != null) {
                Long userId = function.getUser().getId();
                functionsByUserId.computeIfAbsent(userId, k -> new ArrayList<>()).add(function);
            }
        }
        
        // Группируем точки по функциям
        for (Point point : points) {
            if (point.getFunction() != null) {
                Long funcId = point.getFunction().getId();
                pointsByFunctionId.computeIfAbsent(funcId, k -> new ArrayList<>()).add(point);
            }
        }
        
        // Группируем результаты по функциям
        for (Result result : results) {
            if (result.getFunction() != null) {
                Long funcId = result.getFunction().getId();
                resultsByFunctionId.computeIfAbsent(funcId, k -> new ArrayList<>()).add(result);
            }
        }
        
        // Строим иерархию
        for (User user : users) {
            HierarchicalDataNode userNode = new HierarchicalDataNode(user);
            userNodes.put(user.getId(), userNode);
            
            // Добавляем функции пользователя
            List<Function> userFunctions = functionsByUserId.getOrDefault(user.getId(), new ArrayList<>());
            for (Function function : userFunctions) {
                HierarchicalDataNode.FunctionNode functionNode = 
                    new HierarchicalDataNode.FunctionNode(function);
                
                // Добавляем точки функции
                List<Point> functionPoints = pointsByFunctionId.getOrDefault(
                    function.getId(), new ArrayList<>());
                for (Point point : functionPoints) {
                    functionNode.addPoint(point);
                }
                
                // Добавляем результаты функции
                List<Result> functionResults = resultsByFunctionId.getOrDefault(
                    function.getId(), new ArrayList<>());
                for (Result result : functionResults) {
                    functionNode.addResult(result);
                }
                
                userNode.addFunction(functionNode);
            }
            
            logger.debug("Построен узел пользователя: id={}, login={}, функций: {}", 
                user.getId(), user.getLogin(), userNode.getFunctions().size());
        }
        
        List<HierarchicalDataNode> result = new ArrayList<>(userNodes.values());
        
        long endTime = System.currentTimeMillis();
        logger.info("Иерархическая структура построена. Узлов: {}, время выполнения: {} мс", 
            result.size(), (endTime - startTime));
        
        return result;
    }
    
    /**
     * Строит иерархию из User Entity с загруженными связями
     */
    public List<HierarchicalDataNode> buildHierarchyFromUsers(List<User> users) {
        logger.info("Построение иерархии из {} пользователей с загруженными связями", users.size());
        long startTime = System.currentTimeMillis();
        
        List<HierarchicalDataNode> result = new ArrayList<>();
        
        for (User user : users) {
            HierarchicalDataNode userNode = new HierarchicalDataNode(user);
            
            // Используем связи из Entity (Hibernate загрузит их)
            if (user.getFunctions() != null) {
                for (Function function : user.getFunctions()) {
                    HierarchicalDataNode.FunctionNode functionNode = 
                        new HierarchicalDataNode.FunctionNode(function);
                    
                    // Добавляем точки и результаты из Entity
                    if (function.getPoints() != null) {
                        functionNode.setPoints(new ArrayList<>(function.getPoints()));
                    }
                    if (function.getResults() != null) {
                        functionNode.setResults(new ArrayList<>(function.getResults()));
                    }
                    
                    userNode.addFunction(functionNode);
                }
            }
            
            result.add(userNode);
            logger.debug("Построен узел пользователя: id={}, login={}, функций: {}", 
                user.getId(), user.getLogin(), userNode.getFunctions().size());
        }
        
        long endTime = System.currentTimeMillis();
        logger.info("Иерархическая структура построена из Entity. Узлов: {}, время выполнения: {} мс", 
            result.size(), (endTime - startTime));
        
        return result;
    }
}

