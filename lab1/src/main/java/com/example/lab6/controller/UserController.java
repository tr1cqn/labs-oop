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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Контроллер для работы с UserDTO
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
     */
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        logger.info("GET /api/v1/users - получение всех пользователей");
        try {
            List<User> users = userRepository.findAll();
            List<UserDTO> dtos = users.stream()
                    .map(UserMapper::toDTOSafe)
                    .collect(Collectors.toList());
            logger.info("Найдено пользователей: {}", dtos.size());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            logger.error("Ошибка при получении всех пользователей", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Получить пользователя по ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        logger.info("GET /api/v1/users/{} - получение пользователя по ID", id);
        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isPresent()) {
                UserDTO dto = UserMapper.toDTOSafe(userOpt.get());
                return ResponseEntity.ok(dto);
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
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO userDTO) {
        logger.info("POST /api/v1/users - создание пользователя: {}", userDTO.getLogin());
        try {
            User user = UserMapper.toEntity(userDTO);
            User savedUser = userRepository.save(user);
            UserDTO savedDTO = UserMapper.toDTOSafe(savedUser);
            logger.info("Пользователь успешно создан с ID: {}", savedDTO.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedDTO);
        } catch (Exception e) {
            logger.error("Ошибка при создании пользователя", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Обновить пользователя
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
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
                logger.info("Пользователь с ID {} успешно обновлен", id);
                return ResponseEntity.ok(updatedDTO);
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
     * Удалить пользователя
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        logger.info("DELETE /api/v1/users/{} - удаление пользователя", id);
        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isPresent()) {
                userRepository.deleteById(id);
                logger.info("Пользователь с ID {} успешно удален", id);
                return ResponseEntity.noContent().build();
            } else {
                logger.warn("Пользователь с ID {} не найден для удаления", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Ошибка при удалении пользователя с ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

