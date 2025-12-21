package database.search;

import database.dto.UserDTO;
import database.dto.FunctionDTO;
import database.dto.PointDTO;
import database.dto.ResultDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * Вспомогательный класс для построения иерархической структуры данных
 */
public class HierarchicalDataBuilder {
    private static final Logger logger = LogManager.getLogger(HierarchicalDataBuilder.class);
    
    /**
     * Строит иерархическую структуру из списков DTO
     * 
     * @param users список пользователей
     * @param functions список функций
     * @param points список точек
     * @param results список результатов
     * @return список иерархических узлов
     */
    public List<HierarchicalDataNode> buildHierarchy(
            List<UserDTO> users,
            List<FunctionDTO> functions,
            List<PointDTO> points,
            List<ResultDTO> results) {
        
        logger.info("Начало построения иерархической структуры. " +
            "Users: {}, Functions: {}, Points: {}, Results: {}", 
            users.size(), functions.size(), points.size(), results.size());
        
        long startTime = System.currentTimeMillis();
        
        // Создаем карты для быстрого доступа
        Map<Long, HierarchicalDataNode> userNodes = new HashMap<>();
        Map<Long, List<FunctionDTO>> functionsByUserId = new HashMap<>();
        Map<Long, List<PointDTO>> pointsByFunctionId = new HashMap<>();
        Map<Long, List<ResultDTO>> resultsByFunctionId = new HashMap<>();
        
        // Группируем функции по пользователям
        for (FunctionDTO function : functions) {
            functionsByUserId.computeIfAbsent(function.getUserId(), k -> new ArrayList<>()).add(function);
        }
        
        // Группируем точки по функциям
        for (PointDTO point : points) {
            pointsByFunctionId.computeIfAbsent(point.getFuncId(), k -> new ArrayList<>()).add(point);
        }
        
        // Группируем результаты по функциям
        for (ResultDTO result : results) {
            resultsByFunctionId.computeIfAbsent(result.getResultId(), k -> new ArrayList<>()).add(result);
        }
        
        // Строим иерархию
        for (UserDTO user : users) {
            HierarchicalDataNode userNode = new HierarchicalDataNode(user);
            userNodes.put(user.getId(), userNode);
            
            // Добавляем функции пользователя
            List<FunctionDTO> userFunctions = functionsByUserId.getOrDefault(user.getId(), new ArrayList<>());
            for (FunctionDTO function : userFunctions) {
                HierarchicalDataNode.FunctionNode functionNode = 
                    new HierarchicalDataNode.FunctionNode(function);
                
                // Добавляем точки функции
                List<PointDTO> functionPoints = pointsByFunctionId.getOrDefault(
                    function.getId(), new ArrayList<>());
                for (PointDTO point : functionPoints) {
                    functionNode.addPoint(point);
                }
                
                // Добавляем результаты функции
                List<ResultDTO> functionResults = resultsByFunctionId.getOrDefault(
                    function.getId(), new ArrayList<>());
                for (ResultDTO result : functionResults) {
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
     * Строит иерархию только для одного пользователя
     */
    public HierarchicalDataNode buildHierarchyForUser(
            UserDTO user,
            List<FunctionDTO> allFunctions,
            List<PointDTO> allPoints,
            List<ResultDTO> allResults) {
        
        logger.debug("Построение иерархии для пользователя: id={}, login={}", 
            user.getId(), user.getLogin());
        
        HierarchicalDataNode userNode = new HierarchicalDataNode(user);
        
        // Фильтруем функции пользователя
        List<FunctionDTO> userFunctions = new ArrayList<>();
        for (FunctionDTO function : allFunctions) {
            if (function.getUserId().equals(user.getId())) {
                userFunctions.add(function);
            }
        }
        
        // Строим узлы функций
        for (FunctionDTO function : userFunctions) {
            HierarchicalDataNode.FunctionNode functionNode = 
                new HierarchicalDataNode.FunctionNode(function);
            
            // Добавляем точки
            for (PointDTO point : allPoints) {
                if (point.getFuncId().equals(function.getId())) {
                    functionNode.addPoint(point);
                }
            }
            
            // Добавляем результаты
            for (ResultDTO result : allResults) {
                if (result.getResultId().equals(function.getId())) {
                    functionNode.addResult(result);
                }
            }
            
            userNode.addFunction(functionNode);
        }
        
        logger.debug("Иерархия для пользователя построена. Функций: {}", 
            userNode.getFunctions().size());
        
        return userNode;
    }
}

