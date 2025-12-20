package repository;

import entity.Function;
import entity.User;
import org.junit.jupiter.api.*;
import repository.impl.FunctionRepositoryImpl;
import repository.impl.UserRepositoryImpl;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для FunctionRepository с генерацией данных, поиском и удалением
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FunctionRepositoryTest {
    private static FunctionRepository functionRepository;
    private static UserRepository userRepository;
    private static Random random;
    private static User testUser;
    
    @BeforeAll
    static void setUp() {
        functionRepository = new FunctionRepositoryImpl();
        userRepository = new UserRepositoryImpl();
        random = new Random();
        
        testUser = new User("test_user_func_" + random.nextInt(100000), "hash", "test@test.com");
        testUser = userRepository.save(testUser);
    }
    
    @Test
    @Order(1)
    @DisplayName("Тест поиска функции по ID")
    void testFindById() {
        Function function = new Function(testUser, "find_by_id_" + random.nextInt(100000), "linear");
        Function saved = functionRepository.save(function);
        
        Optional<Function> found = functionRepository.findById(saved.getId());
        
        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
        assertEquals(saved.getName(), found.get().getName());
    }
    
    @Test
    @Order(2)
    @DisplayName("Тест поиска всех функций")
    void testFindAll() {
        for (int i = 0; i < 5; i++) {
            Function function = new Function(testUser, "find_all_" + i + "_" + random.nextInt(1000), "type" + i);
            functionRepository.save(function);
        }
        
        List<Function> functions = functionRepository.findAll();
        
        assertNotNull(functions);
        assertFalse(functions.isEmpty());
    }
    
    @Test
    @Order(3)
    @DisplayName("Тест поиска функций пользователя")
    void testFindByUser() {
        User user = new User("user_func_test_" + random.nextInt(100000), "hash", "email@test.com");
        user = userRepository.save(user);
        
        for (int i = 0; i < 3; i++) {
            Function function = new Function(user, "func_" + i, "type");
            functionRepository.save(function);
        }
        
        List<Function> functions = functionRepository.findByUser(user);
        
        assertNotNull(functions);
        assertTrue(functions.size() >= 3);
    }
    
    @Test
    @Order(4)
    @DisplayName("Тест удаления функции")
    void testDelete() {
        Function function = new Function(testUser, "delete_func_" + random.nextInt(100000), "type");
        Function saved = functionRepository.save(function);
        
        functionRepository.delete(saved);
        
        Optional<Function> deleted = functionRepository.findById(saved.getId());
        assertFalse(deleted.isPresent());
    }
    
    @Test
    @Order(5)
    @DisplayName("Тест удаления функции по ID")
    void testDeleteById() {
        Function function = new Function(testUser, "delete_by_id_" + random.nextInt(100000), "type");
        Function saved = functionRepository.save(function);
        
        functionRepository.deleteById(saved.getId());
        
        Optional<Function> deleted = functionRepository.findById(saved.getId());
        assertFalse(deleted.isPresent());
    }
    
    @Test
    @Order(6)
    @DisplayName("Тест с разнообразными типами функций")
    void testVariousFunctionTypes() {
        String[] types = {"linear", "quadratic", "polynomial", "exponential", "trigonometric", "logarithmic"};
        
        for (String type : types) {
            Function function = new Function(testUser, "func_" + type + "_" + random.nextInt(1000), type);
            Function saved = functionRepository.save(function);
            
            Optional<Function> found = functionRepository.findById(saved.getId());
            assertTrue(found.isPresent());
            assertEquals(type, found.get().getType());
        }
    }
}

