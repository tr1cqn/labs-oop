package database.dao;

import database.ConnectionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO для работы с таблицей users
 * Использует SQL запросы из 05_crud_users.sql
 */
public class UserDAO {
    private static final Logger logger = LogManager.getLogger(UserDAO.class);
    
    // SQL запросы из 05_crud_users.sql
    private static final String SELECT_ALL = "SELECT id, login, email, role FROM users";
    private static final String SELECT_BY_ID = "SELECT id, login, email, role FROM users WHERE id = ?";
    private static final String SELECT_BY_LOGIN = "SELECT id, login, email, role FROM users WHERE login = ?";
    private static final String SELECT_BY_EMAIL = "SELECT id, login, email, role FROM users WHERE email = ?";
    private static final String SELECT_BY_LOGIN_LIKE = "SELECT id, login, email, role FROM users WHERE login LIKE ?";

    private static final String SELECT_AUTH_BY_LOGIN = "SELECT id, login, password, role FROM users WHERE login = ?";

    private static final String INSERT_WITH_EMAIL = "INSERT INTO users (login, password, email, role) VALUES (?, ?, ?, ?)";
    private static final String INSERT_WITHOUT_EMAIL = "INSERT INTO users (login, password, role) VALUES (?, ?, ?)";
    private static final String UPDATE_PASSWORD = "UPDATE users SET password = ? WHERE id = ?";
    private static final String UPDATE_EMAIL = "UPDATE users SET email = ? WHERE id = ?";
    private static final String UPDATE_LOGIN = "UPDATE users SET login = ? WHERE id = ?";
    private static final String UPDATE_ALL = "UPDATE users SET login = ?, password = ?, email = ? WHERE id = ?";
    private static final String UPDATE_ROLE = "UPDATE users SET role = ? WHERE id = ?";
    private static final String DELETE_BY_ID = "DELETE FROM users WHERE id = ?";
    private static final String DELETE_BY_LOGIN = "DELETE FROM users WHERE login = ?";
    
    /**
     * Находит всех пользователей
     */
    public List<User> findAll() throws SQLException {
        logger.debug("Поиск всех пользователей");
        List<User> users = new ArrayList<>();
        try (Connection connection = ConnectionManager.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(SELECT_ALL)) {
            
            while (rs.next()) {
                users.add(new User(
                    rs.getLong("id"),
                    rs.getString("login"),
                    rs.getString("email"),
                    rs.getString("role")
                ));
            }
            logger.info("Найдено пользователей: {}", users.size());
            return users;
        } catch (SQLException e) {
            logger.error("Ошибка при поиске всех пользователей: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Находит пользователя по ID
     */
    public Optional<User> findById(Long id) throws SQLException {
        logger.debug("Поиск пользователя по ID: {}", id);
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_ID)) {
            
            statement.setLong(1, id);
            ResultSet rs = statement.executeQuery();
            
            if (rs.next()) {
                User user = new User(rs.getLong("id"), rs.getString("login"), rs.getString("email"), rs.getString("role"));
                logger.debug("Пользователь найден: {}", user);
                return Optional.of(user);
            }
            logger.debug("Пользователь с ID {} не найден", id);
            return Optional.empty();
        } catch (SQLException e) {
            logger.error("Ошибка при поиске пользователя по ID: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Находит пользователя по login
     */
    public Optional<User> findByLogin(String login) throws SQLException {
        logger.debug("Поиск пользователя по login: {}", login);
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_LOGIN)) {
            
            statement.setString(1, login);
            ResultSet rs = statement.executeQuery();
            
            if (rs.next()) {
                User user = new User(rs.getLong("id"), rs.getString("login"), rs.getString("email"), rs.getString("role"));
                logger.debug("Пользователь найден: {}", user);
                return Optional.of(user);
            }
            logger.debug("Пользователь с login {} не найден", login);
            return Optional.empty();
        } catch (SQLException e) {
            logger.error("Ошибка при поиске пользователя по login: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Находит пользователя по email
     */
    public Optional<User> findByEmail(String email) throws SQLException {
        logger.debug("Поиск пользователя по email: {}", email);
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_EMAIL)) {
            
            statement.setString(1, email);
            ResultSet rs = statement.executeQuery();
            
            if (rs.next()) {
                User user = new User(rs.getLong("id"), rs.getString("login"), rs.getString("email"), rs.getString("role"));
                logger.debug("Пользователь найден: {}", user);
                return Optional.of(user);
            }
            logger.debug("Пользователь с email {} не найден", email);
            return Optional.empty();
        } catch (SQLException e) {
            logger.error("Ошибка при поиске пользователя по email: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Поиск пользователей по частичному совпадению login
     */
    public List<User> findByLoginLike(String pattern) throws SQLException {
        logger.debug("Поиск пользователей по login LIKE: {}", pattern);
        List<User> users = new ArrayList<>();
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_LOGIN_LIKE)) {
            
            statement.setString(1, pattern);
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                users.add(new User(rs.getLong("id"), rs.getString("login"), rs.getString("email"), rs.getString("role")));
            }
            logger.info("Найдено пользователей по паттерну {}: {}", pattern, users.size());
            return users;
        } catch (SQLException e) {
            logger.error("Ошибка при поиске пользователей по паттерну: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Добавляет нового пользователя с email
     * @return ID созданного пользователя
     */
    public Long insert(String login, String password, String email) throws SQLException {
        return insert(login, password, email, "USER");
    }

    public Long insert(String login, String password, String email, String role) throws SQLException {
        logger.info("Вставка нового пользователя: login={}, email={}, role={}", login, email, role);
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_WITH_EMAIL, Statement.RETURN_GENERATED_KEYS)) {
            
            statement.setString(1, login);
            statement.setString(2, password);
            statement.setString(3, email);
            statement.setString(4, role);
            
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                ResultSet rs = statement.getGeneratedKeys();
                if (rs.next()) {
                    Long id = rs.getLong(1);
                    logger.info("Пользователь успешно создан с ID: {}", id);
                    return id;
                }
            }
            throw new SQLException("Не удалось получить ID созданного пользователя");
        } catch (SQLException e) {
            logger.error("Ошибка при вставке пользователя: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Добавляет пользователя без email (роль по умолчанию USER)
     * @return ID созданного пользователя
     */
    public Long insert(String login, String password) throws SQLException {
        logger.info("Вставка нового пользователя без email: login={} role=USER", login);
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_WITHOUT_EMAIL, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, login);
            statement.setString(2, password);
            statement.setString(3, "USER");

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                ResultSet rs = statement.getGeneratedKeys();
                if (rs.next()) {
                    Long id = rs.getLong(1);
                    logger.info("Пользователь успешно создан с ID: {}", id);
                    return id;
                }
            }
            throw new SQLException("Не удалось получить ID созданного пользователя");
        } catch (SQLException e) {
            logger.error("Ошибка при вставке пользователя: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Обновляет пароль пользователя
     */
    public boolean updatePassword(Long id, String password) throws SQLException {
        logger.info("Обновление пароля пользователя с ID: {}", id);
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_PASSWORD)) {
            
            statement.setString(1, password);
            statement.setLong(2, id);
            
            int rowsAffected = statement.executeUpdate();
            boolean updated = rowsAffected > 0;
            logger.info("Пароль обновлен: {}, затронуто строк: {}", updated, rowsAffected);
            return updated;
        } catch (SQLException e) {
            logger.error("Ошибка при обновлении пароля: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Обновляет email пользователя
     */
    public boolean updateEmail(Long id, String email) throws SQLException {
        logger.info("Обновление email пользователя с ID: {}", id);
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_EMAIL)) {
            
            statement.setString(1, email);
            statement.setLong(2, id);
            
            int rowsAffected = statement.executeUpdate();
            boolean updated = rowsAffected > 0;
            logger.info("Email обновлен: {}, затронуто строк: {}", updated, rowsAffected);
            return updated;
        } catch (SQLException e) {
            logger.error("Ошибка при обновлении email: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Обновляет login пользователя
     */
    public boolean updateLogin(Long id, String login) throws SQLException {
        logger.info("Обновление login пользователя с ID: {}", id);
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_LOGIN)) {
            
            statement.setString(1, login);
            statement.setLong(2, id);
            
            int rowsAffected = statement.executeUpdate();
            boolean updated = rowsAffected > 0;
            logger.info("Login обновлен: {}, затронуто строк: {}", updated, rowsAffected);
            return updated;
        } catch (SQLException e) {
            logger.error("Ошибка при обновлении login: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Обновляет все данные пользователя
     */
    public boolean update(Long id, String login, String password, String email) throws SQLException {
        logger.info("Обновление всех данных пользователя с ID: {}", id);
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_ALL)) {
            
            statement.setString(1, login);
            statement.setString(2, password);
            statement.setString(3, email);
            statement.setLong(4, id);
            
            int rowsAffected = statement.executeUpdate();
            boolean updated = rowsAffected > 0;
            logger.info("Данные пользователя обновлены: {}, затронуто строк: {}", updated, rowsAffected);
            return updated;
        } catch (SQLException e) {
            logger.error("Ошибка при обновлении данных пользователя: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Удаляет пользователя по ID
     */
    public boolean deleteById(Long id) throws SQLException {
        logger.info("Удаление пользователя с ID: {}", id);
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_BY_ID)) {
            
            statement.setLong(1, id);
            int rowsAffected = statement.executeUpdate();
            boolean deleted = rowsAffected > 0;
            logger.info("Пользователь удален: {}, затронуто строк: {}", deleted, rowsAffected);
            return deleted;
        } catch (SQLException e) {
            logger.error("Ошибка при удалении пользователя: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Удаляет пользователя по login
     */
    public boolean deleteByLogin(String login) throws SQLException {
        logger.info("Удаление пользователя с login: {}", login);
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_BY_LOGIN)) {
            
            statement.setString(1, login);
            int rowsAffected = statement.executeUpdate();
            boolean deleted = rowsAffected > 0;
            logger.info("Пользователь удален: {}, затронуто строк: {}", deleted, rowsAffected);
            return deleted;
        } catch (SQLException e) {
            logger.error("Ошибка при удалении пользователя: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Обновляет роль пользователя (ADMIN/USER)
     */
    public boolean updateRole(Long id, String role) throws SQLException {
        logger.info("Обновление роли пользователя с ID: {} role={}", id, role);
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_ROLE)) {
            statement.setString(1, role);
            statement.setLong(2, id);
            int rowsAffected = statement.executeUpdate();
            boolean updated = rowsAffected > 0;
            logger.info("Роль обновлена: {}, затронуто строк: {}", updated, rowsAffected);
            return updated;
        } catch (SQLException e) {
            logger.error("Ошибка при обновлении роли: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Аутентификация по login/password для Basic Auth.
     */
    public Optional<AuthUser> authenticate(String login, String password) throws SQLException {
        logger.debug("Аутентификация пользователя login={}", login);
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_AUTH_BY_LOGIN)) {
            statement.setString(1, login);
            ResultSet rs = statement.executeQuery();
            if (!rs.next()) {
                return Optional.empty();
            }
            String stored = rs.getString("password");
            if (stored == null || !stored.equals(password)) {
                return Optional.empty();
            }
            Long id = rs.getLong("id");
            String role = rs.getString("role");
            return Optional.of(new AuthUser(id, login, role));
        }
    }

    /**
     * Класс для представления пользователя
     */
    public static class User {
        private final Long id;
        private final String login;
        private final String email;
        private final String role;
        
        public User(Long id, String login, String email, String role) {
            this.id = id;
            this.login = login;
            this.email = email;
            this.role = role;
        }
        
        public Long getId() { return id; }
        public String getLogin() { return login; }
        public String getEmail() { return email; }
        public String getRole() { return role; }
        
        @Override
        public String toString() {
            return "User{id=" + id + ", login='" + login + "', email='" + email + "', role='" + role + "'}";
        }
    }

    /**
     * Минимальный объект аутентифицированного пользователя.
     */
    public static class AuthUser {
        private final Long id;
        private final String login;
        private final String role;

        public AuthUser(Long id, String login, String role) {
            this.id = id;
            this.login = login;
            this.role = role;
        }

        public Long getId() { return id; }
        public String getLogin() { return login; }
        public String getRole() { return role; }
    }
}

