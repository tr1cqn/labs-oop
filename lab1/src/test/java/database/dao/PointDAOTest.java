package database.dao;

import org.junit.jupiter.api.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для PointDAO с генерацией разнообразных данных
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PointDAOTest {
    private static PointDAO pointDAO;
    private static FunctionDAO functionDAO;
    private static UserDAO userDAO;
    private static Random random;
    private static Long testFunctionId;
    
    @BeforeAll
    static void setUp() throws SQLException {
        pointDAO = new PointDAO();
        functionDAO = new FunctionDAO();
        userDAO = new UserDAO();
        random = new Random();
        
        // Создаем тестового пользователя и функцию
        Long userId = userDAO.insert("test_user_point_" + random.nextInt(100000), "hash");
        testFunctionId = functionDAO.insert(userId, "test_function", "linear");
    }
    
    @Test
    @Order(1)
    @DisplayName("Тест вставки точки")
    void testInsert() throws SQLException {
        double x = random.nextDouble() * 100;
        double y = random.nextDouble() * 100;
        
        Long id = pointDAO.insert(testFunctionId, x, y);
        
        assertNotNull(id);
        assertTrue(id > 0);
    }
    
    @Test
    @Order(2)
    @DisplayName("Тест поиска точки по ID")
    void testFindById() throws SQLException {
        double x = random.nextDouble() * 100;
        double y = random.nextDouble() * 100;
        
        Long id = pointDAO.insert(testFunctionId, x, y);
        Optional<PointDAO.Point> found = pointDAO.findById(id);
        
        assertTrue(found.isPresent());
        assertEquals(id, found.get().getId());
        assertEquals(testFunctionId, found.get().getFunctionId());
        assertEquals(x, found.get().getXValue(), 0.0001);
        assertEquals(y, found.get().getYValue(), 0.0001);
    }
    
    @Test
    @Order(3)
    @DisplayName("Тест поиска точек по function_id")
    void testFindByFunctionId() throws SQLException {
        Long functionId = functionDAO.insert(
            userDAO.insert("user_point_test_" + random.nextInt(100000), "hash"),
            "test_func_points", "linear"
        );
        
        // Создаем несколько точек
        for (int i = 0; i < 5; i++) {
            pointDAO.insert(functionId, i * 1.0, i * 2.0);
        }
        
        List<PointDAO.Point> points = pointDAO.findByFunctionId(functionId);
        
        assertNotNull(points);
        assertEquals(5, points.size());
    }
    
    @Test
    @Order(4)
    @DisplayName("Тест поиска отсортированных точек")
    void testFindByFunctionIdOrdered() throws SQLException {
        Long functionId = functionDAO.insert(
            userDAO.insert("user_ordered_" + random.nextInt(100000), "hash"),
            "ordered_func", "linear"
        );
        
        // Создаем точки в случайном порядке
        pointDAO.insert(functionId, 5.0, 10.0);
        pointDAO.insert(functionId, 1.0, 2.0);
        pointDAO.insert(functionId, 3.0, 6.0);
        
        List<PointDAO.Point> points = pointDAO.findByFunctionIdOrdered(functionId);
        
        assertNotNull(points);
        assertTrue(points.size() >= 3);
        // Проверяем сортировку
        for (int i = 1; i < points.size(); i++) {
            assertTrue(points.get(i).getXValue() >= points.get(i-1).getXValue());
        }
    }
    
    @Test
    @Order(5)
    @DisplayName("Тест поиска точки по x_value")
    void testFindByFunctionIdAndX() throws SQLException {
        double x = 42.5;
        double y = 85.0;
        
        pointDAO.insert(testFunctionId, x, y);
        Optional<PointDAO.Point> found = pointDAO.findByFunctionIdAndX(testFunctionId, x);
        
        assertTrue(found.isPresent());
        assertEquals(x, found.get().getXValue(), 0.0001);
    }
    
    @Test
    @Order(6)
    @DisplayName("Тест поиска точек в диапазоне x_value")
    void testFindByFunctionIdAndXRange() throws SQLException {
        Long functionId = functionDAO.insert(
            userDAO.insert("user_range_" + random.nextInt(100000), "hash"),
            "range_func", "linear"
        );
        
        // Создаем точки
        pointDAO.insert(functionId, 1.0, 2.0);
        pointDAO.insert(functionId, 5.0, 10.0);
        pointDAO.insert(functionId, 10.0, 20.0);
        pointDAO.insert(functionId, 15.0, 30.0);
        
        List<PointDAO.Point> points = pointDAO.findByFunctionIdAndXRange(functionId, 5.0, 12.0);
        
        assertNotNull(points);
        assertTrue(points.size() >= 2);
    }
    
    @Test
    @Order(7)
    @DisplayName("Тест поиска точек по y_value")
    void testFindByFunctionIdAndY() throws SQLException {
        Long functionId = functionDAO.insert(
            userDAO.insert("user_y_test_" + random.nextInt(100000), "hash"),
            "y_func", "linear"
        );
        
        double y = 50.0;
        pointDAO.insert(functionId, 10.0, y);
        pointDAO.insert(functionId, 20.0, y);
        
        List<PointDAO.Point> points = pointDAO.findByFunctionIdAndY(functionId, y);
        
        assertNotNull(points);
        assertTrue(points.size() >= 2);
    }
    
    @Test
    @Order(8)
    @DisplayName("Тест поиска точек в диапазоне y_value")
    void testFindByFunctionIdAndYRange() throws SQLException {
        Long functionId = functionDAO.insert(
            userDAO.insert("user_y_range_" + random.nextInt(100000), "hash"),
            "y_range_func", "linear"
        );
        
        pointDAO.insert(functionId, 1.0, 5.0);
        pointDAO.insert(functionId, 2.0, 10.0);
        pointDAO.insert(functionId, 3.0, 15.0);
        pointDAO.insert(functionId, 4.0, 25.0);
        
        List<PointDAO.Point> points = pointDAO.findByFunctionIdAndYRange(functionId, 8.0, 20.0);
        
        assertNotNull(points);
        assertTrue(points.size() >= 2);
    }
    
    @Test
    @Order(9)
    @DisplayName("Тест подсчета количества точек")
    void testCountByFunctionId() throws SQLException {
        Long functionId = functionDAO.insert(
            userDAO.insert("user_count_" + random.nextInt(100000), "hash"),
            "count_func", "linear"
        );
        
        for (int i = 0; i < 7; i++) {
            pointDAO.insert(functionId, i * 1.0, i * 2.0);
        }
        
        int count = pointDAO.countByFunctionId(functionId);
        assertEquals(7, count);
    }
    
    @Test
    @Order(10)
    @DisplayName("Тест поиска min/max x_value")
    void testFindMinMaxXByFunctionId() throws SQLException {
        Long functionId = functionDAO.insert(
            userDAO.insert("user_minmax_" + random.nextInt(100000), "hash"),
            "minmax_func", "linear"
        );
        
        pointDAO.insert(functionId, 1.0, 2.0);
        pointDAO.insert(functionId, 5.0, 10.0);
        pointDAO.insert(functionId, 10.0, 20.0);
        
        PointDAO.XRange range = pointDAO.findMinMaxXByFunctionId(functionId);
        
        assertNotNull(range);
        assertEquals(1.0, range.getMinX(), 0.0001);
        assertEquals(10.0, range.getMaxX(), 0.0001);
    }
    
    @Test
    @Order(11)
    @DisplayName("Тест вставки нескольких точек")
    void testInsertMultiple() throws SQLException {
        Long functionId = functionDAO.insert(
            userDAO.insert("user_multiple_" + random.nextInt(100000), "hash"),
            "multiple_func", "linear"
        );
        
        pointDAO.insertMultiple(functionId, 1.0, 2.0, 3.0, 6.0, 5.0, 10.0);
        
        List<PointDAO.Point> points = pointDAO.findByFunctionId(functionId);
        assertTrue(points.size() >= 3);
    }
    
    @Test
    @Order(12)
    @DisplayName("Тест обновления y_value по ID")
    void testUpdateYById() throws SQLException {
        double x = random.nextDouble() * 100;
        double y = random.nextDouble() * 100;
        
        Long id = pointDAO.insert(testFunctionId, x, y);
        
        double newY = random.nextDouble() * 100;
        boolean updated = pointDAO.updateYById(id, newY);
        assertTrue(updated);
        
        Optional<PointDAO.Point> updatedPoint = pointDAO.findById(id);
        assertTrue(updatedPoint.isPresent());
        assertEquals(newY, updatedPoint.get().getYValue(), 0.0001);
    }
    
    @Test
    @Order(13)
    @DisplayName("Тест обновления y_value по x_value")
    void testUpdateYByFunctionAndX() throws SQLException {
        double x = 25.0;
        double y = 50.0;
        
        pointDAO.insert(testFunctionId, x, y);
        
        double newY = 75.0;
        boolean updated = pointDAO.updateYByFunctionAndX(testFunctionId, x, newY);
        assertTrue(updated);
    }
    
    @Test
    @Order(14)
    @DisplayName("Тест обновления x_value и y_value")
    void testUpdate() throws SQLException {
        double x = random.nextDouble() * 100;
        double y = random.nextDouble() * 100;
        
        Long id = pointDAO.insert(testFunctionId, x, y);
        
        double newX = random.nextDouble() * 100;
        double newY = random.nextDouble() * 100;
        
        boolean updated = pointDAO.update(id, newX, newY);
        assertTrue(updated);
        
        Optional<PointDAO.Point> updatedPoint = pointDAO.findById(id);
        assertTrue(updatedPoint.isPresent());
        assertEquals(newX, updatedPoint.get().getXValue(), 0.0001);
        assertEquals(newY, updatedPoint.get().getYValue(), 0.0001);
    }
    
    @Test
    @Order(15)
    @DisplayName("Тест умножения y_value всех точек")
    void testUpdateYMultiply() throws SQLException {
        Long functionId = functionDAO.insert(
            userDAO.insert("user_multiply_" + random.nextInt(100000), "hash"),
            "multiply_func", "linear"
        );
        
        pointDAO.insert(functionId, 1.0, 2.0);
        pointDAO.insert(functionId, 2.0, 4.0);
        pointDAO.insert(functionId, 3.0, 6.0);
        
        int updated = pointDAO.updateYMultiply(functionId, 2.0);
        assertTrue(updated >= 3);
    }
    
    @Test
    @Order(16)
    @DisplayName("Тест удаления точки по ID")
    void testDeleteById() throws SQLException {
        double x = random.nextDouble() * 100;
        double y = random.nextDouble() * 100;
        
        Long id = pointDAO.insert(testFunctionId, x, y);
        
        boolean deleted = pointDAO.deleteById(id);
        assertTrue(deleted);
        
        Optional<PointDAO.Point> deletedPoint = pointDAO.findById(id);
        assertFalse(deletedPoint.isPresent());
    }
    
    @Test
    @Order(17)
    @DisplayName("Тест удаления точки по x_value")
    void testDeleteByFunctionAndX() throws SQLException {
        double x = 33.0;
        double y = 66.0;
        
        pointDAO.insert(testFunctionId, x, y);
        
        boolean deleted = pointDAO.deleteByFunctionAndX(testFunctionId, x);
        assertTrue(deleted);
    }
    
    @Test
    @Order(18)
    @DisplayName("Тест удаления всех точек функции")
    void testDeleteByFunctionId() throws SQLException {
        Long functionId = functionDAO.insert(
            userDAO.insert("user_delete_all_" + random.nextInt(100000), "hash"),
            "delete_all_func", "linear"
        );
        
        for (int i = 0; i < 3; i++) {
            pointDAO.insert(functionId, i * 1.0, i * 2.0);
        }
        
        int deleted = pointDAO.deleteByFunctionId(functionId);
        assertEquals(3, deleted);
        
        List<PointDAO.Point> points = pointDAO.findByFunctionId(functionId);
        assertTrue(points.isEmpty());
    }
    
    @Test
    @Order(19)
    @DisplayName("Тест удаления точек в диапазоне x_value")
    void testDeleteByFunctionAndXRange() throws SQLException {
        Long functionId = functionDAO.insert(
            userDAO.insert("user_delete_range_" + random.nextInt(100000), "hash"),
            "delete_range_func", "linear"
        );
        
        pointDAO.insert(functionId, 1.0, 2.0);
        pointDAO.insert(functionId, 5.0, 10.0);
        pointDAO.insert(functionId, 10.0, 20.0);
        pointDAO.insert(functionId, 15.0, 30.0);
        
        int deleted = pointDAO.deleteByFunctionAndXRange(functionId, 5.0, 12.0);
        assertTrue(deleted >= 2);
    }
    
    @Test
    @Order(20)
    @DisplayName("Тест с разнообразными значениями координат")
    void testVariousCoordinates() throws SQLException {
        // Тест с отрицательными значениями
        Long id1 = pointDAO.insert(testFunctionId, -10.5, -20.3);
        assertNotNull(id1);
        
        // Тест с нулевыми значениями
        Long id2 = pointDAO.insert(testFunctionId, 0.0, 0.0);
        assertNotNull(id2);
        
        // Тест с большими значениями
        Long id3 = pointDAO.insert(testFunctionId, 1000.0, 2000.0);
        assertNotNull(id3);
        
        // Тест с очень малыми значениями
        Long id4 = pointDAO.insert(testFunctionId, 0.0001, 0.0002);
        assertNotNull(id4);
        
        // Тест с дробными значениями
        Long id5 = pointDAO.insert(testFunctionId, 3.14159, 2.71828);
        assertNotNull(id5);
        
        // Проверяем все точки
        assertTrue(pointDAO.findById(id1).isPresent());
        assertTrue(pointDAO.findById(id2).isPresent());
        assertTrue(pointDAO.findById(id3).isPresent());
        assertTrue(pointDAO.findById(id4).isPresent());
        assertTrue(pointDAO.findById(id5).isPresent());
    }
}

