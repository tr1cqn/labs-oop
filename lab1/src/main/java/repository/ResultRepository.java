package repository;

import entity.Function;
import entity.Result;
import java.util.List;
import java.util.Optional;

/**
 * Интерфейс репозитория для работы с Result Entity
 */
public interface ResultRepository {
    /**
     * Сохраняет результат
     */
    Result save(Result result);
    
    /**
     * Находит результат по ID
     */
    Optional<Result> findById(Long id);
    
    /**
     * Находит все результаты
     */
    List<Result> findAll();
    
    /**
     * Находит результаты функции
     */
    List<Result> findByFunction(Function function);
    
    /**
     * Удаляет результат
     */
    void delete(Result result);
    
    /**
     * Удаляет результат по ID
     */
    void deleteById(Long id);
}

