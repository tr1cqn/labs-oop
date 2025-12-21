package database.mapper;

import database.dto.UserDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Класс для трансформации данных таблицы users в UserDTO
 */
public class UserMapper {
    private static final Logger logger = LogManager.getLogger(UserMapper.class);
    
    /**
     * Преобразует ResultSet в UserDTO
     */
    public static UserDTO toDTO(ResultSet rs) throws SQLException {
        logger.debug("Начало трансформации ResultSet в UserDTO");
        
        try {
            Long id = rs.getLong("id");
            String login = rs.getString("login");
            String password = rs.getString("password");
            String email = rs.getString("email");
            
            UserDTO dto = new UserDTO(id, login, password, email);
            
            logger.debug("Успешно преобразован UserDTO: id={}, login={}", id, login);
            return dto;
        } catch (SQLException e) {
            logger.error("Ошибка при трансформации ResultSet в UserDTO", e);
            throw e;
        }
    }
    
    /**
     * Преобразует отдельные поля в UserDTO
     */
    public static UserDTO toDTO(Long id, String login, String password, String email) {
        logger.debug("Создание UserDTO из отдельных полей: id={}, login={}", id, login);
        
        UserDTO dto = new UserDTO(id, login, password, email);
        
        logger.debug("UserDTO успешно создан");
        return dto;
    }
    
    /**
     * Проверяет валидность UserDTO
     */
    public static boolean isValid(UserDTO dto) {
        if (dto == null) {
            logger.warn("Попытка валидации null UserDTO");
            return false;
        }
        
        boolean valid = dto.getLogin() != null && !dto.getLogin().isEmpty() &&
                       dto.getPassword() != null && !dto.getPassword().isEmpty();
        
        if (!valid) {
            logger.warn("UserDTO не прошел валидацию: login={}", dto.getLogin());
        } else {
            logger.debug("UserDTO прошел валидацию: id={}, login={}", dto.getId(), dto.getLogin());
        }
        
        return valid;
    }
}

