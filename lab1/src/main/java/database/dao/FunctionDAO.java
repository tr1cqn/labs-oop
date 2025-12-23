package database.dao;

import database.ConnectionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO для работы с таблицей functions
 * Использует SQL запросы из 06_crud_functions.sql
 */
public class FunctionDAO {
    private static final Logger logger = LogManager.getLogger(FunctionDAO.class);
    
    // SQL запросы из 06_crud_functions.sql
    private static final String SELECT_ALL = "SELECT id, user_id, name, type FROM functions";
    private static final String SELECT_BY_ID = "SELECT id, user_id, name, type FROM functions WHERE id = ?";
    private static final String SELECT_BY_USER_ID = "SELECT id, user_id, name, type FROM functions WHERE user_id = ?";
    private static final String SELECT_BY_TYPE = "SELECT id, user_id, name, type FROM functions WHERE type = ?";
    private static final String SELECT_BY_NAME = "SELECT id, user_id, name, type FROM functions WHERE name = ?";
    private static final String SELECT_BY_NAME_LIKE = "SELECT id, user_id, name, type FROM functions WHERE name LIKE ?";
    private static final String SELECT_BY_USER_AND_TYPE = "SELECT id, user_id, name, type FROM functions WHERE user_id = ? AND type = ?";
    private static final String COUNT_BY_USER_ID = "SELECT COUNT(*) as function_count FROM functions WHERE user_id = ?";
    private static final String INSERT = "INSERT INTO functions (user_id, name, type) VALUES (?, ?, ?)";
    private static final String UPDATE_NAME = "UPDATE functions SET name = ? WHERE id = ?";
    private static final String UPDATE_TYPE = "UPDATE functions SET type = ? WHERE id = ?";
    private static final String UPDATE_NAME_AND_TYPE = "UPDATE functions SET name = ?, type = ? WHERE id = ?";
    private static final String UPDATE_ALL_BY_USER = "UPDATE functions SET type = ? WHERE user_id = ?";
    private static final String DELETE_BY_ID = "DELETE FROM functions WHERE id = ?";
    private static final String DELETE_BY_USER_ID = "DELETE FROM functions WHERE user_id = ?";
    private static final String DELETE_BY_TYPE = "DELETE FROM functions WHERE type = ?";
    private static final String DELETE_BY_TYPE_AND_USER_ID = "DELETE FROM functions WHERE user_id = ? AND type = ?";
    
    /**
     * Находит все функции
     */
    public List<Function> findAll() throws SQLException {
        logger.debug("Поиск всех функций");
        List<Function> functions = new ArrayList<>();
        try (Connection connection = ConnectionManager.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(SELECT_ALL)) {
            
            while (rs.next()) {
                functions.add(new Function(
                    rs.getLong("id"),
                    rs.getLong("user_id"),
                    rs.getString("name"),
                    rs.getString("type")
                ));
            }
            logger.info("Найдено функций: {}", functions.size());
            return functions;
        } catch (SQLException e) {
            logger.error("Ошибка при поиске всех функций: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Находит функцию по ID
     */
    public Optional<Function> findById(Long id) throws SQLException {
        logger.debug("Поиск функции по ID: {}", id);
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_ID)) {
            
            statement.setLong(1, id);
            ResultSet rs = statement.executeQuery();
            
            if (rs.next()) {
                Function function = new Function(rs.getLong("id"), rs.getLong("user_id"), 
                                                rs.getString("name"), rs.getString("type"));
                logger.debug("Функция найдена: {}", function);
                return Optional.of(function);
            }
            logger.debug("Функция с ID {} не найдена", id);
            return Optional.empty();
        } catch (SQLException e) {
            logger.error("Ошибка при поиске функции по ID: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Находит все функции пользователя
     */
    public List<Function> findByUserId(Long userId) throws SQLException {
        logger.debug("Поиск функций пользователя с ID: {}", userId);
        List<Function> functions = new ArrayList<>();
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_USER_ID)) {
            
            statement.setLong(1, userId);
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                functions.add(new Function(rs.getLong("id"), rs.getLong("user_id"), 
                                         rs.getString("name"), rs.getString("type")));
            }
            logger.info("Найдено функций для пользователя {}: {}", userId, functions.size());
            return functions;
        } catch (SQLException e) {
            logger.error("Ошибка при поиске функций пользователя: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Находит функции по типу
     */
    public List<Function> findByType(String type) throws SQLException {
        logger.debug("Поиск функций по типу: {}", type);
        List<Function> functions = new ArrayList<>();
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_TYPE)) {
            
            statement.setString(1, type);
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                functions.add(new Function(rs.getLong("id"), rs.getLong("user_id"), 
                                         rs.getString("name"), rs.getString("type")));
            }
            logger.info("Найдено функций типа {}: {}", type, functions.size());
            return functions;
        } catch (SQLException e) {
            logger.error("Ошибка при поиске функций по типу: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Находит функции по имени (точное совпадение)
     */
    public List<Function> findByName(String name) throws SQLException {
        logger.debug("Поиск функций по имени: {}", name);
        List<Function> functions = new ArrayList<>();
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_NAME)) {
            
            statement.setString(1, name);
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                functions.add(new Function(rs.getLong("id"), rs.getLong("user_id"), 
                                         rs.getString("name"), rs.getString("type")));
            }
            logger.info("Найдено функций с именем {}: {}", name, functions.size());
            return functions;
        } catch (SQLException e) {
            logger.error("Ошибка при поиске функций по имени: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Находит функции по частичному совпадению имени
     */
    public List<Function> findByNameLike(String pattern) throws SQLException {
        logger.debug("Поиск функций по имени LIKE: {}", pattern);
        List<Function> functions = new ArrayList<>();
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_NAME_LIKE)) {
            
            statement.setString(1, pattern);
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                functions.add(new Function(rs.getLong("id"), rs.getLong("user_id"), 
                                         rs.getString("name"), rs.getString("type")));
            }
            logger.info("Найдено функций по паттерну {}: {}", pattern, functions.size());
            return functions;
        } catch (SQLException e) {
            logger.error("Ошибка при поиске функций по паттерну: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Находит функции пользователя определенного типа
     */
    public List<Function> findByUserIdAndType(Long userId, String type) throws SQLException {
        logger.debug("Поиск функций пользователя {} типа {}", userId, type);
        List<Function> functions = new ArrayList<>();
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_USER_AND_TYPE)) {
            
            statement.setLong(1, userId);
            statement.setString(2, type);
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                functions.add(new Function(rs.getLong("id"), rs.getLong("user_id"), 
                                         rs.getString("name"), rs.getString("type")));
            }
            logger.info("Найдено функций пользователя {} типа {}: {}", userId, type, functions.size());
            return functions;
        } catch (SQLException e) {
            logger.error("Ошибка при поиске функций пользователя и типа: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Подсчитывает количество функций пользователя
     */
    public int countByUserId(Long userId) throws SQLException {
        logger.debug("Подсчет количества функций пользователя: {}", userId);
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(COUNT_BY_USER_ID)) {
            
            statement.setLong(1, userId);
            ResultSet rs = statement.executeQuery();
            
            if (rs.next()) {
                int count = rs.getInt("function_count");
                logger.info("Количество функций пользователя {}: {}", userId, count);
                return count;
            }
            return 0;
        } catch (SQLException e) {
            logger.error("Ошибка при подсчете функций пользователя: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Добавляет новую функцию
     * @return ID созданной функции
     */
    public Long insert(Long userId, String name, String type) throws SQLException {
        logger.info("Вставка новой функции: name={}, type={}, userId={}", name, type, userId);
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            
            statement.setLong(1, userId);
            statement.setString(2, name);
            statement.setString(3, type);
            
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                ResultSet rs = statement.getGeneratedKeys();
                if (rs.next()) {
                    Long id = rs.getLong(1);
                    logger.info("Функция успешно создана с ID: {}", id);
                    return id;
                }
            }
            throw new SQLException("Не удалось получить ID созданной функции");
        } catch (SQLException e) {
            logger.error("Ошибка при вставке функции: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Обновляет имя функции
     */
    public boolean updateName(Long id, String name) throws SQLException {
        logger.info("Обновление имени функции с ID: {}", id);
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_NAME)) {
            
            statement.setString(1, name);
            statement.setLong(2, id);
            
            int rowsAffected = statement.executeUpdate();
            boolean updated = rowsAffected > 0;
            logger.info("Имя функции обновлено: {}, затронуто строк: {}", updated, rowsAffected);
            return updated;
        } catch (SQLException e) {
            logger.error("Ошибка при обновлении имени функции: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Обновляет тип функции
     */
    public boolean updateType(Long id, String type) throws SQLException {
        logger.info("Обновление типа функции с ID: {}", id);
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_TYPE)) {
            
            statement.setString(1, type);
            statement.setLong(2, id);
            
            int rowsAffected = statement.executeUpdate();
            boolean updated = rowsAffected > 0;
            logger.info("Тип функции обновлен: {}, затронуто строк: {}", updated, rowsAffected);
            return updated;
        } catch (SQLException e) {
            logger.error("Ошибка при обновлении типа функции: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Обновляет имя и тип функции
     */
    public boolean update(Long id, String name, String type) throws SQLException {
        logger.info("Обновление имени и типа функции с ID: {}", id);
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_NAME_AND_TYPE)) {
            
            statement.setString(1, name);
            statement.setString(2, type);
            statement.setLong(3, id);
            
            int rowsAffected = statement.executeUpdate();
            boolean updated = rowsAffected > 0;
            logger.info("Функция обновлена: {}, затронуто строк: {}", updated, rowsAffected);
            return updated;
        } catch (SQLException e) {
            logger.error("Ошибка при обновлении функции: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Обновляет все функции пользователя
     */
    public int updateAllByUserId(Long userId, String type) throws SQLException {
        logger.info("Обновление всех функций пользователя {} на тип {}", userId, type);
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_ALL_BY_USER)) {
            
            statement.setString(1, type);
            statement.setLong(2, userId);
            
            int rowsAffected = statement.executeUpdate();
            logger.info("Обновлено функций пользователя {}: {}", userId, rowsAffected);
            return rowsAffected;
        } catch (SQLException e) {
            logger.error("Ошибка при обновлении функций пользователя: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Удаляет функцию по ID
     */
    public boolean deleteById(Long id) throws SQLException {
        logger.info("Удаление функции с ID: {}", id);
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_BY_ID)) {
            
            statement.setLong(1, id);
            int rowsAffected = statement.executeUpdate();
            boolean deleted = rowsAffected > 0;
            logger.info("Функция удалена: {}, затронуто строк: {}", deleted, rowsAffected);
            return deleted;
        } catch (SQLException e) {
            logger.error("Ошибка при удалении функции: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Удаляет все функции пользователя
     */
    public int deleteByUserId(Long userId) throws SQLException {
        logger.info("Удаление всех функций пользователя: {}", userId);
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_BY_USER_ID)) {
            
            statement.setLong(1, userId);
            int rowsAffected = statement.executeUpdate();
            logger.info("Удалено функций пользователя {}: {}", userId, rowsAffected);
            return rowsAffected;
        } catch (SQLException e) {
            logger.error("Ошибка при удалении функций пользователя: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Удаляет функции определенного типа
     */
    public int deleteByType(String type) throws SQLException {
        logger.info("Удаление функций типа: {}", type);
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_BY_TYPE)) {
            
            statement.setString(1, type);
            int rowsAffected = statement.executeUpdate();
            logger.info("Удалено функций типа {}: {}", type, rowsAffected);
            return rowsAffected;
        } catch (SQLException e) {
            logger.error("Ошибка при удалении функций по типу: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Удаляет функции определенного типа у конкретного пользователя (для USER роли)
     */
    public int deleteByTypeAndUserId(Long userId, String type) throws SQLException {
        logger.info("Удаление функций типа {} у пользователя {}", type, userId);
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_BY_TYPE_AND_USER_ID)) {
            statement.setLong(1, userId);
            statement.setString(2, type);
            int rowsAffected = statement.executeUpdate();
            logger.info("Удалено функций типа {} у пользователя {}: {}", type, userId, rowsAffected);
            return rowsAffected;
        } catch (SQLException e) {
            logger.error("Ошибка при удалении функций по типу и пользователю: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Класс для представления функции
     */
    public static class Function {
        private final Long id;
        private final Long userId;
        private final String name;
        private final String type;
        
        public Function(Long id, Long userId, String name, String type) {
            this.id = id;
            this.userId = userId;
            this.name = name;
            this.type = type;
        }
        
        public Long getId() { return id; }
        public Long getUserId() { return userId; }
        public String getName() { return name; }
        public String getType() { return type; }
        
        @Override
        public String toString() {
            return "Function{id=" + id + ", userId=" + userId + ", name='" + name + "', type='" + type + "'}";
        }
    }
}

