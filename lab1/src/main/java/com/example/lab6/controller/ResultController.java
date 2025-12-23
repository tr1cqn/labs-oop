package com.example.lab6.controller;

import com.example.lab6.dto.ResultDTO;
import com.example.lab6.mapper.ResultMapper;
import entity.Function;
import entity.Result;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.example.lab6.security.AuthUtil;
import repository.FunctionRepository;
import repository.ResultRepository;

import java.util.*;
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
    public ResponseEntity<List<ResultDTO>> getAllResults(Authentication auth) {
        logger.info("GET /api/v1/results - получение всех результатов");
        if (!AuthUtil.isAdmin(auth)) {
            logger.warn("FORBIDDEN results list login={}", auth == null ? null : auth.getName());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
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
    public ResponseEntity<ResultDTO> getResultById(@PathVariable Long id, Authentication auth) {
        logger.info("GET /api/v1/results/{} - получение результата по ID", id);
        try {
            Optional<Result> resultOpt = resultRepository.findById(id);
            if (resultOpt.isPresent()) {
                if (!AuthUtil.isAdmin(auth)) {
                    Result r = resultOpt.get();
                    Long funcId = r.getFunction() != null ? r.getFunction().getId() : null;
                    if (funcId == null) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    var fOpt = functionRepository.findById(funcId);
                    if (fOpt.isEmpty() || fOpt.get().getUser() == null || auth == null ||
                            !auth.getName().equals(fOpt.get().getUser().getLogin())) {
                        logger.warn("FORBIDDEN result read id={} login={}", id, auth == null ? null : auth.getName());
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    }
                }
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
    public ResponseEntity<List<ResultDTO>> getResultsByFunctionId(@PathVariable Long funcId, Authentication auth) {
        logger.info("GET /api/v1/results/function/{} - получение результатов функции", funcId);
        try {
            Optional<Function> functionOpt = functionRepository.findById(funcId);
            if (functionOpt.isPresent()) {
                if (!AuthUtil.isAdmin(auth)) {
                    Function f = functionOpt.get();
                    if (f.getUser() == null || auth == null || !auth.getName().equals(f.getUser().getLogin())) {
                        logger.warn("FORBIDDEN results by funcId={} login={}", funcId, auth == null ? null : auth.getName());
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    }
                }
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
    public ResponseEntity<ResultDTO> createResult(@RequestBody ResultDTO resultDTO, Authentication auth) {
        logger.info("POST /api/v1/results - создание результата для функции {}", resultDTO.getResultId());
        try {
            Optional<Function> functionOpt = functionRepository.findById(resultDTO.getResultId());
            if (functionOpt.isPresent()) {
                if (!AuthUtil.isAdmin(auth)) {
                    Function f = functionOpt.get();
                    if (f.getUser() == null || auth == null || !auth.getName().equals(f.getUser().getLogin())) {
                        logger.warn("FORBIDDEN result create funcId={} login={}", resultDTO.getResultId(), auth == null ? null : auth.getName());
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    }
                }
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
    public ResponseEntity<ResultDTO> updateResult(@PathVariable Long id, @RequestBody ResultDTO resultDTO, Authentication auth) {
        logger.info("PUT /api/v1/results/{} - обновление результата", id);
        try {
            Optional<Result> resultOpt = resultRepository.findById(id);
            if (resultOpt.isPresent()) {
                if (!AuthUtil.isAdmin(auth)) {
                    Result r = resultOpt.get();
                    Long funcId = r.getFunction() != null ? r.getFunction().getId() : null;
                    if (funcId == null) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    var fOpt = functionRepository.findById(funcId);
                    if (fOpt.isEmpty() || fOpt.get().getUser() == null || auth == null ||
                            !auth.getName().equals(fOpt.get().getUser().getLogin())) {
                        logger.warn("FORBIDDEN result update id={} login={}", id, auth == null ? null : auth.getName());
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    }
                }
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
     * Получить последний результат функции
     * GET /api/v1/results/function/{resultId}/latest
     */
    @GetMapping("/function/{resultId}/latest")
    public ResponseEntity<Map<String, Object>> getLatestResultByFunctionId(@PathVariable Long resultId, Authentication auth) {
        logger.info("GET /api/v1/results/function/{}/latest - получение последнего результата функции", resultId);
        try {
            Optional<Function> functionOpt = functionRepository.findById(resultId);
            if (functionOpt.isPresent()) {
                if (!AuthUtil.isAdmin(auth)) {
                    Function f = functionOpt.get();
                    if (f.getUser() == null || auth == null || !auth.getName().equals(f.getUser().getLogin())) {
                        logger.warn("FORBIDDEN latest result funcId={} login={}", resultId, auth == null ? null : auth.getName());
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    }
                }
                List<Result> results = resultRepository.findByFunction(functionOpt.get());
                if (results.isEmpty()) {
                    logger.warn("Нет результатов для функции {}", resultId);
                    return ResponseEntity.notFound().build();
                }
                // Последний результат - с максимальным ID
                Result latest = results.stream()
                        .max(Comparator.comparing(Result::getId))
                        .orElse(null);
                if (latest != null) {
                    ResultDTO dto = ResultMapper.toDTO(latest);
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("data", dto);
                    return ResponseEntity.ok(response);
                }
                return ResponseEntity.notFound().build();
            } else {
                logger.warn("Функция с ID {} не найдена", resultId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Ошибка при получении последнего результата функции: {}", resultId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Подсчитать количество результатов функции
     * GET /api/v1/results/function/{resultId}/count
     */
    @GetMapping("/function/{resultId}/count")
    public ResponseEntity<Map<String, Object>> getResultCountByFunctionId(@PathVariable Long resultId, Authentication auth) {
        logger.info("GET /api/v1/results/function/{}/count - подсчет результатов функции", resultId);
        try {
            Optional<Function> functionOpt = functionRepository.findById(resultId);
            if (functionOpt.isPresent()) {
                if (!AuthUtil.isAdmin(auth)) {
                    Function f = functionOpt.get();
                    if (f.getUser() == null || auth == null || !auth.getName().equals(f.getUser().getLogin())) {
                        logger.warn("FORBIDDEN results count funcId={} login={}", resultId, auth == null ? null : auth.getName());
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    }
                }
                List<Result> results = resultRepository.findByFunction(functionOpt.get());
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                Map<String, Object> data = new HashMap<>();
                data.put("result_count", results.size());
                response.put("data", data);
                logger.info("Количество результатов функции {}: {}", resultId, results.size());
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Функция с ID {} не найдена", resultId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Ошибка при подсчете результатов функции: {}", resultId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Поиск результатов по частичному совпадению текста
     * GET /api/v1/results/search?resultLike={pattern}
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchResults(
            @RequestParam(required = false) String resultLike,
            Authentication auth) {
        logger.info("GET /api/v1/results/search - поиск результатов (resultLike={})", resultLike);
        if (!AuthUtil.isAdmin(auth)) {
            logger.warn("FORBIDDEN results search login={}", auth == null ? null : auth.getName());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            List<Result> allResults = resultRepository.findAll();
            List<ResultDTO> results = allResults.stream()
                    .filter(r -> {
                        if (resultLike == null) return true;
                        return r.getResult() != null && 
                               r.getResult().toLowerCase().contains(resultLike.toLowerCase());
                    })
                    .map(ResultMapper::toDTO)
                    .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", results);
            logger.info("Найдено результатов по критериям: {}", results.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Ошибка при поиске результатов", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Поиск результатов функции по частичному совпадению текста
     * GET /api/v1/results/function/{resultId}/search?resultLike={pattern}
     */
    @GetMapping("/function/{resultId}/search")
    public ResponseEntity<Map<String, Object>> searchResultsByFunctionId(
            @PathVariable Long resultId,
            @RequestParam String resultLike,
            Authentication auth) {
        logger.info("GET /api/v1/results/function/{}/search - поиск результатов функции (resultLike={})", resultId, resultLike);
        try {
            Optional<Function> functionOpt = functionRepository.findById(resultId);
            if (functionOpt.isPresent()) {
                if (!AuthUtil.isAdmin(auth)) {
                    Function f = functionOpt.get();
                    if (f.getUser() == null || auth == null || !auth.getName().equals(f.getUser().getLogin())) {
                        logger.warn("FORBIDDEN results search by funcId={} login={}", resultId, auth == null ? null : auth.getName());
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    }
                }
                List<Result> results = resultRepository.findByFunction(functionOpt.get());
                List<ResultDTO> dtos = results.stream()
                        .filter(r -> r.getResult() != null && 
                               r.getResult().toLowerCase().contains(resultLike.toLowerCase()))
                        .map(ResultMapper::toDTO)
                        .collect(Collectors.toList());
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("data", dtos);
                logger.info("Найдено результатов функции {} по критериям: {}", resultId, dtos.size());
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Функция с ID {} не найдена", resultId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Ошибка при поиске результатов функции: {}", resultId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Создать несколько результатов
     * POST /api/v1/results/batch
     */
    @PostMapping("/batch")
    public ResponseEntity<Map<String, Object>> createResultsBatch(@RequestBody Map<String, Object> request, Authentication auth) {
        logger.info("POST /api/v1/results/batch - создание нескольких результатов");
        try {
            Long resultId = Long.valueOf(request.get("resultId").toString());
            Optional<Function> functionOpt = functionRepository.findById(resultId);
            if (!functionOpt.isPresent()) {
                logger.warn("Функция с ID {} не найдена", resultId);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            if (!AuthUtil.isAdmin(auth)) {
                Function f = functionOpt.get();
                if (f.getUser() == null || auth == null || !auth.getName().equals(f.getUser().getLogin())) {
                    logger.warn("FORBIDDEN results batch funcId={} login={}", resultId, auth == null ? null : auth.getName());
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            }
            
            @SuppressWarnings("unchecked")
            List<String> resultsData = (List<String>) request.get("results");
            List<ResultDTO> createdResults = new ArrayList<>();
            
            for (String resultText : resultsData) {
                ResultDTO resultDTO = new ResultDTO();
                resultDTO.setResultId(resultId);
                resultDTO.setResult(resultText);
                
                Result result = ResultMapper.toEntity(resultDTO, functionOpt.get());
                Result savedResult = resultRepository.save(result);
                createdResults.add(ResultMapper.toDTO(savedResult));
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            Map<String, Object> data = new HashMap<>();
            data.put("created", createdResults.size());
            data.put("results", createdResults);
            response.put("data", data);
            response.put("message", "Создано результатов: " + createdResults.size());
            logger.info("Создано результатов: {}", createdResults.size());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error("Ошибка при создании результатов batch", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Обновить все результаты функции
     * PATCH /api/v1/results/function/{resultId}
     */
    @PatchMapping("/function/{resultId}")
    public ResponseEntity<Map<String, Object>> updateAllResultsByFunctionId(
            @PathVariable Long resultId,
            @RequestBody Map<String, String> request,
            Authentication auth) {
        logger.info("PATCH /api/v1/results/function/{} - обновление всех результатов функции", resultId);
        try {
            Optional<Function> functionOpt = functionRepository.findById(resultId);
            if (functionOpt.isPresent()) {
                if (!AuthUtil.isAdmin(auth)) {
                    Function f = functionOpt.get();
                    if (f.getUser() == null || auth == null || !auth.getName().equals(f.getUser().getLogin())) {
                        logger.warn("FORBIDDEN results bulk update funcId={} login={}", resultId, auth == null ? null : auth.getName());
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    }
                }
                String newResult = request.get("result");
                if (newResult == null || newResult.isEmpty()) {
                    logger.warn("Результат не указан в запросе");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                }
                List<Result> results = resultRepository.findByFunction(functionOpt.get());
                int updated = 0;
                for (Result result : results) {
                    result.setResult(newResult);
                    resultRepository.save(result);
                    updated++;
                }
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                Map<String, Object> data = new HashMap<>();
                data.put("updated", updated);
                response.put("data", data);
                response.put("message", "Обновлено результатов: " + updated);
                logger.info("Обновлено результатов функции {}: {}", resultId, updated);
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Функция с ID {} не найдена", resultId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Ошибка при обновлении всех результатов функции: {}", resultId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Обновить последний результат функции
     * PATCH /api/v1/results/function/{resultId}/latest
     */
    @PatchMapping("/function/{resultId}/latest")
    public ResponseEntity<Map<String, Object>> updateLatestResultByFunctionId(
            @PathVariable Long resultId,
            @RequestBody Map<String, String> request,
            Authentication auth) {
        logger.info("PATCH /api/v1/results/function/{}/latest - обновление последнего результата функции", resultId);
        try {
            Optional<Function> functionOpt = functionRepository.findById(resultId);
            if (functionOpt.isPresent()) {
                if (!AuthUtil.isAdmin(auth)) {
                    Function f = functionOpt.get();
                    if (f.getUser() == null || auth == null || !auth.getName().equals(f.getUser().getLogin())) {
                        logger.warn("FORBIDDEN latest result update funcId={} login={}", resultId, auth == null ? null : auth.getName());
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    }
                }
                List<Result> results = resultRepository.findByFunction(functionOpt.get());
                if (results.isEmpty()) {
                    logger.warn("Нет результатов для функции {}", resultId);
                    return ResponseEntity.notFound().build();
                }
                Result latest = results.stream()
                        .max(Comparator.comparing(Result::getId))
                        .orElse(null);
                if (latest != null) {
                    String newResult = request.get("result");
                    if (newResult == null || newResult.isEmpty()) {
                        logger.warn("Результат не указан в запросе");
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                    }
                    latest.setResult(newResult);
                    Result updatedResult = resultRepository.save(latest);
                    ResultDTO dto = ResultMapper.toDTO(updatedResult);
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("data", dto);
                    response.put("message", "Последний результат успешно обновлен");
                    logger.info("Последний результат функции {} успешно обновлен", resultId);
                    return ResponseEntity.ok(response);
                }
                return ResponseEntity.notFound().build();
            } else {
                logger.warn("Функция с ID {} не найдена", resultId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Ошибка при обновлении последнего результата функции: {}", resultId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Удалить результат по ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteResult(@PathVariable Long id, Authentication auth) {
        logger.info("DELETE /api/v1/results/{} - удаление результата", id);
        try {
            Optional<Result> resultOpt = resultRepository.findById(id);
            if (resultOpt.isPresent()) {
                if (!AuthUtil.isAdmin(auth)) {
                    Result r = resultOpt.get();
                    Long funcId = r.getFunction() != null ? r.getFunction().getId() : null;
                    if (funcId == null) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    var fOpt = functionRepository.findById(funcId);
                    if (fOpt.isEmpty() || fOpt.get().getUser() == null || auth == null ||
                            !auth.getName().equals(fOpt.get().getUser().getLogin())) {
                        logger.warn("FORBIDDEN result delete id={} login={}", id, auth == null ? null : auth.getName());
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    }
                }
                resultRepository.deleteById(id);
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Результат успешно удален");
                logger.info("Результат с ID {} успешно удален", id);
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Результат с ID {} не найден для удаления", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Ошибка при удалении результата с ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Удалить все результаты функции
     * DELETE /api/v1/results/function/{resultId}
     */
    @DeleteMapping("/function/{resultId}")
    public ResponseEntity<Map<String, Object>> deleteResultsByFunctionId(@PathVariable Long resultId, Authentication auth) {
        logger.info("DELETE /api/v1/results/function/{} - удаление всех результатов функции", resultId);
        try {
            Optional<Function> functionOpt = functionRepository.findById(resultId);
            if (functionOpt.isPresent()) {
                if (!AuthUtil.isAdmin(auth)) {
                    Function f = functionOpt.get();
                    if (f.getUser() == null || auth == null || !auth.getName().equals(f.getUser().getLogin())) {
                        logger.warn("FORBIDDEN results delete funcId={} login={}", resultId, auth == null ? null : auth.getName());
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    }
                }
                List<Result> results = resultRepository.findByFunction(functionOpt.get());
                int deleted = 0;
                for (Result result : results) {
                    resultRepository.deleteById(result.getId());
                    deleted++;
                }
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Удалено результатов: " + deleted);
                logger.info("Удалено результатов функции {}: {}", resultId, deleted);
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Функция с ID {} не найдена", resultId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Ошибка при удалении результатов функции: {}", resultId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Удалить последний результат функции
     * DELETE /api/v1/results/function/{resultId}/latest
     */
    @DeleteMapping("/function/{resultId}/latest")
    public ResponseEntity<Map<String, Object>> deleteLatestResultByFunctionId(@PathVariable Long resultId, Authentication auth) {
        logger.info("DELETE /api/v1/results/function/{}/latest - удаление последнего результата функции", resultId);
        try {
            Optional<Function> functionOpt = functionRepository.findById(resultId);
            if (functionOpt.isPresent()) {
                if (!AuthUtil.isAdmin(auth)) {
                    Function f = functionOpt.get();
                    if (f.getUser() == null || auth == null || !auth.getName().equals(f.getUser().getLogin())) {
                        logger.warn("FORBIDDEN latest result delete funcId={} login={}", resultId, auth == null ? null : auth.getName());
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    }
                }
                List<Result> results = resultRepository.findByFunction(functionOpt.get());
                if (results.isEmpty()) {
                    logger.warn("Нет результатов для функции {}", resultId);
                    return ResponseEntity.notFound().build();
                }
                Result latest = results.stream()
                        .max(Comparator.comparing(Result::getId))
                        .orElse(null);
                if (latest != null) {
                    resultRepository.deleteById(latest.getId());
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("message", "Последний результат успешно удален");
                    logger.info("Последний результат функции {} успешно удален", resultId);
                    return ResponseEntity.ok(response);
                }
                return ResponseEntity.notFound().build();
            } else {
                logger.warn("Функция с ID {} не найдена", resultId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Ошибка при удалении последнего результата функции: {}", resultId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Удалить результаты функции по частичному совпадению текста
     * DELETE /api/v1/results/function/{resultId}/search?resultLike={pattern}
     */
    @DeleteMapping("/function/{resultId}/search")
    public ResponseEntity<Map<String, Object>> deleteResultsByFunctionIdAndSearch(
            @PathVariable Long resultId,
            @RequestParam String resultLike,
            Authentication auth) {
        logger.info("DELETE /api/v1/results/function/{}/search - удаление результатов по поиску (resultLike={})", resultId, resultLike);
        try {
            Optional<Function> functionOpt = functionRepository.findById(resultId);
            if (functionOpt.isPresent()) {
                if (!AuthUtil.isAdmin(auth)) {
                    Function f = functionOpt.get();
                    if (f.getUser() == null || auth == null || !auth.getName().equals(f.getUser().getLogin())) {
                        logger.warn("FORBIDDEN results delete by search funcId={} login={}", resultId, auth == null ? null : auth.getName());
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    }
                }
                List<Result> results = resultRepository.findByFunction(functionOpt.get());
                List<Result> toDelete = results.stream()
                        .filter(r -> r.getResult() != null && 
                               r.getResult().toLowerCase().contains(resultLike.toLowerCase()))
                        .collect(Collectors.toList());
                int deleted = 0;
                for (Result result : toDelete) {
                    resultRepository.deleteById(result.getId());
                    deleted++;
                }
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Удалено результатов: " + deleted);
                logger.info("Удалено результатов функции {} по поиску: {}", resultId, deleted);
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Функция с ID {} не найдена", resultId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Ошибка при удалении результатов функции по поиску: {}", resultId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

