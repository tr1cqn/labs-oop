package database.dao;

import org.junit.jupiter.api.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для UserDAO с генерацией разнообразных данных
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserDAOTest {
    private static UserDAO userDAO;
    private static Random random;
    
    @BeforeAll
    static void setUp() {
        userDAO = new UserDAO();
        random = new Random();
    }
    
    @Test
    @Order(1)
    @DisplayName("Тест вставки пользователя с email")
    void testInsertWithEmail() throws SQLException {
        String login = "test_user_" + random.nextInt(100000);
        String password = "hash_" + random.nextInt(100000);
        String email = "user" + random.nextInt(100000) + "@test.com";
        
        Long id = userDAO.insert(login, password, email);
        
        assertNotNull(id);
        assertTrue(id > 0);
    }
    
    @Test
    @Order(2)
    @DisplayName("Тест вставки пользователя без email")
    void testInsertWithoutEmail() throws SQLException {
        String login = "test_user_no_email_" + random.nextInt(100000);
        String password = "hash_" + random.nextInt(100000);
        
        Long id = userDAO.insert(login, password);
        
        assertNotNull(id);
        assertTrue(id > 0);
    }
    
    @Test
    @Order(3)
    @DisplayName("Тест поиска пользователя по ID")
    void testFindById() throws SQLException {
        String login = "find_user_" + random.nextInt(100000);
        String password = "hash_" + random.nextInt(100000);
        String email = "find" + random.nextInt(100000) + "@test.com";
        
        Long id = userDAO.insert(login, password, email);
        Optional<UserDAO.User> found = userDAO.findById(id);
        
        assertTrue(found.isPresent());
        assertEquals(id, found.get().getId());
        assertEquals(login, found.get().getLogin());
        assertEquals(email, found.get().getEmail());
    }
    
    @Test
    @Order(4)
    @DisplayName("Тест поиска пользователя по login")
    void testFindByLogin() throws SQLException {
        String login = "find_by_login_" + random.nextInt(100000);
        String password = "hash_" + random.nextInt(100000);
        
        userDAO.insert(login, password);
        Optional<UserDAO.User> found = userDAO.findByLogin(login);
        
        assertTrue(found.isPresent());
        assertEquals(login, found.get().getLogin());
    }
    
    @Test
    @Order(5)
    @DisplayName("Тест поиска пользователя по email")
    void testFindByEmail() throws SQLException {
        String login = "find_by_email_" + random.nextInt(100000);
        String password = "hash_" + random.nextInt(100000);
        String email = "findemail" + random.nextInt(100000) + "@test.com";
        
        userDAO.insert(login, password, email);
        Optional<UserDAO.User> found = userDAO.findByEmail(email);
        
        assertTrue(found.isPresent());
        assertEquals(email, found.get().getEmail());
    }
    
    @Test
    @Order(6)
    @DisplayName("Тест поиска пользователей по частичному совпадению login")
    void testFindByLoginLike() throws SQLException {
        String prefix = "like_test_" + random.nextInt(10000);
        // Создаем несколько пользователей с одинаковым префиксом
        for (int i = 0; i < 3; i++) {
            userDAO.insert(prefix + "_" + i, "hash" + i);
        }
        
        List<UserDAO.User> users = userDAO.findByLoginLike(prefix + "%");
        
        assertNotNull(users);
        assertTrue(users.size() >= 3);
    }
    
    @Test
    @Order(7)
    @DisplayName("Тест получения всех пользователей")
    void testFindAll() throws SQLException {
        // Создаем несколько пользователей
        for (int i = 0; i < 5; i++) {
            userDAO.insert("user_all_" + random.nextInt(100000), "hash_" + i);
        }
        
        List<UserDAO.User> users = userDAO.findAll();
        
        assertNotNull(users);
        assertFalse(users.isEmpty());
    }
    
    @Test
    @Order(8)
    @DisplayName("Тест обновления пароля")
    void testUpdatePassword() throws SQLException {
        String login = "update_pass_" + random.nextInt(100000);
        String password = "old_hash_" + random.nextInt(100000);
        
        Long id = userDAO.insert(login, password);
        
        String newPassword = "new_hash_" + random.nextInt(100000);
        boolean updated = userDAO.updatePassword(id, newPassword);
        
        assertTrue(updated);
    }
    
    @Test
    @Order(9)
    @DisplayName("Тест обновления email")
    void testUpdateEmail() throws SQLException {
        String login = "update_email_" + random.nextInt(100000);
        String password = "hash_" + random.nextInt(100000);
        String email = "old" + random.nextInt(100000) + "@test.com";
        
        Long id = userDAO.insert(login, password, email);
        
        String newEmail = "new" + random.nextInt(100000) + "@test.com";
        boolean updated = userDAO.updateEmail(id, newEmail);
        assertTrue(updated);
        
        Optional<UserDAO.User> updatedUser = userDAO.findById(id);
        assertTrue(updatedUser.isPresent());
        assertEquals(newEmail, updatedUser.get().getEmail());
    }
    
    @Test
    @Order(10)
    @DisplayName("Тест обновления login")
    void testUpdateLogin() throws SQLException {
        String login = "update_login_" + random.nextInt(100000);
        String password = "hash_" + random.nextInt(100000);
        
        Long id = userDAO.insert(login, password);
        
        String newLogin = "updated_" + random.nextInt(100000);
        boolean updated = userDAO.updateLogin(id, newLogin);
        assertTrue(updated);
        
        Optional<UserDAO.User> updatedUser = userDAO.findById(id);
        assertTrue(updatedUser.isPresent());
        assertEquals(newLogin, updatedUser.get().getLogin());
    }
    
    @Test
    @Order(11)
    @DisplayName("Тест обновления всех данных пользователя")
    void testUpdateAll() throws SQLException {
        String login = "update_all_" + random.nextInt(100000);
        String password = "hash_" + random.nextInt(100000);
        String email = "old" + random.nextInt(100000) + "@test.com";
        
        Long id = userDAO.insert(login, password, email);
        
        String newLogin = "updated_all_" + random.nextInt(100000);
        String newPassword = "new_hash_" + random.nextInt(100000);
        String newEmail = "new" + random.nextInt(100000) + "@test.com";
        
        boolean updated = userDAO.update(id, newLogin, newPassword, newEmail);
        assertTrue(updated);
        
        Optional<UserDAO.User> updatedUser = userDAO.findById(id);
        assertTrue(updatedUser.isPresent());
        assertEquals(newLogin, updatedUser.get().getLogin());
        assertEquals(newEmail, updatedUser.get().getEmail());
    }
    
    @Test
    @Order(12)
    @DisplayName("Тест удаления пользователя по ID")
    void testDeleteById() throws SQLException {
        String login = "delete_id_" + random.nextInt(100000);
        String password = "hash_" + random.nextInt(100000);
        
        Long id = userDAO.insert(login, password);
        
        boolean deleted = userDAO.deleteById(id);
        assertTrue(deleted);
        
        Optional<UserDAO.User> deletedUser = userDAO.findById(id);
        assertFalse(deletedUser.isPresent());
    }
    
    @Test
    @Order(13)
    @DisplayName("Тест удаления пользователя по login")
    void testDeleteByLogin() throws SQLException {
        String login = "delete_login_" + random.nextInt(100000);
        String password = "hash_" + random.nextInt(100000);
        
        userDAO.insert(login, password);
        
        boolean deleted = userDAO.deleteByLogin(login);
        assertTrue(deleted);
        
        Optional<UserDAO.User> deletedUser = userDAO.findByLogin(login);
        assertFalse(deletedUser.isPresent());
    }
    
    @Test
    @Order(14)
    @DisplayName("Тест с разнообразными данными - длинные строки")
    void testVariousDataLongStrings() throws SQLException {
        String longLogin = "a".repeat(50) + random.nextInt(1000);
        String longPassword = "b".repeat(200) + random.nextInt(1000);
        String longEmail = "c".repeat(100) + "@test.com";
        
        Long id = userDAO.insert(longLogin, longPassword, longEmail);
        assertNotNull(id);
        
        Optional<UserDAO.User> user = userDAO.findById(id);
        assertTrue(user.isPresent());
    }
    
    @Test
    @Order(15)
    @DisplayName("Тест с разнообразными данными - короткие строки")
    void testVariousDataShortStrings() throws SQLException {
        String shortLogin = "u" + random.nextInt(1000);
        String shortPassword = "p" + random.nextInt(1000);
        
        Long id = userDAO.insert(shortLogin, shortPassword);
        assertNotNull(id);
        
        Optional<UserDAO.User> user = userDAO.findById(id);
        assertTrue(user.isPresent());
    }
    
    @Test
    @Order(16)
    @DisplayName("Тест с разнообразными данными - специальные символы")
    void testVariousDataSpecialChars() throws SQLException {
        String specialLogin = "user_" + random.nextInt(1000) + "_test";
        String specialPassword = "pass_" + random.nextInt(1000) + "_!@#";
        String specialEmail = "email" + random.nextInt(1000) + "+test@test.com";
        
        Long id = userDAO.insert(specialLogin, specialPassword, specialEmail);
        assertNotNull(id);
        
        Optional<UserDAO.User> user = userDAO.findById(id);
        assertTrue(user.isPresent());
    }
}

