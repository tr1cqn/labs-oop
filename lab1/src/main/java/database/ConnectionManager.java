package database;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Класс для управления подключением к базе данных
 */
public class ConnectionManager {
    private static final Logger logger = LogManager.getLogger(ConnectionManager.class);

    // ИЗМЕНИЛ НА H2 (в памяти)
    private static final String DEFAULT_URL = "jdbc:h2:mem:lab5db;DB_CLOSE_DELAY=-1";
    private static final String DEFAULT_USER = "sa";
    private static final String DEFAULT_PASSWORD = "";

    // PostgreSQL (закомментировано)
    // private static final String DEFAULT_URL = "jdbc:postgresql://localhost:5432/lab5_db";
    // private static final String DEFAULT_USER = "postgres";
    // private static final String DEFAULT_PASSWORD = "postgres";

    private static String url = DEFAULT_URL;
    private static String user = DEFAULT_USER;
    private static String password = DEFAULT_PASSWORD;

    static {
        try {
            // Загружаем драйвер H2
            Class.forName("org.h2.Driver");
            logger.info("Драйвер H2 загружен");

            // Для PostgreSQL:
            // Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            logger.error("Драйвер БД не найден: {}", e.getMessage());
            throw new RuntimeException("Драйвер БД не найден", e);
        }
    }

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

            // Автоматически создаем таблицы при первом подключении
            createTablesIfNotExist(connection);

            return connection;
        } catch (SQLException e) {
            logger.error("Ошибка при установке подключения к БД: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Создает таблицы если они не существуют
     */
    private static void createTablesIfNotExist(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Создаем таблицу users
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS users (" +
                            "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                            "login VARCHAR(100) UNIQUE NOT NULL, " +
                            "password VARCHAR(255) NOT NULL, " +
                            "email VARCHAR(255), " +
                            "role VARCHAR(20) DEFAULT 'USER')"
            );

            // Создаем таблицу functions
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS functions (" +
                            "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                            "user_id BIGINT NOT NULL, " +
                            "name VARCHAR(200) NOT NULL, " +
                            "type VARCHAR(50) NOT NULL, " +
                            "FOREIGN KEY (user_id) REFERENCES users(id))"
            );

            // Создаем таблицу points
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS points (" +
                            "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                            "func_id BIGINT NOT NULL, " +
                            "x_value DOUBLE NOT NULL, " +
                            "y_value DOUBLE NOT NULL, " +
                            "FOREIGN KEY (func_id) REFERENCES functions(id))"
            );

            // Создаем таблицу result
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS result (" +
                            "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                            "result_id BIGINT NOT NULL, " +
                            "result TEXT NOT NULL, " +
                            "FOREIGN KEY (result_id) REFERENCES functions(id))"
            );

            logger.info("Таблицы успешно созданы (если не существовали)");
        }

        // Для существующей схемы — гарантируем наличие колонки role
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS role VARCHAR(20) DEFAULT 'USER'");
        } catch (SQLException e) {
            logger.warn("Не удалось добавить колонку role (возможно уже существует): {}", e.getMessage());
        }

        // Bootstrap: создаём первого ADMIN, если его нет
        ensureDefaultAdmin(connection);
    }

    private static void ensureDefaultAdmin(Connection connection) {
        final String adminLogin = "admin";
        final String adminPassword = "admin";
        final String adminEmail = "admin@example.com";

        try (PreparedStatement check = connection.prepareStatement("SELECT COUNT(*) AS c FROM users WHERE login = ?")) {
            check.setString(1, adminLogin);
            try (ResultSet rs = check.executeQuery()) {
                if (rs.next() && rs.getInt("c") > 0) {
                    return;
                }
            }
        } catch (SQLException e) {
            logger.warn("Не удалось проверить наличие admin пользователя: {}", e.getMessage());
            return;
        }

        try (PreparedStatement insert = connection.prepareStatement(
                "INSERT INTO users (login, password, email, role) VALUES (?, ?, ?, ?)")) {
            insert.setString(1, adminLogin);
            insert.setString(2, adminPassword);
            insert.setString(3, adminEmail);
            insert.setString(4, "ADMIN");
            insert.executeUpdate();
            logger.warn("Создан bootstrap ADMIN пользователь: login=admin password=admin (смените пароль)");
        } catch (SQLException e) {
            logger.warn("Не удалось создать bootstrap admin пользователя: {}", e.getMessage());
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