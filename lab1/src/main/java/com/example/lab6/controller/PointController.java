package com.example.lab6.controller;

import com.example.lab6.dto.PointDTO;
import com.example.lab6.mapper.PointMapper;
import entity.Function;
import entity.Point;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.example.lab6.security.AuthUtil;
import repository.FunctionRepository;
import repository.PointRepository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Контроллер для работы с PointDTO
 */
@RestController
@RequestMapping("/api/v1/points")
public class PointController {
    private static final Logger logger = LogManager.getLogger(PointController.class);
    private final PointRepository pointRepository;
    private final FunctionRepository functionRepository;

    public PointController(PointRepository pointRepository, FunctionRepository functionRepository) {
        this.pointRepository = pointRepository;
        this.functionRepository = functionRepository;
    }

    /**
     * Получить все точки
     */
    @GetMapping
    public ResponseEntity<List<PointDTO>> getAllPoints(Authentication auth) {
        logger.info("GET /api/v1/points - получение всех точек");
        if (!AuthUtil.isAdmin(auth)) {
            logger.warn("FORBIDDEN points list login={}", auth == null ? null : auth.getName());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            List<Point> points = pointRepository.findAll();
            List<PointDTO> dtos = points.stream()
                    .map(PointMapper::toDTO)
                    .collect(Collectors.toList());
            logger.info("Найдено точек: {}", dtos.size());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            logger.error("Ошибка при получении всех точек", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Получить точку по ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<PointDTO> getPointById(@PathVariable Long id, Authentication auth) {
        logger.info("GET /api/v1/points/{} - получение точки по ID", id);
        try {
            Optional<Point> pointOpt = pointRepository.findById(id);
            if (pointOpt.isPresent()) {
                if (!AuthUtil.isAdmin(auth)) {
                    Point p = pointOpt.get();
                    Long funcId = p.getFunction() != null ? p.getFunction().getId() : null;
                    if (funcId == null) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    }
                    var fOpt = functionRepository.findById(funcId);
                    if (fOpt.isEmpty() || fOpt.get().getUser() == null ||
                            !auth.getName().equals(fOpt.get().getUser().getLogin())) {
                        logger.warn("FORBIDDEN point read id={} login={}", id, auth == null ? null : auth.getName());
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    }
                }
                PointDTO dto = PointMapper.toDTO(pointOpt.get());
                return ResponseEntity.ok(dto);
            } else {
                logger.warn("Точка с ID {} не найдена", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Ошибка при получении точки по ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Получить точки функции
     */
    @GetMapping("/function/{funcId}")
    public ResponseEntity<List<PointDTO>> getPointsByFunctionId(@PathVariable Long funcId, Authentication auth) {
        logger.info("GET /api/v1/points/function/{} - получение точек функции", funcId);
        try {
            Optional<Function> functionOpt = functionRepository.findById(funcId);
            if (functionOpt.isPresent()) {
                if (!AuthUtil.isAdmin(auth)) {
                    Function f = functionOpt.get();
                    if (f.getUser() == null || !auth.getName().equals(f.getUser().getLogin())) {
                        logger.warn("FORBIDDEN points by functionId={} login={}", funcId, auth == null ? null : auth.getName());
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    }
                }
                List<Point> points = pointRepository.findByFunction(functionOpt.get());
                List<PointDTO> dtos = points.stream()
                        .map(PointMapper::toDTO)
                        .collect(Collectors.toList());
                logger.info("Найдено точек для функции {}: {}", funcId, dtos.size());
                return ResponseEntity.ok(dtos);
            } else {
                logger.warn("Функция с ID {} не найдена", funcId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Ошибка при получении точек функции: {}", funcId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Создать точку
     */
    @PostMapping
    public ResponseEntity<PointDTO> createPoint(@RequestBody PointDTO pointDTO, Authentication auth) {
        logger.info("POST /api/v1/points - создание точки для функции {}", pointDTO.getFuncId());
        try {
            Optional<Function> functionOpt = functionRepository.findById(pointDTO.getFuncId());
            if (functionOpt.isPresent()) {
                if (!AuthUtil.isAdmin(auth)) {
                    Function f = functionOpt.get();
                    if (f.getUser() == null || !auth.getName().equals(f.getUser().getLogin())) {
                        logger.warn("FORBIDDEN point create funcId={} login={}", pointDTO.getFuncId(), auth == null ? null : auth.getName());
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    }
                }
                Point point = PointMapper.toEntity(pointDTO, functionOpt.get());
                Point savedPoint = pointRepository.save(point);
                PointDTO savedDTO = PointMapper.toDTO(savedPoint);
                logger.info("Точка успешно создана с ID: {}", savedDTO.getId());
                return ResponseEntity.status(HttpStatus.CREATED).body(savedDTO);
            } else {
                logger.warn("Функция с ID {} не найдена для создания точки", pointDTO.getFuncId());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        } catch (Exception e) {
            logger.error("Ошибка при создании точки", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Обновить точку
     */
    @PutMapping("/{id}")
    public ResponseEntity<PointDTO> updatePoint(@PathVariable Long id, @RequestBody PointDTO pointDTO, Authentication auth) {
        logger.info("PUT /api/v1/points/{} - обновление точки", id);
        try {
            Optional<Point> pointOpt = pointRepository.findById(id);
            if (pointOpt.isPresent()) {
                if (!AuthUtil.isAdmin(auth)) {
                    Point p = pointOpt.get();
                    Long funcId = p.getFunction() != null ? p.getFunction().getId() : null;
                    if (funcId == null) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    var fOpt = functionRepository.findById(funcId);
                    if (fOpt.isEmpty() || fOpt.get().getUser() == null ||
                            !auth.getName().equals(fOpt.get().getUser().getLogin())) {
                        logger.warn("FORBIDDEN point update id={} login={}", id, auth == null ? null : auth.getName());
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    }
                }
                Point point = pointOpt.get();
                point.setXValue(pointDTO.getXValue());
                point.setYValue(pointDTO.getYValue());
                Point updatedPoint = pointRepository.save(point);
                PointDTO updatedDTO = PointMapper.toDTO(updatedPoint);
                logger.info("Точка с ID {} успешно обновлена", id);
                return ResponseEntity.ok(updatedDTO);
            } else {
                logger.warn("Точка с ID {} не найдена для обновления", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Ошибка при обновлении точки с ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Получить точку функции по x_value
     * GET /api/v1/points/function/{funcId}/x/{xValue}
     */
    @GetMapping("/function/{funcId}/x/{xValue}")
    public ResponseEntity<Map<String, Object>> getPointByFunctionIdAndXValue(
            @PathVariable Long funcId, @PathVariable Double xValue, Authentication auth) {
        logger.info("GET /api/v1/points/function/{}/x/{} - получение точки по x_value", funcId, xValue);
        try {
            Optional<Function> functionOpt = functionRepository.findById(funcId);
            if (functionOpt.isPresent()) {
                if (!AuthUtil.isAdmin(auth)) {
                    Function f = functionOpt.get();
                    if (f.getUser() == null || !auth.getName().equals(f.getUser().getLogin())) {
                        logger.warn("FORBIDDEN point by x funcId={} login={}", funcId, auth == null ? null : auth.getName());
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    }
                }
                List<Point> points = pointRepository.findByFunction(functionOpt.get());
                Optional<Point> pointOpt = points.stream()
                        .filter(p -> Math.abs(p.getXValue() - xValue) < 1e-10)
                        .findFirst();
                if (pointOpt.isPresent()) {
                    PointDTO dto = PointMapper.toDTO(pointOpt.get());
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("data", dto);
                    return ResponseEntity.ok(response);
                } else {
                    logger.warn("Точка с x_value {} не найдена для функции {}", xValue, funcId);
                    return ResponseEntity.notFound().build();
                }
            } else {
                logger.warn("Функция с ID {} не найдена", funcId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Ошибка при получении точки по x_value: {}", xValue, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Получить точки функции в диапазоне x_value
     * GET /api/v1/points/function/{funcId}/range?xMin={min}&xMax={max}
     */
    @GetMapping("/function/{funcId}/range")
    public ResponseEntity<Map<String, Object>> getPointsByFunctionIdInRange(
            @PathVariable Long funcId,
            @RequestParam Double xMin,
            @RequestParam Double xMax,
            Authentication auth) {
        logger.info("GET /api/v1/points/function/{}/range - получение точек в диапазоне xMin={}, xMax={}", funcId, xMin, xMax);
        try {
            Optional<Function> functionOpt = functionRepository.findById(funcId);
            if (functionOpt.isPresent()) {
                if (!AuthUtil.isAdmin(auth)) {
                    Function f = functionOpt.get();
                    if (f.getUser() == null || !auth.getName().equals(f.getUser().getLogin())) {
                        logger.warn("FORBIDDEN points range funcId={} login={}", funcId, auth == null ? null : auth.getName());
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    }
                }
                List<Point> points = pointRepository.findByFunction(functionOpt.get());
                List<PointDTO> dtos = points.stream()
                        .filter(p -> p.getXValue() >= xMin && p.getXValue() <= xMax)
                        .map(PointMapper::toDTO)
                        .collect(Collectors.toList());
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("data", dtos);
                logger.info("Найдено точек в диапазоне: {}", dtos.size());
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Функция с ID {} не найдена", funcId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Ошибка при получении точек в диапазоне", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Получить точки функции с определенным y_value
     * GET /api/v1/points/function/{funcId}/y/{yValue}
     */
    @GetMapping("/function/{funcId}/y/{yValue}")
    public ResponseEntity<Map<String, Object>> getPointsByFunctionIdAndYValue(
            @PathVariable Long funcId, @PathVariable Double yValue, Authentication auth) {
        logger.info("GET /api/v1/points/function/{}/y/{} - получение точек по y_value", funcId, yValue);
        try {
            Optional<Function> functionOpt = functionRepository.findById(funcId);
            if (functionOpt.isPresent()) {
                if (!AuthUtil.isAdmin(auth)) {
                    Function f = functionOpt.get();
                    if (f.getUser() == null || !auth.getName().equals(f.getUser().getLogin())) {
                        logger.warn("FORBIDDEN points y funcId={} login={}", funcId, auth == null ? null : auth.getName());
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    }
                }
                List<Point> points = pointRepository.findByFunction(functionOpt.get());
                List<PointDTO> dtos = points.stream()
                        .filter(p -> Math.abs(p.getYValue() - yValue) < 1e-10)
                        .map(PointMapper::toDTO)
                        .collect(Collectors.toList());
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("data", dtos);
                logger.info("Найдено точек с y_value {}: {}", yValue, dtos.size());
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Функция с ID {} не найдена", funcId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Ошибка при получении точек по y_value", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Получить точки функции с y_value в диапазоне
     * GET /api/v1/points/function/{funcId}/yRange?yMin={min}&yMax={max}
     */
    @GetMapping("/function/{funcId}/yRange")
    public ResponseEntity<Map<String, Object>> getPointsByFunctionIdInYRange(
            @PathVariable Long funcId,
            @RequestParam Double yMin,
            @RequestParam Double yMax,
            Authentication auth) {
        logger.info("GET /api/v1/points/function/{}/yRange - получение точек в диапазоне yMin={}, yMax={}", funcId, yMin, yMax);
        try {
            Optional<Function> functionOpt = functionRepository.findById(funcId);
            if (functionOpt.isPresent()) {
                if (!AuthUtil.isAdmin(auth)) {
                    Function f = functionOpt.get();
                    if (f.getUser() == null || !auth.getName().equals(f.getUser().getLogin())) {
                        logger.warn("FORBIDDEN points yRange funcId={} login={}", funcId, auth == null ? null : auth.getName());
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    }
                }
                List<Point> points = pointRepository.findByFunction(functionOpt.get());
                List<PointDTO> dtos = points.stream()
                        .filter(p -> p.getYValue() >= yMin && p.getYValue() <= yMax)
                        .map(PointMapper::toDTO)
                        .collect(Collectors.toList());
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("data", dtos);
                logger.info("Найдено точек в диапазоне y: {}", dtos.size());
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Функция с ID {} не найдена", funcId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Ошибка при получении точек в диапазоне y", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Подсчитать количество точек функции
     * GET /api/v1/points/function/{funcId}/count
     */
    @GetMapping("/function/{funcId}/count")
    public ResponseEntity<Map<String, Object>> getPointCountByFunctionId(@PathVariable Long funcId, Authentication auth) {
        logger.info("GET /api/v1/points/function/{}/count - подсчет точек функции", funcId);
        try {
            Optional<Function> functionOpt = functionRepository.findById(funcId);
            if (functionOpt.isPresent()) {
                if (!AuthUtil.isAdmin(auth)) {
                    Function f = functionOpt.get();
                    if (f.getUser() == null || auth == null || !auth.getName().equals(f.getUser().getLogin())) {
                        logger.warn("FORBIDDEN points count funcId={} login={}", funcId, auth == null ? null : auth.getName());
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    }
                }
                List<Point> points = pointRepository.findByFunction(functionOpt.get());
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                Map<String, Object> data = new HashMap<>();
                data.put("point_count", points.size());
                response.put("data", data);
                logger.info("Количество точек функции {}: {}", funcId, points.size());
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Функция с ID {} не найдена", funcId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Ошибка при подсчете точек функции: {}", funcId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Получить минимальное и максимальное x_value функции
     * GET /api/v1/points/function/{funcId}/bounds
     */
    @GetMapping("/function/{funcId}/bounds")
    public ResponseEntity<Map<String, Object>> getBoundsByFunctionId(@PathVariable Long funcId, Authentication auth) {
        logger.info("GET /api/v1/points/function/{}/bounds - получение границ функции", funcId);
        try {
            Optional<Function> functionOpt = functionRepository.findById(funcId);
            if (functionOpt.isPresent()) {
                if (!AuthUtil.isAdmin(auth)) {
                    Function f = functionOpt.get();
                    if (f.getUser() == null || auth == null || !auth.getName().equals(f.getUser().getLogin())) {
                        logger.warn("FORBIDDEN points bounds funcId={} login={}", funcId, auth == null ? null : auth.getName());
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    }
                }
                List<Point> points = pointRepository.findByFunction(functionOpt.get());
                if (points.isEmpty()) {
                    logger.warn("Нет точек для функции {}", funcId);
                    return ResponseEntity.notFound().build();
                }
                Double minX = points.stream()
                        .map(Point::getXValue)
                        .min(Double::compareTo)
                        .orElse(0.0);
                Double maxX = points.stream()
                        .map(Point::getXValue)
                        .max(Double::compareTo)
                        .orElse(0.0);
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                Map<String, Object> data = new HashMap<>();
                data.put("min_x", minX);
                data.put("max_x", maxX);
                response.put("data", data);
                logger.info("Границы функции {}: min_x={}, max_x={}", funcId, minX, maxX);
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Функция с ID {} не найдена", funcId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Ошибка при получении границ функции: {}", funcId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Создать несколько точек
     * POST /api/v1/points/batch
     */
    @PostMapping("/batch")
    public ResponseEntity<Map<String, Object>> createPointsBatch(@RequestBody Map<String, Object> request, Authentication auth) {
        logger.info("POST /api/v1/points/batch - создание нескольких точек");
        try {
            Long funcId = Long.valueOf(request.get("funcId").toString());
            Optional<Function> functionOpt = functionRepository.findById(funcId);
            if (!functionOpt.isPresent()) {
                logger.warn("Функция с ID {} не найдена", funcId);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            if (!AuthUtil.isAdmin(auth)) {
                Function f = functionOpt.get();
                if (f.getUser() == null || auth == null || !auth.getName().equals(f.getUser().getLogin())) {
                    logger.warn("FORBIDDEN points batch funcId={} login={}", funcId, auth == null ? null : auth.getName());
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            }
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> pointsData = (List<Map<String, Object>>) request.get("points");
            List<PointDTO> createdPoints = new ArrayList<>();
            
            for (Map<String, Object> pointData : pointsData) {
                PointDTO pointDTO = new PointDTO();
                pointDTO.setFuncId(funcId);
                pointDTO.setXValue(Double.valueOf(pointData.get("xValue").toString()));
                pointDTO.setYValue(Double.valueOf(pointData.get("yValue").toString()));
                
                Point point = PointMapper.toEntity(pointDTO, functionOpt.get());
                Point savedPoint = pointRepository.save(point);
                createdPoints.add(PointMapper.toDTO(savedPoint));
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            Map<String, Object> data = new HashMap<>();
            data.put("created", createdPoints.size());
            data.put("points", createdPoints);
            response.put("data", data);
            response.put("message", "Создано точек: " + createdPoints.size());
            logger.info("Создано точек: {}", createdPoints.size());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error("Ошибка при создании точек batch", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Обновить y_value точки
     * PATCH /api/v1/points/{id}/y
     */
    @PatchMapping("/{id}/y")
    public ResponseEntity<Map<String, Object>> updatePointY(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request,
            Authentication auth) {
        logger.info("PATCH /api/v1/points/{}/y - обновление y_value", id);
        try {
            Optional<Point> pointOpt = pointRepository.findById(id);
            if (pointOpt.isPresent()) {
                if (!AuthUtil.isAdmin(auth)) {
                    Point p = pointOpt.get();
                    Long funcId = p.getFunction() != null ? p.getFunction().getId() : null;
                    if (funcId == null) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    var fOpt = functionRepository.findById(funcId);
                    if (fOpt.isEmpty() || fOpt.get().getUser() == null || auth == null ||
                            !auth.getName().equals(fOpt.get().getUser().getLogin())) {
                        logger.warn("FORBIDDEN point y update id={} login={}", id, auth == null ? null : auth.getName());
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    }
                }
                Point point = pointOpt.get();
                Double newY = Double.valueOf(request.get("yValue").toString());
                point.setYValue(newY);
                Point updatedPoint = pointRepository.save(point);
                PointDTO dto = PointMapper.toDTO(updatedPoint);
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("data", dto);
                response.put("message", "y_value успешно обновлен");
                logger.info("y_value точки с ID {} успешно обновлен", id);
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Точка с ID {} не найдена", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Ошибка при обновлении y_value точки с ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Обновить y_value точки функции по x_value
     * PATCH /api/v1/points/function/{funcId}/x/{xValue}/y
     */
    @PatchMapping("/function/{funcId}/x/{xValue}/y")
    public ResponseEntity<Map<String, Object>> updatePointYByXValue(
            @PathVariable Long funcId,
            @PathVariable Double xValue,
            @RequestBody Map<String, Object> request,
            Authentication auth) {
        logger.info("PATCH /api/v1/points/function/{}/x/{}/y - обновление y_value по x_value", funcId, xValue);
        try {
            Optional<Function> functionOpt = functionRepository.findById(funcId);
            if (functionOpt.isPresent()) {
                if (!AuthUtil.isAdmin(auth)) {
                    Function f = functionOpt.get();
                    if (f.getUser() == null || auth == null || !auth.getName().equals(f.getUser().getLogin())) {
                        logger.warn("FORBIDDEN point y by x funcId={} login={}", funcId, auth == null ? null : auth.getName());
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    }
                }
                List<Point> points = pointRepository.findByFunction(functionOpt.get());
                Optional<Point> pointOpt = points.stream()
                        .filter(p -> Math.abs(p.getXValue() - xValue) < 1e-10)
                        .findFirst();
                if (pointOpt.isPresent()) {
                    Point point = pointOpt.get();
                    Double newY = Double.valueOf(request.get("yValue").toString());
                    point.setYValue(newY);
                    Point updatedPoint = pointRepository.save(point);
                    PointDTO dto = PointMapper.toDTO(updatedPoint);
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("data", dto);
                    response.put("message", "y_value успешно обновлен");
                    logger.info("y_value точки функции {} с x_value {} успешно обновлен", funcId, xValue);
                    return ResponseEntity.ok(response);
                } else {
                    logger.warn("Точка с x_value {} не найдена для функции {}", xValue, funcId);
                    return ResponseEntity.notFound().build();
                }
            } else {
                logger.warn("Функция с ID {} не найдена", funcId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Ошибка при обновлении y_value по x_value", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Умножить y_value всех точек функции на коэффициент
     * PATCH /api/v1/points/function/{funcId}/multiply?coefficient={coeff}
     */
    @PatchMapping("/function/{funcId}/multiply")
    public ResponseEntity<Map<String, Object>> multiplyPointsByCoefficient(
            @PathVariable Long funcId,
            @RequestParam Double coefficient,
            Authentication auth) {
        logger.info("PATCH /api/v1/points/function/{}/multiply?coefficient={} - умножение y_value", funcId, coefficient);
        try {
            Optional<Function> functionOpt = functionRepository.findById(funcId);
            if (functionOpt.isPresent()) {
                if (!AuthUtil.isAdmin(auth)) {
                    Function f = functionOpt.get();
                    if (f.getUser() == null || auth == null || !auth.getName().equals(f.getUser().getLogin())) {
                        logger.warn("FORBIDDEN points multiply funcId={} login={}", funcId, auth == null ? null : auth.getName());
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    }
                }
                List<Point> points = pointRepository.findByFunction(functionOpt.get());
                int updated = 0;
                for (Point point : points) {
                    point.setYValue(point.getYValue() * coefficient);
                    pointRepository.save(point);
                    updated++;
                }
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                Map<String, Object> data = new HashMap<>();
                data.put("updated", updated);
                response.put("data", data);
                response.put("message", "Обновлено точек: " + updated);
                logger.info("Обновлено точек функции {}: {}", funcId, updated);
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Функция с ID {} не найдена", funcId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Ошибка при умножении y_value точек функции: {}", funcId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Удалить точку по ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deletePoint(@PathVariable Long id, Authentication auth) {
        logger.info("DELETE /api/v1/points/{} - удаление точки", id);
        try {
            Optional<Point> pointOpt = pointRepository.findById(id);
            if (pointOpt.isPresent()) {
                if (!AuthUtil.isAdmin(auth)) {
                    Point p = pointOpt.get();
                    Long funcId = p.getFunction() != null ? p.getFunction().getId() : null;
                    if (funcId == null) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    var fOpt = functionRepository.findById(funcId);
                    if (fOpt.isEmpty() || fOpt.get().getUser() == null || auth == null ||
                            !auth.getName().equals(fOpt.get().getUser().getLogin())) {
                        logger.warn("FORBIDDEN point delete id={} login={}", id, auth == null ? null : auth.getName());
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    }
                }
                pointRepository.deleteById(id);
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Точка успешно удалена");
                logger.info("Точка с ID {} успешно удалена", id);
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Точка с ID {} не найдена для удаления", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Ошибка при удалении точки с ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Удалить точку функции по x_value
     * DELETE /api/v1/points/function/{funcId}/x/{xValue}
     */
    @DeleteMapping("/function/{funcId}/x/{xValue}")
    public ResponseEntity<Map<String, Object>> deletePointByFunctionIdAndXValue(
            @PathVariable Long funcId, @PathVariable Double xValue, Authentication auth) {
        logger.info("DELETE /api/v1/points/function/{}/x/{} - удаление точки по x_value", funcId, xValue);
        try {
            Optional<Function> functionOpt = functionRepository.findById(funcId);
            if (functionOpt.isPresent()) {
                if (!AuthUtil.isAdmin(auth)) {
                    Function f = functionOpt.get();
                    if (f.getUser() == null || auth == null || !auth.getName().equals(f.getUser().getLogin())) {
                        logger.warn("FORBIDDEN point delete by x funcId={} login={}", funcId, auth == null ? null : auth.getName());
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    }
                }
                List<Point> points = pointRepository.findByFunction(functionOpt.get());
                Optional<Point> pointOpt = points.stream()
                        .filter(p -> Math.abs(p.getXValue() - xValue) < 1e-10)
                        .findFirst();
                if (pointOpt.isPresent()) {
                    pointRepository.deleteById(pointOpt.get().getId());
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("message", "Точка успешно удалена");
                    logger.info("Точка с x_value {} успешно удалена", xValue);
                    return ResponseEntity.ok(response);
                } else {
                    logger.warn("Точка с x_value {} не найдена", xValue);
                    return ResponseEntity.notFound().build();
                }
            } else {
                logger.warn("Функция с ID {} не найдена", funcId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Ошибка при удалении точки по x_value", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Удалить все точки функции
     * DELETE /api/v1/points/function/{funcId}
     */
    @DeleteMapping("/function/{funcId}")
    public ResponseEntity<Map<String, Object>> deletePointsByFunctionId(@PathVariable Long funcId, Authentication auth) {
        logger.info("DELETE /api/v1/points/function/{} - удаление всех точек функции", funcId);
        try {
            Optional<Function> functionOpt = functionRepository.findById(funcId);
            if (functionOpt.isPresent()) {
                if (!AuthUtil.isAdmin(auth)) {
                    Function f = functionOpt.get();
                    if (f.getUser() == null || auth == null || !auth.getName().equals(f.getUser().getLogin())) {
                        logger.warn("FORBIDDEN points delete funcId={} login={}", funcId, auth == null ? null : auth.getName());
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    }
                }
                List<Point> points = pointRepository.findByFunction(functionOpt.get());
                int deleted = 0;
                for (Point point : points) {
                    pointRepository.deleteById(point.getId());
                    deleted++;
                }
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Удалено точек: " + deleted);
                logger.info("Удалено точек функции {}: {}", funcId, deleted);
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Функция с ID {} не найдена", funcId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Ошибка при удалении точек функции: {}", funcId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Удалить точки функции в диапазоне x_value
     * DELETE /api/v1/points/function/{funcId}/range?xMin={min}&xMax={max}
     */
    @DeleteMapping("/function/{funcId}/range")
    public ResponseEntity<Map<String, Object>> deletePointsByFunctionIdInRange(
            @PathVariable Long funcId,
            @RequestParam Double xMin,
            @RequestParam Double xMax,
            Authentication auth) {
        logger.info("DELETE /api/v1/points/function/{}/range - удаление точек в диапазоне xMin={}, xMax={}", funcId, xMin, xMax);
        try {
            Optional<Function> functionOpt = functionRepository.findById(funcId);
            if (functionOpt.isPresent()) {
                if (!AuthUtil.isAdmin(auth)) {
                    Function f = functionOpt.get();
                    if (f.getUser() == null || auth == null || !auth.getName().equals(f.getUser().getLogin())) {
                        logger.warn("FORBIDDEN points delete range funcId={} login={}", funcId, auth == null ? null : auth.getName());
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    }
                }
                List<Point> points = pointRepository.findByFunction(functionOpt.get());
                List<Point> toDelete = points.stream()
                        .filter(p -> p.getXValue() >= xMin && p.getXValue() <= xMax)
                        .collect(Collectors.toList());
                int deleted = 0;
                for (Point point : toDelete) {
                    pointRepository.deleteById(point.getId());
                    deleted++;
                }
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Удалено точек: " + deleted);
                logger.info("Удалено точек в диапазоне: {}", deleted);
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Функция с ID {} не найдена", funcId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Ошибка при удалении точек в диапазоне", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

