package com.example.lab6.controller;

import com.example.lab6.dto.FunctionDTO;
import com.example.lab6.mapper.FunctionMapper;
import entity.Function;
import entity.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.example.lab6.security.AuthUtil;
import repository.FunctionRepository;
import repository.UserRepository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Контроллер для работы с FunctionDTO
 * Реализует все операции из API контракта для работы с функциями
 */
@RestController
@RequestMapping("/api/v1/functions")
public class FunctionController {
    private static final Logger logger = LogManager.getLogger(FunctionController.class);
    private final FunctionRepository functionRepository;
    private final UserRepository userRepository;

    public FunctionController(FunctionRepository functionRepository, UserRepository userRepository) {
        this.functionRepository = functionRepository;
        this.userRepository = userRepository;
    }

    /**
     * Получить все функции
     * GET /api/v1/functions?sortBy={field}&order={asc|desc}&limit={n}&offset={n}
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllFunctions(
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String order,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Integer offset,
            Authentication auth) {
        logger.info("GET /api/v1/functions - получение всех функций (sortBy={}, order={}, limit={}, offset={})", 
                sortBy, order, limit, offset);
        try {
            List<Function> functions;
            if (AuthUtil.isAdmin(auth)) {
                functions = functionRepository.findAll();
            } else {
                var meOpt = AuthUtil.currentUser(auth, userRepository);
                if (meOpt.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
                }
                functions = functionRepository.findByUser(meOpt.get());
            }
            List<FunctionDTO> dtos = functions.stream()
                    .map(FunctionMapper::toDTO)
                    .collect(Collectors.toList());
            
            // Сортировка
            if (sortBy != null) {
                dtos = sortFunctions(dtos, sortBy, order);
                logger.debug("Функции отсортированы по полю: {}, направление: {}", sortBy, order);
            }
            
            // Пагинация
            int total = dtos.size();
            if (offset != null && offset > 0) {
                dtos = dtos.subList(Math.min(offset, dtos.size()), dtos.size());
            }
            if (limit != null && limit > 0) {
                dtos = dtos.subList(0, Math.min(limit, dtos.size()));
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", dtos);
            response.put("total", total);
            
            logger.info("Найдено функций: {} (показано: {})", total, dtos.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Ошибка при получении всех функций", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Получить функцию по ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getFunctionById(@PathVariable Long id, Authentication auth) {
        logger.info("GET /api/v1/functions/{} - получение функции по ID", id);
        try {
            Optional<Function> functionOpt = functionRepository.findById(id);
            if (functionOpt.isPresent()) {
                if (!AuthUtil.isAdmin(auth)) {
                    var meOpt = AuthUtil.currentUser(auth, userRepository);
                    if (meOpt.isEmpty() || functionOpt.get().getUser() == null ||
                            !meOpt.get().getId().equals(functionOpt.get().getUser().getId())) {
                        logger.warn("FORBIDDEN function read id={} login={}", id, auth == null ? null : auth.getName());
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    }
                }
                FunctionDTO dto = FunctionMapper.toDTO(functionOpt.get());
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("data", dto);
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Функция с ID {} не найдена", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Ошибка при получении функции по ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Получить функции пользователя
     * GET /api/v1/functions/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getFunctionsByUserId(@PathVariable Long userId, Authentication auth) {
        logger.info("GET /api/v1/functions/user/{} - получение функций пользователя", userId);
        try {
            if (!AuthUtil.isAdmin(auth)) {
                var meOpt = AuthUtil.currentUser(auth, userRepository);
                if (meOpt.isEmpty() || !userId.equals(meOpt.get().getId())) {
                    logger.warn("FORBIDDEN functions by userId={} login={}", userId, auth == null ? null : auth.getName());
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            }
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                List<Function> functions = functionRepository.findByUser(userOpt.get());
                List<FunctionDTO> dtos = functions.stream()
                        .map(FunctionMapper::toDTO)
                        .collect(Collectors.toList());
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("data", dtos);
                logger.info("Найдено функций для пользователя {}: {}", userId, dtos.size());
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Пользователь с ID {} не найден", userId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Ошибка при получении функций пользователя: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Подсчитать количество функций пользователя
     * GET /api/v1/functions/user/{userId}/count
     */
    @GetMapping("/user/{userId}/count")
    public ResponseEntity<Map<String, Object>> getFunctionCountByUserId(@PathVariable Long userId, Authentication auth) {
        logger.info("GET /api/v1/functions/user/{}/count - подсчет функций пользователя", userId);
        try {
            if (!AuthUtil.isAdmin(auth)) {
                var meOpt = AuthUtil.currentUser(auth, userRepository);
                if (meOpt.isEmpty() || !userId.equals(meOpt.get().getId())) {
                    logger.warn("FORBIDDEN function count userId={} login={}", userId, auth == null ? null : auth.getName());
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            }
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                List<Function> functions = functionRepository.findByUser(userOpt.get());
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                Map<String, Object> data = new HashMap<>();
                data.put("function_count", functions.size());
                response.put("data", data);
                logger.info("Количество функций пользователя {}: {}", userId, functions.size());
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Пользователь с ID {} не найден", userId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Ошибка при подсчете функций пользователя: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Поиск функций
     * GET /api/v1/functions/search?type={type}&name={name}&nameLike={pattern}&userId={userId}
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchFunctions(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String nameLike,
            @RequestParam(required = false) Long userId,
            Authentication auth) {
        logger.info("GET /api/v1/functions/search - поиск функций (type={}, name={}, nameLike={}, userId={})", 
                type, name, nameLike, userId);
        try {
            Long enforcedUserId = userId;
            if (!AuthUtil.isAdmin(auth)) {
                var meOpt = AuthUtil.currentUser(auth, userRepository);
                if (meOpt.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
                enforcedUserId = meOpt.get().getId();
            }
            List<Function> allFunctions = functionRepository.findAll();
            List<FunctionDTO> results = allFunctions.stream()
                    .filter(f -> {
                        if (type != null && !type.equals(f.getType())) return false;
                        if (name != null && !name.equals(f.getName())) return false;
                        if (nameLike != null && (f.getName() == null || !f.getName().toLowerCase().contains(nameLike.toLowerCase()))) return false;
                        if (enforcedUserId != null && (f.getUser() == null || !enforcedUserId.equals(f.getUser().getId()))) return false;
                        return true;
                    })
                    .map(FunctionMapper::toDTO)
                    .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", results);
            logger.info("Найдено функций по критериям: {}", results.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Ошибка при поиске функций", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Создать функцию
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createFunction(@RequestBody FunctionDTO functionDTO, Authentication auth) {
        logger.info("POST /api/v1/functions - создание функции: {}", functionDTO.getName());
        try {
            if (!AuthUtil.isAdmin(auth)) {
                var meOpt = AuthUtil.currentUser(auth, userRepository);
                if (meOpt.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
                functionDTO.setUserId(meOpt.get().getId());
            }
            Optional<User> userOpt = userRepository.findById(functionDTO.getUserId());
            if (userOpt.isPresent()) {
                Function function = FunctionMapper.toEntity(functionDTO, userOpt.get());
                Function savedFunction = functionRepository.save(function);
                FunctionDTO savedDTO = FunctionMapper.toDTO(savedFunction);
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("data", savedDTO);
                response.put("message", "Функция успешно создана");
                logger.info("Функция успешно создана с ID: {}", savedDTO.getId());
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else {
                logger.warn("Пользователь с ID {} не найден для создания функции", functionDTO.getUserId());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        } catch (Exception e) {
            logger.error("Ошибка при создании функции", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Обновить функцию
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateFunction(@PathVariable Long id, @RequestBody FunctionDTO functionDTO, Authentication auth) {
        logger.info("PUT /api/v1/functions/{} - обновление функции", id);
        try {
            Optional<Function> functionOpt = functionRepository.findById(id);
            if (functionOpt.isPresent()) {
                if (!AuthUtil.isAdmin(auth)) {
                    var meOpt = AuthUtil.currentUser(auth, userRepository);
                    if (meOpt.isEmpty() || functionOpt.get().getUser() == null ||
                            !meOpt.get().getId().equals(functionOpt.get().getUser().getId())) {
                        logger.warn("FORBIDDEN function update id={} login={}", id, auth == null ? null : auth.getName());
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    }
                }
                Function function = functionOpt.get();
                function.setName(functionDTO.getName());
                function.setType(functionDTO.getType());
                Function updatedFunction = functionRepository.save(function);
                FunctionDTO updatedDTO = FunctionMapper.toDTO(updatedFunction);
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("data", updatedDTO);
                response.put("message", "Функция успешно обновлена");
                logger.info("Функция с ID {} успешно обновлена", id);
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Функция с ID {} не найдена для обновления", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Ошибка при обновлении функции с ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Обновить имя функции
     * PATCH /api/v1/functions/{id}/name
     */
    @PatchMapping("/{id}/name")
    public ResponseEntity<Map<String, Object>> updateFunctionName(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            Authentication auth) {
        logger.info("PATCH /api/v1/functions/{}/name - обновление имени функции", id);
        try {
            Optional<Function> functionOpt = functionRepository.findById(id);
            if (functionOpt.isPresent()) {
                if (!AuthUtil.isAdmin(auth)) {
                    var meOpt = AuthUtil.currentUser(auth, userRepository);
                    if (meOpt.isEmpty() || functionOpt.get().getUser() == null ||
                            !meOpt.get().getId().equals(functionOpt.get().getUser().getId())) {
                        logger.warn("FORBIDDEN function name patch id={} login={}", id, auth == null ? null : auth.getName());
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    }
                }
                Function function = functionOpt.get();
                String newName = request.get("name");
                if (newName == null || newName.isEmpty()) {
                    logger.warn("Имя не указано в запросе");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                }
                function.setName(newName);
                Function updatedFunction = functionRepository.save(function);
                FunctionDTO dto = FunctionMapper.toDTO(updatedFunction);
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("data", dto);
                response.put("message", "Имя функции успешно обновлено");
                logger.info("Имя функции с ID {} успешно обновлено", id);
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Функция с ID {} не найдена для обновления имени", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Ошибка при обновлении имени функции с ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Обновить тип функции
     * PATCH /api/v1/functions/{id}/type
     */
    @PatchMapping("/{id}/type")
    public ResponseEntity<Map<String, Object>> updateFunctionType(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            Authentication auth) {
        logger.info("PATCH /api/v1/functions/{}/type - обновление типа функции", id);
        try {
            Optional<Function> functionOpt = functionRepository.findById(id);
            if (functionOpt.isPresent()) {
                if (!AuthUtil.isAdmin(auth)) {
                    var meOpt = AuthUtil.currentUser(auth, userRepository);
                    if (meOpt.isEmpty() || functionOpt.get().getUser() == null ||
                            !meOpt.get().getId().equals(functionOpt.get().getUser().getId())) {
                        logger.warn("FORBIDDEN function type patch id={} login={}", id, auth == null ? null : auth.getName());
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    }
                }
                Function function = functionOpt.get();
                String newType = request.get("type");
                if (newType == null || newType.isEmpty()) {
                    logger.warn("Тип не указан в запросе");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                }
                function.setType(newType);
                Function updatedFunction = functionRepository.save(function);
                FunctionDTO dto = FunctionMapper.toDTO(updatedFunction);
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("data", dto);
                response.put("message", "Тип функции успешно обновлен");
                logger.info("Тип функции с ID {} успешно обновлен", id);
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Функция с ID {} не найдена для обновления типа", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Ошибка при обновлении типа функции с ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Обновить тип всех функций пользователя
     * PATCH /api/v1/functions/user/{userId}/type
     */
    @PatchMapping("/user/{userId}/type")
    public ResponseEntity<Map<String, Object>> updateAllFunctionsTypeByUserId(
            @PathVariable Long userId,
            @RequestBody Map<String, String> request,
            Authentication auth) {
        logger.info("PATCH /api/v1/functions/user/{}/type - обновление типа всех функций пользователя", userId);
        try {
            if (!AuthUtil.isAdmin(auth)) {
                var meOpt = AuthUtil.currentUser(auth, userRepository);
                if (meOpt.isEmpty() || !userId.equals(meOpt.get().getId())) {
                    logger.warn("FORBIDDEN functions bulk type userId={} login={}", userId, auth == null ? null : auth.getName());
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            }
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                String newType = request.get("type");
                if (newType == null || newType.isEmpty()) {
                    logger.warn("Тип не указан в запросе");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                }
                List<Function> functions = functionRepository.findByUser(userOpt.get());
                int updated = 0;
                for (Function function : functions) {
                    function.setType(newType);
                    functionRepository.save(function);
                    updated++;
                }
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                Map<String, Object> data = new HashMap<>();
                data.put("updated", updated);
                response.put("data", data);
                response.put("message", "Обновлено функций: " + updated);
                logger.info("Тип всех функций пользователя {} успешно обновлен. Обновлено: {}", userId, updated);
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Пользователь с ID {} не найден", userId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Ошибка при обновлении типа всех функций пользователя: {}", userId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Удалить функцию по ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteFunction(@PathVariable Long id, Authentication auth) {
        logger.info("DELETE /api/v1/functions/{} - удаление функции", id);
        try {
            Optional<Function> functionOpt = functionRepository.findById(id);
            if (functionOpt.isPresent()) {
                if (!AuthUtil.isAdmin(auth)) {
                    var meOpt = AuthUtil.currentUser(auth, userRepository);
                    if (meOpt.isEmpty() || functionOpt.get().getUser() == null ||
                            !meOpt.get().getId().equals(functionOpt.get().getUser().getId())) {
                        logger.warn("FORBIDDEN function delete id={} login={}", id, auth == null ? null : auth.getName());
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    }
                }
                functionRepository.deleteById(id);
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Функция успешно удалена");
                logger.info("Функция с ID {} успешно удалена", id);
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Функция с ID {} не найдена для удаления", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Ошибка при удалении функции с ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Удалить все функции пользователя
     * DELETE /api/v1/functions/user/{userId}
     */
    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> deleteFunctionsByUserId(@PathVariable Long userId, Authentication auth) {
        logger.info("DELETE /api/v1/functions/user/{} - удаление всех функций пользователя", userId);
        try {
            if (!AuthUtil.isAdmin(auth)) {
                var meOpt = AuthUtil.currentUser(auth, userRepository);
                if (meOpt.isEmpty() || !userId.equals(meOpt.get().getId())) {
                    logger.warn("FORBIDDEN functions delete by userId={} login={}", userId, auth == null ? null : auth.getName());
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            }
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                List<Function> functions = functionRepository.findByUser(userOpt.get());
                int deleted = 0;
                for (Function function : functions) {
                    functionRepository.deleteById(function.getId());
                    deleted++;
                }
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Удалено функций: " + deleted);
                logger.info("Удалено функций пользователя {}: {}", userId, deleted);
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Пользователь с ID {} не найден", userId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Ошибка при удалении функций пользователя: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Удалить функции определенного типа
     * DELETE /api/v1/functions?type={type}
     */
    @DeleteMapping
    public ResponseEntity<Map<String, Object>> deleteFunctionsByType(@RequestParam String type, Authentication auth) {
        logger.info("DELETE /api/v1/functions?type={} - удаление функций по типу", type);
        try {
            List<Function> allFunctions = functionRepository.findAll();
            Long enforcedUserId = null;
            if (!AuthUtil.isAdmin(auth)) {
                var meOpt = AuthUtil.currentUser(auth, userRepository);
                if (meOpt.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
                enforcedUserId = meOpt.get().getId();
            }
            List<Function> toDelete = allFunctions.stream()
                    .filter(f -> type.equals(f.getType()))
                    .filter(f -> enforcedUserId == null || (f.getUser() != null && enforcedUserId.equals(f.getUser().getId())))
                    .collect(Collectors.toList());
            
            int deleted = 0;
            for (Function function : toDelete) {
                functionRepository.deleteById(function.getId());
                deleted++;
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Удалено функций: " + deleted);
            logger.info("Удалено функций типа {}: {}", type, deleted);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Ошибка при удалении функций по типу: {}", type, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Вспомогательный метод для сортировки функций
     */
    private List<FunctionDTO> sortFunctions(List<FunctionDTO> functions, String sortBy, String order) {
        Comparator<FunctionDTO> comparator = null;
        
        switch (sortBy.toLowerCase()) {
            case "id":
                comparator = Comparator.comparing(FunctionDTO::getId, Comparator.nullsLast(Comparator.naturalOrder()));
                break;
            case "name":
                comparator = Comparator.comparing(FunctionDTO::getName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
                break;
            case "type":
                comparator = Comparator.comparing(FunctionDTO::getType, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
                break;
            case "userid":
                comparator = Comparator.comparing(FunctionDTO::getUserId, Comparator.nullsLast(Comparator.naturalOrder()));
                break;
            default:
                logger.warn("Неизвестное поле для сортировки: {}", sortBy);
                return functions;
        }
        
        if (comparator != null) {
            if ("desc".equalsIgnoreCase(order)) {
                comparator = comparator.reversed();
            }
            functions.sort(comparator);
        }
        
        return functions;
    }
}
