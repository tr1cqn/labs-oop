package servlet;

import database.dao.FunctionDAO;
import database.dto.FunctionDTO;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Сервлет для работы с FunctionDTO
 * Реализует CRUD операции согласно API контракту
 */
@WebServlet("/api/v1/functions/*")
public class FunctionServlet extends AbstractApiServlet {
    private final FunctionDAO functionDAO = new FunctionDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        prepareJsonResponse(resp);
        List<String> segments = pathSegments(req);
        logger.info("GET {} pathInfo={} params={}", req.getRequestURI(), req.getPathInfo(), req.getQueryString());

        try {
            // GET /api/v1/functions
            if (segments.isEmpty()) {
                AuthContext ctx = auth(req);
                if (ctx != null && !ctx.isAdmin()) {
                    handleGetByUserId(ctx.getUserId(), req, resp);
                } else {
                    handleGetAll(req, resp);
                }
                return;
            }

            // GET /api/v1/functions/search?... (контракт)
            if ("search".equalsIgnoreCase(segments.get(0))) {
                AuthContext ctx = auth(req);
                if (ctx != null && !ctx.isAdmin()) {
                    // Для USER разрешаем только поиск по типу в рамках своего userId
                    String type = req.getParameter("type");
                    String userId = req.getParameter("userId");
                    if (userId != null && !userId.isBlank() && !String.valueOf(ctx.getUserId()).equals(userId)) {
                        sendError(resp, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN", "Доступ только к своим данным");
                        return;
                    }
                    if (type != null) {
                        handleSearchByUserAndType(ctx.getUserId(), type, resp);
                        return;
                    }
                    sendError(resp, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN", "Недостаточно прав для данного поиска");
                    return;
                }
                handleSearch(req, resp);
                return;
            }

            // GET /api/v1/functions/{id}
            if (segments.size() == 1 && segments.get(0).matches("\\d+")) {
                Long id = parseLongStrict(segments.get(0));
                AuthContext ctx = auth(req);
                if (ctx != null && !ctx.isAdmin()) {
                    var opt = functionDAO.findById(id);
                    if (opt.isEmpty()) {
                        sendError(resp, HttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", "Функция не найдена");
                        return;
                    }
                    if (!ctx.getUserId().equals(opt.get().getUserId())) {
                        sendError(resp, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN", "Доступ только к своим данным");
                        return;
                    }
                }
                handleGetById(id, resp);
                return;
            }

            // GET /api/v1/functions/user/{userId} | /user/{userId}/count
            if (segments.size() >= 2 && "user".equalsIgnoreCase(segments.get(0)) && segments.get(1).matches("\\d+")) {
                Long userId = parseLongStrict(segments.get(1));
                AuthContext ctx = auth(req);
                if (ctx != null && !ctx.isAdmin() && !ctx.getUserId().equals(userId)) {
                    sendError(resp, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN", "Доступ только к своим данным");
                    return;
                }
                if (segments.size() == 3 && "count".equalsIgnoreCase(segments.get(2))) {
                    handleCountByUserId(userId, resp);
                    return;
                }
                if (segments.size() == 2) {
                    handleGetByUserId(userId, req, resp);
                    return;
                }
            }

            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", "Маршрут не найден");
        } catch (Exception e) {
            logger.error("Ошибка при обработке GET запроса", e);
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        prepareJsonResponse(resp);
        List<String> segments = pathSegments(req);
        logger.info("POST {} pathInfo={}", req.getRequestURI(), req.getPathInfo());

        try {
            if (!segments.isEmpty()) {
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", "Маршрут не найден");
                return;
            }
            FunctionDTO functionDTO = objectMapper.readValue(req.getReader(), FunctionDTO.class);
            AuthContext ctx = auth(req);
            if (ctx != null && !ctx.isAdmin()) {
                // USER может создавать только свои функции
                functionDTO.setUserId(ctx.getUserId());
            }
            handleCreate(functionDTO, resp);
        } catch (Exception e) {
            logger.error("Ошибка при обработке POST запроса", e);
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        prepareJsonResponse(resp);
        List<String> segments = pathSegments(req);
        logger.info("PUT {} pathInfo={}", req.getRequestURI(), req.getPathInfo());

        try {
            if (segments.size() != 1 || !segments.get(0).matches("\\d+")) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", "ID обязателен");
                return;
            }

            Long id = parseLongStrict(segments.get(0));
            AuthContext ctx = auth(req);
            if (ctx != null && !ctx.isAdmin()) {
                var opt = functionDAO.findById(id);
                if (opt.isEmpty()) {
                    sendError(resp, HttpServletResponse.SC_NOT_FOUND, "FUNCTION_NOT_FOUND", "Функция не найдена");
                    return;
                }
                if (!ctx.getUserId().equals(opt.get().getUserId())) {
                    sendError(resp, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN", "Доступ только к своим данным");
                    return;
                }
            }
            FunctionDTO functionDTO = objectMapper.readValue(req.getReader(), FunctionDTO.class);
            handleUpdate(id, functionDTO, resp);
        } catch (Exception e) {
            logger.error("Ошибка при обработке PUT запроса", e);
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", e.getMessage());
        }
    }

    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        prepareJsonResponse(resp);
        List<String> segments = pathSegments(req);
        logger.info("PATCH {} pathInfo={} params={}", req.getRequestURI(), req.getPathInfo(), req.getQueryString());

        try {
            // PATCH /api/v1/functions/{id}/name | /type
            if (segments.size() == 2 && segments.get(0).matches("\\d+")) {
                Long id = parseLongStrict(segments.get(0));
                AuthContext ctx = auth(req);
                if (ctx != null && !ctx.isAdmin()) {
                    var opt = functionDAO.findById(id);
                    if (opt.isEmpty()) {
                        sendError(resp, HttpServletResponse.SC_NOT_FOUND, "FUNCTION_NOT_FOUND", "Функция не найдена");
                        return;
                    }
                    if (!ctx.getUserId().equals(opt.get().getUserId())) {
                        sendError(resp, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN", "Доступ только к своим данным");
                        return;
                    }
                }
                String field = segments.get(1);
                Map<String, Object> body = readJson(req, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>(){});

                if ("name".equalsIgnoreCase(field)) {
                    Object name = body.get("name");
                    if (!(name instanceof String) || ((String) name).isBlank()) {
                        sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", "name обязателен");
                        return;
                    }
                    boolean updated = functionDAO.updateName(id, (String) name);
                    if (!updated) {
                        sendError(resp, HttpServletResponse.SC_NOT_FOUND, "FUNCTION_NOT_FOUND", "Функция не найдена");
                        return;
                    }
                    sendSuccess(resp, HttpServletResponse.SC_OK, Map.of("id", id, "name", name), "Имя функции обновлено");
                    return;
                }

                if ("type".equalsIgnoreCase(field)) {
                    Object type = body.get("type");
                    if (!(type instanceof String) || ((String) type).isBlank()) {
                        sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", "type обязателен");
                        return;
                    }
                    boolean updated = functionDAO.updateType(id, (String) type);
                    if (!updated) {
                        sendError(resp, HttpServletResponse.SC_NOT_FOUND, "FUNCTION_NOT_FOUND", "Функция не найдена");
                        return;
                    }
                    sendSuccess(resp, HttpServletResponse.SC_OK, Map.of("id", id, "type", type), "Тип функции обновлен");
                    return;
                }
            }

            // PATCH /api/v1/functions/user/{userId}/type
            if (segments.size() == 3 && "user".equalsIgnoreCase(segments.get(0)) && segments.get(1).matches("\\d+") && "type".equalsIgnoreCase(segments.get(2))) {
                Long userId = parseLongStrict(segments.get(1));
                AuthContext ctx = auth(req);
                if (ctx != null && !ctx.isAdmin() && !ctx.getUserId().equals(userId)) {
                    sendError(resp, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN", "Доступ только к своим данным");
                    return;
                }
                Map<String, Object> body = readJson(req, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>(){});
                Object type = body.get("type");
                if (!(type instanceof String) || ((String) type).isBlank()) {
                    sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", "type обязателен");
                    return;
                }
                int updated = functionDAO.updateAllByUserId(userId, (String) type);
                Map<String, Object> data = new HashMap<>();
                data.put("updated", updated);
                sendSuccess(resp, HttpServletResponse.SC_OK, data, "Обновлено функций: " + updated);
                return;
            }

            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", "Маршрут не найден");
        } catch (Exception e) {
            logger.error("Ошибка при обработке PATCH запроса", e);
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        prepareJsonResponse(resp);
        List<String> segments = pathSegments(req);
        logger.info("DELETE {} pathInfo={} params={}", req.getRequestURI(), req.getPathInfo(), req.getQueryString());

        try {
            // DELETE /api/v1/functions/{id}
            if (segments.size() == 1 && segments.get(0).matches("\\d+")) {
                Long id = parseLongStrict(segments.get(0));
                AuthContext ctx = auth(req);
                if (ctx != null && !ctx.isAdmin()) {
                    var opt = functionDAO.findById(id);
                    if (opt.isEmpty()) {
                        sendError(resp, HttpServletResponse.SC_NOT_FOUND, "FUNCTION_NOT_FOUND", "Функция не найдена");
                        return;
                    }
                    if (!ctx.getUserId().equals(opt.get().getUserId())) {
                        sendError(resp, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN", "Доступ только к своим данным");
                        return;
                    }
                }
                handleDeleteById(id, resp);
                return;
            }

            // DELETE /api/v1/functions/user/{userId}
            if (segments.size() == 2 && "user".equalsIgnoreCase(segments.get(0)) && segments.get(1).matches("\\d+")) {
                Long userId = parseLongStrict(segments.get(1));
                AuthContext ctx = auth(req);
                if (ctx != null && !ctx.isAdmin() && !ctx.getUserId().equals(userId)) {
                    sendError(resp, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN", "Доступ только к своим данным");
                    return;
                }
                int deleted = functionDAO.deleteByUserId(userId);
                sendSuccess(resp, HttpServletResponse.SC_OK, Map.of("deleted", deleted), "Удалено функций: " + deleted);
                return;
            }

            // DELETE /api/v1/functions?type=...
            if (segments.isEmpty() && req.getParameter("type") != null) {
                AuthContext ctx = auth(req);
                String type = req.getParameter("type");
                int deleted;
                if (ctx != null && !ctx.isAdmin()) {
                    deleted = functionDAO.deleteByTypeAndUserId(ctx.getUserId(), type);
                } else {
                    deleted = functionDAO.deleteByType(type);
                }
                sendSuccess(resp, HttpServletResponse.SC_OK, Map.of("deleted", deleted), "Удалено функций: " + deleted);
                return;
            }

            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", "ID, userId или type обязателен");
        } catch (Exception e) {
            logger.error("Ошибка при обработке DELETE запроса", e);
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", e.getMessage());
        }
    }

    private void handleGetAll(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
        logger.debug("Получение всех функций");
        List<FunctionDAO.Function> functions = functionDAO.findAll();
        List<FunctionDTO> dtos = new ArrayList<>();
        
        for (FunctionDAO.Function function : functions) {
            FunctionDTO dto = new FunctionDTO(function.getId(), function.getUserId(), 
                                             function.getName(), function.getType());
            dtos.add(dto);
        }

        String sortBy = getStringParam(req, "sortBy");
        String order = getStringParam(req, "order");
        Integer limit = getIntParam(req, "limit");
        Integer offset = getIntParam(req, "offset");

        if (sortBy != null) {
            Comparator<FunctionDTO> comparator;
            switch (sortBy) {
                case "id":
                    comparator = Comparator.comparing(FunctionDTO::getId, Comparator.nullsLast(Long::compareTo));
                    break;
                case "name":
                    comparator = Comparator.comparing(FunctionDTO::getName, Comparator.nullsLast(String::compareToIgnoreCase));
                    break;
                case "type":
                    comparator = Comparator.comparing(FunctionDTO::getType, Comparator.nullsLast(String::compareToIgnoreCase));
                    break;
                case "userId":
                    comparator = Comparator.comparing(FunctionDTO::getUserId, Comparator.nullsLast(Long::compareTo));
                    break;
                default:
                    comparator = null;
            }
            if (comparator != null) {
                if ("desc".equalsIgnoreCase(order)) comparator = comparator.reversed();
                dtos.sort(comparator);
            }
        }

        int total = dtos.size();
        List<FunctionDTO> page = applyLimitOffset(dtos, limit, offset);
        sendSuccessList(resp, HttpServletResponse.SC_OK, page, total);
    }

    private void handleGetById(Long id, HttpServletResponse resp) throws IOException, SQLException {
        logger.debug("Получение функции по ID: {}", id);
        var functionOpt = functionDAO.findById(id);
        
        if (functionOpt.isPresent()) {
            FunctionDAO.Function function = functionOpt.get();
            FunctionDTO dto = new FunctionDTO(function.getId(), function.getUserId(), 
                                             function.getName(), function.getType());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", dto);
            
            objectMapper.writeValue(resp.getWriter(), response);
            resp.setStatus(HttpServletResponse.SC_OK);
        } else {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", "Функция не найдена");
        }
    }

    private void handleGetByUserId(Long userId, HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
        logger.debug("Получение функций пользователя с ID: {}", userId);
        List<FunctionDAO.Function> functions = functionDAO.findByUserId(userId);
        List<FunctionDTO> dtos = new ArrayList<>();
        
        for (FunctionDAO.Function function : functions) {
            FunctionDTO dto = new FunctionDTO(function.getId(), function.getUserId(), 
                                             function.getName(), function.getType());
            dtos.add(dto);
        }

        String sortBy = getStringParam(req, "sortBy");
        String order = getStringParam(req, "order");
        Integer limit = getIntParam(req, "limit");
        Integer offset = getIntParam(req, "offset");

        if (sortBy != null) {
            Comparator<FunctionDTO> comparator;
            switch (sortBy) {
                case "id":
                    comparator = Comparator.comparing(FunctionDTO::getId, Comparator.nullsLast(Long::compareTo));
                    break;
                case "name":
                    comparator = Comparator.comparing(FunctionDTO::getName, Comparator.nullsLast(String::compareToIgnoreCase));
                    break;
                case "type":
                    comparator = Comparator.comparing(FunctionDTO::getType, Comparator.nullsLast(String::compareToIgnoreCase));
                    break;
                default:
                    comparator = null;
            }
            if (comparator != null) {
                if ("desc".equalsIgnoreCase(order)) comparator = comparator.reversed();
                dtos.sort(comparator);
            }
        }
        int total = dtos.size();
        List<FunctionDTO> page = applyLimitOffset(dtos, limit, offset);
        sendSuccessList(resp, HttpServletResponse.SC_OK, page, total);
    }

    private void handleSearchByType(String type, HttpServletResponse resp) throws IOException, SQLException {
        logger.debug("Поиск функций по типу: {}", type);
        List<FunctionDAO.Function> functions = functionDAO.findByType(type);
        List<FunctionDTO> dtos = new ArrayList<>();
        
        for (FunctionDAO.Function function : functions) {
            FunctionDTO dto = new FunctionDTO(function.getId(), function.getUserId(), 
                                             function.getName(), function.getType());
            dtos.add(dto);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", dtos);
        response.put("total", dtos.size());

        objectMapper.writeValue(resp.getWriter(), response);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    private void handleSearchByName(String name, HttpServletResponse resp) throws IOException, SQLException {
        logger.debug("Поиск функции по имени: {}", name);
        List<FunctionDAO.Function> functions = functionDAO.findByName(name);

        if (!functions.isEmpty()) {
            FunctionDAO.Function function = functions.get(0);
            FunctionDTO dto = new FunctionDTO(function.getId(), function.getUserId(), 
                                             function.getName(), function.getType());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", dto);
            
            objectMapper.writeValue(resp.getWriter(), response);
            resp.setStatus(HttpServletResponse.SC_OK);
        } else {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", "Функция не найдена");
        }
    }

    private void handleSearchByNameLike(String pattern, HttpServletResponse resp) throws IOException, SQLException {
        logger.debug("Поиск функций по nameLike: {}", pattern);
        List<FunctionDAO.Function> functions = functionDAO.findByNameLike("%" + pattern + "%");
        List<FunctionDTO> dtos = new ArrayList<>();
        
        for (FunctionDAO.Function function : functions) {
            FunctionDTO dto = new FunctionDTO(function.getId(), function.getUserId(), 
                                             function.getName(), function.getType());
            dtos.add(dto);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", dtos);
        response.put("total", dtos.size());

        objectMapper.writeValue(resp.getWriter(), response);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    private void handleSearchByUserAndType(Long userId, String type, HttpServletResponse resp) throws IOException, SQLException {
        logger.debug("Поиск функций пользователя {} по типу: {}", userId, type);
        List<FunctionDAO.Function> functions = functionDAO.findByUserIdAndType(userId, type);
        List<FunctionDTO> dtos = new ArrayList<>();
        
        for (FunctionDAO.Function function : functions) {
            FunctionDTO dto = new FunctionDTO(function.getId(), function.getUserId(), 
                                             function.getName(), function.getType());
            dtos.add(dto);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", dtos);
        response.put("total", dtos.size());

        objectMapper.writeValue(resp.getWriter(), response);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    private void handleCreate(FunctionDTO functionDTO, HttpServletResponse resp) throws IOException, SQLException {
        logger.debug("Создание функции: {}", functionDTO.getName());
        
        Long id = functionDAO.insert(functionDTO.getUserId(), functionDTO.getName(), functionDTO.getType());

        FunctionDTO createdDTO = new FunctionDTO(id, functionDTO.getUserId(), 
                                                functionDTO.getName(), functionDTO.getType());
        
        sendSuccess(resp, HttpServletResponse.SC_CREATED, createdDTO, "Функция успешно создана");
    }

    private void handleUpdate(Long id, FunctionDTO functionDTO, HttpServletResponse resp) throws IOException, SQLException {
        logger.debug("Обновление функции с ID: {}", id);
        
        boolean updated = functionDAO.update(id, functionDTO.getName(), functionDTO.getType());
        
        if (updated) {
            FunctionDTO updatedDTO = new FunctionDTO(id, functionDTO.getUserId(), 
                                                    functionDTO.getName(), functionDTO.getType());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", updatedDTO);
            response.put("message", "Функция успешно обновлена");

            objectMapper.writeValue(resp.getWriter(), response);
            resp.setStatus(HttpServletResponse.SC_OK);
        } else {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", "Функция не найдена");
        }
    }

    private void handleDeleteById(Long id, HttpServletResponse resp) throws IOException, SQLException {
        logger.debug("Удаление функции по ID: {}", id);
        boolean deleted = functionDAO.deleteById(id);
        
        if (deleted) {
            sendSuccess(resp, HttpServletResponse.SC_OK, null, "Функция успешно удалена");
        } else {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", "Функция не найдена");
        }
    }

    private void handleCountByUserId(Long userId, HttpServletResponse resp) throws IOException, SQLException {
        int count = functionDAO.countByUserId(userId);
        sendSuccess(resp, HttpServletResponse.SC_OK, Map.of("function_count", count), null);
    }

    private void handleSearch(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
        // GET /api/v1/functions/search?type= | name= | nameLike= | userId=&type=
        String type = req.getParameter("type");
        String userId = req.getParameter("userId");
        String name = req.getParameter("name");
        String nameLike = req.getParameter("nameLike");

        if (type != null && userId != null) {
            handleSearchByUserAndType(parseLongStrict(userId), type, resp);
            return;
        }
        if (type != null) {
            handleSearchByType(type, resp);
            return;
        }
        if (name != null) {
            handleSearchByName(name, resp);
            return;
        }
        if (nameLike != null) {
            handleSearchByNameLike(nameLike, resp);
            return;
        }
        sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR",
                "Ожидается один из параметров: type, name, nameLike или (userId+type)");
    }
}

