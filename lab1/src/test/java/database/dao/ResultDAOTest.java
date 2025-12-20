package database.dao;

import org.junit.jupiter.api.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для ResultDAO с генерацией разнообразных данных
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ResultDAOTest {
    private static ResultDAO resultDAO;
    private static FunctionDAO functionDAO;
    private static UserDAO userDAO;
    private static Random random;
    private static Long testFunctionId;
    
    @BeforeAll
    static void setUp() throws SQLException {
        resultDAO = new ResultDAO();
        functionDAO = new FunctionDAO();
        userDAO = new UserDAO();
        random = new Random();
        
        // Создаем тестового пользователя и функцию
        Long userId = userDAO.insert("test_user_result_" + random.nextInt(100000), "hash");
        testFunctionId = functionDAO.insert(userId, "test_function", "linear");
    }
    
    @Test
    @Order(1)
    @DisplayName("Тест вставки результата")
    void testInsert() throws SQLException {
        String result = "Result: " + random.nextInt(100000);
        
        Long id = resultDAO.insert(testFunctionId, result);
        
        assertNotNull(id);
        assertTrue(id > 0);
    }
    
    @Test
    @Order(2)
    @DisplayName("Тест поиска результата по ID")
    void testFindById() throws SQLException {
        String result = "Find result: " + random.nextInt(100000);
        
        Long id = resultDAO.insert(testFunctionId, result);
        Optional<ResultDAO.Result> found = resultDAO.findById(id);
        
        assertTrue(found.isPresent());
        assertEquals(id, found.get().getId());
        assertEquals(testFunctionId, found.get().getFunctionId());
        assertEquals(result, found.get().getResult());
    }
    
    @Test
    @Order(3)
    @DisplayName("Тест поиска результатов по function_id")
    void testFindByFunctionId() throws SQLException {
        Long functionId = functionDAO.insert(
            userDAO.insert("user_result_test_" + random.nextInt(100000), "hash"),
            "test_func_results", "linear"
        );
        
        // Создаем несколько результатов
        for (int i = 0; i < 5; i++) {
            resultDAO.insert(functionId, "Result " + i + ": " + random.nextInt(1000));
        }
        
        List<ResultDAO.Result> results = resultDAO.findByFunctionId(functionId);
        
        assertNotNull(results);
        assertEquals(5, results.size());
    }
    
    @Test
    @Order(4)
    @DisplayName("Тест поиска последнего результата функции")
    void testFindLastByFunctionId() throws SQLException {
        Long functionId = functionDAO.insert(
            userDAO.insert("user_last_" + random.nextInt(100000), "hash"),
            "last_func", "linear"
        );
        
        resultDAO.insert(functionId, "First result");
        resultDAO.insert(functionId, "Second result");
        resultDAO.insert(functionId, "Last result");
        
        Optional<ResultDAO.Result> last = resultDAO.findLastByFunctionId(functionId);
        
        assertTrue(last.isPresent());
        assertEquals("Last result", last.get().getResult());
    }
    
    @Test
    @Order(5)
    @DisplayName("Тест поиска отсортированных результатов")
    void testFindByFunctionIdOrdered() throws SQLException {
        Long functionId = functionDAO.insert(
            userDAO.insert("user_ordered_" + random.nextInt(100000), "hash"),
            "ordered_func", "linear"
        );
        
        resultDAO.insert(functionId, "Result 3");
        resultDAO.insert(functionId, "Result 1");
        resultDAO.insert(functionId, "Result 2");
        
        List<ResultDAO.Result> results = resultDAO.findByFunctionIdOrdered(functionId);
        
        assertNotNull(results);
        assertTrue(results.size() >= 3);
    }
    
    @Test
    @Order(6)
    @DisplayName("Тест подсчета количества результатов")
    void testCountByFunctionId() throws SQLException {
        Long functionId = functionDAO.insert(
            userDAO.insert("user_count_" + random.nextInt(100000), "hash"),
            "count_func", "linear"
        );
        
        for (int i = 0; i < 7; i++) {
            resultDAO.insert(functionId, "Result " + i);
        }
        
        int count = resultDAO.countByFunctionId(functionId);
        assertEquals(7, count);
    }
    
    @Test
    @Order(7)
    @DisplayName("Тест поиска результатов по частичному совпадению текста")
    void testFindByResultLike() throws SQLException {
        String prefix = "like_test_" + random.nextInt(10000);
        
        // Создаем несколько результатов с одинаковым префиксом
        for (int i = 0; i < 3; i++) {
            resultDAO.insert(testFunctionId, prefix + "_result_" + i);
        }
        
        List<ResultDAO.Result> results = resultDAO.findByResultLike(prefix + "%");
        
        assertNotNull(results);
        assertTrue(results.size() >= 3);
    }
    
    @Test
    @Order(8)
    @DisplayName("Тест поиска результатов функции по частичному совпадению текста")
    void testFindByFunctionIdAndResultLike() throws SQLException {
        Long functionId = functionDAO.insert(
            userDAO.insert("user_like_" + random.nextInt(100000), "hash"),
            "like_func", "linear"
        );
        
        String pattern = "pattern_" + random.nextInt(10000);
        resultDAO.insert(functionId, pattern + "_1");
        resultDAO.insert(functionId, pattern + "_2");
        resultDAO.insert(functionId, "other_result");
        
        List<ResultDAO.Result> results = resultDAO.findByFunctionIdAndResultLike(functionId, pattern + "%");
        
        assertNotNull(results);
        assertTrue(results.size() >= 2);
    }
    
    @Test
    @Order(9)
    @DisplayName("Тест вставки нескольких результатов")
    void testInsertMultiple() throws SQLException {
        Long functionId = functionDAO.insert(
            userDAO.insert("user_multiple_" + random.nextInt(100000), "hash"),
            "multiple_func", "linear"
        );
        
        resultDAO.insertMultiple(functionId, "Result 1", "Result 2", "Result 3");
        
        List<ResultDAO.Result> results = resultDAO.findByFunctionId(functionId);
        assertTrue(results.size() >= 3);
    }
    
    @Test
    @Order(10)
    @DisplayName("Тест обновления результата по ID")
    void testUpdateById() throws SQLException {
        String result = "Original result: " + random.nextInt(100000);
        
        Long id = resultDAO.insert(testFunctionId, result);
        
        String newResult = "Updated result: " + random.nextInt(100000);
        boolean updated = resultDAO.updateById(id, newResult);
        assertTrue(updated);
        
        Optional<ResultDAO.Result> updatedResult = resultDAO.findById(id);
        assertTrue(updatedResult.isPresent());
        assertEquals(newResult, updatedResult.get().getResult());
    }
    
    @Test
    @Order(11)
    @DisplayName("Тест обновления всех результатов функции")
    void testUpdateAllByFunctionId() throws SQLException {
        Long functionId = functionDAO.insert(
            userDAO.insert("user_update_all_" + random.nextInt(100000), "hash"),
            "update_all_func", "linear"
        );
        
        resultDAO.insert(functionId, "Result 1");
        resultDAO.insert(functionId, "Result 2");
        resultDAO.insert(functionId, "Result 3");
        
        int updated = resultDAO.updateAllByFunctionId(functionId, "Updated result");
        assertTrue(updated >= 3);
    }
    
    @Test
    @Order(12)
    @DisplayName("Тест обновления последнего результата функции")
    void testUpdateLastByFunctionId() throws SQLException {
        Long functionId = functionDAO.insert(
            userDAO.insert("user_update_last_" + random.nextInt(100000), "hash"),
            "update_last_func", "linear"
        );
        
        resultDAO.insert(functionId, "First result");
        resultDAO.insert(functionId, "Second result");
        resultDAO.insert(functionId, "Last result");
        
        boolean updated = resultDAO.updateLastByFunctionId(functionId, "Updated last result");
        assertTrue(updated);
        
        Optional<ResultDAO.Result> last = resultDAO.findLastByFunctionId(functionId);
        assertTrue(last.isPresent());
        assertEquals("Updated last result", last.get().getResult());
    }
    
    @Test
    @Order(13)
    @DisplayName("Тест удаления результата по ID")
    void testDeleteById() throws SQLException {
        String result = "Delete result: " + random.nextInt(100000);
        
        Long id = resultDAO.insert(testFunctionId, result);
        
        boolean deleted = resultDAO.deleteById(id);
        assertTrue(deleted);
        
        Optional<ResultDAO.Result> deletedResult = resultDAO.findById(id);
        assertFalse(deletedResult.isPresent());
    }
    
    @Test
    @Order(14)
    @DisplayName("Тест удаления всех результатов функции")
    void testDeleteByFunctionId() throws SQLException {
        Long functionId = functionDAO.insert(
            userDAO.insert("user_delete_all_" + random.nextInt(100000), "hash"),
            "delete_all_func", "linear"
        );
        
        for (int i = 0; i < 3; i++) {
            resultDAO.insert(functionId, "Result " + i);
        }
        
        int deleted = resultDAO.deleteByFunctionId(functionId);
        assertEquals(3, deleted);
        
        List<ResultDAO.Result> results = resultDAO.findByFunctionId(functionId);
        assertTrue(results.isEmpty());
    }
    
    @Test
    @Order(15)
    @DisplayName("Тест удаления последнего результата функции")
    void testDeleteLastByFunctionId() throws SQLException {
        Long functionId = functionDAO.insert(
            userDAO.insert("user_delete_last_" + random.nextInt(100000), "hash"),
            "delete_last_func", "linear"
        );
        
        resultDAO.insert(functionId, "First result");
        resultDAO.insert(functionId, "Second result");
        resultDAO.insert(functionId, "Last result");
        
        boolean deleted = resultDAO.deleteLastByFunctionId(functionId);
        assertTrue(deleted);
        
        Optional<ResultDAO.Result> last = resultDAO.findLastByFunctionId(functionId);
        assertFalse(last.isPresent() && last.get().getResult().equals("Last result"));
    }
    
    @Test
    @Order(16)
    @DisplayName("Тест удаления результатов по частичному совпадению текста")
    void testDeleteByFunctionIdAndResultLike() throws SQLException {
        Long functionId = functionDAO.insert(
            userDAO.insert("user_delete_like_" + random.nextInt(100000), "hash"),
            "delete_like_func", "linear"
        );
        
        String pattern = "delete_pattern_" + random.nextInt(10000);
        resultDAO.insert(functionId, pattern + "_1");
        resultDAO.insert(functionId, pattern + "_2");
        resultDAO.insert(functionId, "keep_this");
        
        int deleted = resultDAO.deleteByFunctionIdAndResultLike(functionId, pattern + "%");
        assertTrue(deleted >= 2);
    }
    
    @Test
    @Order(17)
    @DisplayName("Тест с разнообразными текстовыми данными")
    void testVariousTextData() throws SQLException {
        // Тест с длинным текстом
        String longText = "A".repeat(500) + " result";
        Long id1 = resultDAO.insert(testFunctionId, longText);
        assertNotNull(id1);
        
        // Тест с коротким текстом
        String shortText = "OK";
        Long id2 = resultDAO.insert(testFunctionId, shortText);
        assertNotNull(id2);
        
        // Тест с специальными символами
        String specialText = "Result: !@#$%^&*()_+-=[]{}|;':\",./<>?";
        Long id3 = resultDAO.insert(testFunctionId, specialText);
        assertNotNull(id3);
        
        // Тест с числами в тексте
        String numberText = "Result: 12345.67890";
        Long id4 = resultDAO.insert(testFunctionId, numberText);
        assertNotNull(id4);
        
        // Тест с многострочным текстом
        String multilineText = "Line 1\nLine 2\nLine 3";
        Long id5 = resultDAO.insert(testFunctionId, multilineText);
        assertNotNull(id5);
        
        // Проверяем все результаты
        assertTrue(resultDAO.findById(id1).isPresent());
        assertTrue(resultDAO.findById(id2).isPresent());
        assertTrue(resultDAO.findById(id3).isPresent());
        assertTrue(resultDAO.findById(id4).isPresent());
        assertTrue(resultDAO.findById(id5).isPresent());
    }
}

