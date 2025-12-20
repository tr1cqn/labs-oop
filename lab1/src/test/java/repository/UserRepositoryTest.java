package repository;

import entity.User;
import org.junit.jupiter.api.*;
import repository.impl.UserRepositoryImpl;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для UserRepository с генерацией данных, поиском и удалением
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserRepositoryTest {
    private static UserRepository userRepository;
    private static Random random;
    
    @BeforeAll
    static void setUp() {
        userRepository = new UserRepositoryImpl();
        random = new Random();
    }
    
    @Test
    @Order(1)
    @DisplayName("Тест поиска пользователя по ID")
    void testFindById() {
        User user = new User("find_by_id_" + random.nextInt(100000), 
                            "password_" + random.nextInt(100000), 
                            "email" + random.nextInt(100000) + "@test.com");
        User saved = userRepository.save(user);
        
        Optional<User> found = userRepository.findById(saved.getId());
        
        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
        assertEquals(saved.getLogin(), found.get().getLogin());
    }
    
    @Test
    @Order(2)
    @DisplayName("Тест поиска всех пользователей")
    void testFindAll() {
        for (int i = 0; i < 5; i++) {
            User user = new User("find_all_" + random.nextInt(100000), 
                                "password_" + i, 
                                "email" + i + "@test.com");
            userRepository.save(user);
        }
        
        List<User> users = userRepository.findAll();
        
        assertNotNull(users);
        assertFalse(users.isEmpty());
    }
    
    @Test
    @Order(3)
    @DisplayName("Тест поиска пользователя по login")
    void testFindByLogin() {
        String login = "find_by_login_" + random.nextInt(100000);
        User user = new User(login, "password", "email@test.com");
        userRepository.save(user);
        
        Optional<User> found = userRepository.findByLogin(login);
        
        assertTrue(found.isPresent());
        assertEquals(login, found.get().getLogin());
    }
    
    @Test
    @Order(4)
    @DisplayName("Тест удаления пользователя")
    void testDelete() {
        User user = new User("delete_user_" + random.nextInt(100000), 
                            "password", "email@test.com");
        User saved = userRepository.save(user);
        
        userRepository.delete(saved);
        
        Optional<User> deleted = userRepository.findById(saved.getId());
        assertFalse(deleted.isPresent());
    }
    
    @Test
    @Order(5)
    @DisplayName("Тест удаления пользователя по ID")
    void testDeleteById() {
        User user = new User("delete_by_id_" + random.nextInt(100000), 
                            "password", "email@test.com");
        User saved = userRepository.save(user);
        
        userRepository.deleteById(saved.getId());
        
        Optional<User> deleted = userRepository.findById(saved.getId());
        assertFalse(deleted.isPresent());
    }
    
    @Test
    @Order(6)
    @DisplayName("Тест с разнообразными данными - длинные строки")
    void testVariousDataLongStrings() {
        String longLogin = "a".repeat(50) + random.nextInt(1000);
        String longPassword = "b".repeat(200) + random.nextInt(1000);
        String longEmail = "c".repeat(100) + "@test.com";
        
        User user = new User(longLogin, longPassword, longEmail);
        User saved = userRepository.save(user);
        
        Optional<User> found = userRepository.findById(saved.getId());
        assertTrue(found.isPresent());
    }
    
    @Test
    @Order(7)
    @DisplayName("Тест с разнообразными данными - короткие строки")
    void testVariousDataShortStrings() {
        User user = new User("u" + random.nextInt(1000), "p", null);
        User saved = userRepository.save(user);
        
        Optional<User> found = userRepository.findById(saved.getId());
        assertTrue(found.isPresent());
    }
    
    @Test
    @Order(8)
    @DisplayName("Тест с разнообразными данными - специальные символы")
    void testVariousDataSpecialChars() {
        String specialLogin = "user_" + random.nextInt(1000) + "_test";
        String specialPassword = "pass_" + random.nextInt(1000) + "_!@#";
        String specialEmail = "email" + random.nextInt(1000) + "+test@test.com";
        
        User user = new User(specialLogin, specialPassword, specialEmail);
        User saved = userRepository.save(user);
        
        Optional<User> found = userRepository.findById(saved.getId());
        assertTrue(found.isPresent());
    }
}

