package repository;

import entity.Function;
import entity.Point;
import entity.User;
import org.junit.jupiter.api.*;
import repository.impl.FunctionRepositoryImpl;
import repository.impl.PointRepositoryImpl;
import repository.impl.UserRepositoryImpl;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для PointRepository с генерацией данных, поиском и удалением
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PointRepositoryTest {
    private static PointRepository pointRepository;
    private static FunctionRepository functionRepository;
    private static UserRepository userRepository;
    private static Random random;
    private static Function testFunction;
    
    @BeforeAll
    static void setUp() {
        pointRepository = new PointRepositoryImpl();
        functionRepository = new FunctionRepositoryImpl();
        userRepository = new UserRepositoryImpl();
        random = new Random();
        
        User user = new User("test_user_point_" + random.nextInt(100000), "hash", "test@test.com");
        user = userRepository.save(user);
        testFunction = new Function(user, "test_function", "linear");
        testFunction = functionRepository.save(testFunction);
    }
    
    @Test
    @Order(1)
    @DisplayName("Тест поиска точки по ID")
    void testFindById() {
        Point point = new Point(testFunction, random.nextDouble() * 100, random.nextDouble() * 100);
        Point saved = pointRepository.save(point);
        
        Optional<Point> found = pointRepository.findById(saved.getId());
        
        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
        assertEquals(saved.getXValue(), found.get().getXValue(), 0.0001);
    }
    
    @Test
    @Order(2)
    @DisplayName("Тест поиска всех точек")
    void testFindAll() {
        for (int i = 0; i < 5; i++) {
            Point point = new Point(testFunction, i * 1.0, i * 2.0);
            pointRepository.save(point);
        }
        
        List<Point> points = pointRepository.findAll();
        
        assertNotNull(points);
        assertFalse(points.isEmpty());
    }
    
    @Test
    @Order(3)
    @DisplayName("Тест поиска точек функции")
    void testFindByFunction() {
        Function function = new Function(testFunction.getUser(), "test_func_points", "linear");
        function = functionRepository.save(function);
        
        for (int i = 0; i < 3; i++) {
            Point point = new Point(function, i * 1.0, i * 2.0);
            pointRepository.save(point);
        }
        
        List<Point> points = pointRepository.findByFunction(function);
        
        assertNotNull(points);
        assertEquals(3, points.size());
    }
    
    @Test
    @Order(4)
    @DisplayName("Тест удаления точки")
    void testDelete() {
        Point point = new Point(testFunction, random.nextDouble() * 100, random.nextDouble() * 100);
        Point saved = pointRepository.save(point);
        
        pointRepository.delete(saved);
        
        Optional<Point> deleted = pointRepository.findById(saved.getId());
        assertFalse(deleted.isPresent());
    }
    
    @Test
    @Order(5)
    @DisplayName("Тест удаления точки по ID")
    void testDeleteById() {
        Point point = new Point(testFunction, random.nextDouble() * 100, random.nextDouble() * 100);
        Point saved = pointRepository.save(point);
        
        pointRepository.deleteById(saved.getId());
        
        Optional<Point> deleted = pointRepository.findById(saved.getId());
        assertFalse(deleted.isPresent());
    }
    
    @Test
    @Order(6)
    @DisplayName("Тест с разнообразными значениями координат")
    void testVariousCoordinates() {
        // Отрицательные значения
        Point point1 = new Point(testFunction, -10.5, -20.3);
        Point saved1 = pointRepository.save(point1);
        assertTrue(pointRepository.findById(saved1.getId()).isPresent());
        
        // Нулевые значения
        Point point2 = new Point(testFunction, 0.0, 0.0);
        Point saved2 = pointRepository.save(point2);
        assertTrue(pointRepository.findById(saved2.getId()).isPresent());
        
        // Большие значения
        Point point3 = new Point(testFunction, 1000.0, 2000.0);
        Point saved3 = pointRepository.save(point3);
        assertTrue(pointRepository.findById(saved3.getId()).isPresent());
        
        // Малые значения
        Point point4 = new Point(testFunction, 0.0001, 0.0002);
        Point saved4 = pointRepository.save(point4);
        assertTrue(pointRepository.findById(saved4.getId()).isPresent());
        
        // Дробные значения
        Point point5 = new Point(testFunction, 3.14159, 2.71828);
        Point saved5 = pointRepository.save(point5);
        assertTrue(pointRepository.findById(saved5.getId()).isPresent());
    }
}

