package benchmark;

/**
 * Шаблон бенчмарка для Manual ветки (JDBC DAO)
 * 
 * ИНСТРУКЦИЯ:
 * 1. Скопировать этот класс в ветку manual
 * 2. Импортировать DAO классы из database.dao
 * 3. Реализовать методы аналогично FrameworkPerformanceBenchmark
 * 4. Использовать UserDAO, FunctionDAO, PointDAO, ResultDAO вместо Repository
 * 
 * Пример использования DAO:
 * 
 * UserDAO userDAO = new UserDAO();
 * Long userId = userDAO.insert("login", "password", "email");
 * Optional<UserDAO.User> user = userDAO.findById(userId);
 * userDAO.deleteById(userId);
 */
public class ManualPerformanceBenchmarkTemplate {
    // TODO: Реализовать аналогично FrameworkPerformanceBenchmark
    // Использовать DAO классы вместо Repository
}

