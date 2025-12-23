package servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import database.dao.UserDAO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Basic Auth фильтр для всех API запросов.
 *
 * Исключения:
 * - OPTIONS (preflight)
 * - GET /api/v1/test
 * - POST /api/v1/users (регистрация)
 */
@WebFilter("/api/v1/*")
public class AuthFilter implements Filter {
    private static final Logger logger = LogManager.getLogger(AuthFilter.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final UserDAO userDAO = new UserDAO();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        // CORS preflight
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        String path = req.getRequestURI();
        // Публичный healthcheck
        if ("/api/v1/test".equals(path)) {
            chain.doFilter(request, response);
            return;
        }

        // Публичная регистрация пользователя
        if ("/api/v1/users".equals(path) && "POST".equalsIgnoreCase(req.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        String auth = req.getHeader("Authorization");
        if (auth == null || !auth.regionMatches(true, 0, "Basic ", 0, 6)) {
            logger.warn("Unauthorized request: {} {} (missing Authorization)", req.getMethod(), path);
            sendUnauthorized(resp, "UNAUTHORIZED", "Требуется Basic Auth");
            return;
        }

        String base64 = auth.substring(6).trim();
        String decoded;
        try {
            decoded = new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            logger.warn("Unauthorized request: {} {} (invalid base64)", req.getMethod(), path);
            sendUnauthorized(resp, "UNAUTHORIZED", "Некорректный Authorization header");
            return;
        }

        int idx = decoded.indexOf(':');
        if (idx <= 0) {
            logger.warn("Unauthorized request: {} {} (invalid credentials format)", req.getMethod(), path);
            sendUnauthorized(resp, "UNAUTHORIZED", "Некорректный формат учётных данных");
            return;
        }

        String login = decoded.substring(0, idx);
        String password = decoded.substring(idx + 1);

        try {
            var authUserOpt = userDAO.authenticate(login, password);
            if (authUserOpt.isEmpty()) {
                logger.warn("Unauthorized request: {} {} (bad credentials) login={}", req.getMethod(), path, login);
                sendUnauthorized(resp, "UNAUTHORIZED", "Неверный логин или пароль");
                return;
            }

            UserDAO.AuthUser authUser = authUserOpt.get();
            AuthContext ctx = new AuthContext(authUser.getId(), authUser.getLogin(), UserRole.from(authUser.getRole()));
            req.setAttribute(AuthContext.ATTR_NAME, ctx);
            logger.debug("Authenticated: login={} role={} {} {}", ctx.getLogin(), ctx.getRole(), req.getMethod(), path);
            chain.doFilter(request, response);
        } catch (Exception e) {
            logger.error("Auth error on {} {}", req.getMethod(), path, e);
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Ошибка аутентификации");
        }
    }

    private void sendUnauthorized(HttpServletResponse resp, String code, String message) throws IOException {
        resp.setHeader("WWW-Authenticate", "Basic realm=\"lab6\"");
        sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, code, message);
    }

    private void sendError(HttpServletResponse resp, int status, String code, String message) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        Map<String, Object> errorResponse = new LinkedHashMap<>();
        errorResponse.put("success", false);
        Map<String, String> error = new LinkedHashMap<>();
        error.put("code", code);
        error.put("message", message);
        errorResponse.put("error", error);
        resp.setStatus(status);
        objectMapper.writeValue(resp.getWriter(), errorResponse);
    }
}


