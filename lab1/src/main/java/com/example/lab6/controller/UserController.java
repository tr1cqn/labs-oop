package com.example.lab6.controller;

import com.example.lab6.dto.UserDTO;
import com.example.lab6.mapper.UserMapper;
import entity.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import repository.UserRepository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Контроллер для работы с UserDTO
 * Реализует все операции из API контракта для работы с пользователями
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private static final Logger logger = LogManager.getLogger(UserController.class);
    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Получить всех пользователей
     * GET /api/v1/users?sortBy={field}&order={asc|desc}&limit={n}&offset={n}
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllUsers(
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String order,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Integer offset) {
        logger.info("GET /api/v1/users - получение всех пользователей (sortBy={}, order={}, limit={}, offset={})", 
                sortBy, order, limit, offset);
        try {
            List<User> users = userRepository.findAll();
            List<UserDTO> dtos = users.stream()
                    .map(UserMapper::toDTOSafe)
                    .collect(Collectors.toList());
            
            // Сортировка
            if (sortBy != null) {
                dtos = sortUsers(dtos, sortBy, order);
                logger.debug("Пользователи отсортированы по полю: {}, направление: {}", sortBy, order);
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
            
            logger.info("Найдено пользователей: {} (показано: {})", total, dtos.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Ошибка при получении всех пользователей", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Получить пользователя по ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long id) {
        logger.info("GET /api/v1/users/{} - получение пользователя по ID", id);
        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isPresent()) {
                UserDTO dto = UserMapper.toDTOSafe(userOpt.get());
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("data", dto);
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Пользователь с ID {} не найден", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Ошибка при получении пользователя по ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Создать пользователя
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody UserDTO userDTO) {
        logger.info("POST /api/v1/users - создание пользователя: {}", userDTO.getLogin());
        try {
            User user = UserMapper.toEntity(userDTO);
            User savedUser = userRepository.save(user);
            UserDTO savedDTO = UserMapper.toDTOSafe(savedUser);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", savedDTO);
            response.put("message", "Пользователь успешно создан");
            logger.info("Пользователь успешно создан с ID: {}", savedDTO.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error("Ошибка при создании пользователя", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Обновить пользователя
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        logger.info("PUT /api/v1/users/{} - обновление пользователя", id);
        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setLogin(userDTO.getLogin());
                user.setPassword(userDTO.getPassword());
                user.setEmail(userDTO.getEmail());
                User updatedUser = userRepository.save(user);
                UserDTO updatedDTO = UserMapper.toDTOSafe(updatedUser);
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("data", updatedDTO);
                response.put("message", "Пользователь успешно обновлен");
                logger.info("Пользователь с ID {} успешно обновлен", id);
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Пользователь с ID {} не найден для обновления", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Ошибка при обновлении пользователя с ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Поиск пользователя по login (точное совпадение)
     * GET /api/v1/users/search?login={login}
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchUser(
            @RequestParam(required = false) String login,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String loginLike) {
        logger.info("GET /api/v1/users/search - поиск пользователя (login={}, email={}, loginLike={})", 
                login, email, loginLike);
        try {
            Optional<User> userOpt = Optional.empty();
            
            if (login != null) {
                userOpt = userRepository.findByLogin(login);
                logger.debug("Поиск по login: {}", login);
            } else if (email != null) {
                List<User> users = userRepository.findAll();
                userOpt = users.stream()
                        .filter(u -> email.equals(u.getEmail()))
                        .findFirst();
                logger.debug("Поиск по email: {}", email);
            } else if (loginLike != null) {
                List<User> users = userRepository.findAll();
                userOpt = users.stream()
                        .filter(u -> u.getLogin() != null && u.getLogin().toLowerCase().contains(loginLike.toLowerCase()))
                        .findFirst();
                logger.debug("Поиск по loginLike: {}", loginLike);
            }
            
            if (userOpt.isPresent()) {
                UserDTO dto = UserMapper.toDTOSafe(userOpt.get());
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("data", dto);
                logger.info("Пользователь найден: {}", dto.getLogin());
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Пользователь не найден по критериям поиска");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Ошибка при поиске пользователя", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Удалить пользователя по ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id) {
        logger.info("DELETE /api/v1/users/{} - удаление пользователя", id);
        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isPresent()) {
                userRepository.deleteById(id);
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Пользователь успешно удален");
                logger.info("Пользователь с ID {} успешно удален", id);
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Пользователь с ID {} не найден для удаления", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Ошибка при удалении пользователя с ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Удалить пользователя по login
     * DELETE /api/v1/users?login={login}
     */
    @DeleteMapping
    public ResponseEntity<Map<String, Object>> deleteUserByLogin(@RequestParam String login) {
        logger.info("DELETE /api/v1/users?login={} - удаление пользователя по login", login);
        try {
            Optional<User> userOpt = userRepository.findByLogin(login);
            if (userOpt.isPresent()) {
                userRepository.deleteById(userOpt.get().getId());
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Пользователь успешно удален");
                logger.info("Пользователь с login {} успешно удален", login);
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Пользователь с login {} не найден для удаления", login);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Ошибка при удалении пользователя с login: {}", login, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Обновить пароль пользователя
     * PATCH /api/v1/users/{id}/password
     */
    @PatchMapping("/{id}/password")
    public ResponseEntity<Map<String, Object>> updatePassword(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        logger.info("PATCH /api/v1/users/{}/password - обновление пароля", id);
        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                String newPassword = request.get("password");
                if (newPassword == null || newPassword.isEmpty()) {
                    logger.warn("Пароль не указан в запросе");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                }
                user.setPassword(newPassword);
                User updatedUser = userRepository.save(user);
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Пароль успешно обновлен");
                logger.info("Пароль пользователя с ID {} успешно обновлен", id);
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Пользователь с ID {} не найден для обновления пароля", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Ошибка при обновлении пароля пользователя с ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Обновить email пользователя
     * PATCH /api/v1/users/{id}/email
     */
    @PatchMapping("/{id}/email")
    public ResponseEntity<Map<String, Object>> updateEmail(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        logger.info("PATCH /api/v1/users/{}/email - обновление email", id);
        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                String newEmail = request.get("email");
                if (newEmail == null || newEmail.isEmpty()) {
                    logger.warn("Email не указан в запросе");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                }
                user.setEmail(newEmail);
                User updatedUser = userRepository.save(user);
                UserDTO dto = UserMapper.toDTOSafe(updatedUser);
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("data", dto);
                response.put("message", "Email успешно обновлен");
                logger.info("Email пользователя с ID {} успешно обновлен", id);
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Пользователь с ID {} не найден для обновления email", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Ошибка при обновлении email пользователя с ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Обновить login пользователя
     * PATCH /api/v1/users/{id}/login
     */
    @PatchMapping("/{id}/login")
    public ResponseEntity<Map<String, Object>> updateLogin(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        logger.info("PATCH /api/v1/users/{}/login - обновление login", id);
        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                String newLogin = request.get("login");
                if (newLogin == null || newLogin.isEmpty()) {
                    logger.warn("Login не указан в запросе");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                }
                user.setLogin(newLogin);
                User updatedUser = userRepository.save(user);
                UserDTO dto = UserMapper.toDTOSafe(updatedUser);
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("data", dto);
                response.put("message", "Login успешно обновлен");
                logger.info("Login пользователя с ID {} успешно обновлен", id);
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Пользователь с ID {} не найден для обновления login", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Ошибка при обновлении login пользователя с ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Вспомогательный метод для сортировки пользователей
     */
    private List<UserDTO> sortUsers(List<UserDTO> users, String sortBy, String order) {
        Comparator<UserDTO> comparator = null;
        
        switch (sortBy.toLowerCase()) {
            case "id":
                comparator = Comparator.comparing(UserDTO::getId, Comparator.nullsLast(Comparator.naturalOrder()));
                break;
            case "login":
                comparator = Comparator.comparing(UserDTO::getLogin, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
                break;
            case "email":
                comparator = Comparator.comparing(UserDTO::getEmail, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
                break;
            default:
                logger.warn("Неизвестное поле для сортировки: {}", sortBy);
                return users;
        }
        
        if (comparator != null) {
            if ("desc".equalsIgnoreCase(order)) {
                comparator = comparator.reversed();
            }
            users.sort(comparator);
        }
        
        return users;
    }
}

