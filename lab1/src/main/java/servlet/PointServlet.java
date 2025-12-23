package servlet;

import database.dao.PointDAO;
import database.dao.FunctionDAO;
import database.dto.PointDTO;
import database.mapper.PointMapper;

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
 * Сервлет для работы с PointDTO
 * Реализует CRUD операции согласно API контракту
 */
@WebServlet("/api/v1/points/*")
public class PointServlet extends AbstractApiServlet {
    private final PointDAO pointDAO = new PointDAO();
    private final FunctionDAO functionDAO = new FunctionDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        prepareJsonResponse(resp);
        List<String> segments = pathSegments(req);
        logger.info("GET {} pathInfo={} params={}", req.getRequestURI(), req.getPathInfo(), req.getQueryString());

        try {
            // GET /api/v1/points
            if (segments.isEmpty()) {
                if (!isAdmin(req)) {
                    sendError(resp, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN", "Недостаточно прав");
                    return;
                }
                handleGetAll(req, resp);
                return;
            }

            // GET /api/v1/points/{id}
            if (segments.size() == 1 && segments.get(0).matches("\\d+")) {
                Long id = parseLongStrict(segments.get(0));
                AuthContext ctx = auth(req);
                if (ctx != null && !ctx.isAdmin()) {
                    var pOpt = pointDAO.findById(id);
                    if (pOpt.isEmpty()) {
                        sendError(resp, HttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", "Точка не найдена");
                        return;
                    }
                    var fOpt = functionDAO.findById(pOpt.get().getFunctionId());
                    if (fOpt.isEmpty() || !ctx.getUserId().equals(fOpt.get().getUserId())) {
                        sendError(resp, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN", "Доступ только к своим данным");
                        return;
                    }
                }
                handleGetById(id, resp);
                return;
            }

            // GET /api/v1/points/function/{funcId}/...
            if (segments.size() >= 2 && "function".equalsIgnoreCase(segments.get(0)) && segments.get(1).matches("\\d+")) {
                Long funcId = parseLongStrict(segments.get(1));
                AuthContext ctx = auth(req);
                if (ctx != null && !ctx.isAdmin()) {
                    var fOpt = functionDAO.findById(funcId);
                    if (fOpt.isEmpty() || !ctx.getUserId().equals(fOpt.get().getUserId())) {
                        sendError(resp, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN", "Доступ только к своим данным");
                        return;
                    }
                }

                // /function/{funcId}
                if (segments.size() == 2) {
                    handleGetByFunctionId(funcId, req, resp);
                    return;
                }

                // /function/{funcId}/count
                if (segments.size() == 3 && "count".equalsIgnoreCase(segments.get(2))) {
                    int count = pointDAO.countByFunctionId(funcId);
                    sendSuccess(resp, HttpServletResponse.SC_OK, Map.of("point_count", count), null);
                    return;
                }

                // /function/{funcId}/bounds
                if (segments.size() == 3 && "bounds".equalsIgnoreCase(segments.get(2))) {
                    PointDAO.XRange range = pointDAO.findMinMaxXByFunctionId(funcId);
                    sendSuccess(resp, HttpServletResponse.SC_OK, Map.of("min_x", range.getMinX(), "max_x", range.getMaxX()), null);
                    return;
                }

                // /function/{funcId}/x/{xValue}
                if (segments.size() == 4 && "x".equalsIgnoreCase(segments.get(2))) {
                    Double xValue = parseDoubleStrict(segments.get(3));
                    handleGetByFunctionAndX(funcId, xValue, resp);
                    return;
                }

                // /function/{funcId}/range?xMin=&xMax=
                if (segments.size() == 3 && "range".equalsIgnoreCase(segments.get(2))) {
                    Double xMin = req.getParameter("xMin") == null ? null : Double.parseDouble(req.getParameter("xMin"));
                    Double xMax = req.getParameter("xMax") == null ? null : Double.parseDouble(req.getParameter("xMax"));
                    if (xMin == null || xMax == null) {
                        sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", "xMin и xMax обязательны");
                        return;
                    }
                    handleGetByFunctionAndXRange(funcId, xMin, xMax, resp);
                    return;
                }

                // /function/{funcId}/y/{yValue}
                if (segments.size() == 4 && "y".equalsIgnoreCase(segments.get(2))) {
                    Double yValue = parseDoubleStrict(segments.get(3));
                    handleGetByFunctionAndY(funcId, yValue, resp);
                    return;
                }

                // /function/{funcId}/yRange?yMin=&yMax=
                if (segments.size() == 3 && "yRange".equalsIgnoreCase(segments.get(2))) {
                    Double yMin = req.getParameter("yMin") == null ? null : Double.parseDouble(req.getParameter("yMin"));
                    Double yMax = req.getParameter("yMax") == null ? null : Double.parseDouble(req.getParameter("yMax"));
                    if (yMin == null || yMax == null) {
                        sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", "yMin и yMax обязательны");
                        return;
                    }
                    handleGetByFunctionAndYRange(funcId, yMin, yMax, resp);
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
            // POST /api/v1/points
            if (segments.isEmpty()) {
                PointDTO pointDTO = objectMapper.readValue(req.getReader(), PointDTO.class);
                if (!PointMapper.isValid(pointDTO)) {
                    sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", "funcId, xValue, yValue обязательны");
                    return;
                }
                AuthContext ctx = auth(req);
                if (ctx != null && !ctx.isAdmin()) {
                    var fOpt = functionDAO.findById(pointDTO.getFuncId());
                    if (fOpt.isEmpty() || !ctx.getUserId().equals(fOpt.get().getUserId())) {
                        sendError(resp, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN", "Доступ только к своим данным");
                        return;
                    }
                }
                handleCreate(pointDTO, resp);
                return;
            }

            // POST /api/v1/points/batch
            if (segments.size() == 1 && "batch".equalsIgnoreCase(segments.get(0))) {
                BatchPointsRequest body = readJson(req, BatchPointsRequest.class);
                if (body == null || body.funcId == null || body.points == null || body.points.isEmpty()) {
                    sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", "funcId и points обязательны");
                    return;
                }
                AuthContext ctx = auth(req);
                if (ctx != null && !ctx.isAdmin()) {
                    var fOpt = functionDAO.findById(body.funcId);
                    if (fOpt.isEmpty() || !ctx.getUserId().equals(fOpt.get().getUserId())) {
                        sendError(resp, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN", "Доступ только к своим данным");
                        return;
                    }
                }

                List<PointDTO> created = new ArrayList<>();
                for (SimplePoint p : body.points) {
                    if (p == null) continue;
                    PointDTO dto = new PointDTO(null, body.funcId, p.xValue, p.yValue);
                    if (!PointMapper.isValid(dto)) {
                        sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", "Некорректная точка в списке");
                        return;
                    }
                    Long id = pointDAO.insert(body.funcId, p.xValue, p.yValue);
                    created.add(new PointDTO(id, body.funcId, p.xValue, p.yValue));
                }

                Map<String, Object> data = new HashMap<>();
                data.put("created", created.size());
                data.put("points", created);
                sendSuccess(resp, HttpServletResponse.SC_CREATED, data, "Создано точек: " + created.size());
                return;
            }

            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", "Маршрут не найден");
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
                var pOpt = pointDAO.findById(id);
                if (pOpt.isEmpty()) {
                    sendError(resp, HttpServletResponse.SC_NOT_FOUND, "POINT_NOT_FOUND", "Точка не найдена");
                    return;
                }
                var fOpt = functionDAO.findById(pOpt.get().getFunctionId());
                if (fOpt.isEmpty() || !ctx.getUserId().equals(fOpt.get().getUserId())) {
                    sendError(resp, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN", "Доступ только к своим данным");
                    return;
                }
            }
            PointDTO pointDTO = objectMapper.readValue(req.getReader(), PointDTO.class);
            handleUpdate(id, pointDTO, resp);
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
            // PATCH /api/v1/points/{id}/y
            if (segments.size() == 2 && segments.get(0).matches("\\d+") && "y".equalsIgnoreCase(segments.get(1))) {
                Long id = parseLongStrict(segments.get(0));
                AuthContext ctx = auth(req);
                if (ctx != null && !ctx.isAdmin()) {
                    var pOpt = pointDAO.findById(id);
                    if (pOpt.isEmpty()) {
                        sendError(resp, HttpServletResponse.SC_NOT_FOUND, "POINT_NOT_FOUND", "Точка не найдена");
                        return;
                    }
                    var fOpt = functionDAO.findById(pOpt.get().getFunctionId());
                    if (fOpt.isEmpty() || !ctx.getUserId().equals(fOpt.get().getUserId())) {
                        sendError(resp, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN", "Доступ только к своим данным");
                        return;
                    }
                }
                Map<String, Object> body = readJson(req, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>(){});
                Object y = body.get("yValue");
                if (!(y instanceof Number)) {
                    sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", "yValue обязателен");
                    return;
                }
                double yValue = ((Number) y).doubleValue();
                boolean updated = pointDAO.updateYById(id, yValue);
                if (!updated) {
                    sendError(resp, HttpServletResponse.SC_NOT_FOUND, "POINT_NOT_FOUND", "Точка не найдена");
                    return;
                }
                sendSuccess(resp, HttpServletResponse.SC_OK, Map.of("id", id, "yValue", yValue), "y_value обновлен");
                return;
            }

            // PATCH /api/v1/points/function/{funcId}/x/{xValue}/y
            if (segments.size() == 5
                    && "function".equalsIgnoreCase(segments.get(0))
                    && segments.get(1).matches("\\d+")
                    && "x".equalsIgnoreCase(segments.get(2))
                    && "y".equalsIgnoreCase(segments.get(4))) {
                Long funcId = parseLongStrict(segments.get(1));
                AuthContext ctx = auth(req);
                if (ctx != null && !ctx.isAdmin()) {
                    var fOpt = functionDAO.findById(funcId);
                    if (fOpt.isEmpty() || !ctx.getUserId().equals(fOpt.get().getUserId())) {
                        sendError(resp, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN", "Доступ только к своим данным");
                        return;
                    }
                }
                Double xValue = parseDoubleStrict(segments.get(3));
                Map<String, Object> body = readJson(req, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>(){});
                Object y = body.get("yValue");
                if (!(y instanceof Number)) {
                    sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", "yValue обязателен");
                    return;
                }
                double yValue = ((Number) y).doubleValue();
                boolean updated = pointDAO.updateYByFunctionAndX(funcId, xValue, yValue);
                if (!updated) {
                    sendError(resp, HttpServletResponse.SC_NOT_FOUND, "POINT_NOT_FOUND", "Точка не найдена");
                    return;
                }
                sendSuccess(resp, HttpServletResponse.SC_OK, Map.of("funcId", funcId, "xValue", xValue, "yValue", yValue), "y_value обновлен");
                return;
            }

            // PATCH /api/v1/points/function/{funcId}/multiply?coefficient=...
            if (segments.size() == 3
                    && "function".equalsIgnoreCase(segments.get(0))
                    && segments.get(1).matches("\\d+")
                    && "multiply".equalsIgnoreCase(segments.get(2))) {
                Long funcId = parseLongStrict(segments.get(1));
                AuthContext ctx = auth(req);
                if (ctx != null && !ctx.isAdmin()) {
                    var fOpt = functionDAO.findById(funcId);
                    if (fOpt.isEmpty() || !ctx.getUserId().equals(fOpt.get().getUserId())) {
                        sendError(resp, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN", "Доступ только к своим данным");
                        return;
                    }
                }
                String coeff = req.getParameter("coefficient");
                if (coeff == null) {
                    sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", "coefficient обязателен");
                    return;
                }
                double multiplier = Double.parseDouble(coeff);
                int updated = pointDAO.updateYMultiply(funcId, multiplier);
                sendSuccess(resp, HttpServletResponse.SC_OK, Map.of("updated", updated), "Обновлено точек: " + updated);
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
            // DELETE /api/v1/points/{id}
            if (segments.size() == 1 && segments.get(0).matches("\\d+")) {
                Long id = parseLongStrict(segments.get(0));
                AuthContext ctx = auth(req);
                if (ctx != null && !ctx.isAdmin()) {
                    var pOpt = pointDAO.findById(id);
                    if (pOpt.isEmpty()) {
                        sendError(resp, HttpServletResponse.SC_NOT_FOUND, "POINT_NOT_FOUND", "Точка не найдена");
                        return;
                    }
                    var fOpt = functionDAO.findById(pOpt.get().getFunctionId());
                    if (fOpt.isEmpty() || !ctx.getUserId().equals(fOpt.get().getUserId())) {
                        sendError(resp, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN", "Доступ только к своим данным");
                        return;
                    }
                }
                handleDeleteById(id, resp);
                return;
            }

            // DELETE /api/v1/points/function/{funcId}/x/{xValue}
            if (segments.size() == 4
                    && "function".equalsIgnoreCase(segments.get(0))
                    && segments.get(1).matches("\\d+")
                    && "x".equalsIgnoreCase(segments.get(2))) {
                Long funcId = parseLongStrict(segments.get(1));
                AuthContext ctx = auth(req);
                if (ctx != null && !ctx.isAdmin()) {
                    var fOpt = functionDAO.findById(funcId);
                    if (fOpt.isEmpty() || !ctx.getUserId().equals(fOpt.get().getUserId())) {
                        sendError(resp, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN", "Доступ только к своим данным");
                        return;
                    }
                }
                Double xValue = parseDoubleStrict(segments.get(3));
                boolean deleted = pointDAO.deleteByFunctionAndX(funcId, xValue);
                if (!deleted) {
                    sendError(resp, HttpServletResponse.SC_NOT_FOUND, "POINT_NOT_FOUND", "Точка не найдена");
                    return;
                }
                sendSuccess(resp, HttpServletResponse.SC_OK, null, "Точка успешно удалена");
                return;
            }

            // DELETE /api/v1/points/function/{funcId}
            if (segments.size() == 2 && "function".equalsIgnoreCase(segments.get(0)) && segments.get(1).matches("\\d+")) {
                Long funcId = parseLongStrict(segments.get(1));
                AuthContext ctx = auth(req);
                if (ctx != null && !ctx.isAdmin()) {
                    var fOpt = functionDAO.findById(funcId);
                    if (fOpt.isEmpty() || !ctx.getUserId().equals(fOpt.get().getUserId())) {
                        sendError(resp, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN", "Доступ только к своим данным");
                        return;
                    }
                }
                int deleted = pointDAO.deleteByFunctionId(funcId);
                sendSuccess(resp, HttpServletResponse.SC_OK, Map.of("deleted", deleted), "Удалено точек: " + deleted);
                return;
            }

            // DELETE /api/v1/points/function/{funcId}/range?xMin=&xMax=
            if (segments.size() == 3
                    && "function".equalsIgnoreCase(segments.get(0))
                    && segments.get(1).matches("\\d+")
                    && "range".equalsIgnoreCase(segments.get(2))) {
                Long funcId = parseLongStrict(segments.get(1));
                AuthContext ctx = auth(req);
                if (ctx != null && !ctx.isAdmin()) {
                    var fOpt = functionDAO.findById(funcId);
                    if (fOpt.isEmpty() || !ctx.getUserId().equals(fOpt.get().getUserId())) {
                        sendError(resp, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN", "Доступ только к своим данным");
                        return;
                    }
                }
                Double xMin = req.getParameter("xMin") == null ? null : Double.parseDouble(req.getParameter("xMin"));
                Double xMax = req.getParameter("xMax") == null ? null : Double.parseDouble(req.getParameter("xMax"));
                if (xMin == null || xMax == null) {
                    sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", "xMin и xMax обязательны");
                    return;
                }
                int deleted = pointDAO.deleteByFunctionAndXRange(funcId, xMin, xMax);
                sendSuccess(resp, HttpServletResponse.SC_OK, Map.of("deleted", deleted), "Удалено точек: " + deleted);
                return;
            }

            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", "Неверный маршрут");
        } catch (Exception e) {
            logger.error("Ошибка при обработке DELETE запроса", e);
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", e.getMessage());
        }
    }

    private void handleGetAll(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
        logger.debug("Получение всех точек");
        List<PointDAO.Point> points = pointDAO.findAll();
        List<PointDTO> dtos = new ArrayList<>();
        
        for (PointDAO.Point point : points) {
            PointDTO dto = new PointDTO(point.getId(), point.getFunctionId(), 
                                        point.getXValue(), point.getYValue());
            dtos.add(dto);
        }

        String sortBy = getStringParam(req, "sortBy");
        String order = getStringParam(req, "order");
        Integer limit = getIntParam(req, "limit");
        Integer offset = getIntParam(req, "offset");

        if (sortBy != null) {
            Comparator<PointDTO> comparator;
            switch (sortBy) {
                case "id":
                    comparator = Comparator.comparing(PointDTO::getId, Comparator.nullsLast(Long::compareTo));
                    break;
                case "xValue":
                    comparator = Comparator.comparing(PointDTO::getXValue, Comparator.nullsLast(Double::compareTo));
                    break;
                case "yValue":
                    comparator = Comparator.comparing(PointDTO::getYValue, Comparator.nullsLast(Double::compareTo));
                    break;
                case "funcId":
                    comparator = Comparator.comparing(PointDTO::getFuncId, Comparator.nullsLast(Long::compareTo));
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
        List<PointDTO> page = applyLimitOffset(dtos, limit, offset);
        sendSuccessList(resp, HttpServletResponse.SC_OK, page, total);
    }

    private void handleGetById(Long id, HttpServletResponse resp) throws IOException, SQLException {
        logger.debug("Получение точки по ID: {}", id);
        var pointOpt = pointDAO.findById(id);
        
        if (pointOpt.isPresent()) {
            PointDAO.Point point = pointOpt.get();
            PointDTO dto = new PointDTO(point.getId(), point.getFunctionId(), 
                                       point.getXValue(), point.getYValue());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", dto);
            
            objectMapper.writeValue(resp.getWriter(), response);
            resp.setStatus(HttpServletResponse.SC_OK);
        } else {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", "Точка не найдена");
        }
    }

    private void handleGetByFunctionId(Long funcId, HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
        logger.debug("Получение точек функции с ID: {}", funcId);
        List<PointDAO.Point> points = pointDAO.findByFunctionId(funcId);
        List<PointDTO> dtos = new ArrayList<>();
        
        for (PointDAO.Point point : points) {
            PointDTO dto = new PointDTO(point.getId(), point.getFunctionId(), 
                                       point.getXValue(), point.getYValue());
            dtos.add(dto);
        }

        String sortBy = getStringParam(req, "sortBy");
        String order = getStringParam(req, "order");

        if (sortBy != null) {
            Comparator<PointDTO> comparator;
            switch (sortBy) {
                case "id":
                    comparator = Comparator.comparing(PointDTO::getId, Comparator.nullsLast(Long::compareTo));
                    break;
                case "xValue":
                    comparator = Comparator.comparing(PointDTO::getXValue, Comparator.nullsLast(Double::compareTo));
                    break;
                case "yValue":
                    comparator = Comparator.comparing(PointDTO::getYValue, Comparator.nullsLast(Double::compareTo));
                    break;
                default:
                    comparator = null;
            }
            if (comparator != null) {
                if ("desc".equalsIgnoreCase(order)) comparator = comparator.reversed();
                dtos.sort(comparator);
            }
        }

        sendSuccessList(resp, HttpServletResponse.SC_OK, dtos, dtos.size());
    }

    private void handleGetByFunctionAndX(Long funcId, Double xValue, HttpServletResponse resp) throws IOException, SQLException {
        logger.debug("Получение точки функции {} с xValue: {}", funcId, xValue);
        var pointOpt = pointDAO.findByFunctionIdAndX(funcId, xValue);
        
        if (pointOpt.isPresent()) {
            PointDAO.Point point = pointOpt.get();
            PointDTO dto = new PointDTO(point.getId(), point.getFunctionId(), 
                                       point.getXValue(), point.getYValue());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", dto);
            
            objectMapper.writeValue(resp.getWriter(), response);
            resp.setStatus(HttpServletResponse.SC_OK);
        } else {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", "Точка не найдена");
        }
    }

    private void handleCreate(PointDTO pointDTO, HttpServletResponse resp) throws IOException, SQLException {
        logger.debug("Создание точки для функции: {}", pointDTO.getFuncId());
        
        Long id = pointDAO.insert(pointDTO.getFuncId(), pointDTO.getXValue(), pointDTO.getYValue());

        PointDTO createdDTO = new PointDTO(id, pointDTO.getFuncId(), 
                                          pointDTO.getXValue(), pointDTO.getYValue());
        
        sendSuccess(resp, HttpServletResponse.SC_CREATED, createdDTO, "Точка успешно создана");
    }

    private void handleUpdate(Long id, PointDTO pointDTO, HttpServletResponse resp) throws IOException, SQLException {
        logger.debug("Обновление точки с ID: {}", id);
        
        boolean updated = pointDAO.update(id, pointDTO.getXValue(), pointDTO.getYValue());
        
        if (updated) {
            PointDTO updatedDTO = new PointDTO(id, pointDTO.getFuncId(), 
                                              pointDTO.getXValue(), pointDTO.getYValue());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", updatedDTO);
            response.put("message", "Точка успешно обновлена");

            objectMapper.writeValue(resp.getWriter(), response);
            resp.setStatus(HttpServletResponse.SC_OK);
        } else {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", "Точка не найдена");
        }
    }

    private void handleDeleteById(Long id, HttpServletResponse resp) throws IOException, SQLException {
        logger.debug("Удаление точки по ID: {}", id);
        boolean deleted = pointDAO.deleteById(id);
        
        if (deleted) {
            sendSuccess(resp, HttpServletResponse.SC_OK, null, "Точка успешно удалена");
        } else {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", "Точка не найдена");
        }
    }

    private void handleGetByFunctionAndXRange(Long funcId, double xMin, double xMax, HttpServletResponse resp) throws IOException, SQLException {
        List<PointDAO.Point> points = pointDAO.findByFunctionIdAndXRange(funcId, xMin, xMax);
        List<PointDTO> dtos = new ArrayList<>();
        for (PointDAO.Point p : points) {
            dtos.add(new PointDTO(p.getId(), p.getFunctionId(), p.getXValue(), p.getYValue()));
        }
        sendSuccessList(resp, HttpServletResponse.SC_OK, dtos, dtos.size());
    }

    private void handleGetByFunctionAndY(Long funcId, double yValue, HttpServletResponse resp) throws IOException, SQLException {
        List<PointDAO.Point> points = pointDAO.findByFunctionIdAndY(funcId, yValue);
        List<PointDTO> dtos = new ArrayList<>();
        for (PointDAO.Point p : points) {
            dtos.add(new PointDTO(p.getId(), p.getFunctionId(), p.getXValue(), p.getYValue()));
        }
        sendSuccessList(resp, HttpServletResponse.SC_OK, dtos, dtos.size());
    }

    private void handleGetByFunctionAndYRange(Long funcId, double yMin, double yMax, HttpServletResponse resp) throws IOException, SQLException {
        List<PointDAO.Point> points = pointDAO.findByFunctionIdAndYRange(funcId, yMin, yMax);
        List<PointDTO> dtos = new ArrayList<>();
        for (PointDAO.Point p : points) {
            dtos.add(new PointDTO(p.getId(), p.getFunctionId(), p.getXValue(), p.getYValue()));
        }
        sendSuccessList(resp, HttpServletResponse.SC_OK, dtos, dtos.size());
    }

    public static class BatchPointsRequest {
        public Long funcId;
        public List<SimplePoint> points;
    }

    public static class SimplePoint {
        public Double xValue;
        public Double yValue;
    }
}

