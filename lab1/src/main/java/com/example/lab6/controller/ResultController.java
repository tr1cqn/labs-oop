package com.example.lab6.controller;

import com.example.lab6.dto.ResultDTO;
import com.example.lab6.mapper.ResultMapper;
import entity.Function;
import entity.Result;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import repository.FunctionRepository;
import repository.ResultRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Контроллер для работы с ResultDTO
 */
@RestController
@RequestMapping("/api/v1/results")
public class ResultController {
    private static final Logger logger = LogManager.getLogger(ResultController.class);
    private final ResultRepository resultRepository;
    private final FunctionRepository functionRepository;

    public ResultController(ResultRepository resultRepository, FunctionRepository functionRepository) {
        this.resultRepository = resultRepository;
        this.functionRepository = functionRepository;
    }

    /**
     * Получить все результаты
     */
    @GetMapping
    public ResponseEntity<List<ResultDTO>> getAllResults() {
        logger.info("GET /api/v1/results - получение всех результатов");
        try {
            List<Result> results = resultRepository.findAll();
            List<ResultDTO> dtos = results.stream()
                    .map(ResultMapper::toDTO)
                    .collect(Collectors.toList());
            logger.info("Найдено результатов: {}", dtos.size());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            logger.error("Ошибка при получении всех результатов", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Получить результат по ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResultDTO> getResultById(@PathVariable Long id) {
        logger.info("GET /api/v1/results/{} - получение результата по ID", id);
        try {
            Optional<Result> resultOpt = resultRepository.findById(id);
            if (resultOpt.isPresent()) {
                ResultDTO dto = ResultMapper.toDTO(resultOpt.get());
                return ResponseEntity.ok(dto);
            } else {
                logger.warn("Результат с ID {} не найден", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Ошибка при получении результата по ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Получить результаты функции
     */
    @GetMapping("/function/{funcId}")
    public ResponseEntity<List<ResultDTO>> getResultsByFunctionId(@PathVariable Long funcId) {
        logger.info("GET /api/v1/results/function/{} - получение результатов функции", funcId);
        try {
            Optional<Function> functionOpt = functionRepository.findById(funcId);
            if (functionOpt.isPresent()) {
                List<Result> results = resultRepository.findByFunction(functionOpt.get());
                List<ResultDTO> dtos = results.stream()
                        .map(ResultMapper::toDTO)
                        .collect(Collectors.toList());
                logger.info("Найдено результатов для функции {}: {}", funcId, dtos.size());
                return ResponseEntity.ok(dtos);
            } else {
                logger.warn("Функция с ID {} не найдена", funcId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Ошибка при получении результатов функции: {}", funcId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Создать результат
     */
    @PostMapping
    public ResponseEntity<ResultDTO> createResult(@RequestBody ResultDTO resultDTO) {
        logger.info("POST /api/v1/results - создание результата для функции {}", resultDTO.getResultId());
        try {
            Optional<Function> functionOpt = functionRepository.findById(resultDTO.getResultId());
            if (functionOpt.isPresent()) {
                Result result = ResultMapper.toEntity(resultDTO, functionOpt.get());
                Result savedResult = resultRepository.save(result);
                ResultDTO savedDTO = ResultMapper.toDTO(savedResult);
                logger.info("Результат успешно создан с ID: {}", savedDTO.getId());
                return ResponseEntity.status(HttpStatus.CREATED).body(savedDTO);
            } else {
                logger.warn("Функция с ID {} не найдена для создания результата", resultDTO.getResultId());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        } catch (Exception e) {
            logger.error("Ошибка при создании результата", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Обновить результат
     */
    @PutMapping("/{id}")
    public ResponseEntity<ResultDTO> updateResult(@PathVariable Long id, @RequestBody ResultDTO resultDTO) {
        logger.info("PUT /api/v1/results/{} - обновление результата", id);
        try {
            Optional<Result> resultOpt = resultRepository.findById(id);
            if (resultOpt.isPresent()) {
                Result result = resultOpt.get();
                result.setResult(resultDTO.getResult());
                Result updatedResult = resultRepository.save(result);
                ResultDTO updatedDTO = ResultMapper.toDTO(updatedResult);
                logger.info("Результат с ID {} успешно обновлен", id);
                return ResponseEntity.ok(updatedDTO);
            } else {
                logger.warn("Результат с ID {} не найден для обновления", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Ошибка при обновлении результата с ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Удалить результат
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResult(@PathVariable Long id) {
        logger.info("DELETE /api/v1/results/{} - удаление результата", id);
        try {
            Optional<Result> resultOpt = resultRepository.findById(id);
            if (resultOpt.isPresent()) {
                resultRepository.deleteById(id);
                logger.info("Результат с ID {} успешно удален", id);
                return ResponseEntity.noContent().build();
            } else {
                logger.warn("Результат с ID {} не найден для удаления", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Ошибка при удалении результата с ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

