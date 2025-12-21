package servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import database.dao.UserDAO;
import database.dto.UserDTO;
import database.mapper.UserMapper;
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
 * Сервлет для работы с UserDTO
 * Реализует CRUD операции согласно API контракту
 */
@WebServlet("/api/v1/users/*")
public class UserServlet extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(UserServlet.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            String pathInfo = req.getPathInfo();
            
            // GET /api/v1/users/{id}
            if (pathInfo != null && pathInfo.matches("/\\d+")) {
                Long id = Long.parseLong(pathInfo.substring(1));
                handleGetById(id, resp);
            }
            // GET /api/v1/users/search?login=... или ?email=... или ?loginLike=...
            else if (req.getParameter("login") != null) {
                handleSearchByLogin(req.getParameter("login"), resp);
            } else if (req.getParameter("email") != null) {
                handleSearchByEmail(req.getParameter("email"), resp);
            } else if (req.getParameter("loginLike") != null) {
                handleSearchByLoginLike(req.getParameter("loginLike"), resp);
            }
            // GET /api/v1/users
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
            UserDTO userDTO = objectMapper.readValue(req.getReader(), UserDTO.class);
            handleCreate(userDTO, resp);
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
            UserDTO userDTO = objectMapper.readValue(req.getReader(), UserDTO.class);
            handleUpdate(id, userDTO, resp);
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
            
            // DELETE /api/v1/users/{id}
            if (pathInfo != null && pathInfo.matches("/\\d+")) {
                Long id = Long.parseLong(pathInfo.substring(1));
                handleDeleteById(id, resp);
            }
            // DELETE /api/v1/users?login=...
            else if (req.getParameter("login") != null) {
                handleDeleteByLogin(req.getParameter("login"), resp);
            } else {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "BAD_REQUEST", "ID или login обязателен");
            }
        } catch (Exception e) {
            logger.error("Ошибка при обработке DELETE запроса", e);
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", e.getMessage());
        }
    }

    private void handleGetAll(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
        logger.debug("Получение всех пользователей");
        List<UserDAO.User> users = userDAO.findAll();
        List<UserDTO> dtos = new ArrayList<>();
        
        for (UserDAO.User user : users) {
            // Преобразуем User в UserDTO (без пароля для безопасности)
            UserDTO dto = new UserDTO(user.getId(), user.getLogin(), null, user.getEmail());
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
        logger.debug("Получение пользователя по ID: {}", id);
        var userOpt = userDAO.findById(id);
        
        if (userOpt.isPresent()) {
            UserDAO.User user = userOpt.get();
            UserDTO dto = new UserDTO(user.getId(), user.getLogin(), null, user.getEmail());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", dto);
            
            objectMapper.writeValue(resp.getWriter(), response);
            resp.setStatus(HttpServletResponse.SC_OK);
        } else {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", "Пользователь не найден");
        }
    }

    private void handleSearchByLogin(String login, HttpServletResponse resp) throws IOException, SQLException {
        logger.debug("Поиск пользователя по login: {}", login);
        var userOpt = userDAO.findByLogin(login);
        
        if (userOpt.isPresent()) {
            UserDAO.User user = userOpt.get();
            UserDTO dto = new UserDTO(user.getId(), user.getLogin(), null, user.getEmail());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", dto);
            
            objectMapper.writeValue(resp.getWriter(), response);
            resp.setStatus(HttpServletResponse.SC_OK);
        } else {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", "Пользователь не найден");
        }
    }

    private void handleSearchByEmail(String email, HttpServletResponse resp) throws IOException, SQLException {
        logger.debug("Поиск пользователя по email: {}", email);
        var userOpt = userDAO.findByEmail(email);
        
        if (userOpt.isPresent()) {
            UserDAO.User user = userOpt.get();
            UserDTO dto = new UserDTO(user.getId(), user.getLogin(), null, user.getEmail());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", dto);
            
            objectMapper.writeValue(resp.getWriter(), response);
            resp.setStatus(HttpServletResponse.SC_OK);
        } else {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", "Пользователь не найден");
        }
    }

    private void handleSearchByLoginLike(String pattern, HttpServletResponse resp) throws IOException, SQLException {
        logger.debug("Поиск пользователей по loginLike: {}", pattern);
        List<UserDAO.User> users = userDAO.findByLoginLike("%" + pattern + "%");
        List<UserDTO> dtos = new ArrayList<>();
        
        for (UserDAO.User user : users) {
            UserDTO dto = new UserDTO(user.getId(), user.getLogin(), null, user.getEmail());
            dtos.add(dto);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", dtos);
        response.put("total", dtos.size());

        objectMapper.writeValue(resp.getWriter(), response);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    private void handleCreate(UserDTO userDTO, HttpServletResponse resp) throws IOException, SQLException {
        logger.debug("Создание пользователя: {}", userDTO.getLogin());
        
        Long id;
        if (userDTO.getEmail() != null && !userDTO.getEmail().isEmpty()) {
            id = userDAO.insert(userDTO.getLogin(), userDTO.getPassword(), userDTO.getEmail());
        } else {
            id = userDAO.insert(userDTO.getLogin(), userDTO.getPassword());
        }

        UserDTO createdDTO = new UserDTO(id, userDTO.getLogin(), null, userDTO.getEmail());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", createdDTO);
        response.put("message", "Пользователь успешно создан");

        objectMapper.writeValue(resp.getWriter(), response);
        resp.setStatus(HttpServletResponse.SC_CREATED);
    }

    private void handleUpdate(Long id, UserDTO userDTO, HttpServletResponse resp) throws IOException, SQLException {
        logger.debug("Обновление пользователя с ID: {}", id);
        
        boolean updated = userDAO.update(id, userDTO.getLogin(), userDTO.getPassword(), userDTO.getEmail());
        
        if (updated) {
            UserDTO updatedDTO = new UserDTO(id, userDTO.getLogin(), null, userDTO.getEmail());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", updatedDTO);
            response.put("message", "Пользователь успешно обновлен");

            objectMapper.writeValue(resp.getWriter(), response);
            resp.setStatus(HttpServletResponse.SC_OK);
        } else {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", "Пользователь не найден");
        }
    }

    private void handleDeleteById(Long id, HttpServletResponse resp) throws IOException, SQLException {
        logger.debug("Удаление пользователя по ID: {}", id);
        boolean deleted = userDAO.deleteById(id);
        
        if (deleted) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Пользователь успешно удален");

            objectMapper.writeValue(resp.getWriter(), response);
            resp.setStatus(HttpServletResponse.SC_OK);
        } else {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", "Пользователь не найден");
        }
    }

    private void handleDeleteByLogin(String login, HttpServletResponse resp) throws IOException, SQLException {
        logger.debug("Удаление пользователя по login: {}", login);
        boolean deleted = userDAO.deleteByLogin(login);
        
        if (deleted) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Пользователь успешно удален");

            objectMapper.writeValue(resp.getWriter(), response);
            resp.setStatus(HttpServletResponse.SC_OK);
        } else {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", "Пользователь не найден");
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

