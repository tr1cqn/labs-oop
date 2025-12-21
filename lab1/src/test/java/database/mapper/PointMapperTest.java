package database.mapper;

import database.dto.PointDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для PointMapper
 */
public class PointMapperTest {
    
    @Test
    @DisplayName("Тест трансформации отдельных полей в PointDTO")
    void testToDTOFromFields() {
        PointDTO dto = PointMapper.toDTO(1L, 10L, 5.5, 10.2);
        
        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals(10L, dto.getFuncId());
        assertEquals(5.5, dto.getXValue(), 0.0001);
        assertEquals(10.2, dto.getYValue(), 0.0001);
    }
    
    @Test
    @DisplayName("Тест трансформации с отрицательными значениями")
    void testToDTOWithNegativeValues() {
        PointDTO dto = PointMapper.toDTO(1L, 10L, -5.5, -10.2);
        
        assertNotNull(dto);
        assertEquals(-5.5, dto.getXValue(), 0.0001);
        assertEquals(-10.2, dto.getYValue(), 0.0001);
    }
    
    @Test
    @DisplayName("Тест трансформации с нулевыми значениями")
    void testToDTOWithZeroValues() {
        PointDTO dto = PointMapper.toDTO(1L, 10L, 0.0, 0.0);
        
        assertNotNull(dto);
        assertEquals(0.0, dto.getXValue(), 0.0001);
        assertEquals(0.0, dto.getYValue(), 0.0001);
    }
    
    @Test
    @DisplayName("Тест трансформации с большими значениями")
    void testToDTOWithLargeValues() {
        PointDTO dto = PointMapper.toDTO(1L, 10L, 1000.0, 2000.0);
        
        assertNotNull(dto);
        assertEquals(1000.0, dto.getXValue(), 0.0001);
        assertEquals(2000.0, dto.getYValue(), 0.0001);
    }
    
    @Test
    @DisplayName("Тест трансформации с малыми значениями")
    void testToDTOWithSmallValues() {
        PointDTO dto = PointMapper.toDTO(1L, 10L, 0.0001, 0.0002);
        
        assertNotNull(dto);
        assertEquals(0.0001, dto.getXValue(), 0.0001);
        assertEquals(0.0002, dto.getYValue(), 0.0001);
    }
    
    @Test
    @DisplayName("Тест трансформации с дробными значениями")
    void testToDTOWithFractionalValues() {
        PointDTO dto = PointMapper.toDTO(1L, 10L, 3.14159, 2.71828);
        
        assertNotNull(dto);
        assertEquals(3.14159, dto.getXValue(), 0.0001);
        assertEquals(2.71828, dto.getYValue(), 0.0001);
    }
    
    @Test
    @DisplayName("Тест валидации валидного PointDTO")
    void testIsValidValidDTO() {
        PointDTO dto = new PointDTO(1L, 10L, 5.5, 10.2);
        
        assertTrue(PointMapper.isValid(dto));
    }
    
    @Test
    @DisplayName("Тест валидации PointDTO с null xValue")
    void testIsValidNullXValue() {
        PointDTO dto = new PointDTO(1L, 10L, null, 10.2);
        
        assertFalse(PointMapper.isValid(dto));
    }
    
    @Test
    @DisplayName("Тест валидации PointDTO с null yValue")
    void testIsValidNullYValue() {
        PointDTO dto = new PointDTO(1L, 10L, 5.5, null);
        
        assertFalse(PointMapper.isValid(dto));
    }
    
    @Test
    @DisplayName("Тест валидации PointDTO с null funcId")
    void testIsValidNullFuncId() {
        PointDTO dto = new PointDTO(1L, null, 5.5, 10.2);
        
        assertFalse(PointMapper.isValid(dto));
    }
    
    @Test
    @DisplayName("Тест валидации PointDTO с NaN значениями")
    void testIsValidNaNValues() {
        PointDTO dto = new PointDTO(1L, 10L, Double.NaN, 10.2);
        
        assertFalse(PointMapper.isValid(dto));
    }
    
    @Test
    @DisplayName("Тест валидации PointDTO с Infinite значениями")
    void testIsValidInfiniteValues() {
        PointDTO dto = new PointDTO(1L, 10L, Double.POSITIVE_INFINITY, 10.2);
        
        assertFalse(PointMapper.isValid(dto));
    }
    
    @Test
    @DisplayName("Тест валидации null PointDTO")
    void testIsValidNullDTO() {
        assertFalse(PointMapper.isValid(null));
    }
    
    @Test
    @DisplayName("Тест equals и hashCode")
    void testEqualsAndHashCode() {
        PointDTO dto1 = new PointDTO(1L, 10L, 5.5, 10.2);
        PointDTO dto2 = new PointDTO(1L, 10L, 5.5, 10.2);
        PointDTO dto3 = new PointDTO(2L, 10L, 5.5, 10.2);
        
        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }
}

