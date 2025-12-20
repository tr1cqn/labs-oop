package repository;

import entity.Function;
import entity.User;
import java.util.List;
import java.util.Optional;

/**
 * Интерфейс репозитория для работы с Function Entity
 */
public interface FunctionRepository {
    /**
     * Сохраняет функцию
     */
    Function save(Function function);
    
    /**
     * Находит функцию по ID
     */
    Optional<Function> findById(Long id);
    
    /**
     * Находит все функции
     */
    List<Function> findAll();
    
    /**
     * Находит функции пользователя
     */
    List<Function> findByUser(User user);
    
    /**
     * Удаляет функцию
     */
    void delete(Function function);
    
    /**
     * Удаляет функцию по ID
     */
    void deleteById(Long id);
}

