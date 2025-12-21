package database.mapper;

import database.dto.PointDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Класс для трансформации данных таблицы points в PointDTO
 */
public class PointMapper {
    private static final Logger logger = LogManager.getLogger(PointMapper.class);
    
    /**
     * Преобразует ResultSet в PointDTO
     */
    public static PointDTO toDTO(ResultSet rs) throws SQLException {
        logger.debug("Начало трансформации ResultSet в PointDTO");
        
        try {
            Long id = rs.getLong("id");
            Long funcId = rs.getLong("func_id");
            Double xValue = rs.getDouble("x_value");
            Double yValue = rs.getDouble("y_value");
            
            PointDTO dto = new PointDTO(id, funcId, xValue, yValue);
            
            logger.debug("Успешно преобразован PointDTO: id={}, funcId={}, x={}, y={}", 
                id, funcId, xValue, yValue);
            return dto;
        } catch (SQLException e) {
            logger.error("Ошибка при трансформации ResultSet в PointDTO", e);
            throw e;
        }
    }
    
    /**
     * Преобразует отдельные поля в PointDTO
     */
    public static PointDTO toDTO(Long id, Long funcId, Double xValue, Double yValue) {
        logger.debug("Создание PointDTO из отдельных полей: id={}, funcId={}, x={}, y={}", 
            id, funcId, xValue, yValue);
        
        PointDTO dto = new PointDTO(id, funcId, xValue, yValue);
        
        logger.debug("PointDTO успешно создан");
        return dto;
    }
    
    /**
     * Проверяет валидность PointDTO
     */
    public static boolean isValid(PointDTO dto) {
        if (dto == null) {
            logger.warn("Попытка валидации null PointDTO");
            return false;
        }
        
        boolean valid = dto.getXValue() != null && 
                       dto.getYValue() != null &&
                       dto.getFuncId() != null &&
                       !dto.getXValue().isNaN() &&
                       !dto.getYValue().isNaN() &&
                       !dto.getXValue().isInfinite() &&
                       !dto.getYValue().isInfinite();
        
        if (!valid) {
            logger.warn("PointDTO не прошел валидацию: x={}, y={}, funcId={}", 
                dto.getXValue(), dto.getYValue(), dto.getFuncId());
        } else {
            logger.debug("PointDTO прошел валидацию: id={}, x={}, y={}", 
                dto.getId(), dto.getXValue(), dto.getYValue());
        }
        
        return valid;
    }
}

