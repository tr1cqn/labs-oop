package benchmark;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Шаблон сервиса для выполнения бенчмарков производительности Manual (JDBC DAO)
 * 
 * ИНСТРУКЦИЯ ДЛЯ MANUAL ВЕТКИ:
 * 1. Скопировать этот класс в manual ветку
 * 2. Переименовать в ManualBenchmarkService
 * 3. Импортировать DAO классы из database.dao:
 *    - import database.dao.UserDAO;
 *    - import database.dao.FunctionDAO;
 *    - import database.dao.PointDAO;
 *    - import database.dao.ResultDAO;
 * 4. Заменить Repository на DAO в конструкторе и методах
 * 5. Использовать методы DAO вместо методов Repository
 * 
 * Пример использования DAO:
 * 
 * UserDAO userDAO = new UserDAO();
 * Long userId = userDAO.insert("login", "password", "email");
 * Optional<UserDAO.User> user = userDAO.findById(userId);
 * userDAO.deleteById(userId);
 */
public class ManualBenchmarkServiceTemplate {
    private static final int TOTAL_RECORDS = 10000;
    private static final Random random = new Random();
    
    // TODO: Заменить на DAO классы
    // private final UserDAO userDAO;
    // private final FunctionDAO functionDAO;
    // private final PointDAO pointDAO;
    // private final ResultDAO resultDAO;
    
    public ManualBenchmarkServiceTemplate() {
        // TODO: Инициализировать DAO классы
        // this.userDAO = new UserDAO();
        // this.functionDAO = new FunctionDAO();
        // this.pointDAO = new PointDAO();
        // this.resultDAO = new ResultDAO();
    }
    
    /**
     * Запускает все бенчмарки и возвращает результаты
     * Реализовать аналогично FrameworkBenchmarkService
     */
    public List<BenchmarkResult> runBenchmarks() {
        List<BenchmarkResult> results = new ArrayList<>();
        
        // TODO: Реализовать аналогично FrameworkBenchmarkService
        // Использовать DAO классы вместо Repository
        
        return results;
    }
}

