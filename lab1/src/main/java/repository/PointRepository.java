package repository;

import entity.Function;
import entity.Point;
import java.util.List;
import java.util.Optional;

/**
 * Интерфейс репозитория для работы с Point Entity
 */
public interface PointRepository {
    /**
     * Сохраняет точку
     */
    Point save(Point point);
    
    /**
     * Находит точку по ID
     */
    Optional<Point> findById(Long id);
    
    /**
     * Находит все точки
     */
    List<Point> findAll();
    
    /**
     * Находит точки функции
     */
    List<Point> findByFunction(Function function);
    
    /**
     * Удаляет точку
     */
    void delete(Point point);
    
    /**
     * Удаляет точку по ID
     */
    void deleteById(Long id);
}

