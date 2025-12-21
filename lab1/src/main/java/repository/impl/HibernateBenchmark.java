package repository.impl;

import entity.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// Внутренний класс для хранения результатов (чтобы не зависеть от manual ветки)
class BenchmarkResult {
    private final String operation;
    private final long timeMs;
    private final String approach;

    public BenchmarkResult(String operation, long timeMs, String approach) {
        this.operation = operation;
        this.timeMs = timeMs;
        this.approach = approach;
    }

    public String getOperation() { return operation; }
    public long getTimeMs() { return timeMs; }
    public String getApproach() { return approach; }

    @Override
    public String toString() {
        return String.format("%s | %s | %d ms", approach, operation, timeMs);
    }
}

public class HibernateBenchmark {
    private static final Logger logger = LogManager.getLogger(HibernateBenchmark.class);
    private final EntityManager em;
    private final Random random = new Random();

    public HibernateBenchmark(EntityManager em) {
        this.em = em;
    }

    private long measureTime(Runnable operation) {
        long startTime = System.nanoTime();
        operation.run();
        long endTime = System.nanoTime();
        return (endTime - startTime) / 1_000_000; // в миллисекундах
    }

    // Генерация тестовых данных (10к+ записей)
    public void generateTestData(int recordsCount) {
        logger.info("Генерация {} тестовых записей...", recordsCount);

        em.getTransaction().begin();

        // Очистка таблиц
        Query deleteQuery = em.createQuery("DELETE FROM Point");
        deleteQuery.executeUpdate();
        deleteQuery = em.createQuery("DELETE FROM Result");
        deleteQuery.executeUpdate();
        deleteQuery = em.createQuery("DELETE FROM Function");
        deleteQuery.executeUpdate();
        deleteQuery = em.createQuery("DELETE FROM User");
        deleteQuery.executeUpdate();

        // 1. Создание 100 пользователей
        logger.info("Создание пользователей...");
        List<User> users = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            User user = new User();
            user.setLogin("user" + i);
            user.setPassword("pass" + i);
            user.setEmail("user" + i + "@test.com");
            em.persist(user);
            users.add(user);
        }

        // 2. Создание функций (1/10 от общего количества)
        logger.info("Создание функций...");
        int functionCount = recordsCount / 10;
        List<Function> functions = new ArrayList<>();
        for (int i = 0; i < functionCount; i++) {
            Function function = new Function();
            function.setUser(users.get(i % 100));
            function.setName("function_" + i);
            function.setType(i % 2 == 0 ? "linear" : "quadratic");
            em.persist(function);
            functions.add(function);

            // 3. Создание точек для функции (по 10 точек на функцию)
            for (int pointNum = 0; pointNum < 10; pointNum++) {
                Point point = new Point();
                point.setFunction(function);
                point.setXValue(pointNum * 0.5);
                point.setYValue(Math.sin(pointNum * 0.5));
                em.persist(point);
            }

            // 4. Создание результата для функции
            Result result = new Result();
            result.setFunction(function);
            result.setResult("Result for function " + i);
            em.persist(result);

            // Периодически сбрасываем изменения (чтобы не переполнить память)
            if (i % 500 == 0) {
                em.flush();
            }
        }

        em.getTransaction().commit();
        logger.info("Тестовые данные сгенерированы: {} функций, {} точек",
                functionCount, functionCount * 10);
    }

    // Тестирование операций
    public List<BenchmarkResult> runBenchmarks() {
        List<BenchmarkResult> results = new ArrayList<>();

        // 1. SELECT всех точек через JPQL
        logger.info("Тест 1: SELECT всех точек (JPQL)");
        long selectTime = measureTime(() -> {
            Query query = em.createQuery("SELECT p FROM Point p");
            List<Point> points = query.getResultList();
            // Просто читаем данные, чтобы имитировать обработку
            for (Point p : points) {
                p.getXValue();
                p.getYValue();
            }
        });
        results.add(new BenchmarkResult("SELECT * FROM points", selectTime, "framework"));

        // 2. SELECT с JOIN через JPQL
        logger.info("Тест 2: SELECT с JOIN (JPQL)");
        long joinTime = measureTime(() -> {
            Query query = em.createQuery(
                    "SELECT f.name, p.xValue, p.yValue " +
                            "FROM Function f " +
                            "JOIN Point p ON f.id = p.function.id " +
                            "WHERE f.user.id = 1");
            query.setMaxResults(100);
            List<Object[]> resultsList = query.getResultList();
            for (Object[] row : resultsList) {
                String name = (String) row[0];
                Double x = (Double) row[1];
                Double y = (Double) row[2];
                // Имитация обработки
            }
        });
        results.add(new BenchmarkResult("JOIN functions + points", joinTime, "framework"));

        // 3. INSERT новой точки
        logger.info("Тест 3: INSERT точки");
        long insertTime = measureTime(() -> {
            em.getTransaction().begin();
            try {
                Point point = new Point();
                point.setXValue(random.nextDouble() * 100);
                point.setYValue(random.nextDouble() * 100);

                // Находим первую функцию для связи
                Query query = em.createQuery("SELECT f FROM Function f WHERE f.id = 1");
                Function function = (Function) query.getSingleResult();
                point.setFunction(function);

                em.persist(point);
                em.getTransaction().commit();
            } catch (Exception e) {
                em.getTransaction().rollback();
                logger.error("Ошибка INSERT: {}", e.getMessage());
            }
        });
        results.add(new BenchmarkResult("INSERT point", insertTime, "framework"));

        // 4. UPDATE точки
        logger.info("Тест 4: UPDATE точки");
        long updateTime = measureTime(() -> {
            em.getTransaction().begin();
            try {
                Point point = em.find(Point.class, 1L);
                if (point != null) {
                    point.setYValue(point.getYValue() + 50.0);
                }
                em.getTransaction().commit();
            } catch (Exception e) {
                em.getTransaction().rollback();
                logger.error("Ошибка UPDATE: {}", e.getMessage());
            }
        });
        results.add(new BenchmarkResult("UPDATE point", updateTime, "framework"));

        // 5. DELETE точки
        logger.info("Тест 5: DELETE точки");
        long deleteTime = measureTime(() -> {
            em.getTransaction().begin();
            try {
                // Пытаемся удалить несуществующую запись
                Point point = em.find(Point.class, 999999L);
                if (point != null) {
                    em.remove(point);
                }
                em.getTransaction().commit();
            } catch (Exception e) {
                em.getTransaction().rollback();
                logger.error("Ошибка DELETE: {}", e.getMessage());
            }
        });
        results.add(new BenchmarkResult("DELETE point", deleteTime, "framework"));

        return results;
    }

    // Главный метод для запуска
    public static void main(String[] args) {
        try {
            logger.info("=== ЗАПУСК HIBERNATE BENCHMARK ===");

            // Получаем EntityManager
            EntityManager em = EntityManagerProvider.getEntityManager();
            HibernateBenchmark benchmark = new HibernateBenchmark(em);

            // Генерируем тестовые данные (минимум 10к точек)
            benchmark.generateTestData(10000);

            // Запускаем тесты производительности
            List<BenchmarkResult> results = benchmark.runBenchmarks();

            // Выводим результаты в консоль
            System.out.println("\n=== РЕЗУЛЬТАТЫ ТЕСТИРОВАНИЯ (framework - Hibernate) ===");
            System.out.println("| Операция | Время (ms) |");
            System.out.println("|----------|------------|");
            for (BenchmarkResult result : results) {
                System.out.printf("| %s | %d |\n", result.getOperation(), result.getTimeMs());
            }

            // Сохраняем результаты в файл
            try (java.io.FileWriter writer = new java.io.FileWriter("framework_performance.md")) {
                writer.write("# Результаты производительности (Framework Hibernate)\n\n");
                writer.write("**Тестовые данные:** 10,000+ записей в таблице points\n");
                writer.write("**База данных:** H2 (in-memory)\n\n");
                writer.write("| Операция | Время (ms) |\n");
                writer.write("|----------|------------|\n");
                for (BenchmarkResult result : results) {
                    writer.write(String.format("| %s | %d |\n", result.getOperation(), result.getTimeMs()));
                }

                writer.write("\n## Выводы\n");
                writer.write("1. Hibernate добавляет накладные расходы на маппинг объектов\n");
                writer.write("2. JPQL запросы удобнее для сложных операций\n");
                writer.write("3. Автоматическое управление транзакциями и связями\n");
            }

            logger.info("Результаты сохранены в framework_performance.md");

            // Закрываем соединение
            em.close();
            EntityManagerProvider.close();

        } catch (Exception e) {
            logger.error("Ошибка при выполнении бенчмарка: {}", e.getMessage(), e);
            System.err.println("ОШИБКА: " + e.getMessage());
            e.printStackTrace();
        }
    }
}