package benchmark;

import entity.*;
import repository.*;
import repository.impl.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Сервис для выполнения бенчмарков производительности Framework (Hibernate Repository)
 */
public class FrameworkBenchmarkService {
    private static final int TOTAL_RECORDS = 10000;
    private static final Random random = new Random();
    
    private final UserRepository userRepository;
    private final FunctionRepository functionRepository;
    private final PointRepository pointRepository;
    private final ResultRepository resultRepository;
    
    public FrameworkBenchmarkService() {
        this.userRepository = new UserRepositoryImpl();
        this.functionRepository = new FunctionRepositoryImpl();
        this.pointRepository = new PointRepositoryImpl();
        this.resultRepository = new ResultRepositoryImpl();
    }
    
    /**
     * Запускает все бенчмарки и возвращает результаты
     */
    public List<BenchmarkResult> runBenchmarks() {
        List<BenchmarkResult> results = new ArrayList<>();
        
        try {
            // Очистка базы данных
            clearDatabase();
            
            // Создание тестовых данных
            User testUser = createTestUser();
            Function testFunction = createTestFunction(testUser);
            
            // Бенчмарки для Users
            benchUsers(results);
            
            // Бенчмарки для Functions
            benchFunctions(testUser, results);
            
            // Бенчмарки для Points
            benchPoints(testFunction, results);
            
            // Бенчмарки для Results
            benchResults(testFunction, results);
            
        } finally {
            // Очистка после тестов
            clearDatabase();
        }
        
        return results;
    }
    
    private void clearDatabase() {
        // Очистка в правильном порядке (с учетом foreign keys)
        // Используем deleteById для более эффективной очистки
        try {
            List<Result> results = resultRepository.findAll();
            for (Result r : results) {
                if (r.getId() != null) {
                    resultRepository.deleteById(r.getId());
                }
            }
            
            List<Point> points = pointRepository.findAll();
            for (Point p : points) {
                if (p.getId() != null) {
                    pointRepository.deleteById(p.getId());
                }
            }
            
            List<Function> functions = functionRepository.findAll();
            for (Function f : functions) {
                if (f.getId() != null) {
                    functionRepository.deleteById(f.getId());
                }
            }
            
            List<User> users = userRepository.findAll();
            for (User u : users) {
                if (u.getId() != null) {
                    userRepository.deleteById(u.getId());
                }
            }
        } catch (Exception e) {
            // Игнорируем ошибки при очистке (возможно, таблицы уже пустые)
            System.err.println("Предупреждение при очистке базы данных: " + e.getMessage());
        }
    }
    
    private User createTestUser() {
        User user = new User("benchmark_user_" + random.nextInt(100000), 
                            "password", 
                            "benchmark@test.com");
        return userRepository.save(user);
    }
    
    private Function createTestFunction(User user) {
        Function function = new Function(user, "benchmark_function", "linear");
        return functionRepository.save(function);
    }
    
    private void benchUsers(List<BenchmarkResult> results) {
        // Вставка пользователей
        long startTime = System.currentTimeMillis();
        List<User> users = generateUsers(TOTAL_RECORDS);
        for (User user : users) {
            userRepository.save(user);
        }
        long endTime = System.currentTimeMillis();
        results.add(new BenchmarkResult(endTime - startTime, "insertInUsersTable", TOTAL_RECORDS));
        
        // Поиск по ID
        if (!users.isEmpty()) {
            Long userId = users.get(0).getId();
            startTime = System.currentTimeMillis();
            for (int i = 0; i < 100; i++) {
                userRepository.findById(userId);
            }
            endTime = System.currentTimeMillis();
            results.add(new BenchmarkResult(endTime - startTime, "findUserById (100 операций)", 100));
        }
        
        // Поиск всех
        startTime = System.currentTimeMillis();
        userRepository.findAll();
        endTime = System.currentTimeMillis();
        results.add(new BenchmarkResult(endTime - startTime, "findAllUsers", users.size()));
        
        // Обновление
        if (!users.isEmpty()) {
            User userToUpdate = users.get(0);
            userToUpdate.setEmail("updated@test.com");
            startTime = System.currentTimeMillis();
            for (int i = 0; i < 100; i++) {
                userRepository.save(userToUpdate);
            }
            endTime = System.currentTimeMillis();
            results.add(new BenchmarkResult(endTime - startTime, "updateUser (100 операций)", 100));
        }
        
        // Удаление
        if (users.size() >= 100) {
            startTime = System.currentTimeMillis();
            for (int i = 0; i < 100; i++) {
                userRepository.deleteById(users.get(i).getId());
            }
            endTime = System.currentTimeMillis();
            results.add(new BenchmarkResult(endTime - startTime, "deleteUserById (100 операций)", 100));
        }
    }
    
    private void benchFunctions(User user, List<BenchmarkResult> results) {
        // Вставка функций
        long startTime = System.currentTimeMillis();
        List<Function> functions = generateFunctions(TOTAL_RECORDS, user);
        for (Function function : functions) {
            functionRepository.save(function);
        }
        long endTime = System.currentTimeMillis();
        results.add(new BenchmarkResult(endTime - startTime, "insertInFunctionsTable", TOTAL_RECORDS));
        
        // Поиск по ID
        if (!functions.isEmpty()) {
            Long functionId = functions.get(0).getId();
            startTime = System.currentTimeMillis();
            for (int i = 0; i < 100; i++) {
                functionRepository.findById(functionId);
            }
            endTime = System.currentTimeMillis();
            results.add(new BenchmarkResult(endTime - startTime, "findFunctionById (100 операций)", 100));
        }
        
        // Поиск по пользователю
        startTime = System.currentTimeMillis();
        for (int i = 0; i < 50; i++) {
            functionRepository.findByUser(user);
        }
        endTime = System.currentTimeMillis();
        results.add(new BenchmarkResult(endTime - startTime, "findFunctionsByUser (50 операций)", 50));
    }
    
    private void benchPoints(Function function, List<BenchmarkResult> results) {
        // Вставка точек
        long startTime = System.currentTimeMillis();
        List<Point> points = generatePoints(TOTAL_RECORDS, function);
        for (Point point : points) {
            pointRepository.save(point);
        }
        long endTime = System.currentTimeMillis();
        results.add(new BenchmarkResult(endTime - startTime, "insertInPointsTable", TOTAL_RECORDS));
        
        // Поиск по ID
        if (!points.isEmpty()) {
            Long pointId = points.get(0).getId();
            startTime = System.currentTimeMillis();
            for (int i = 0; i < 100; i++) {
                pointRepository.findById(pointId);
            }
            endTime = System.currentTimeMillis();
            results.add(new BenchmarkResult(endTime - startTime, "findPointById (100 операций)", 100));
        }
        
        // Поиск по функции
        startTime = System.currentTimeMillis();
        for (int i = 0; i < 50; i++) {
            pointRepository.findByFunction(function);
        }
        endTime = System.currentTimeMillis();
        results.add(new BenchmarkResult(endTime - startTime, "findPointsByFunction (50 операций)", 50));
    }
    
    private void benchResults(Function function, List<BenchmarkResult> results) {
        // Вставка результатов
        long startTime = System.currentTimeMillis();
        List<Result> resultList = generateResults(TOTAL_RECORDS / 2, function);
        for (Result result : resultList) {
            resultRepository.save(result);
        }
        long endTime = System.currentTimeMillis();
        results.add(new BenchmarkResult(endTime - startTime, "insertInResultsTable", resultList.size()));
        
        // Поиск по ID
        if (!resultList.isEmpty()) {
            Long resultId = resultList.get(0).getId();
            startTime = System.currentTimeMillis();
            for (int i = 0; i < 100; i++) {
                resultRepository.findById(resultId);
            }
            endTime = System.currentTimeMillis();
            results.add(new BenchmarkResult(endTime - startTime, "findResultById (100 операций)", 100));
        }
        
        // Поиск по функции
        startTime = System.currentTimeMillis();
        for (int i = 0; i < 50; i++) {
            resultRepository.findByFunction(function);
        }
        endTime = System.currentTimeMillis();
        results.add(new BenchmarkResult(endTime - startTime, "findResultsByFunction (50 операций)", 50));
    }
    
    private List<User> generateUsers(int count) {
        List<User> users = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            User user = new User("user" + i + "_" + random.nextInt(100000), 
                               "password" + i, 
                               "email" + i + "@test.com");
            users.add(user);
        }
        return users;
    }
    
    private List<Function> generateFunctions(int count, User user) {
        List<Function> functions = new ArrayList<>();
        String[] types = {"linear", "quadratic", "polynomial", "exponential"};
        for (int i = 1; i <= count; i++) {
            Function function = new Function(user, "function_" + i, types[i % types.length]);
            functions.add(function);
        }
        return functions;
    }
    
    private List<Point> generatePoints(int count, Function function) {
        List<Point> points = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Point point = new Point(function, random.nextDouble() * 100, random.nextDouble() * 100);
            points.add(point);
        }
        return points;
    }
    
    private List<Result> generateResults(int count, Function function) {
        List<Result> results = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            Result result = new Result(function, "Result " + i + ": " + random.nextInt(1000));
            results.add(result);
        }
        return results;
    }
}

