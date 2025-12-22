package servlet;

import database.dao.ResultDAO;
import database.dto.ResultDTO;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Сервлет для работы с ResultDTO
 * Реализует CRUD операции согласно API контракту
 */
@WebServlet("/api/v1/results/*")
public class ResultServlet extends AbstractApiServlet {
    private final ResultDAO resultDAO = new ResultDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        prepareJsonResponse(resp);
        List<String> segments = pathSegments(req);
        logger.info("GET {} pathInfo={} params={}", req.getRequestURI(), req.getPathInfo(), req.getQueryString());

        try {
            // GET /api/v1/results
            if (segments.isEmpty()) {
                // GET /api/v1/results/search?resultLike=...
                if (req.getParameter("resultLike") != null) {
                    handleSearchByResultLike(req.getParameter("resultLike"), resp);
                    return;
                }
                handleGetAll(req, resp);
                return;
            }

            // GET /api/v1/results/{id}
            if (segments.size() == 1 && segments.get(0).matches("\\d+")) {
                handleGetById(parseLongStrict(segments.get(0)), resp);
                return;
            }

            // GET /api/v1/results/function/{resultId}/...
            if (segments.size() >= 2 && "function".equalsIgnoreCase(segments.get(0)) && segments.get(1).matches("\\d+")) {
                Long resultId = parseLongStrict(segments.get(1));

                // /function/{resultId}
                if (segments.size() == 2) {
                    handleGetByFunctionId(resultId, resp);
                    return;
                }

                // /function/{resultId}/latest
                if (segments.size() == 3 && "latest".equalsIgnoreCase(segments.get(2))) {
                    var lastOpt = resultDAO.findLastByFunctionId(resultId);
                    if (lastOpt.isPresent()) {
                        ResultDAO.Result r = lastOpt.get();
                        ResultDTO dto = new ResultDTO(r.getId(), r.getFunctionId(), r.getResult());
                        sendSuccess(resp, HttpServletResponse.SC_OK, dto, null);
                    } else {
                        sendError(resp, HttpServletResponse.SC_NOT_FOUND, "RESULT_NOT_FOUND", "Результат не найден");
                    }
                    return;
                }

                // /function/{resultId}/count
                if (segments.size() == 3 && "count".equalsIgnoreCase(segments.get(2))) {
                    int count = resultDAO.countByFunctionId(resultId);
                    sendSuccess(resp, HttpServletResponse.SC_OK, Map.of("result_count", count), null);
                    return;
                }

                // /function/{resultId}/search?resultLike=...
                if (segments.size() == 3 && "search".equalsIgnoreCase(segments.get(2))) {
                    String like = req.getParameter("resultLike");
                    if (like == null) {
                        sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", "resultLike обязателен");
                        return;
                    }
                    handleSearchByFunctionAndResultLike(resultId, like, resp);
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
            // POST /api/v1/results
            if (segments.isEmpty()) {
                ResultDTO resultDTO = objectMapper.readValue(req.getReader(), ResultDTO.class);
                handleCreate(resultDTO, resp);
                return;
            }

            // POST /api/v1/results/batch
            if (segments.size() == 1 && "batch".equalsIgnoreCase(segments.get(0))) {
                BatchResultsRequest body = readJson(req, BatchResultsRequest.class);
                if (body == null || body.resultId == null || body.results == null || body.results.isEmpty()) {
                    sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", "resultId и results обязательны");
                    return;
                }
                List<ResultDTO> created = new ArrayList<>();
                for (String r : body.results) {
                    if (r == null) continue;
                    Long id = resultDAO.insert(body.resultId, r);
                    created.add(new ResultDTO(id, body.resultId, r));
                }
                Map<String, Object> data = new HashMap<>();
                data.put("created", created.size());
                data.put("results", created);
                sendSuccess(resp, HttpServletResponse.SC_CREATED, data, "Создано результатов: " + created.size());
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
            ResultDTO resultDTO = objectMapper.readValue(req.getReader(), ResultDTO.class);
            handleUpdate(id, resultDTO, resp);
        } catch (Exception e) {
            logger.error("Ошибка при обработке PUT запроса", e);
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", e.getMessage());
        }
    }

    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        prepareJsonResponse(resp);
        List<String> segments = pathSegments(req);
        logger.info("PATCH {} pathInfo={}", req.getRequestURI(), req.getPathInfo());

        try {
            // PATCH /api/v1/results/function/{resultId}
            if (segments.size() == 2 && "function".equalsIgnoreCase(segments.get(0)) && segments.get(1).matches("\\d+")) {
                Long resultId = parseLongStrict(segments.get(1));
                Map<String, Object> body = readJson(req, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>(){});
                Object result = body.get("result");
                if (!(result instanceof String) || ((String) result).isBlank()) {
                    sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", "result обязателен");
                    return;
                }
                int updated = resultDAO.updateAllByFunctionId(resultId, (String) result);
                sendSuccess(resp, HttpServletResponse.SC_OK, Map.of("updated", updated), "Обновлено результатов: " + updated);
                return;
            }

            // PATCH /api/v1/results/function/{resultId}/latest
            if (segments.size() == 3 && "function".equalsIgnoreCase(segments.get(0)) && segments.get(1).matches("\\d+") && "latest".equalsIgnoreCase(segments.get(2))) {
                Long resultId = parseLongStrict(segments.get(1));
                Map<String, Object> body = readJson(req, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>(){});
                Object result = body.get("result");
                if (!(result instanceof String) || ((String) result).isBlank()) {
                    sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", "result обязателен");
                    return;
                }
                boolean updated = resultDAO.updateLastByFunctionId(resultId, (String) result);
                if (!updated) {
                    sendError(resp, HttpServletResponse.SC_NOT_FOUND, "RESULT_NOT_FOUND", "Результат не найден");
                    return;
                }
                sendSuccess(resp, HttpServletResponse.SC_OK, Map.of("resultId", resultId), "Последний результат обновлен");
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
            // DELETE /api/v1/results/{id}
            if (segments.size() == 1 && segments.get(0).matches("\\d+")) {
                Long id = parseLongStrict(segments.get(0));
                handleDeleteById(id, resp);
                return;
            }

            // DELETE /api/v1/results/function/{resultId}
            if (segments.size() == 2 && "function".equalsIgnoreCase(segments.get(0)) && segments.get(1).matches("\\d+")) {
                Long resultId = parseLongStrict(segments.get(1));
                int deleted = resultDAO.deleteByFunctionId(resultId);
                sendSuccess(resp, HttpServletResponse.SC_OK, Map.of("deleted", deleted), "Удалено результатов: " + deleted);
                return;
            }

            // DELETE /api/v1/results/function/{resultId}/latest
            if (segments.size() == 3 && "function".equalsIgnoreCase(segments.get(0)) && segments.get(1).matches("\\d+") && "latest".equalsIgnoreCase(segments.get(2))) {
                Long resultId = parseLongStrict(segments.get(1));
                boolean deleted = resultDAO.deleteLastByFunctionId(resultId);
                if (!deleted) {
                    sendError(resp, HttpServletResponse.SC_NOT_FOUND, "RESULT_NOT_FOUND", "Результат не найден");
                    return;
                }
                sendSuccess(resp, HttpServletResponse.SC_OK, null, "Последний результат удален");
                return;
            }

            // DELETE /api/v1/results/function/{resultId}/search?resultLike=...
            if (segments.size() == 3 && "function".equalsIgnoreCase(segments.get(0)) && segments.get(1).matches("\\d+") && "search".equalsIgnoreCase(segments.get(2))) {
                Long resultId = parseLongStrict(segments.get(1));
                String like = req.getParameter("resultLike");
                if (like == null) {
                    sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", "resultLike обязателен");
                    return;
                }
                int deleted = resultDAO.deleteByFunctionIdAndResultLike(resultId, "%" + like + "%");
                sendSuccess(resp, HttpServletResponse.SC_OK, Map.of("deleted", deleted), "Удалено результатов: " + deleted);
                return;
            }

            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", "Неверный маршрут");
        } catch (Exception e) {
            logger.error("Ошибка при обработке DELETE запроса", e);
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", e.getMessage());
        }
    }

    private void handleGetAll(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
        logger.debug("Получение всех результатов");
        List<ResultDAO.Result> results = resultDAO.findAll();
        List<ResultDTO> dtos = new ArrayList<>();
        
        for (ResultDAO.Result result : results) {
            ResultDTO dto = new ResultDTO(result.getId(), result.getFunctionId(), result.getResult());
            dtos.add(dto);
        }

        sendSuccessList(resp, HttpServletResponse.SC_OK, dtos, dtos.size());
    }

    private void handleGetById(Long id, HttpServletResponse resp) throws IOException, SQLException {
        logger.debug("Получение результата по ID: {}", id);
        var resultOpt = resultDAO.findById(id);
        
        if (resultOpt.isPresent()) {
            ResultDAO.Result result = resultOpt.get();
            ResultDTO dto = new ResultDTO(result.getId(), result.getFunctionId(), result.getResult());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", dto);
            
            objectMapper.writeValue(resp.getWriter(), response);
            resp.setStatus(HttpServletResponse.SC_OK);
        } else {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", "Результат не найден");
        }
    }

    private void handleGetByFunctionId(Long funcId, HttpServletResponse resp) throws IOException, SQLException {
        logger.debug("Получение результатов функции с ID: {}", funcId);
        List<ResultDAO.Result> results = resultDAO.findByFunctionId(funcId);
        List<ResultDTO> dtos = new ArrayList<>();
        
        for (ResultDAO.Result result : results) {
            ResultDTO dto = new ResultDTO(result.getId(), result.getFunctionId(), result.getResult());
            dtos.add(dto);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", dtos);
        response.put("total", dtos.size());

        objectMapper.writeValue(resp.getWriter(), response);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    private void handleCreate(ResultDTO resultDTO, HttpServletResponse resp) throws IOException, SQLException {
        logger.debug("Создание результата для функции: {}", resultDTO.getResultId());
        
        Long id = resultDAO.insert(resultDTO.getResultId(), resultDTO.getResult());

        ResultDTO createdDTO = new ResultDTO(id, resultDTO.getResultId(), resultDTO.getResult());
        
        sendSuccess(resp, HttpServletResponse.SC_CREATED, createdDTO, "Результат успешно создан");
    }

    private void handleUpdate(Long id, ResultDTO resultDTO, HttpServletResponse resp) throws IOException, SQLException {
        logger.debug("Обновление результата с ID: {}", id);
        
        boolean updated = resultDAO.updateById(id, resultDTO.getResult());
        
        if (updated) {
            ResultDTO updatedDTO = new ResultDTO(id, resultDTO.getResultId(), resultDTO.getResult());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", updatedDTO);
            response.put("message", "Результат успешно обновлен");

            objectMapper.writeValue(resp.getWriter(), response);
            resp.setStatus(HttpServletResponse.SC_OK);
        } else {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", "Результат не найден");
        }
    }

    private void handleDeleteById(Long id, HttpServletResponse resp) throws IOException, SQLException {
        logger.debug("Удаление результата по ID: {}", id);
        boolean deleted = resultDAO.deleteById(id);
        
        if (deleted) {
            sendSuccess(resp, HttpServletResponse.SC_OK, null, "Результат успешно удален");
        } else {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", "Результат не найден");
        }
    }

    private void handleSearchByResultLike(String like, HttpServletResponse resp) throws IOException, SQLException {
        List<ResultDAO.Result> results = resultDAO.findByResultLike("%" + like + "%");
        List<ResultDTO> dtos = new ArrayList<>();
        for (ResultDAO.Result r : results) {
            dtos.add(new ResultDTO(r.getId(), r.getFunctionId(), r.getResult()));
        }
        sendSuccessList(resp, HttpServletResponse.SC_OK, dtos, dtos.size());
    }

    private void handleSearchByFunctionAndResultLike(Long resultId, String like, HttpServletResponse resp) throws IOException, SQLException {
        List<ResultDAO.Result> results = resultDAO.findByFunctionIdAndResultLike(resultId, "%" + like + "%");
        List<ResultDTO> dtos = new ArrayList<>();
        for (ResultDAO.Result r : results) {
            dtos.add(new ResultDTO(r.getId(), r.getFunctionId(), r.getResult()));
        }
        sendSuccessList(resp, HttpServletResponse.SC_OK, dtos, dtos.size());
    }

    public static class BatchResultsRequest {
        public Long resultId;
        public List<String> results;
    }
}

