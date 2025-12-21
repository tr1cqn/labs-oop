package database.mapper;

import database.dto.ResultDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для ResultMapper
 */
public class ResultMapperTest {
    
    @Test
    @DisplayName("Тест трансформации отдельных полей в ResultDTO")
    void testToDTOFromFields() {
        ResultDTO dto = ResultMapper.toDTO(1L, 10L, "Computation result: 42.5");
        
        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals(10L, dto.getResultId());
        assertEquals("Computation result: 42.5", dto.getResult());
    }
    
    @Test
    @DisplayName("Тест трансформации с длинным текстом")
    void testToDTOWithLongText() {
        String longText = "A".repeat(1000);
        ResultDTO dto = ResultMapper.toDTO(1L, 10L, longText);
        
        assertNotNull(dto);
        assertEquals(longText, dto.getResult());
    }
    
    @Test
    @DisplayName("Тест трансформации с коротким текстом")
    void testToDTOWithShortText() {
        ResultDTO dto = ResultMapper.toDTO(1L, 10L, "OK");
        
        assertNotNull(dto);
        assertEquals("OK", dto.getResult());
    }
    
    @Test
    @DisplayName("Тест трансформации с многострочным текстом")
    void testToDTOWithMultilineText() {
        String multilineText = "Line 1\nLine 2\nLine 3";
        ResultDTO dto = ResultMapper.toDTO(1L, 10L, multilineText);
        
        assertNotNull(dto);
        assertEquals(multilineText, dto.getResult());
    }
    
    @Test
    @DisplayName("Тест трансформации с специальными символами")
    void testToDTOWithSpecialChars() {
        String specialText = "Result: !@#$%^&*()_+-=[]{}|;':\",./<>?";
        ResultDTO dto = ResultMapper.toDTO(1L, 10L, specialText);
        
        assertNotNull(dto);
        assertEquals(specialText, dto.getResult());
    }
    
    @Test
    @DisplayName("Тест трансформации с числами в тексте")
    void testToDTOWithNumbersInText() {
        String textWithNumbers = "Result: 12345.67890";
        ResultDTO dto = ResultMapper.toDTO(1L, 10L, textWithNumbers);
        
        assertNotNull(dto);
        assertEquals(textWithNumbers, dto.getResult());
    }
    
    @Test
    @DisplayName("Тест валидации валидного ResultDTO")
    void testIsValidValidDTO() {
        ResultDTO dto = new ResultDTO(1L, 10L, "Computation result: 42.5");
        
        assertTrue(ResultMapper.isValid(dto));
    }
    
    @Test
    @DisplayName("Тест валидации ResultDTO с null result")
    void testIsValidNullResult() {
        ResultDTO dto = new ResultDTO(1L, 10L, null);
        
        assertFalse(ResultMapper.isValid(dto));
    }
    
    @Test
    @DisplayName("Тест валидации ResultDTO с пустым result")
    void testIsValidEmptyResult() {
        ResultDTO dto = new ResultDTO(1L, 10L, "");
        
        assertFalse(ResultMapper.isValid(dto));
    }
    
    @Test
    @DisplayName("Тест валидации ResultDTO с null resultId")
    void testIsValidNullResultId() {
        ResultDTO dto = new ResultDTO(1L, null, "Result");
        
        assertFalse(ResultMapper.isValid(dto));
    }
    
    @Test
    @DisplayName("Тест валидации null ResultDTO")
    void testIsValidNullDTO() {
        assertFalse(ResultMapper.isValid(null));
    }
    
    @Test
    @DisplayName("Тест equals и hashCode")
    void testEqualsAndHashCode() {
        ResultDTO dto1 = new ResultDTO(1L, 10L, "Result");
        ResultDTO dto2 = new ResultDTO(1L, 10L, "Result");
        ResultDTO dto3 = new ResultDTO(2L, 10L, "Result");
        
        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }
}


