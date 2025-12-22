package servlet;

import database.dao.FunctionDAO;
import database.dao.PointDAO;
import database.dao.ResultDAO;
import database.dao.UserDAO;
import database.dto.FunctionDTO;
import database.dto.PointDTO;
import database.dto.ResultDTO;
import database.dto.UserDTO;
import database.search.DataSearchService;
import database.search.HierarchicalDataBuilder;
import database.search.HierarchicalDataNode;
import database.search.SearchCriteria;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.*;

@WebServlet("/api/v1/search/*")
public class SearchServlet extends AbstractApiServlet {
    private final UserDAO userDAO = new UserDAO();
    private final FunctionDAO functionDAO = new FunctionDAO();
    private final PointDAO pointDAO = new PointDAO();
    private final ResultDAO resultDAO = new ResultDAO();

    private final HierarchicalDataBuilder hierarchicalDataBuilder = new HierarchicalDataBuilder();
    private final DataSearchService dataSearchService = new DataSearchService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        prepareJsonResponse(resp);
        List<String> segments = pathSegments(req);
        logger.info("POST {} pathInfo={}", req.getRequestURI(), req.getPathInfo());

        try {
            if (segments.size() != 1) {
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", "Маршрут не найден");
                return;
            }

            String action = segments.get(0).toLowerCase(Locale.ROOT);

            switch (action) {
                case "dfs":
                    handleDfs(req, resp);
                    return;
                case "bfs":
                    handleBfs(req, resp);
                    return;
                case "hierarchical":
                    handleHierarchical(req, resp);
                    return;
                case "single":
                    handleSingle(req, resp);
                    return;
                case "multiple":
                    handleMultiple(req, resp);
                    return;
                default:
                    sendError(resp, HttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", "Маршрут не найден");
            }
        } catch (Exception e) {
            logger.error("Ошибка при обработке Search POST", e);
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", e.getMessage());
        }
    }

    private void handleDfs(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
        SearchRequest body = readJson(req, SearchRequest.class);
        if (body == null || body.criteria == null) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", "criteria обязателен");
            return;
        }

        SearchCriteria criteria = toSearchCriteria(body.criteria, null);

        List<HierarchicalDataNode> hierarchy = buildHierarchy();
        long start = System.currentTimeMillis();
        List<Object> results = dataSearchService.searchDFS(hierarchy, criteria);
        long exec = System.currentTimeMillis() - start;

        List<Map<String, Object>> wrapped = wrapTypedResults(results);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("data", wrapped);
        response.put("executionTime", exec);
        response.put("total", wrapped.size());
        resp.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(resp.getWriter(), response);
    }

    private void handleBfs(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
        SearchRequest body = readJson(req, SearchRequest.class);
        if (body == null || body.criteria == null) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", "criteria обязателен");
            return;
        }

        SearchCriteria criteria = toSearchCriteria(body.criteria, null);

        List<HierarchicalDataNode> hierarchy = buildHierarchy();
        long start = System.currentTimeMillis();
        List<Object> results = dataSearchService.searchBFS(hierarchy, criteria);
        long exec = System.currentTimeMillis() - start;

        List<Map<String, Object>> wrapped = wrapTypedResults(results);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("data", wrapped);
        response.put("executionTime", exec);
        response.put("total", wrapped.size());
        resp.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(resp.getWriter(), response);
    }

    private void handleHierarchical(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
        SearchRequest body = readJson(req, SearchRequest.class);
        if (body == null || body.criteria == null) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", "criteria обязателен");
            return;
        }

        SearchCriteria criteria = toSearchCriteria(body.criteria, null);
        List<HierarchicalDataNode> hierarchy = buildHierarchy();

        long start = System.currentTimeMillis();
        List<HierarchicalDataNode> results = dataSearchService.searchHierarchical(hierarchy, criteria);
        long exec = System.currentTimeMillis() - start;

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("data", results);
        response.put("executionTime", exec);
        response.put("total", results.size());
        resp.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(resp.getWriter(), response);
    }

    private void handleSingle(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
        SingleOrMultipleRequest body = readJson(req, SingleOrMultipleRequest.class);
        if (body == null || body.criteria == null || body.entityType == null) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", "entityType и criteria обязательны");
            return;
        }

        Entity entity = Entity.from(body.entityType);
        if (entity == null) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", "Неподдерживаемый entityType");
            return;
        }

        SearchCriteria criteria = toSearchCriteria(body.criteria, entity.dtoClass);
        List<?> data = loadEntityList(entity);

        long start = System.currentTimeMillis();
        List<?> results = dataSearchService.searchSingle((List) data, criteria);
        long exec = System.currentTimeMillis() - start;

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("data", results);
        response.put("executionTime", exec);
        response.put("total", results.size());
        resp.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(resp.getWriter(), response);
    }

    private void handleMultiple(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
        SingleOrMultipleRequest body = readJson(req, SingleOrMultipleRequest.class);
        if (body == null || body.criteria == null || body.entityType == null) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", "entityType и criteria обязательны");
            return;
        }

        Entity entity = Entity.from(body.entityType);
        if (entity == null) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", "Неподдерживаемый entityType");
            return;
        }

        SearchCriteria criteria = toSearchCriteria(body.criteria, entity.dtoClass);
        List<?> data = loadEntityList(entity);

        long start = System.currentTimeMillis();
        List<?> results = dataSearchService.searchMultiple((List) data, criteria);
        long exec = System.currentTimeMillis() - start;

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("data", results);
        response.put("executionTime", exec);
        response.put("total", results.size());
        resp.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(resp.getWriter(), response);
    }

    private List<HierarchicalDataNode> buildHierarchy() throws SQLException {
        List<UserDTO> users = loadUsers();
        List<FunctionDTO> functions = loadFunctions();
        List<PointDTO> points = loadPoints();
        List<ResultDTO> results = loadResults();
        return hierarchicalDataBuilder.buildHierarchy(users, functions, points, results);
    }

    private List<UserDTO> loadUsers() throws SQLException {
        List<UserDAO.User> users = userDAO.findAll();
        List<UserDTO> dtos = new ArrayList<>();
        for (UserDAO.User u : users) {
            dtos.add(new UserDTO(u.getId(), u.getLogin(), null, u.getEmail()));
        }
        return dtos;
    }

    private List<FunctionDTO> loadFunctions() throws SQLException {
        List<FunctionDAO.Function> functions = functionDAO.findAll();
        List<FunctionDTO> dtos = new ArrayList<>();
        for (FunctionDAO.Function f : functions) {
            dtos.add(new FunctionDTO(f.getId(), f.getUserId(), f.getName(), f.getType()));
        }
        return dtos;
    }

    private List<PointDTO> loadPoints() throws SQLException {
        List<PointDAO.Point> points = pointDAO.findAll();
        List<PointDTO> dtos = new ArrayList<>();
        for (PointDAO.Point p : points) {
            dtos.add(new PointDTO(p.getId(), p.getFunctionId(), p.getXValue(), p.getYValue()));
        }
        return dtos;
    }

    private List<ResultDTO> loadResults() throws SQLException {
        List<ResultDAO.Result> results = resultDAO.findAll();
        List<ResultDTO> dtos = new ArrayList<>();
        for (ResultDAO.Result r : results) {
            dtos.add(new ResultDTO(r.getId(), r.getFunctionId(), r.getResult()));
        }
        return dtos;
    }

    private List<?> loadEntityList(Entity entity) throws SQLException {
        switch (entity) {
            case USER:
                return loadUsers();
            case FUNCTION:
                return loadFunctions();
            case POINT:
                return loadPoints();
            case RESULT:
                return loadResults();
            default:
                return Collections.emptyList();
        }
    }

    private List<Map<String, Object>> wrapTypedResults(List<Object> results) {
        List<Map<String, Object>> wrapped = new ArrayList<>();
        for (Object obj : results) {
            if (obj == null) continue;
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("type", obj.getClass().getSimpleName());
            item.put("value", obj);
            wrapped.add(item);
        }
        return wrapped;
    }

    private SearchCriteria toSearchCriteria(CriteriaDto dto, Class<?> entityDtoClass) {
        if (dto == null) {
            throw new IllegalArgumentException("criteria обязателен");
        }

        SearchCriteria criteria;
        if (dto.multipleCriteria != null && !dto.multipleCriteria.isEmpty()) {
            List<SearchCriteria> list = new ArrayList<>();
            for (CriteriaDto c : dto.multipleCriteria) {
                list.add(toSearchCriteria(c, entityDtoClass));
            }
            criteria = new SearchCriteria(list);
        } else {
            if (dto.fieldName == null || dto.operator == null) {
                throw new IllegalArgumentException("fieldName и operator обязательны");
            }
            SearchCriteria.SearchOperator op = SearchCriteria.SearchOperator.valueOf(dto.operator);
            Object value = coerceValue(entityDtoClass, dto.fieldName, dto.value);
            criteria = new SearchCriteria(dto.fieldName, value, op);
        }

        if (dto.sortField != null) criteria.setSortField(dto.sortField);
        if (dto.sortDirection != null) criteria.setSortDirection(SearchCriteria.SortDirection.valueOf(dto.sortDirection));
        return criteria;
    }

    private Object coerceValue(Class<?> dtoClass, String fieldName, Object value) {
        if (value == null) return null;

        // Для DFS/BFS/Hierarchical: тип объекта заранее неизвестен — делаем эвристику.
        if (dtoClass == null) {
            if (value instanceof Number) {
                if ("xValue".equals(fieldName) || "yValue".equals(fieldName)) {
                    return ((Number) value).doubleValue();
                }
                if ("id".equals(fieldName) || fieldName.endsWith("Id")) {
                    return ((Number) value).longValue();
                }
            }
            return value;
        }

        Field f = findField(dtoClass, fieldName);
        if (f == null) return value;
        Class<?> t = f.getType();

        try {
            if (t == Long.class || t == long.class) {
                if (value instanceof Number) return ((Number) value).longValue();
                if (value instanceof String) return Long.parseLong((String) value);
            }
            if (t == Double.class || t == double.class) {
                if (value instanceof Number) return ((Number) value).doubleValue();
                if (value instanceof String) return Double.parseDouble((String) value);
            }
            if (t == String.class) {
                return String.valueOf(value);
            }
        } catch (Exception ignored) {
            return value;
        }

        return value;
    }

    private Field findField(Class<?> clazz, String fieldName) {
        Class<?> c = clazz;
        while (c != null) {
            try {
                return c.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                c = c.getSuperclass();
            }
        }
        return null;
    }

    private enum Entity {
        USER("UserDTO", UserDTO.class),
        FUNCTION("FunctionDTO", FunctionDTO.class),
        POINT("PointDTO", PointDTO.class),
        RESULT("ResultDTO", ResultDTO.class);

        final String wireName;
        final Class<?> dtoClass;

        Entity(String wireName, Class<?> dtoClass) {
            this.wireName = wireName;
            this.dtoClass = dtoClass;
        }

        static Entity from(String s) {
            if (s == null) return null;
            for (Entity e : values()) {
                if (e.wireName.equalsIgnoreCase(s)) return e;
            }
            return null;
        }
    }

    public static class SearchRequest {
        public CriteriaDto criteria;
    }

    public static class SingleOrMultipleRequest {
        public String entityType;
        public CriteriaDto criteria;
    }

    public static class CriteriaDto {
        public String fieldName;
        public Object value;
        public String operator;
        public List<CriteriaDto> multipleCriteria;
        public String sortField;
        public String sortDirection;
    }
}

