package database.mapper;

import database.dto.UserDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для UserMapper
 */
public class UserMapperTest {
    
    @Test
    @DisplayName("Тест трансформации отдельных полей в UserDTO")
    void testToDTOFromFields() {
        UserDTO dto = UserMapper.toDTO(1L, "testuser", "password123", "test@example.com");
        
        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("testuser", dto.getLogin());
        assertEquals("password123", dto.getPassword());
        assertEquals("test@example.com", dto.getEmail());
    }
    
    @Test
    @DisplayName("Тест трансформации с null email")
    void testToDTOWithNullEmail() {
        UserDTO dto = UserMapper.toDTO(1L, "testuser", "password123", null);
        
        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("testuser", dto.getLogin());
        assertNull(dto.getEmail());
    }
    
    @Test
    @DisplayName("Тест трансформации с различными значениями")
    void testToDTOWithVariousValues() {
        // Длинные строки
        UserDTO dto1 = UserMapper.toDTO(1L, "a".repeat(50), "b".repeat(200), "c".repeat(100) + "@test.com");
        assertNotNull(dto1);
        
        // Короткие строки
        UserDTO dto2 = UserMapper.toDTO(2L, "u", "p", null);
        assertNotNull(dto2);
        
        // Специальные символы
        UserDTO dto3 = UserMapper.toDTO(3L, "user_123", "pass!@#", "email+test@test.com");
        assertNotNull(dto3);
    }
    
    @Test
    @DisplayName("Тест валидации валидного UserDTO")
    void testIsValidValidDTO() {
        UserDTO dto = new UserDTO(1L, "testuser", "password123", "test@example.com");
        
        assertTrue(UserMapper.isValid(dto));
    }
    
    @Test
    @DisplayName("Тест валидации UserDTO с null login")
    void testIsValidNullLogin() {
        UserDTO dto = new UserDTO(1L, null, "password123", "test@example.com");
        
        assertFalse(UserMapper.isValid(dto));
    }
    
    @Test
    @DisplayName("Тест валидации UserDTO с пустым login")
    void testIsValidEmptyLogin() {
        UserDTO dto = new UserDTO(1L, "", "password123", "test@example.com");
        
        assertFalse(UserMapper.isValid(dto));
    }
    
    @Test
    @DisplayName("Тест валидации UserDTO с null password")
    void testIsValidNullPassword() {
        UserDTO dto = new UserDTO(1L, "testuser", null, "test@example.com");
        
        assertFalse(UserMapper.isValid(dto));
    }
    
    @Test
    @DisplayName("Тест валидации UserDTO с пустым password")
    void testIsValidEmptyPassword() {
        UserDTO dto = new UserDTO(1L, "testuser", "", "test@example.com");
        
        assertFalse(UserMapper.isValid(dto));
    }
    
    @Test
    @DisplayName("Тест валидации null UserDTO")
    void testIsValidNullDTO() {
        assertFalse(UserMapper.isValid(null));
    }
    
    @Test
    @DisplayName("Тест equals и hashCode")
    void testEqualsAndHashCode() {
        UserDTO dto1 = new UserDTO(1L, "testuser", "password", "test@example.com");
        UserDTO dto2 = new UserDTO(1L, "testuser", "password", "test@example.com");
        UserDTO dto3 = new UserDTO(2L, "testuser", "password", "test@example.com");
        
        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }
}

