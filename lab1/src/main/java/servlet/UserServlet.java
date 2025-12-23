package servlet;

import database.dao.UserDAO;
import database.dto.UserDTO;
import database.mapper.UserMapper;

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
 * Сервлет для работы с UserDTO
 * Реализует CRUD операции согласно API контракту
 */
@WebServlet("/api/v1/users/*")
public class UserServlet extends AbstractApiServlet {
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        prepareJsonResponse(resp);
        List<String> segments = pathSegments(req);
        logger.info("GET {} pathInfo={} params={}", req.getRequestURI(), req.getPathInfo(), req.getQueryString());

        try {
            // GET /api/v1/users
            if (segments.isEmpty()) {
                if (!isAdmin(req)) {
                    sendError(resp, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN", "Недостаточно прав");
                    return;
                }
                handleGetAll(req, resp);
                return;
            }

            // GET /api/v1/users/search?... (контракт)
            if ("search".equalsIgnoreCase(segments.get(0))) {
                if (!isAdmin(req)) {
                    sendError(resp, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN", "Недостаточно прав");
                    return;
                }
                handleSearch(req, resp);
                return;
            }

            // GET /api/v1/users/{id}
            if (segments.size() == 1 && segments.get(0).matches("\\d+")) {
                Long id = parseLongStrict(segments.get(0));
                if (!isAdmin(req) && !isSelf(req, id)) {
                    sendError(resp, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN", "Доступ только к своему профилю");
                    return;
                }
                handleGetById(id, resp);
                return;
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
            UserDTO userDTO = objectMapper.readValue(req.getReader(), UserDTO.class);
            if (!UserMapper.isValid(userDTO)) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", "login и password обязательны");
                return;
            }
            // Роль можно задавать только ADMIN. Иначе всегда создаём USER.
            if (!isAdmin(req)) {
                userDTO.setRole("USER");
            } else if (userDTO.getRole() == null || userDTO.getRole().isBlank()) {
                userDTO.setRole("USER");
            }
            handleCreate(userDTO, resp);
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
            if (!isAdmin(req) && !isSelf(req, id)) {
                sendError(resp, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN", "Доступ только к своему профилю");
                return;
            }
            UserDTO userDTO = objectMapper.readValue(req.getReader(), UserDTO.class);
            if (!UserMapper.isValid(userDTO)) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", "login и password обязательны");
                return;
            }
            handleUpdate(id, userDTO, resp);
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
            // PATCH /api/v1/users/{id}/password|email|login
            if (segments.size() != 2 || !segments.get(0).matches("\\d+")) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", "Ожидается /users/{id}/{field}");
                return;
            }
            Long id = parseLongStrict(segments.get(0));
            String field = segments.get(1);
            if (!isAdmin(req) && !isSelf(req, id)) {
                sendError(resp, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN", "Доступ только к своему профилю");
                return;
            }

            Map<String, Object> body = readJson(req, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>(){});

            if ("password".equalsIgnoreCase(field)) {
                Object pass = body.get("password");
                if (!(pass instanceof String) || ((String) pass).isBlank()) {
                    sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", "password обязателен");
                    return;
                }
                boolean updated = userDAO.updatePassword(id, (String) pass);
                if (!updated) {
                    sendError(resp, HttpServletResponse.SC_NOT_FOUND, "USER_NOT_FOUND", "Пользователь не найден");
                    return;
                }
                sendSuccess(resp, HttpServletResponse.SC_OK, Map.of("id", id), "Пароль обновлен");
                return;
            }

            if ("email".equalsIgnoreCase(field)) {
                Object email = body.get("email");
                if (!(email instanceof String) || ((String) email).isBlank()) {
                    sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", "email обязателен");
                    return;
                }
                boolean updated = userDAO.updateEmail(id, (String) email);
                if (!updated) {
                    sendError(resp, HttpServletResponse.SC_NOT_FOUND, "USER_NOT_FOUND", "Пользователь не найден");
                    return;
                }
                sendSuccess(resp, HttpServletResponse.SC_OK, Map.of("id", id, "email", email), "Email обновлен");
                return;
            }

            if ("login".equalsIgnoreCase(field)) {
                Object login = body.get("login");
                if (!(login instanceof String) || ((String) login).isBlank()) {
                    sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", "login обязателен");
                    return;
                }
                boolean updated = userDAO.updateLogin(id, (String) login);
                if (!updated) {
                    sendError(resp, HttpServletResponse.SC_NOT_FOUND, "USER_NOT_FOUND", "Пользователь не найден");
                    return;
                }
                sendSuccess(resp, HttpServletResponse.SC_OK, Map.of("id", id, "login", login), "Login обновлен");
                return;
            }

            // PATCH /api/v1/users/{id}/role (только ADMIN)
            if ("role".equalsIgnoreCase(field)) {
                if (!isAdmin(req)) {
                    sendError(resp, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN", "Недостаточно прав");
                    return;
                }
                Object role = body.get("role");
                if (!(role instanceof String) || ((String) role).isBlank()) {
                    sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", "role обязателен");
                    return;
                }
                String r = ((String) role).trim().toUpperCase();
                if (!("ADMIN".equals(r) || "USER".equals(r))) {
                    sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", "role должен быть ADMIN или USER");
                    return;
                }
                boolean updated = userDAO.updateRole(id, r);
                if (!updated) {
                    sendError(resp, HttpServletResponse.SC_NOT_FOUND, "USER_NOT_FOUND", "Пользователь не найден");
                    return;
                }
                sendSuccess(resp, HttpServletResponse.SC_OK, Map.of("id", id, "role", r), "Роль обновлена");
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
            // DELETE /api/v1/users/{id}
            if (segments.size() == 1 && segments.get(0).matches("\\d+")) {
                Long id = parseLongStrict(segments.get(0));
                if (!isAdmin(req) && !isSelf(req, id)) {
                    sendError(resp, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN", "Доступ только к своему профилю");
                    return;
                }
                handleDeleteById(id, resp);
            }
            // DELETE /api/v1/users?login=...
            else if (segments.isEmpty() && req.getParameter("login") != null) {
                if (!isAdmin(req)) {
                    sendError(resp, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN", "Недостаточно прав");
                    return;
                }
                handleDeleteByLogin(req.getParameter("login"), resp);
            } else {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", "ID или login обязателен");
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
            UserDTO dto = new UserDTO(user.getId(), user.getLogin(), null, user.getEmail(), user.getRole());
            dtos.add(dto);
        }

        // сортировка и пагинация (контракт)
        String sortBy = getStringParam(req, "sortBy");
        String order = getStringParam(req, "order");
        Integer limit = getIntParam(req, "limit");
        Integer offset = getIntParam(req, "offset");

        if (sortBy != null) {
            Comparator<UserDTO> comparator;
            switch (sortBy) {
                case "id":
                    comparator = Comparator.comparing(UserDTO::getId, Comparator.nullsLast(Long::compareTo));
                    break;
                case "login":
                    comparator = Comparator.comparing(UserDTO::getLogin, Comparator.nullsLast(String::compareToIgnoreCase));
                    break;
                case "email":
                    comparator = Comparator.comparing(UserDTO::getEmail, Comparator.nullsLast(String::compareToIgnoreCase));
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
        List<UserDTO> page = applyLimitOffset(dtos, limit, offset);
        sendSuccessList(resp, HttpServletResponse.SC_OK, page, total);
    }

    private void handleGetById(Long id, HttpServletResponse resp) throws IOException, SQLException {
        logger.debug("Получение пользователя по ID: {}", id);
        var userOpt = userDAO.findById(id);
        
        if (userOpt.isPresent()) {
            UserDAO.User user = userOpt.get();
            UserDTO dto = new UserDTO(user.getId(), user.getLogin(), null, user.getEmail(), user.getRole());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", dto);
            
            objectMapper.writeValue(resp.getWriter(), response);
            resp.setStatus(HttpServletResponse.SC_OK);
        } else {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", "Пользователь не найден");
        }
    }

    private void handleSearch(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
        // GET /api/v1/users/search?login=... | email=... | loginLike=...
        if (req.getParameter("login") != null) {
            handleSearchByLogin(req.getParameter("login"), resp);
            return;
        }
        if (req.getParameter("email") != null) {
            handleSearchByEmail(req.getParameter("email"), resp);
            return;
        }
        if (req.getParameter("loginLike") != null) {
            handleSearchByLoginLike(req.getParameter("loginLike"), resp);
            return;
        }
        sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR",
                "Ожидается один из параметров: login, email, loginLike");
    }

    private void handleSearchByLogin(String login, HttpServletResponse resp) throws IOException, SQLException {
        logger.debug("Поиск пользователя по login: {}", login);
        var userOpt = userDAO.findByLogin(login);
        
        if (userOpt.isPresent()) {
            UserDAO.User user = userOpt.get();
            UserDTO dto = new UserDTO(user.getId(), user.getLogin(), null, user.getEmail(), user.getRole());
            
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
            UserDTO dto = new UserDTO(user.getId(), user.getLogin(), null, user.getEmail(), user.getRole());
            
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
            UserDTO dto = new UserDTO(user.getId(), user.getLogin(), null, user.getEmail(), user.getRole());
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
            id = userDAO.insert(userDTO.getLogin(), userDTO.getPassword(), userDTO.getEmail(), userDTO.getRole());
        } else {
            id = userDAO.insert(userDTO.getLogin(), userDTO.getPassword(), userDTO.getRole());
        }

        UserDTO createdDTO = new UserDTO(id, userDTO.getLogin(), null, userDTO.getEmail(), userDTO.getRole());
        sendSuccess(resp, HttpServletResponse.SC_CREATED, createdDTO, "Пользователь успешно создан");
    }

    private void handleUpdate(Long id, UserDTO userDTO, HttpServletResponse resp) throws IOException, SQLException {
        logger.debug("Обновление пользователя с ID: {}", id);
        
        boolean updated = userDAO.update(id, userDTO.getLogin(), userDTO.getPassword(), userDTO.getEmail());
        
        if (updated) {
            // роль тут не меняем (отдельный endpoint /role)
            var userOpt = userDAO.findById(id);
            String role = userOpt.isPresent() ? userOpt.get().getRole() : null;
            UserDTO updatedDTO = new UserDTO(id, userDTO.getLogin(), null, userDTO.getEmail(), role);
            
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
            sendSuccess(resp, HttpServletResponse.SC_OK, null, "Пользователь успешно удален");
        } else {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", "Пользователь не найден");
        }
    }

    private void handleDeleteByLogin(String login, HttpServletResponse resp) throws IOException, SQLException {
        logger.debug("Удаление пользователя по login: {}", login);
        boolean deleted = userDAO.deleteByLogin(login);
        
        if (deleted) {
            sendSuccess(resp, HttpServletResponse.SC_OK, null, "Пользователь успешно удален");
        } else {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", "Пользователь не найден");
        }
    }
}

