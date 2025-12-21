package servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import database.dao.PointDAO;
import database.dto.PointDTO;
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
 * Сервлет для работы с PointDTO
 * Реализует CRUD операции согласно API контракту
 */
@WebServlet("/api/v1/points/*")
public class PointServlet extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(PointServlet.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PointDAO pointDAO = new PointDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            String pathInfo = req.getPathInfo();
            
            // GET /api/v1/points/{id}
            if (pathInfo != null && pathInfo.matches("/\\d+")) {
                Long id = Long.parseLong(pathInfo.substring(1));
                handleGetById(id, resp);
            }
            // GET /api/v1/points/function/{funcId}
            else if (pathInfo != null && pathInfo.startsWith("/function/")) {
                Long funcId = Long.parseLong(pathInfo.substring(10));
                handleGetByFunctionId(funcId, resp);
            }
            // GET /api/v1/points/search?funcId=...&xValue=... или другие параметры
            else if (req.getParameter("funcId") != null) {
                Long funcId = Long.parseLong(req.getParameter("funcId"));
                if (req.getParameter("xValue") != null) {
                    Double xValue = Double.parseDouble(req.getParameter("xValue"));
                    handleGetByFunctionAndX(funcId, xValue, resp);
                } else {
                    handleGetByFunctionId(funcId, resp);
                }
            }
            // GET /api/v1/points
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
            PointDTO pointDTO = objectMapper.readValue(req.getReader(), PointDTO.class);
            handleCreate(pointDTO, resp);
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
            PointDTO pointDTO = objectMapper.readValue(req.getReader(), PointDTO.class);
            handleUpdate(id, pointDTO, resp);
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
            
            // DELETE /api/v1/points/{id}
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
        logger.debug("Получение всех точек");
        List<PointDAO.Point> points = pointDAO.findAll();
        List<PointDTO> dtos = new ArrayList<>();
        
        for (PointDAO.Point point : points) {
            PointDTO dto = new PointDTO(point.getId(), point.getFunctionId(), 
                                        point.getXValue(), point.getYValue());
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

    private void handleGetByFunctionId(Long funcId, HttpServletResponse resp) throws IOException, SQLException {
        logger.debug("Получение точек функции с ID: {}", funcId);
        List<PointDAO.Point> points = pointDAO.findByFunctionId(funcId);
        List<PointDTO> dtos = new ArrayList<>();
        
        for (PointDAO.Point point : points) {
            PointDTO dto = new PointDTO(point.getId(), point.getFunctionId(), 
                                       point.getXValue(), point.getYValue());
            dtos.add(dto);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", dtos);
        response.put("total", dtos.size());

        objectMapper.writeValue(resp.getWriter(), response);
        resp.setStatus(HttpServletResponse.SC_OK);
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
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", createdDTO);
        response.put("message", "Точка успешно создана");

        objectMapper.writeValue(resp.getWriter(), response);
        resp.setStatus(HttpServletResponse.SC_CREATED);
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
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Точка успешно удалена");

            objectMapper.writeValue(resp.getWriter(), response);
            resp.setStatus(HttpServletResponse.SC_OK);
        } else {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", "Точка не найдена");
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

