package servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import database.dao.FunctionDAO;
import database.dto.FunctionDTO;
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
 * Сервлет для работы с FunctionDTO
 * Реализует CRUD операции согласно API контракту
 */
@WebServlet("/api/v1/functions/*")
public class FunctionServlet extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(FunctionServlet.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final FunctionDAO functionDAO = new FunctionDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            String pathInfo = req.getPathInfo();
            
            // GET /api/v1/functions/{id}
            if (pathInfo != null && pathInfo.matches("/\\d+")) {
                Long id = Long.parseLong(pathInfo.substring(1));
                handleGetById(id, resp);
            }
            // GET /api/v1/functions/user/{userId}
            else if (pathInfo != null && pathInfo.startsWith("/user/")) {
                Long userId = Long.parseLong(pathInfo.substring(6));
                handleGetByUserId(userId, resp);
            }
            // GET /api/v1/functions/search?type=... или ?name=... или ?nameLike=...
            else if (req.getParameter("type") != null && req.getParameter("userId") != null) {
                Long userId = Long.parseLong(req.getParameter("userId"));
                handleSearchByUserAndType(userId, req.getParameter("type"), resp);
            } else if (req.getParameter("type") != null) {
                handleSearchByType(req.getParameter("type"), resp);
            } else if (req.getParameter("name") != null) {
                handleSearchByName(req.getParameter("name"), resp);
            } else if (req.getParameter("nameLike") != null) {
                handleSearchByNameLike(req.getParameter("nameLike"), resp);
            }
            // GET /api/v1/functions
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
            FunctionDTO functionDTO = objectMapper.readValue(req.getReader(), FunctionDTO.class);
            handleCreate(functionDTO, resp);
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
            FunctionDTO functionDTO = objectMapper.readValue(req.getReader(), FunctionDTO.class);
            handleUpdate(id, functionDTO, resp);
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
            
            // DELETE /api/v1/functions/{id}
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
        logger.debug("Получение всех функций");
        List<FunctionDAO.Function> functions = functionDAO.findAll();
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

    private void handleGetByUserId(Long userId, HttpServletResponse resp) throws IOException, SQLException {
        logger.debug("Получение функций пользователя с ID: {}", userId);
        List<FunctionDAO.Function> functions = functionDAO.findByUserId(userId);
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
        var functionOpt = functionDAO.findByName(name);
        
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
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", createdDTO);
        response.put("message", "Функция успешно создана");

        objectMapper.writeValue(resp.getWriter(), response);
        resp.setStatus(HttpServletResponse.SC_CREATED);
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
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Функция успешно удалена");

            objectMapper.writeValue(resp.getWriter(), response);
            resp.setStatus(HttpServletResponse.SC_OK);
        } else {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", "Функция не найдена");
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

