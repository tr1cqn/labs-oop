package com.example.lab6.controller;

import com.example.lab6.dto.FunctionDTO;
import com.example.lab6.dto.PointDTO;
import com.example.lab6.dto.ResultDTO;
import com.example.lab6.dto.UserDTO;
import com.example.lab6.mapper.FunctionMapper;
import com.example.lab6.mapper.PointMapper;
import com.example.lab6.mapper.ResultMapper;
import com.example.lab6.mapper.UserMapper;
import entity.Function;
import entity.Point;
import entity.Result;
import entity.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import repository.FunctionRepository;
import repository.PointRepository;
import repository.ResultRepository;
import repository.UserRepository;
import com.example.lab6.security.AuthUtil;
import search.DataSearchService;
import search.HierarchicalDataBuilder;
import search.HierarchicalDataNode;
import search.SearchCriteria;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Контроллер для работы с системой поиска
 * Реализует все операции поиска из API контракта
 */
@RestController
@RequestMapping("/api/v1/search")
public class SearchController {
    private static final Logger logger = LogManager.getLogger(SearchController.class);
    private final DataSearchService searchService;
    private final HierarchicalDataBuilder hierarchicalBuilder;
    private final UserRepository userRepository;
    private final FunctionRepository functionRepository;
    private final PointRepository pointRepository;
    private final ResultRepository resultRepository;

    public SearchController(
            DataSearchService searchService,
            HierarchicalDataBuilder hierarchicalBuilder,
            UserRepository userRepository,
            FunctionRepository functionRepository,
            PointRepository pointRepository,
            ResultRepository resultRepository) {
        this.searchService = searchService;
        this.hierarchicalBuilder = hierarchicalBuilder;
        this.userRepository = userRepository;
        this.functionRepository = functionRepository;
        this.pointRepository = pointRepository;
        this.resultRepository = resultRepository;
    }

    /**
     * Поиск в глубину (Depth-First Search)
     * POST /api/v1/search/dfs
     */
    @PostMapping("/dfs")
    public ResponseEntity<Map<String, Object>> searchDFS(@RequestBody Map<String, Object> request, Authentication auth) {
        logger.info("POST /api/v1/search/dfs - поиск в глубину");
        if (!AuthUtil.isAdmin(auth)) {
            logger.warn("FORBIDDEN search dfs login={}", auth == null ? null : auth.getName());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        long startTime = System.currentTimeMillis();
        try {
            SearchCriteria criteria = buildSearchCriteria(request);
            List<HierarchicalDataNode> nodes = buildHierarchicalNodes();
            
            List<Object> results = searchService.searchDFS(nodes, criteria);
            
            List<Map<String, Object>> responseData = results.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            
            long executionTime = System.currentTimeMillis() - startTime;
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", responseData);
            response.put("executionTime", executionTime);
            response.put("total", responseData.size());
            
            logger.info("Поиск в глубину завершен. Найдено результатов: {}, время выполнения: {} мс", 
                    responseData.size(), executionTime);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Ошибка при выполнении поиска в глубину", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Поиск в ширину (Breadth-First Search)
     * POST /api/v1/search/bfs
     */
    @PostMapping("/bfs")
    public ResponseEntity<Map<String, Object>> searchBFS(@RequestBody Map<String, Object> request, Authentication auth) {
        logger.info("POST /api/v1/search/bfs - поиск в ширину");
        if (!AuthUtil.isAdmin(auth)) {
            logger.warn("FORBIDDEN search bfs login={}", auth == null ? null : auth.getName());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        long startTime = System.currentTimeMillis();
        try {
            SearchCriteria criteria = buildSearchCriteria(request);
            List<HierarchicalDataNode> nodes = buildHierarchicalNodes();
            
            List<Object> results = searchService.searchBFS(nodes, criteria);
            
            List<Map<String, Object>> responseData = results.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            
            long executionTime = System.currentTimeMillis() - startTime;
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", responseData);
            response.put("executionTime", executionTime);
            response.put("total", responseData.size());
            
            logger.info("Поиск в ширину завершен. Найдено результатов: {}, время выполнения: {} мс", 
                    responseData.size(), executionTime);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Ошибка при выполнении поиска в ширину", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Поиск по иерархии
     * POST /api/v1/search/hierarchical
     */
    @PostMapping("/hierarchical")
    public ResponseEntity<Map<String, Object>> searchHierarchical(@RequestBody Map<String, Object> request, Authentication auth) {
        logger.info("POST /api/v1/search/hierarchical - поиск по иерархии");
        if (!AuthUtil.isAdmin(auth)) {
            logger.warn("FORBIDDEN search hierarchical login={}", auth == null ? null : auth.getName());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        long startTime = System.currentTimeMillis();
        try {
            SearchCriteria criteria = buildSearchCriteria(request);
            List<HierarchicalDataNode> nodes = buildHierarchicalNodes();
            
            List<HierarchicalDataNode> results = searchService.searchHierarchical(nodes, criteria);
            
            List<Map<String, Object>> responseData = results.stream()
                    .map(this::convertHierarchicalNode)
                    .collect(Collectors.toList());
            
            long executionTime = System.currentTimeMillis() - startTime;
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", responseData);
            response.put("executionTime", executionTime);
            response.put("total", responseData.size());
            
            logger.info("Иерархический поиск завершен. Найдено узлов: {}, время выполнения: {} мс", 
                    responseData.size(), executionTime);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Ошибка при выполнении иерархического поиска", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Одиночный поиск
     * POST /api/v1/search/single
     */
    @PostMapping("/single")
    public ResponseEntity<Map<String, Object>> searchSingle(@RequestBody Map<String, Object> request, Authentication auth) {
        logger.info("POST /api/v1/search/single - одиночный поиск");
        if (!AuthUtil.isAdmin(auth)) {
            logger.warn("FORBIDDEN search single login={}", auth == null ? null : auth.getName());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        long startTime = System.currentTimeMillis();
        try {
            String entityType = (String) request.get("entityType");
            SearchCriteria criteria = buildSearchCriteria((Map<String, Object>) request.get("criteria"));
            
            List<Object> entities = getEntitiesByType(entityType);
            List<Object> results = searchService.searchSingle(entities, criteria);
            
            List<Object> responseData = results.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            
            long executionTime = System.currentTimeMillis() - startTime;
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", responseData);
            response.put("executionTime", executionTime);
            response.put("total", responseData.size());
            
            logger.info("Одиночный поиск завершен. Найдено результатов: {}, время выполнения: {} мс", 
                    responseData.size(), executionTime);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Ошибка при выполнении одиночного поиска", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Множественный поиск
     * POST /api/v1/search/multiple
     */
    @PostMapping("/multiple")
    public ResponseEntity<Map<String, Object>> searchMultiple(@RequestBody Map<String, Object> request, Authentication auth) {
        logger.info("POST /api/v1/search/multiple - множественный поиск");
        if (!AuthUtil.isAdmin(auth)) {
            logger.warn("FORBIDDEN search multiple login={}", auth == null ? null : auth.getName());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        long startTime = System.currentTimeMillis();
        try {
            String entityType = (String) request.get("entityType");
            @SuppressWarnings("unchecked")
            Map<String, Object> criteriaMap = (Map<String, Object>) request.get("criteria");
            
            SearchCriteria criteria = buildMultipleSearchCriteria(criteriaMap);
            
            List<Object> entities = getEntitiesByType(entityType);
            List<Object> results = searchService.searchMultiple(entities, criteria);
            
            List<Object> responseData = results.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            
            long executionTime = System.currentTimeMillis() - startTime;
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", responseData);
            response.put("executionTime", executionTime);
            response.put("total", responseData.size());
            
            logger.info("Множественный поиск завершен. Найдено результатов: {}, время выполнения: {} мс", 
                    responseData.size(), executionTime);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Ошибка при выполнении множественного поиска", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Вспомогательный метод для построения критериев поиска
     */
    @SuppressWarnings("unchecked")
    private SearchCriteria buildSearchCriteria(Map<String, Object> request) {
        Map<String, Object> criteriaMap = (Map<String, Object>) request.get("criteria");
        if (criteriaMap == null) {
            criteriaMap = request;
        }
        
        String fieldName = (String) criteriaMap.get("fieldName");
        Object value = criteriaMap.get("value");
        String operatorStr = (String) criteriaMap.get("operator");
        SearchCriteria.SearchOperator operator = SearchCriteria.SearchOperator.EQUALS;
        
        if (operatorStr != null) {
            try {
                operator = SearchCriteria.SearchOperator.valueOf(operatorStr);
            } catch (IllegalArgumentException e) {
                logger.warn("Неизвестный оператор: {}, используется EQUALS", operatorStr);
            }
        }
        
        SearchCriteria criteria = new SearchCriteria(fieldName, value, operator);
        
        String sortField = (String) criteriaMap.get("sortField");
        String sortDirectionStr = (String) criteriaMap.get("sortDirection");
        if (sortField != null) {
            criteria.setSortField(sortField);
            if (sortDirectionStr != null) {
                try {
                    criteria.setSortDirection(SearchCriteria.SortDirection.valueOf(sortDirectionStr));
                } catch (IllegalArgumentException e) {
                    criteria.setSortDirection(SearchCriteria.SortDirection.ASC);
                }
            }
        }
        
        return criteria;
    }

    /**
     * Вспомогательный метод для построения множественных критериев поиска
     */
    @SuppressWarnings("unchecked")
    private SearchCriteria buildMultipleSearchCriteria(Map<String, Object> criteriaMap) {
        List<Map<String, Object>> multipleCriteriaList = 
                (List<Map<String, Object>>) criteriaMap.get("multipleCriteria");
        
        List<SearchCriteria> criteriaList = new ArrayList<>();
        for (Map<String, Object> singleCriteriaMap : multipleCriteriaList) {
            String fieldName = (String) singleCriteriaMap.get("fieldName");
            Object value = singleCriteriaMap.get("value");
            String operatorStr = (String) singleCriteriaMap.get("operator");
            SearchCriteria.SearchOperator operator = SearchCriteria.SearchOperator.EQUALS;
            
            if (operatorStr != null) {
                try {
                    operator = SearchCriteria.SearchOperator.valueOf(operatorStr);
                } catch (IllegalArgumentException e) {
                    logger.warn("Неизвестный оператор: {}, используется EQUALS", operatorStr);
                }
            }
            
            criteriaList.add(new SearchCriteria(fieldName, value, operator));
        }
        
        SearchCriteria criteria = new SearchCriteria(criteriaList);
        
        String sortField = (String) criteriaMap.get("sortField");
        String sortDirectionStr = (String) criteriaMap.get("sortDirection");
        if (sortField != null) {
            criteria.setSortField(sortField);
            if (sortDirectionStr != null) {
                try {
                    criteria.setSortDirection(SearchCriteria.SortDirection.valueOf(sortDirectionStr));
                } catch (IllegalArgumentException e) {
                    criteria.setSortDirection(SearchCriteria.SortDirection.ASC);
                }
            }
        }
        
        return criteria;
    }

    /**
     * Вспомогательный метод для получения сущностей по типу
     */
    @SuppressWarnings("unchecked")
    private List<Object> getEntitiesByType(String entityType) {
        List<Object> entities = new ArrayList<>();
        
        switch (entityType) {
            case "UserDTO":
            case "User":
                entities.addAll(userRepository.findAll());
                break;
            case "FunctionDTO":
            case "Function":
                entities.addAll(functionRepository.findAll());
                break;
            case "PointDTO":
            case "Point":
                entities.addAll(pointRepository.findAll());
                break;
            case "ResultDTO":
            case "Result":
                entities.addAll(resultRepository.findAll());
                break;
            default:
                logger.warn("Неизвестный тип сущности: {}", entityType);
        }
        
        return entities;
    }

    /**
     * Вспомогательный метод для построения иерархических узлов
     */
    private List<HierarchicalDataNode> buildHierarchicalNodes() {
        List<User> users = userRepository.findAll();
        List<Function> functions = functionRepository.findAll();
        List<Point> points = pointRepository.findAll();
        List<Result> results = resultRepository.findAll();
        
        return hierarchicalBuilder.buildHierarchy(users, functions, points, results);
    }

    /**
     * Вспомогательный метод для преобразования сущности в DTO
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> convertToDTO(Object entity) {
        Map<String, Object> result = new HashMap<>();
        
        if (entity instanceof User) {
            result.put("type", "UserDTO");
            result.put("value", UserMapper.toDTOSafe((User) entity));
        } else if (entity instanceof Function) {
            result.put("type", "FunctionDTO");
            result.put("value", FunctionMapper.toDTO((Function) entity));
        } else if (entity instanceof Point) {
            result.put("type", "PointDTO");
            result.put("value", PointMapper.toDTO((Point) entity));
        } else if (entity instanceof Result) {
            result.put("type", "ResultDTO");
            result.put("value", ResultMapper.toDTO((Result) entity));
        } else {
            result.put("type", entity.getClass().getSimpleName());
            result.put("value", entity);
        }
        
        return result;
    }

    /**
     * Вспомогательный метод для преобразования иерархического узла
     */
    private Map<String, Object> convertHierarchicalNode(HierarchicalDataNode node) {
        Map<String, Object> result = new HashMap<>();
        
        if (node.getUser() != null) {
            result.put("user", UserMapper.toDTOSafe(node.getUser()));
        }
        
        List<Map<String, Object>> functions = new ArrayList<>();
        for (HierarchicalDataNode.FunctionNode funcNode : node.getFunctions()) {
            Map<String, Object> funcMap = new HashMap<>();
            funcMap.put("function", FunctionMapper.toDTO(funcNode.getFunction()));
            
            List<PointDTO> points = funcNode.getPoints().stream()
                    .map(PointMapper::toDTO)
                    .collect(Collectors.toList());
            funcMap.put("points", points);
            
            List<ResultDTO> results = funcNode.getResults().stream()
                    .map(ResultMapper::toDTO)
                    .collect(Collectors.toList());
            funcMap.put("results", results);
            
            functions.add(funcMap);
        }
        result.put("functions", functions);
        
        return result;
    }
}

