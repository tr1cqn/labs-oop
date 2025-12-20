package repository;

import entity.Function;
import entity.Result;
import entity.User;
import org.junit.jupiter.api.*;
import repository.impl.FunctionRepositoryImpl;
import repository.impl.ResultRepositoryImpl;
import repository.impl.UserRepositoryImpl;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для ResultRepository с генерацией данных, поиском и удалением
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ResultRepositoryTest {
    private static ResultRepository resultRepository;
    private static FunctionRepository functionRepository;
    private static UserRepository userRepository;
    private static Random random;
    private static Function testFunction;
    
    @BeforeAll
    static void setUp() {
        resultRepository = new ResultRepositoryImpl();
        functionRepository = new FunctionRepositoryImpl();
        userRepository = new UserRepositoryImpl();
        random = new Random();
        
        User user = new User("test_user_result_" + random.nextInt(100000), "hash", "test@test.com");
        user = userRepository.save(user);
        testFunction = new Function(user, "test_function", "linear");
        testFunction = functionRepository.save(testFunction);
    }
    
    @Test
    @Order(1)
    @DisplayName("Тест поиска результата по ID")
    void testFindById() {
        Result result = new Result(testFunction, "Result: " + random.nextInt(100000));
        Result saved = resultRepository.save(result);
        
        Optional<Result> found = resultRepository.findById(saved.getId());
        
        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
        assertEquals(saved.getResult(), found.get().getResult());
    }
    
    @Test
    @Order(2)
    @DisplayName("Тест поиска всех результатов")
    void testFindAll() {
        for (int i = 0; i < 5; i++) {
            Result result = new Result(testFunction, "Result " + i + ": " + random.nextInt(1000));
            resultRepository.save(result);
        }
        
        List<Result> results = resultRepository.findAll();
        
        assertNotNull(results);
        assertFalse(results.isEmpty());
    }
    
    @Test
    @Order(3)
    @DisplayName("Тест поиска результатов функции")
    void testFindByFunction() {
        Function function = new Function(testFunction.getUser(), "test_func_results", "linear");
        function = functionRepository.save(function);
        
        for (int i = 0; i < 3; i++) {
            Result result = new Result(function, "Result " + i);
            resultRepository.save(result);
        }
        
        List<Result> results = resultRepository.findByFunction(function);
        
        assertNotNull(results);
        assertEquals(3, results.size());
    }
    
    @Test
    @Order(4)
    @DisplayName("Тест удаления результата")
    void testDelete() {
        Result result = new Result(testFunction, "Delete result: " + random.nextInt(100000));
        Result saved = resultRepository.save(result);
        
        resultRepository.delete(saved);
        
        Optional<Result> deleted = resultRepository.findById(saved.getId());
        assertFalse(deleted.isPresent());
    }
    
    @Test
    @Order(5)
    @DisplayName("Тест удаления результата по ID")
    void testDeleteById() {
        Result result = new Result(testFunction, "Delete by id: " + random.nextInt(100000));
        Result saved = resultRepository.save(result);
        
        resultRepository.deleteById(saved.getId());
        
        Optional<Result> deleted = resultRepository.findById(saved.getId());
        assertFalse(deleted.isPresent());
    }
    
    @Test
    @Order(6)
    @DisplayName("Тест с разнообразными текстовыми данными")
    void testVariousTextData() {
        // Длинный текст
        Result result1 = new Result(testFunction, "A".repeat(500) + " result");
        Result saved1 = resultRepository.save(result1);
        assertTrue(resultRepository.findById(saved1.getId()).isPresent());
        
        // Короткий текст
        Result result2 = new Result(testFunction, "OK");
        Result saved2 = resultRepository.save(result2);
        assertTrue(resultRepository.findById(saved2.getId()).isPresent());
        
        // Специальные символы
        Result result3 = new Result(testFunction, "Result: !@#$%^&*()_+-=[]{}|;':\",./<>?");
        Result saved3 = resultRepository.save(result3);
        assertTrue(resultRepository.findById(saved3.getId()).isPresent());
        
        // Числа в тексте
        Result result4 = new Result(testFunction, "Result: 12345.67890");
        Result saved4 = resultRepository.save(result4);
        assertTrue(resultRepository.findById(saved4.getId()).isPresent());
        
        // Многострочный текст
        Result result5 = new Result(testFunction, "Line 1\nLine 2\nLine 3");
        Result saved5 = resultRepository.save(result5);
        assertTrue(resultRepository.findById(saved5.getId()).isPresent());
    }
}

