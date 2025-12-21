package database.mapper;

import database.dto.FunctionDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для FunctionMapper
 */
public class FunctionMapperTest {
    
    @Test
    @DisplayName("Тест трансформации отдельных полей в FunctionDTO")
    void testToDTOFromFields() {
        FunctionDTO dto = FunctionMapper.toDTO(1L, 10L, "Linear Function", "linear");
        
        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals(10L, dto.getUserId());
        assertEquals("Linear Function", dto.getName());
        assertEquals("linear", dto.getType());
    }
    
    @Test
    @DisplayName("Тест трансформации с различными типами функций")
    void testToDTOWithVariousTypes() {
        String[] types = {"linear", "quadratic", "polynomial", "exponential", "trigonometric"};
        
        for (String type : types) {
            FunctionDTO dto = FunctionMapper.toDTO(1L, 10L, "Function " + type, type);
            assertNotNull(dto);
            assertEquals(type, dto.getType());
        }
    }
    
    @Test
    @DisplayName("Тест трансформации с длинными именами")
    void testToDTOWithLongNames() {
        String longName = "A".repeat(200);
        FunctionDTO dto = FunctionMapper.toDTO(1L, 10L, longName, "linear");
        
        assertNotNull(dto);
        assertEquals(longName, dto.getName());
    }
    
    @Test
    @DisplayName("Тест валидации валидного FunctionDTO")
    void testIsValidValidDTO() {
        FunctionDTO dto = new FunctionDTO(1L, 10L, "Linear Function", "linear");
        
        assertTrue(FunctionMapper.isValid(dto));
    }
    
    @Test
    @DisplayName("Тест валидации FunctionDTO с null name")
    void testIsValidNullName() {
        FunctionDTO dto = new FunctionDTO(1L, 10L, null, "linear");
        
        assertFalse(FunctionMapper.isValid(dto));
    }
    
    @Test
    @DisplayName("Тест валидации FunctionDTO с пустым name")
    void testIsValidEmptyName() {
        FunctionDTO dto = new FunctionDTO(1L, 10L, "", "linear");
        
        assertFalse(FunctionMapper.isValid(dto));
    }
    
    @Test
    @DisplayName("Тест валидации FunctionDTO с null type")
    void testIsValidNullType() {
        FunctionDTO dto = new FunctionDTO(1L, 10L, "Linear Function", null);
        
        assertFalse(FunctionMapper.isValid(dto));
    }
    
    @Test
    @DisplayName("Тест валидации FunctionDTO с пустым type")
    void testIsValidEmptyType() {
        FunctionDTO dto = new FunctionDTO(1L, 10L, "Linear Function", "");
        
        assertFalse(FunctionMapper.isValid(dto));
    }
    
    @Test
    @DisplayName("Тест валидации FunctionDTO с null userId")
    void testIsValidNullUserId() {
        FunctionDTO dto = new FunctionDTO(1L, null, "Linear Function", "linear");
        
        assertFalse(FunctionMapper.isValid(dto));
    }
    
    @Test
    @DisplayName("Тест валидации null FunctionDTO")
    void testIsValidNullDTO() {
        assertFalse(FunctionMapper.isValid(null));
    }
    
    @Test
    @DisplayName("Тест equals и hashCode")
    void testEqualsAndHashCode() {
        FunctionDTO dto1 = new FunctionDTO(1L, 10L, "Linear Function", "linear");
        FunctionDTO dto2 = new FunctionDTO(1L, 10L, "Linear Function", "linear");
        FunctionDTO dto3 = new FunctionDTO(2L, 10L, "Linear Function", "linear");
        
        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }
}

