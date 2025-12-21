package servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import database.dao.ResultDAO;
import database.dto.ResultDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
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
public class ResultServlet extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(ResultServlet.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ResultDAO resultDAO = new ResultDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            String pathInfo = req.getPathInfo();
            
            // GET /api/v1/results/{id}
            if (pathInfo != null && pathInfo.matches("/\\d+")) {
                Long id = Long.parseLong(pathInfo.substring(1));
                handleGetById(id, resp);
            }
            // GET /api/v1/results/function/{funcId}
            else if (pathInfo != null && pathInfo.startsWith("/function/")) {
                Long funcId = Long.parseLong(pathInfo.substring(10));
                handleGetByFunctionId(funcId, resp);
            }
            // GET /api/v1/results/search?resultId=...
            else if (req.getParameter("resultId") != null) {
                Long resultId = Long.parseLong(req.getParameter("resultId"));
                handleGetByFunctionId(resultId, resp);
            }
            // GET /api/v1/results
            else {
                handleGetAll(req, resp);
            }
        } catch (Exception e) {
            logger.error("Ошибка при обработке GET запроса", e);
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            ResultDTO resultDTO = objectMapper.readValue(req.getReader(), ResultDTO.class);
            handleCreate(resultDTO, resp);
        } catch (Exception e) {
            logger.error("Ошибка при обработке POST запроса", e);
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "BAD_REQUEST", e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            String pathInfo = req.getPathInfo();
            if (pathInfo == null || !pathInfo.matches("/\\d+")) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "BAD_REQUEST", "ID обязателен");
                return;
            }

            Long id = Long.parseLong(pathInfo.substring(1));
            ResultDTO resultDTO = objectMapper.readValue(req.getReader(), ResultDTO.class);
            handleUpdate(id, resultDTO, resp);
        } catch (Exception e) {
            logger.error("Ошибка при обработке PUT запроса", e);
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "BAD_REQUEST", e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            String pathInfo = req.getPathInfo();
            
            // DELETE /api/v1/results/{id}
            if (pathInfo != null && pathInfo.matches("/\\d+")) {
                Long id = Long.parseLong(pathInfo.substring(1));
                handleDeleteById(id, resp);
            } else {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "BAD_REQUEST", "ID обязателен");
            }
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

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", dtos);
        response.put("total", dtos.size());

        objectMapper.writeValue(resp.getWriter(), response);
        resp.setStatus(HttpServletResponse.SC_OK);
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
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", createdDTO);
        response.put("message", "Результат успешно создан");

        objectMapper.writeValue(resp.getWriter(), response);
        resp.setStatus(HttpServletResponse.SC_CREATED);
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
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Результат успешно удален");

            objectMapper.writeValue(resp.getWriter(), response);
            resp.setStatus(HttpServletResponse.SC_OK);
        } else {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", "Результат не найден");
        }
    }

    private void sendError(HttpServletResponse resp, int status, String code, String message) throws IOException {
        resp.setStatus(status);
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        
        Map<String, String> error = new HashMap<>();
        error.put("code", code);
        error.put("message", message);
        errorResponse.put("error", error);

        objectMapper.writeValue(resp.getWriter(), errorResponse);
    }
}

