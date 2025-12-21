package com.example.lab6.controller;

import com.example.lab6.dto.PointDTO;
import com.example.lab6.mapper.PointMapper;
import entity.Function;
import entity.Point;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import repository.FunctionRepository;
import repository.PointRepository;

import java.util.List;
import java.util.Optional;
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
    public ResponseEntity<List<PointDTO>> getAllPoints() {
        logger.info("GET /api/v1/points - получение всех точек");
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
    public ResponseEntity<PointDTO> getPointById(@PathVariable Long id) {
        logger.info("GET /api/v1/points/{} - получение точки по ID", id);
        try {
            Optional<Point> pointOpt = pointRepository.findById(id);
            if (pointOpt.isPresent()) {
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
    public ResponseEntity<List<PointDTO>> getPointsByFunctionId(@PathVariable Long funcId) {
        logger.info("GET /api/v1/points/function/{} - получение точек функции", funcId);
        try {
            Optional<Function> functionOpt = functionRepository.findById(funcId);
            if (functionOpt.isPresent()) {
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
    public ResponseEntity<PointDTO> createPoint(@RequestBody PointDTO pointDTO) {
        logger.info("POST /api/v1/points - создание точки для функции {}", pointDTO.getFuncId());
        try {
            Optional<Function> functionOpt = functionRepository.findById(pointDTO.getFuncId());
            if (functionOpt.isPresent()) {
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
    public ResponseEntity<PointDTO> updatePoint(@PathVariable Long id, @RequestBody PointDTO pointDTO) {
        logger.info("PUT /api/v1/points/{} - обновление точки", id);
        try {
            Optional<Point> pointOpt = pointRepository.findById(id);
            if (pointOpt.isPresent()) {
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
     * Удалить точку
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePoint(@PathVariable Long id) {
        logger.info("DELETE /api/v1/points/{} - удаление точки", id);
        try {
            Optional<Point> pointOpt = pointRepository.findById(id);
            if (pointOpt.isPresent()) {
                pointRepository.deleteById(id);
                logger.info("Точка с ID {} успешно удалена", id);
                return ResponseEntity.noContent().build();
            } else {
                logger.warn("Точка с ID {} не найдена для удаления", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Ошибка при удалении точки с ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

