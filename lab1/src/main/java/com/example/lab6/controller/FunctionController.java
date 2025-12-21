package com.example.lab6.controller;

import com.example.lab6.dto.FunctionDTO;
import com.example.lab6.mapper.FunctionMapper;
import entity.Function;
import entity.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import repository.FunctionRepository;
import repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Контроллер для работы с FunctionDTO
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
     */
    @GetMapping
    public ResponseEntity<List<FunctionDTO>> getAllFunctions() {
        logger.info("GET /api/v1/functions - получение всех функций");
        try {
            List<Function> functions = functionRepository.findAll();
            List<FunctionDTO> dtos = functions.stream()
                    .map(FunctionMapper::toDTO)
                    .collect(Collectors.toList());
            logger.info("Найдено функций: {}", dtos.size());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            logger.error("Ошибка при получении всех функций", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Получить функцию по ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<FunctionDTO> getFunctionById(@PathVariable Long id) {
        logger.info("GET /api/v1/functions/{} - получение функции по ID", id);
        try {
            Optional<Function> functionOpt = functionRepository.findById(id);
            if (functionOpt.isPresent()) {
                FunctionDTO dto = FunctionMapper.toDTO(functionOpt.get());
                return ResponseEntity.ok(dto);
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
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<FunctionDTO>> getFunctionsByUserId(@PathVariable Long userId) {
        logger.info("GET /api/v1/functions/user/{} - получение функций пользователя", userId);
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                List<Function> functions = functionRepository.findByUser(userOpt.get());
                List<FunctionDTO> dtos = functions.stream()
                        .map(FunctionMapper::toDTO)
                        .collect(Collectors.toList());
                logger.info("Найдено функций для пользователя {}: {}", userId, dtos.size());
                return ResponseEntity.ok(dtos);
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
     * Создать функцию
     */
    @PostMapping
    public ResponseEntity<FunctionDTO> createFunction(@RequestBody FunctionDTO functionDTO) {
        logger.info("POST /api/v1/functions - создание функции: {}", functionDTO.getName());
        try {
            Optional<User> userOpt = userRepository.findById(functionDTO.getUserId());
            if (userOpt.isPresent()) {
                Function function = FunctionMapper.toEntity(functionDTO, userOpt.get());
                Function savedFunction = functionRepository.save(function);
                FunctionDTO savedDTO = FunctionMapper.toDTO(savedFunction);
                logger.info("Функция успешно создана с ID: {}", savedDTO.getId());
                return ResponseEntity.status(HttpStatus.CREATED).body(savedDTO);
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
    public ResponseEntity<FunctionDTO> updateFunction(@PathVariable Long id, @RequestBody FunctionDTO functionDTO) {
        logger.info("PUT /api/v1/functions/{} - обновление функции", id);
        try {
            Optional<Function> functionOpt = functionRepository.findById(id);
            if (functionOpt.isPresent()) {
                Function function = functionOpt.get();
                function.setName(functionDTO.getName());
                function.setType(functionDTO.getType());
                Function updatedFunction = functionRepository.save(function);
                FunctionDTO updatedDTO = FunctionMapper.toDTO(updatedFunction);
                logger.info("Функция с ID {} успешно обновлена", id);
                return ResponseEntity.ok(updatedDTO);
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
     * Удалить функцию
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFunction(@PathVariable Long id) {
        logger.info("DELETE /api/v1/functions/{} - удаление функции", id);
        try {
            Optional<Function> functionOpt = functionRepository.findById(id);
            if (functionOpt.isPresent()) {
                functionRepository.deleteById(id);
                logger.info("Функция с ID {} успешно удалена", id);
                return ResponseEntity.noContent().build();
            } else {
                logger.warn("Функция с ID {} не найдена для удаления", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Ошибка при удалении функции с ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

