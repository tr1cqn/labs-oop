package database.dao;

import database.ConnectionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO для работы с таблицей points
 * Использует SQL запросы из 07_crud_points.sql
 */
public class PointDAO {
    private static final Logger logger = LogManager.getLogger(PointDAO.class);
    
    // SQL запросы из 07_crud_points.sql
    private static final String SELECT_ALL = "SELECT id, func_id, x_value, y_value FROM points";
    private static final String SELECT_BY_ID = "SELECT id, func_id, x_value, y_value FROM points WHERE id = ?";
    private static final String SELECT_BY_FUNCTION_ID = "SELECT id, func_id, x_value, y_value FROM points WHERE func_id = ?";
    private static final String SELECT_BY_FUNCTION_ID_ORDERED = "SELECT id, func_id, x_value, y_value FROM points WHERE func_id = ? ORDER BY x_value ASC";
    private static final String SELECT_BY_FUNCTION_AND_X = "SELECT id, func_id, x_value, y_value FROM points WHERE func_id = ? AND x_value = ?";
    private static final String SELECT_BY_FUNCTION_AND_X_RANGE = "SELECT id, func_id, x_value, y_value FROM points WHERE func_id = ? AND x_value BETWEEN ? AND ? ORDER BY x_value ASC";
    private static final String SELECT_BY_FUNCTION_AND_Y = "SELECT id, func_id, x_value, y_value FROM points WHERE func_id = ? AND y_value = ?";
    private static final String SELECT_BY_FUNCTION_AND_Y_RANGE = "SELECT id, func_id, x_value, y_value FROM points WHERE func_id = ? AND y_value BETWEEN ? AND ? ORDER BY x_value ASC";
    private static final String COUNT_BY_FUNCTION_ID = "SELECT COUNT(*) as point_count FROM points WHERE func_id = ?";
    private static final String MIN_MAX_X_BY_FUNCTION = "SELECT MIN(x_value) as min_x, MAX(x_value) as max_x FROM points WHERE func_id = ?";
    private static final String INSERT = "INSERT INTO points (func_id, x_value, y_value) VALUES (?, ?, ?)";
    private static final String INSERT_MULTIPLE = "INSERT INTO points (func_id, x_value, y_value) VALUES (?, ?, ?), (?, ?, ?), (?, ?, ?)";
    private static final String UPDATE_Y_BY_ID = "UPDATE points SET y_value = ? WHERE id = ?";
    private static final String UPDATE_Y_BY_FUNCTION_AND_X = "UPDATE points SET y_value = ? WHERE func_id = ? AND x_value = ?";
    private static final String UPDATE_XY_BY_ID = "UPDATE points SET x_value = ?, y_value = ? WHERE id = ?";
    private static final String UPDATE_Y_MULTIPLY = "UPDATE points SET y_value = y_value * ? WHERE func_id = ?";
    private static final String DELETE_BY_ID = "DELETE FROM points WHERE id = ?";
    private static final String DELETE_BY_FUNCTION_AND_X = "DELETE FROM points WHERE func_id = ? AND x_value = ?";
    private static final String DELETE_BY_FUNCTION_ID = "DELETE FROM points WHERE func_id = ?";
    private static final String DELETE_BY_FUNCTION_AND_X_RANGE = "DELETE FROM points WHERE func_id = ? AND x_value BETWEEN ? AND ?";
    
    /**
     * Находит все точки
     */
    public List<Point> findAll() throws SQLException {
        logger.debug("Поиск всех точек");
        List<Point> points = new ArrayList<>();
        try (Connection connection = ConnectionManager.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(SELECT_ALL)) {
            
            while (rs.next()) {
                points.add(new Point(rs.getLong("id"), rs.getLong("func_id"), 
                                   rs.getDouble("x_value"), rs.getDouble("y_value")));
            }
            logger.info("Найдено точек: {}", points.size());
            return points;
        } catch (SQLException e) {
            logger.error("Ошибка при поиске всех точек: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Находит точку по ID
     */
    public Optional<Point> findById(Long id) throws SQLException {
        logger.debug("Поиск точки по ID: {}", id);
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_ID)) {
            
            statement.setLong(1, id);
            ResultSet rs = statement.executeQuery();
            
            if (rs.next()) {
                Point point = new Point(rs.getLong("id"), rs.getLong("func_id"), 
                                      rs.getDouble("x_value"), rs.getDouble("y_value"));
                logger.debug("Точка найдена: {}", point);
                return Optional.of(point);
            }
            logger.debug("Точка с ID {} не найдена", id);
            return Optional.empty();
        } catch (SQLException e) {
            logger.error("Ошибка при поиске точки по ID: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Находит все точки функции
     */
    public List<Point> findByFunctionId(Long functionId) throws SQLException {
        logger.debug("Поиск точек функции с ID: {}", functionId);
        List<Point> points = new ArrayList<>();
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_FUNCTION_ID)) {
            
            statement.setLong(1, functionId);
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                points.add(new Point(rs.getLong("id"), rs.getLong("func_id"), 
                                   rs.getDouble("x_value"), rs.getDouble("y_value")));
            }
            logger.info("Найдено точек для функции {}: {}", functionId, points.size());
            return points;
        } catch (SQLException e) {
            logger.error("Ошибка при поиске точек функции: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Находит точки функции, отсортированные по x_value
     */
    public List<Point> findByFunctionIdOrdered(Long functionId) throws SQLException {
        logger.debug("Поиск точек функции {} отсортированных по x_value", functionId);
        List<Point> points = new ArrayList<>();
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_FUNCTION_ID_ORDERED)) {
            
            statement.setLong(1, functionId);
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                points.add(new Point(rs.getLong("id"), rs.getLong("func_id"), 
                                   rs.getDouble("x_value"), rs.getDouble("y_value")));
            }
            logger.info("Найдено отсортированных точек для функции {}: {}", functionId, points.size());
            return points;
        } catch (SQLException e) {
            logger.error("Ошибка при поиске отсортированных точек: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Находит точку функции по x_value
     */
    public Optional<Point> findByFunctionIdAndX(Long functionId, double xValue) throws SQLException {
        logger.debug("Поиск точки функции {} с x_value={}", functionId, xValue);
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_FUNCTION_AND_X)) {
            
            statement.setLong(1, functionId);
            statement.setDouble(2, xValue);
            ResultSet rs = statement.executeQuery();
            
            if (rs.next()) {
                Point point = new Point(rs.getLong("id"), rs.getLong("func_id"), 
                                      rs.getDouble("x_value"), rs.getDouble("y_value"));
                logger.debug("Точка найдена: {}", point);
                return Optional.of(point);
            }
            logger.debug("Точка функции {} с x_value={} не найдена", functionId, xValue);
            return Optional.empty();
        } catch (SQLException e) {
            logger.error("Ошибка при поиске точки по x_value: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Находит точки функции в диапазоне x_value
     */
    public List<Point> findByFunctionIdAndXRange(Long functionId, double minX, double maxX) throws SQLException {
        logger.debug("Поиск точек функции {} в диапазоне x_value [{}, {}]", functionId, minX, maxX);
        List<Point> points = new ArrayList<>();
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_FUNCTION_AND_X_RANGE)) {
            
            statement.setLong(1, functionId);
            statement.setDouble(2, minX);
            statement.setDouble(3, maxX);
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                points.add(new Point(rs.getLong("id"), rs.getLong("func_id"), 
                                   rs.getDouble("x_value"), rs.getDouble("y_value")));
            }
            logger.info("Найдено точек в диапазоне [{}, {}]: {}", minX, maxX, points.size());
            return points;
        } catch (SQLException e) {
            logger.error("Ошибка при поиске точек в диапазоне: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Находит точки функции с определенным y_value
     */
    public List<Point> findByFunctionIdAndY(Long functionId, double yValue) throws SQLException {
        logger.debug("Поиск точек функции {} с y_value={}", functionId, yValue);
        List<Point> points = new ArrayList<>();
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_FUNCTION_AND_Y)) {
            
            statement.setLong(1, functionId);
            statement.setDouble(2, yValue);
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                points.add(new Point(rs.getLong("id"), rs.getLong("func_id"), 
                                   rs.getDouble("x_value"), rs.getDouble("y_value")));
            }
            logger.info("Найдено точек с y_value={}: {}", yValue, points.size());
            return points;
        } catch (SQLException e) {
            logger.error("Ошибка при поиске точек по y_value: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Находит точки функции с y_value в диапазоне
     */
    public List<Point> findByFunctionIdAndYRange(Long functionId, double minY, double maxY) throws SQLException {
        logger.debug("Поиск точек функции {} в диапазоне y_value [{}, {}]", functionId, minY, maxY);
        List<Point> points = new ArrayList<>();
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_FUNCTION_AND_Y_RANGE)) {
            
            statement.setLong(1, functionId);
            statement.setDouble(2, minY);
            statement.setDouble(3, maxY);
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                points.add(new Point(rs.getLong("id"), rs.getLong("func_id"), 
                                   rs.getDouble("x_value"), rs.getDouble("y_value")));
            }
            logger.info("Найдено точек в диапазоне y_value [{}, {}]: {}", minY, maxY, points.size());
            return points;
        } catch (SQLException e) {
            logger.error("Ошибка при поиске точек в диапазоне y_value: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Подсчитывает количество точек функции
     */
    public int countByFunctionId(Long functionId) throws SQLException {
        logger.debug("Подсчет количества точек функции: {}", functionId);
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(COUNT_BY_FUNCTION_ID)) {
            
            statement.setLong(1, functionId);
            ResultSet rs = statement.executeQuery();
            
            if (rs.next()) {
                int count = rs.getInt("point_count");
                logger.info("Количество точек функции {}: {}", functionId, count);
                return count;
            }
            return 0;
        } catch (SQLException e) {
            logger.error("Ошибка при подсчете точек функции: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Находит минимальное и максимальное x_value функции
     */
    public XRange findMinMaxXByFunctionId(Long functionId) throws SQLException {
        logger.debug("Поиск min/max x_value для функции: {}", functionId);
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(MIN_MAX_X_BY_FUNCTION)) {
            
            statement.setLong(1, functionId);
            ResultSet rs = statement.executeQuery();
            
            if (rs.next()) {
                XRange range = new XRange(rs.getDouble("min_x"), rs.getDouble("max_x"));
                logger.info("Диапазон x_value для функции {}: {}", functionId, range);
                return range;
            }
            return new XRange(0.0, 0.0);
        } catch (SQLException e) {
            logger.error("Ошибка при поиске min/max x_value: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Добавляет новую точку
     * @return ID созданной точки
     */
    public Long insert(Long functionId, double xValue, double yValue) throws SQLException {
        logger.debug("Вставка новой точки: functionId={}, x={}, y={}", functionId, xValue, yValue);
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            
            statement.setLong(1, functionId);
            statement.setDouble(2, xValue);
            statement.setDouble(3, yValue);
            
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                ResultSet rs = statement.getGeneratedKeys();
                if (rs.next()) {
                    Long id = rs.getLong(1);
                    logger.debug("Точка успешно создана с ID: {}", id);
                    return id;
                }
            }
            throw new SQLException("Не удалось получить ID созданной точки");
        } catch (SQLException e) {
            logger.error("Ошибка при вставке точки: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Добавляет несколько точек одной функцией
     */
    public void insertMultiple(Long functionId, double x1, double y1, double x2, double y2, double x3, double y3) throws SQLException {
        logger.info("Вставка нескольких точек для функции: {}", functionId);
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_MULTIPLE)) {
            
            statement.setLong(1, functionId);
            statement.setDouble(2, x1);
            statement.setDouble(3, y1);
            statement.setLong(4, functionId);
            statement.setDouble(5, x2);
            statement.setDouble(6, y2);
            statement.setLong(7, functionId);
            statement.setDouble(8, x3);
            statement.setDouble(9, y3);
            
            int rowsAffected = statement.executeUpdate();
            logger.info("Вставлено точек: {}", rowsAffected);
        } catch (SQLException e) {
            logger.error("Ошибка при вставке нескольких точек: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Обновляет y_value точки по ID
     */
    public boolean updateYById(Long id, double yValue) throws SQLException {
        logger.info("Обновление y_value точки с ID: {}", id);
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_Y_BY_ID)) {
            
            statement.setDouble(1, yValue);
            statement.setLong(2, id);
            
            int rowsAffected = statement.executeUpdate();
            boolean updated = rowsAffected > 0;
            logger.info("y_value обновлен: {}, затронуто строк: {}", updated, rowsAffected);
            return updated;
        } catch (SQLException e) {
            logger.error("Ошибка при обновлении y_value: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Обновляет y_value точки функции по x_value
     */
    public boolean updateYByFunctionAndX(Long functionId, double xValue, double yValue) throws SQLException {
        logger.info("Обновление y_value точки функции {} с x_value={}", functionId, xValue);
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_Y_BY_FUNCTION_AND_X)) {
            
            statement.setDouble(1, yValue);
            statement.setLong(2, functionId);
            statement.setDouble(3, xValue);
            
            int rowsAffected = statement.executeUpdate();
            boolean updated = rowsAffected > 0;
            logger.info("y_value обновлен: {}, затронуто строк: {}", updated, rowsAffected);
            return updated;
        } catch (SQLException e) {
            logger.error("Ошибка при обновлении y_value по x_value: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Обновляет x_value и y_value точки
     */
    public boolean update(Long id, double xValue, double yValue) throws SQLException {
        logger.info("Обновление x_value и y_value точки с ID: {}", id);
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_XY_BY_ID)) {
            
            statement.setDouble(1, xValue);
            statement.setDouble(2, yValue);
            statement.setLong(3, id);
            
            int rowsAffected = statement.executeUpdate();
            boolean updated = rowsAffected > 0;
            logger.info("Точка обновлена: {}, затронуто строк: {}", updated, rowsAffected);
            return updated;
        } catch (SQLException e) {
            logger.error("Ошибка при обновлении точки: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Обновляет все точки функции (умножает y_value на коэффициент)
     */
    public int updateYMultiply(Long functionId, double multiplier) throws SQLException {
        logger.info("Умножение y_value всех точек функции {} на {}", functionId, multiplier);
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_Y_MULTIPLY)) {
            
            statement.setDouble(1, multiplier);
            statement.setLong(2, functionId);
            
            int rowsAffected = statement.executeUpdate();
            logger.info("Обновлено точек функции {}: {}", functionId, rowsAffected);
            return rowsAffected;
        } catch (SQLException e) {
            logger.error("Ошибка при умножении y_value: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Удаляет точку по ID
     */
    public boolean deleteById(Long id) throws SQLException {
        logger.info("Удаление точки с ID: {}", id);
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_BY_ID)) {
            
            statement.setLong(1, id);
            int rowsAffected = statement.executeUpdate();
            boolean deleted = rowsAffected > 0;
            logger.info("Точка удалена: {}, затронуто строк: {}", deleted, rowsAffected);
            return deleted;
        } catch (SQLException e) {
            logger.error("Ошибка при удалении точки: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Удаляет точку функции по x_value
     */
    public boolean deleteByFunctionAndX(Long functionId, double xValue) throws SQLException {
        logger.info("Удаление точки функции {} с x_value={}", functionId, xValue);
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_BY_FUNCTION_AND_X)) {
            
            statement.setLong(1, functionId);
            statement.setDouble(2, xValue);
            int rowsAffected = statement.executeUpdate();
            boolean deleted = rowsAffected > 0;
            logger.info("Точка удалена: {}, затронуто строк: {}", deleted, rowsAffected);
            return deleted;
        } catch (SQLException e) {
            logger.error("Ошибка при удалении точки по x_value: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Удаляет все точки функции
     */
    public int deleteByFunctionId(Long functionId) throws SQLException {
        logger.info("Удаление всех точек функции: {}", functionId);
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_BY_FUNCTION_ID)) {
            
            statement.setLong(1, functionId);
            int rowsAffected = statement.executeUpdate();
            logger.info("Удалено точек функции {}: {}", functionId, rowsAffected);
            return rowsAffected;
        } catch (SQLException e) {
            logger.error("Ошибка при удалении точек функции: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Удаляет точки функции в диапазоне x_value
     */
    public int deleteByFunctionAndXRange(Long functionId, double minX, double maxX) throws SQLException {
        logger.info("Удаление точек функции {} в диапазоне x_value [{}, {}]", functionId, minX, maxX);
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_BY_FUNCTION_AND_X_RANGE)) {
            
            statement.setLong(1, functionId);
            statement.setDouble(2, minX);
            statement.setDouble(3, maxX);
            int rowsAffected = statement.executeUpdate();
            logger.info("Удалено точек в диапазоне [{}, {}]: {}", minX, maxX, rowsAffected);
            return rowsAffected;
        } catch (SQLException e) {
            logger.error("Ошибка при удалении точек в диапазоне: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Класс для представления точки
     */
    public static class Point {
        private final Long id;
        private final Long functionId;
        private final double xValue;
        private final double yValue;
        
        public Point(Long id, Long functionId, double xValue, double yValue) {
            this.id = id;
            this.functionId = functionId;
            this.xValue = xValue;
            this.yValue = yValue;
        }
        
        public Long getId() { return id; }
        public Long getFunctionId() { return functionId; }
        public double getXValue() { return xValue; }
        public double getYValue() { return yValue; }
        
        @Override
        public String toString() {
            return "Point{id=" + id + ", functionId=" + functionId + ", xValue=" + xValue + ", yValue=" + yValue + "}";
        }
    }
    
    /**
     * Класс для представления диапазона x_value
     */
    public static class XRange {
        private final double minX;
        private final double maxX;
        
        public XRange(double minX, double maxX) {
            this.minX = minX;
            this.maxX = maxX;
        }
        
        public double getMinX() { return minX; }
        public double getMaxX() { return maxX; }
        
        @Override
        public String toString() {
            return "XRange{minX=" + minX + ", maxX=" + maxX + "}";
        }
    }
}

