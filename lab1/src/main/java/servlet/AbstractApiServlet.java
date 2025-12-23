package servlet;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * Общая база для API-сервлетов:
 * - унифицированные ответы success/data/message/error
 * - парсинг pathInfo в сегменты
 * - базовая валидация query/body
 */
public abstract class AbstractApiServlet extends HttpServlet {
    protected final Logger logger = LogManager.getLogger(getClass());
    protected final ObjectMapper objectMapper = new ObjectMapper();

    protected AuthContext auth(HttpServletRequest req) {
        Object v = req.getAttribute(AuthContext.ATTR_NAME);
        return (v instanceof AuthContext) ? (AuthContext) v : null;
    }

    protected boolean isAdmin(HttpServletRequest req) {
        AuthContext ctx = auth(req);
        return ctx != null && ctx.isAdmin();
    }

    protected void requireAdmin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!isAdmin(req)) {
            AuthContext ctx = auth(req);
            logger.warn("FORBIDDEN admin-only: {} {} login={}",
                    req.getMethod(), req.getRequestURI(), ctx == null ? null : ctx.getLogin());
            sendError(resp, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN", "Недостаточно прав");
        }
    }

    protected boolean isSelf(HttpServletRequest req, Long userId) {
        AuthContext ctx = auth(req);
        return ctx != null && userId != null && userId.equals(ctx.getUserId());
    }

    protected void prepareJsonResponse(HttpServletResponse resp) {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
    }

    protected List<String> pathSegments(HttpServletRequest req) {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.isBlank() || "/".equals(pathInfo)) {
            return Collections.emptyList();
        }
        String[] raw = pathInfo.split("/");
        List<String> segments = new ArrayList<>();
        for (String s : raw) {
            if (s != null && !s.isBlank()) {
                segments.add(s);
            }
        }
        return segments;
    }

    protected Long parseLongStrict(String s) {
        return Long.parseLong(s);
    }

    protected Double parseDoubleStrict(String s) {
        return Double.parseDouble(s);
    }

    protected Integer getIntParam(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        if (v == null || v.isBlank()) return null;
        return Integer.parseInt(v);
    }

    protected String getStringParam(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        if (v == null || v.isBlank()) return null;
        return v;
    }

    protected <T> T readJson(HttpServletRequest req, Class<T> clazz) throws IOException {
        return objectMapper.readValue(req.getReader(), clazz);
    }

    protected <T> T readJson(HttpServletRequest req, TypeReference<T> ref) throws IOException {
        return objectMapper.readValue(req.getReader(), ref);
    }

    protected void sendSuccess(HttpServletResponse resp, int status, Object data, String message) throws IOException {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        if (data != null) response.put("data", data);
        if (message != null) response.put("message", message);
        resp.setStatus(status);
        objectMapper.writeValue(resp.getWriter(), response);
    }

    protected void sendSuccessList(HttpServletResponse resp, int status, List<?> data, Integer total) throws IOException {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("data", data);
        if (total != null) response.put("total", total);
        resp.setStatus(status);
        objectMapper.writeValue(resp.getWriter(), response);
    }

    protected void sendError(HttpServletResponse resp, int status, String code, String message) throws IOException {
        Map<String, Object> errorResponse = new LinkedHashMap<>();
        errorResponse.put("success", false);
        Map<String, String> error = new LinkedHashMap<>();
        error.put("code", code);
        error.put("message", message);
        errorResponse.put("error", error);
        resp.setStatus(status);
        objectMapper.writeValue(resp.getWriter(), errorResponse);
    }

    protected <T> List<T> applyLimitOffset(List<T> items, Integer limit, Integer offset) {
        if (items == null) return Collections.emptyList();
        int total = items.size();
        int from = offset == null ? 0 : Math.max(0, offset);
        if (from >= total) return Collections.emptyList();
        int to = limit == null ? total : Math.min(total, from + Math.max(0, limit));
        return items.subList(from, to);
    }
}


