package database.mapper;

import database.dto.ResultDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Класс для трансформации данных таблицы result в ResultDTO
 */
public class ResultMapper {
    private static final Logger logger = LogManager.getLogger(ResultMapper.class);
    
    /**
     * Преобразует ResultSet в ResultDTO
     */
    public static ResultDTO toDTO(ResultSet rs) throws SQLException {
        logger.debug("Начало трансформации ResultSet в ResultDTO");
        
        try {
            Long id = rs.getLong("id");
            Long resultId = rs.getLong("result_id");
            String result = rs.getString("result");
            
            ResultDTO dto = new ResultDTO(id, resultId, result);
            
            logger.debug("Успешно преобразован ResultDTO: id={}, resultId={}", id, resultId);
            return dto;
        } catch (SQLException e) {
            logger.error("Ошибка при трансформации ResultSet в ResultDTO", e);
            throw e;
        }
    }
    
    /**
     * Преобразует отдельные поля в ResultDTO
     */
    public static ResultDTO toDTO(Long id, Long resultId, String result) {
        logger.debug("Создание ResultDTO из отдельных полей: id={}, resultId={}", id, resultId);
        
        ResultDTO dto = new ResultDTO(id, resultId, result);
        
        logger.debug("ResultDTO успешно создан");
        return dto;
    }
    
    /**
     * Проверяет валидность ResultDTO
     */
    public static boolean isValid(ResultDTO dto) {
        if (dto == null) {
            logger.warn("Попытка валидации null ResultDTO");
            return false;
        }
        
        boolean valid = dto.getResult() != null && !dto.getResult().isEmpty() &&
                       dto.getResultId() != null;
        
        if (!valid) {
            logger.warn("ResultDTO не прошел валидацию: resultId={}, result={}", 
                dto.getResultId(), dto.getResult());
        } else {
            logger.debug("ResultDTO прошел валидацию: id={}, resultId={}", 
                dto.getId(), dto.getResultId());
        }
        
        return valid;
    }
}

