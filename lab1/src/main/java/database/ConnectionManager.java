package database;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Класс для управления подключением к базе данных PostgreSQL
 */
public class ConnectionManager {
    private static final Logger logger = LogManager.getLogger(ConnectionManager.class);
    
    private static final String DEFAULT_URL = "jdbc:postgresql://localhost:5432/lab5_db";
    private static final String DEFAULT_USER = "postgres";
    private static final String DEFAULT_PASSWORD = "postgres";
    
    private static String url = DEFAULT_URL;
    private static String user = DEFAULT_USER;
    private static String password = DEFAULT_PASSWORD;
    
    /**
     * Устанавливает параметры подключения к БД
     */
    public static void setConnectionParams(String url, String user, String password) {
        logger.info("Установка параметров подключения к БД: url={}, user={}", url, user);
        ConnectionManager.url = url;
        ConnectionManager.user = user;
        ConnectionManager.password = password;
    }
    
    /**
     * Получает подключение к базе данных
     * @return Connection объект
     * @throws SQLException если не удалось установить подключение
     */
    public static Connection getConnection() throws SQLException {
        logger.debug("Попытка установить подключение к БД: {}", url);
        try {
            Connection connection = DriverManager.getConnection(url, user, password);
            logger.info("Подключение к БД успешно установлено");
            return connection;
        } catch (SQLException e) {
            logger.error("Ошибка при установке подключения к БД: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Закрывает подключение к базе данных
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
                logger.debug("Подключение к БД закрыто");
            } catch (SQLException e) {
                logger.error("Ошибка при закрытии подключения: {}", e.getMessage(), e);
            }
        }
    }
}

