package database.dao;

import org.junit.jupiter.api.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для FunctionDAO с генерацией разнообразных данных
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FunctionDAOTest {
    private static FunctionDAO functionDAO;
    private static UserDAO userDAO;
    private static Random random;
    private static Long testUserId;
    
    @BeforeAll
    static void setUp() throws SQLException {
        functionDAO = new FunctionDAO();
        userDAO = new UserDAO();
        random = new Random();
        
        // Создаем тестового пользователя
        testUserId = userDAO.insert("test_user_func_" + random.nextInt(100000), "hash");
    }
    
    @Test
    @Order(1)
    @DisplayName("Тест вставки функции")
    void testInsert() throws SQLException {
        String name = "test_function_" + random.nextInt(100000);
        String type = "linear";
        
        Long id = functionDAO.insert(testUserId, name, type);
        
        assertNotNull(id);
        assertTrue(id > 0);
    }
    
    @Test
    @Order(2)
    @DisplayName("Тест поиска функции по ID")
    void testFindById() throws SQLException {
        String name = "find_function_" + random.nextInt(100000);
        String type = "quadratic";
        
        Long id = functionDAO.insert(testUserId, name, type);
        Optional<FunctionDAO.Function> found = functionDAO.findById(id);
        
        assertTrue(found.isPresent());
        assertEquals(id, found.get().getId());
        assertEquals(name, found.get().getName());
        assertEquals(type, found.get().getType());
    }
    
    @Test
    @Order(3)
    @DisplayName("Тест поиска функций по user_id")
    void testFindByUserId() throws SQLException {
        Long userId = userDAO.insert("user_func_test_" + random.nextInt(100000), "hash");
        
        // Создаем несколько функций для пользователя
        for (int i = 0; i < 3; i++) {
            functionDAO.insert(userId, "func_" + i + "_" + random.nextInt(1000), "type" + i);
        }
        
        List<FunctionDAO.Function> functions = functionDAO.findByUserId(userId);
        
        assertNotNull(functions);
        assertTrue(functions.size() >= 3);
    }
    
    @Test
    @Order(4)
    @DisplayName("Тест поиска функций по типу")
    void testFindByType() throws SQLException {
        String type = "polynomial";
        
        // Создаем несколько функций одного типа
        for (int i = 0; i < 3; i++) {
            functionDAO.insert(testUserId, "func_type_" + i + "_" + random.nextInt(1000), type);
        }
        
        List<FunctionDAO.Function> functions = functionDAO.findByType(type);
        
        assertNotNull(functions);
        assertTrue(functions.size() >= 3);
    }
    
    @Test
    @Order(5)
    @DisplayName("Тест поиска функций по имени")
    void testFindByName() throws SQLException {
        String name = "exact_name_" + random.nextInt(100000);
        String type = "exponential";
        
        functionDAO.insert(testUserId, name, type);
        List<FunctionDAO.Function> functions = functionDAO.findByName(name);
        
        assertNotNull(functions);
        assertFalse(functions.isEmpty());
    }
    
    @Test
    @Order(6)
    @DisplayName("Тест поиска функций по частичному совпадению имени")
    void testFindByNameLike() throws SQLException {
        String prefix = "like_func_" + random.nextInt(10000);
        
        // Создаем несколько функций с одинаковым префиксом
        for (int i = 0; i < 3; i++) {
            functionDAO.insert(testUserId, prefix + "_" + i, "type");
        }
        
        List<FunctionDAO.Function> functions = functionDAO.findByNameLike(prefix + "%");
        
        assertNotNull(functions);
        assertTrue(functions.size() >= 3);
    }
    
    @Test
    @Order(7)
    @DisplayName("Тест поиска функций пользователя определенного типа")
    void testFindByUserIdAndType() throws SQLException {
        Long userId = userDAO.insert("user_type_test_" + random.nextInt(100000), "hash");
        String type = "trigonometric";
        
        // Создаем функции разных типов
        functionDAO.insert(userId, "func1", type);
        functionDAO.insert(userId, "func2", type);
        functionDAO.insert(userId, "func3", "other_type");
        
        List<FunctionDAO.Function> functions = functionDAO.findByUserIdAndType(userId, type);
        
        assertNotNull(functions);
        assertEquals(2, functions.size());
    }
    
    @Test
    @Order(8)
    @DisplayName("Тест подсчета количества функций пользователя")
    void testCountByUserId() throws SQLException {
        Long userId = userDAO.insert("user_count_" + random.nextInt(100000), "hash");
        
        // Создаем несколько функций
        for (int i = 0; i < 5; i++) {
            functionDAO.insert(userId, "func_count_" + i, "type");
        }
        
        int count = functionDAO.countByUserId(userId);
        assertTrue(count >= 5);
    }
    
    @Test
    @Order(9)
    @DisplayName("Тест обновления имени функции")
    void testUpdateName() throws SQLException {
        String name = "update_name_" + random.nextInt(100000);
        String type = "logarithmic";
        
        Long id = functionDAO.insert(testUserId, name, type);
        
        String newName = "updated_" + random.nextInt(100000);
        boolean updated = functionDAO.updateName(id, newName);
        assertTrue(updated);
        
        Optional<FunctionDAO.Function> updatedFunc = functionDAO.findById(id);
        assertTrue(updatedFunc.isPresent());
        assertEquals(newName, updatedFunc.get().getName());
    }
    
    @Test
    @Order(10)
    @DisplayName("Тест обновления типа функции")
    void testUpdateType() throws SQLException {
        String name = "update_type_" + random.nextInt(100000);
        String type = "old_type";
        
        Long id = functionDAO.insert(testUserId, name, type);
        
        String newType = "new_type";
        boolean updated = functionDAO.updateType(id, newType);
        assertTrue(updated);
        
        Optional<FunctionDAO.Function> updatedFunc = functionDAO.findById(id);
        assertTrue(updatedFunc.isPresent());
        assertEquals(newType, updatedFunc.get().getType());
    }
    
    @Test
    @Order(11)
    @DisplayName("Тест обновления имени и типа функции")
    void testUpdate() throws SQLException {
        String name = "update_all_" + random.nextInt(100000);
        String type = "old_type";
        
        Long id = functionDAO.insert(testUserId, name, type);
        
        String newName = "updated_all_" + random.nextInt(100000);
        String newType = "new_type";
        
        boolean updated = functionDAO.update(id, newName, newType);
        assertTrue(updated);
        
        Optional<FunctionDAO.Function> updatedFunc = functionDAO.findById(id);
        assertTrue(updatedFunc.isPresent());
        assertEquals(newName, updatedFunc.get().getName());
        assertEquals(newType, updatedFunc.get().getType());
    }
    
    @Test
    @Order(12)
    @DisplayName("Тест обновления всех функций пользователя")
    void testUpdateAllByUserId() throws SQLException {
        Long userId = userDAO.insert("user_update_all_" + random.nextInt(100000), "hash");
        
        // Создаем несколько функций
        for (int i = 0; i < 3; i++) {
            functionDAO.insert(userId, "func_update_" + i, "old_type");
        }
        
        int updated = functionDAO.updateAllByUserId(userId, "new_type");
        assertTrue(updated >= 3);
    }
    
    @Test
    @Order(13)
    @DisplayName("Тест удаления функции по ID")
    void testDeleteById() throws SQLException {
        String name = "delete_func_" + random.nextInt(100000);
        String type = "trigonometric";
        
        Long id = functionDAO.insert(testUserId, name, type);
        
        boolean deleted = functionDAO.deleteById(id);
        assertTrue(deleted);
        
        Optional<FunctionDAO.Function> deletedFunc = functionDAO.findById(id);
        assertFalse(deletedFunc.isPresent());
    }
    
    @Test
    @Order(14)
    @DisplayName("Тест удаления всех функций пользователя")
    void testDeleteByUserId() throws SQLException {
        Long userId = userDAO.insert("user_delete_all_" + random.nextInt(100000), "hash");
        
        // Создаем несколько функций
        for (int i = 0; i < 3; i++) {
            functionDAO.insert(userId, "func_delete_" + i, "type");
        }
        
        int deleted = functionDAO.deleteByUserId(userId);
        assertTrue(deleted >= 3);
        
        List<FunctionDAO.Function> functions = functionDAO.findByUserId(userId);
        assertTrue(functions.isEmpty());
    }
    
    @Test
    @Order(15)
    @DisplayName("Тест удаления функций определенного типа")
    void testDeleteByType() throws SQLException {
        String type = "delete_type_" + random.nextInt(10000);
        
        // Создаем несколько функций этого типа
        for (int i = 0; i < 3; i++) {
            functionDAO.insert(testUserId, "func_delete_type_" + i, type);
        }
        
        int deleted = functionDAO.deleteByType(type);
        assertTrue(deleted >= 3);
    }
    
    @Test
    @Order(16)
    @DisplayName("Тест с разнообразными типами функций")
    void testVariousFunctionTypes() throws SQLException {
        String[] types = {"linear", "quadratic", "polynomial", "exponential", "trigonometric", "logarithmic"};
        
        for (String type : types) {
            Long id = functionDAO.insert(testUserId, "func_" + type + "_" + random.nextInt(1000), type);
            assertNotNull(id);
            
            Optional<FunctionDAO.Function> func = functionDAO.findById(id);
            assertTrue(func.isPresent());
            assertEquals(type, func.get().getType());
        }
    }
}

