package benchmark;

import entity.Function;
import entity.Point;
import entity.Result;
import entity.User;
import repository.*;
import repository.impl.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Бенчмарк для измерения производительности Framework подхода (Hibernate Repository)
 */
public class FrameworkPerformanceBenchmark {
    private static final int RECORD_COUNT = 10000;
    private static final Random random = new Random();
    
    private final UserRepository userRepository;
    private final FunctionRepository functionRepository;
    private final PointRepository pointRepository;
    private final ResultRepository resultRepository;
    
    private List<Long> userIds = new ArrayList<>();
    private List<Long> functionIds = new ArrayList<>();
    private List<Long> pointIds = new ArrayList<>();
    private List<Long> resultIds = new ArrayList<>();
    
    public FrameworkPerformanceBenchmark() {
        this.userRepository = new UserRepositoryImpl();
        this.functionRepository = new FunctionRepositoryImpl();
        this.pointRepository = new PointRepositoryImpl();
        this.resultRepository = new ResultRepositoryImpl();
    }
    
    /**
     * Генерирует тестовые данные
     */
    public void generateTestData() {
        System.out.println("Генерация " + RECORD_COUNT + " записей...");
        
        // Генерация пользователей
        for (int i = 0; i < RECORD_COUNT / 10; i++) {
            User user = new User("user_" + i + "_" + random.nextInt(100000), 
                               "password_" + i, 
                               "email" + i + "@test.com");
            user = userRepository.save(user);
            userIds.add(user.getId());
        }
        
        // Генерация функций
        for (int i = 0; i < RECORD_COUNT / 5; i++) {
            User user = userRepository.findById(userIds.get(random.nextInt(userIds.size()))).orElse(null);
            if (user != null) {
                Function function = new Function(user, "function_" + i, "type_" + (i % 10));
                function = functionRepository.save(function);
                functionIds.add(function.getId());
            }
        }
        
        // Генерация точек
        for (int i = 0; i < RECORD_COUNT; i++) {
            Function function = functionRepository.findById(
                functionIds.get(random.nextInt(functionIds.size()))).orElse(null);
            if (function != null) {
                Point point = new Point(function, random.nextDouble() * 100, random.nextDouble() * 100);
                point = pointRepository.save(point);
                pointIds.add(point.getId());
            }
        }
        
        // Генерация результатов
        for (int i = 0; i < RECORD_COUNT / 2; i++) {
            Function function = functionRepository.findById(
                functionIds.get(random.nextInt(functionIds.size()))).orElse(null);
            if (function != null) {
                Result result = new Result(function, "Result " + i + ": " + random.nextInt(1000));
                result = resultRepository.save(result);
                resultIds.add(result.getId());
            }
        }
        
        System.out.println("Генерация завершена");
    }
    
    /**
     * Измеряет время выполнения поиска по ID
     */
    public long measureFindById() {
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < 100; i++) {
            Long id = userIds.get(random.nextInt(userIds.size()));
            userRepository.findById(id);
        }
        
        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }
    
    /**
     * Измеряет время выполнения поиска всех записей
     */
    public long measureFindAll() {
        long startTime = System.currentTimeMillis();
        
        userRepository.findAll();
        functionRepository.findAll();
        pointRepository.findAll();
        resultRepository.findAll();
        
        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }
    
    /**
     * Измеряет время выполнения поиска с условиями
     */
    public long measureFindWithConditions() {
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < 50; i++) {
            User user = userRepository.findById(userIds.get(random.nextInt(userIds.size()))).orElse(null);
            if (user != null) {
                functionRepository.findByUser(user);
            }
        }
        
        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }
    
    /**
     * Измеряет время выполнения вставки
     */
    public long measureInsert() {
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < 100; i++) {
            User user = new User("bench_user_" + i + "_" + random.nextInt(100000), 
                               "password", "email@test.com");
            userRepository.save(user);
        }
        
        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }
    
    /**
     * Измеряет время выполнения обновления
     */
    public long measureUpdate() {
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < 100; i++) {
            Long id = userIds.get(random.nextInt(userIds.size()));
            User user = userRepository.findById(id).orElse(null);
            if (user != null) {
                user.setEmail("updated" + i + "@test.com");
                userRepository.save(user);
            }
        }
        
        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }
    
    /**
     * Измеряет время выполнения удаления
     */
    public long measureDelete() {
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < 100; i++) {
            if (!pointIds.isEmpty()) {
                Long id = pointIds.remove(0);
                pointRepository.deleteById(id);
            }
        }
        
        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }
    
    /**
     * Запускает все бенчмарки и возвращает результаты
     */
    public PerformanceResults runBenchmarks() {
        System.out.println("Запуск бенчмарков Framework (Hibernate)...");
        
        long findById = measureFindById();
        long findAll = measureFindAll();
        long findWithConditions = measureFindWithConditions();
        long insert = measureInsert();
        long update = measureUpdate();
        long delete = measureDelete();
        
        return new PerformanceResults("Framework (Hibernate)", 
                                    findById, findAll, findWithConditions, 
                                    insert, update, delete);
    }
    
    /**
     * Класс для хранения результатов производительности
     */
    public static class PerformanceResults {
        private final String approach;
        private final long findById;
        private final long findAll;
        private final long findWithConditions;
        private final long insert;
        private final long update;
        private final long delete;
        
        public PerformanceResults(String approach, long findById, long findAll, 
                                long findWithConditions, long insert, long update, long delete) {
            this.approach = approach;
            this.findById = findById;
            this.findAll = findAll;
            this.findWithConditions = findWithConditions;
            this.insert = insert;
            this.update = update;
            this.delete = delete;
        }
        
        public String getApproach() { return approach; }
        public long getFindById() { return findById; }
        public long getFindAll() { return findAll; }
        public long getFindWithConditions() { return findWithConditions; }
        public long getInsert() { return insert; }
        public long getUpdate() { return update; }
        public long getDelete() { return delete; }
    }
}

