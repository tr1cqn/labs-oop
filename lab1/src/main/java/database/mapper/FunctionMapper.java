package database.mapper;

import database.dto.FunctionDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Класс для трансформации данных таблицы functions в FunctionDTO
 */
public class FunctionMapper {
    private static final Logger logger = LogManager.getLogger(FunctionMapper.class);
    
    /**
     * Преобразует ResultSet в FunctionDTO
     */
    public static FunctionDTO toDTO(ResultSet rs) throws SQLException {
        logger.debug("Начало трансформации ResultSet в FunctionDTO");
        
        try {
            Long id = rs.getLong("id");
            Long userId = rs.getLong("user_id");
            String name = rs.getString("name");
            String type = rs.getString("type");
            
            FunctionDTO dto = new FunctionDTO(id, userId, name, type);
            
            logger.debug("Успешно преобразован FunctionDTO: id={}, name={}, type={}", id, name, type);
            return dto;
        } catch (SQLException e) {
            logger.error("Ошибка при трансформации ResultSet в FunctionDTO", e);
            throw e;
        }
    }
    
    /**
     * Преобразует отдельные поля в FunctionDTO
     */
    public static FunctionDTO toDTO(Long id, Long userId, String name, String type) {
        logger.debug("Создание FunctionDTO из отдельных полей: id={}, name={}, type={}", id, name, type);
        
        FunctionDTO dto = new FunctionDTO(id, userId, name, type);
        
        logger.debug("FunctionDTO успешно создан");
        return dto;
    }
    
    /**
     * Проверяет валидность FunctionDTO
     */
    public static boolean isValid(FunctionDTO dto) {
        if (dto == null) {
            logger.warn("Попытка валидации null FunctionDTO");
            return false;
        }
        
        boolean valid = dto.getName() != null && !dto.getName().isEmpty() &&
                       dto.getType() != null && !dto.getType().isEmpty() &&
                       dto.getUserId() != null;
        
        if (!valid) {
            logger.warn("FunctionDTO не прошел валидацию: name={}, type={}", dto.getName(), dto.getType());
        } else {
            logger.debug("FunctionDTO прошел валидацию: id={}, name={}", dto.getId(), dto.getName());
        }
        
        return valid;
    }
}

