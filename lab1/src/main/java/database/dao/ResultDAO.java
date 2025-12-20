package database.dao;

import database.ConnectionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO для работы с таблицей result
 * Использует SQL запросы из 08_crud_result.sql
 */
public class ResultDAO {
    private static final Logger logger = LogManager.getLogger(ResultDAO.class);
    
    // SQL запросы из 08_crud_result.sql
    private static final String SELECT_ALL = "SELECT id, result_id, result FROM result";
    private static final String SELECT_BY_ID = "SELECT id, result_id, result FROM result WHERE id = ?";
    private static final String SELECT_BY_FUNCTION_ID = "SELECT id, result_id, result FROM result WHERE result_id = ?";
    private static final String SELECT_LAST_BY_FUNCTION_ID = "SELECT id, result_id, result FROM result WHERE result_id = ? ORDER BY id DESC LIMIT 1";
    private static final String SELECT_BY_FUNCTION_ID_ORDERED = "SELECT id, result_id, result FROM result WHERE result_id = ? ORDER BY id ASC";
    private static final String COUNT_BY_FUNCTION_ID = "SELECT COUNT(*) as result_count FROM result WHERE result_id = ?";
    private static final String SELECT_BY_RESULT_LIKE = "SELECT id, result_id, result FROM result WHERE result LIKE ?";
    private static final String SELECT_BY_FUNCTION_AND_RESULT_LIKE = "SELECT id, result_id, result FROM result WHERE result_id = ? AND result LIKE ?";
    private static final String INSERT = "INSERT INTO result (result_id, result) VALUES (?, ?)";
    private static final String INSERT_MULTIPLE = "INSERT INTO result (result_id, result) VALUES (?, ?), (?, ?), (?, ?)";
    private static final String UPDATE_BY_ID = "UPDATE result SET result = ? WHERE id = ?";
    private static final String UPDATE_ALL_BY_FUNCTION = "UPDATE result SET result = ? WHERE result_id = ?";
    private static final String UPDATE_LAST_BY_FUNCTION = "UPDATE result SET result = ? WHERE result_id = ? AND id = (SELECT MAX(id) FROM result WHERE result_id = ?)";
    private static final String DELETE_BY_ID = "DELETE FROM result WHERE id = ?";
    private static final String DELETE_BY_FUNCTION_ID = "DELETE FROM result WHERE result_id = ?";
    private static final String DELETE_LAST_BY_FUNCTION = "DELETE FROM result WHERE result_id = ? AND id = (SELECT MAX(id) FROM result WHERE result_id = ?)";
    private static final String DELETE_BY_FUNCTION_AND_RESULT_LIKE = "DELETE FROM result WHERE result_id = ? AND result LIKE ?";
    
    /**
     * Находит все результаты
     */
    public List<Result> findAll() throws SQLException {
        logger.debug("Поиск всех результатов");
        List<Result> results = new ArrayList<>();
        try (Connection connection = ConnectionManager.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(SELECT_ALL)) {
            
            while (rs.next()) {
                results.add(new Result(rs.getLong("id"), rs.getLong("result_id"), rs.getString("result")));
            }
            logger.info("Найдено результатов: {}", results.size());
            return results;
        } catch (SQLException e) {
            logger.error("Ошибка при поиске всех результатов: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Находит результат по ID
     */
    public Optional<Result> findById(Long id) throws SQLException {
        logger.debug("Поиск результата по ID: {}", id);
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_ID)) {
            
            statement.setLong(1, id);
            ResultSet rs = statement.executeQuery();
            
            if (rs.next()) {
                Result result = new Result(rs.getLong("id"), rs.getLong("result_id"), rs.getString("result"));
                logger.debug("Результат найден: {}", result);
                return Optional.of(result);
            }
            logger.debug("Результат с ID {} не найден", id);
            return Optional.empty();
        } catch (SQLException e) {
            logger.error("Ошибка при поиске результата по ID: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Находит все результаты функции
     */
    public List<Result> findByFunctionId(Long functionId) throws SQLException {
        logger.debug("Поиск результатов функции с ID: {}", functionId);
        List<Result> results = new ArrayList<>();
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_FUNCTION_ID)) {
            
            statement.setLong(1, functionId);
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                results.add(new Result(rs.getLong("id"), rs.getLong("result_id"), rs.getString("result")));
            }
            logger.info("Найдено результатов для функции {}: {}", functionId, results.size());
            return results;
        } catch (SQLException e) {
            logger.error("Ошибка при поиске результатов функции: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Находит последний результат функции (по ID)
     */
    public Optional<Result> findLastByFunctionId(Long functionId) throws SQLException {
        logger.debug("Поиск последнего результата функции: {}", functionId);
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_LAST_BY_FUNCTION_ID)) {
            
            statement.setLong(1, functionId);
            ResultSet rs = statement.executeQuery();
            
            if (rs.next()) {
                Result result = new Result(rs.getLong("id"), rs.getLong("result_id"), rs.getString("result"));
                logger.debug("Последний результат найден: {}", result);
                return Optional.of(result);
            }
            logger.debug("Последний результат функции {} не найден", functionId);
            return Optional.empty();
        } catch (SQLException e) {
            logger.error("Ошибка при поиске последнего результата: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Находит результаты функции, отсортированные по ID
     */
    public List<Result> findByFunctionIdOrdered(Long functionId) throws SQLException {
        logger.debug("Поиск результатов функции {} отсортированных по ID", functionId);
        List<Result> results = new ArrayList<>();
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_FUNCTION_ID_ORDERED)) {
            
            statement.setLong(1, functionId);
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                results.add(new Result(rs.getLong("id"), rs.getLong("result_id"), rs.getString("result")));
            }
            logger.info("Найдено отсортированных результатов для функции {}: {}", functionId, results.size());
            return results;
        } catch (SQLException e) {
            logger.error("Ошибка при поиске отсортированных результатов: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Подсчитывает количество результатов функции
     */
    public int countByFunctionId(Long functionId) throws SQLException {
        logger.debug("Подсчет количества результатов функции: {}", functionId);
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(COUNT_BY_FUNCTION_ID)) {
            
            statement.setLong(1, functionId);
            ResultSet rs = statement.executeQuery();
            
            if (rs.next()) {
                int count = rs.getInt("result_count");
                logger.info("Количество результатов функции {}: {}", functionId, count);
                return count;
            }
            return 0;
        } catch (SQLException e) {
            logger.error("Ошибка при подсчете результатов функции: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Находит результаты по частичному совпадению текста
     */
    public List<Result> findByResultLike(String pattern) throws SQLException {
        logger.debug("Поиск результатов по тексту LIKE: {}", pattern);
        List<Result> results = new ArrayList<>();
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_RESULT_LIKE)) {
            
            statement.setString(1, pattern);
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                results.add(new Result(rs.getLong("id"), rs.getLong("result_id"), rs.getString("result")));
            }
            logger.info("Найдено результатов по паттерну {}: {}", pattern, results.size());
            return results;
        } catch (SQLException e) {
            logger.error("Ошибка при поиске результатов по паттерну: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Находит результаты функции по частичному совпадению текста
     */
    public List<Result> findByFunctionIdAndResultLike(Long functionId, String pattern) throws SQLException {
        logger.debug("Поиск результатов функции {} по тексту LIKE: {}", functionId, pattern);
        List<Result> results = new ArrayList<>();
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_FUNCTION_AND_RESULT_LIKE)) {
            
            statement.setLong(1, functionId);
            statement.setString(2, pattern);
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                results.add(new Result(rs.getLong("id"), rs.getLong("result_id"), rs.getString("result")));
            }
            logger.info("Найдено результатов функции {} по паттерну {}: {}", functionId, pattern, results.size());
            return results;
        } catch (SQLException e) {
            logger.error("Ошибка при поиске результатов функции по паттерну: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Добавляет новый результат
     * @return ID созданного результата
     */
    public Long insert(Long functionId, String result) throws SQLException {
        logger.info("Вставка нового результата для функции: {}", functionId);
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            
            statement.setLong(1, functionId);
            statement.setString(2, result);
            
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                ResultSet rs = statement.getGeneratedKeys();
                if (rs.next()) {
                    Long id = rs.getLong(1);
                    logger.info("Результат успешно создан с ID: {}", id);
                    return id;
                }
            }
            throw new SQLException("Не удалось получить ID созданного результата");
        } catch (SQLException e) {
            logger.error("Ошибка при вставке результата: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Добавляет несколько результатов одной функцией
     */
    public void insertMultiple(Long functionId, String result1, String result2, String result3) throws SQLException {
        logger.info("Вставка нескольких результатов для функции: {}", functionId);
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_MULTIPLE)) {
            
            statement.setLong(1, functionId);
            statement.setString(2, result1);
            statement.setLong(3, functionId);
            statement.setString(4, result2);
            statement.setLong(5, functionId);
            statement.setString(6, result3);
            
            int rowsAffected = statement.executeUpdate();
            logger.info("Вставлено результатов: {}", rowsAffected);
        } catch (SQLException e) {
            logger.error("Ошибка при вставке нескольких результатов: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Обновляет результат по ID
     */
    public boolean updateById(Long id, String result) throws SQLException {
        logger.info("Обновление результата с ID: {}", id);
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_BY_ID)) {
            
            statement.setString(1, result);
            statement.setLong(2, id);
            
            int rowsAffected = statement.executeUpdate();
            boolean updated = rowsAffected > 0;
            logger.info("Результат обновлен: {}, затронуто строк: {}", updated, rowsAffected);
            return updated;
        } catch (SQLException e) {
            logger.error("Ошибка при обновлении результата: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Обновляет все результаты функции
     */
    public int updateAllByFunctionId(Long functionId, String result) throws SQLException {
        logger.info("Обновление всех результатов функции: {}", functionId);
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_ALL_BY_FUNCTION)) {
            
            statement.setString(1, result);
            statement.setLong(2, functionId);
            
            int rowsAffected = statement.executeUpdate();
            logger.info("Обновлено результатов функции {}: {}", functionId, rowsAffected);
            return rowsAffected;
        } catch (SQLException e) {
            logger.error("Ошибка при обновлении всех результатов функции: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Обновляет последний результат функции
     */
    public boolean updateLastByFunctionId(Long functionId, String result) throws SQLException {
        logger.info("Обновление последнего результата функции: {}", functionId);
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_LAST_BY_FUNCTION)) {
            
            statement.setString(1, result);
            statement.setLong(2, functionId);
            statement.setLong(3, functionId);
            
            int rowsAffected = statement.executeUpdate();
            boolean updated = rowsAffected > 0;
            logger.info("Последний результат обновлен: {}, затронуто строк: {}", updated, rowsAffected);
            return updated;
        } catch (SQLException e) {
            logger.error("Ошибка при обновлении последнего результата: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Удаляет результат по ID
     */
    public boolean deleteById(Long id) throws SQLException {
        logger.info("Удаление результата с ID: {}", id);
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_BY_ID)) {
            
            statement.setLong(1, id);
            int rowsAffected = statement.executeUpdate();
            boolean deleted = rowsAffected > 0;
            logger.info("Результат удален: {}, затронуто строк: {}", deleted, rowsAffected);
            return deleted;
        } catch (SQLException e) {
            logger.error("Ошибка при удалении результата: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Удаляет все результаты функции
     */
    public int deleteByFunctionId(Long functionId) throws SQLException {
        logger.info("Удаление всех результатов функции: {}", functionId);
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_BY_FUNCTION_ID)) {
            
            statement.setLong(1, functionId);
            int rowsAffected = statement.executeUpdate();
            logger.info("Удалено результатов функции {}: {}", functionId, rowsAffected);
            return rowsAffected;
        } catch (SQLException e) {
            logger.error("Ошибка при удалении результатов функции: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Удаляет последний результат функции
     */
    public boolean deleteLastByFunctionId(Long functionId) throws SQLException {
        logger.info("Удаление последнего результата функции: {}", functionId);
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_LAST_BY_FUNCTION)) {
            
            statement.setLong(1, functionId);
            statement.setLong(2, functionId);
            int rowsAffected = statement.executeUpdate();
            boolean deleted = rowsAffected > 0;
            logger.info("Последний результат удален: {}, затронуто строк: {}", deleted, rowsAffected);
            return deleted;
        } catch (SQLException e) {
            logger.error("Ошибка при удалении последнего результата: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Удаляет результаты функции по частичному совпадению текста
     */
    public int deleteByFunctionIdAndResultLike(Long functionId, String pattern) throws SQLException {
        logger.info("Удаление результатов функции {} по тексту LIKE: {}", functionId, pattern);
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_BY_FUNCTION_AND_RESULT_LIKE)) {
            
            statement.setLong(1, functionId);
            statement.setString(2, pattern);
            int rowsAffected = statement.executeUpdate();
            logger.info("Удалено результатов по паттерну {}: {}", pattern, rowsAffected);
            return rowsAffected;
        } catch (SQLException e) {
            logger.error("Ошибка при удалении результатов по паттерну: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Класс для представления результата
     */
    public static class Result {
        private final Long id;
        private final Long functionId;
        private final String result;
        
        public Result(Long id, Long functionId, String result) {
            this.id = id;
            this.functionId = functionId;
            this.result = result;
        }
        
        public Long getId() { return id; }
        public Long getFunctionId() { return functionId; }
        public String getResult() { return result; }
        
        @Override
        public String toString() {
            return "Result{id=" + id + ", functionId=" + functionId + ", result='" + result + "'}";
        }
    }
}

