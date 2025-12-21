package database;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PerformanceBenchmark {
    private static final Logger logger = LogManager.getLogger(PerformanceBenchmark.class);
    private final Connection connection;

    public PerformanceBenchmark(Connection connection) {
        this.connection = connection;
    }

    private long measureTime(Runnable operation) {
        long startTime = System.nanoTime();
        operation.run();
        long endTime = System.nanoTime();
        return (endTime - startTime) / 1_000_000; // в миллисекундах
    }

    public void generateTestData(int recordsCount) throws SQLException {
        logger.info("Генерация {} тестовых записей...", recordsCount);

        // Очистка таблиц (для H2)
        try (Statement stmt = connection.createStatement()) {
            // H2 не поддерживает TRUNCATE нескольких таблиц, делаем по отдельности
            // Важен порядок из-за foreign keys!
            stmt.execute("DELETE FROM result");
            stmt.execute("DELETE FROM points");
            stmt.execute("DELETE FROM functions");
            stmt.execute("DELETE FROM users");

            // Сброс автоинкремента (для H2)
            stmt.execute("ALTER TABLE users ALTER COLUMN id RESTART WITH 1");
            stmt.execute("ALTER TABLE functions ALTER COLUMN id RESTART WITH 1");
            stmt.execute("ALTER TABLE points ALTER COLUMN id RESTART WITH 1");
            stmt.execute("ALTER TABLE result ALTER COLUMN id RESTART WITH 1");
        }

        // 1. Вставка 100 пользователей
        logger.info("Вставка пользователей...");
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO users (login, password, email) VALUES (?, ?, ?)")) {
            for (int i = 0; i < 100; i++) {
                pstmt.setString(1, "user" + i);
                pstmt.setString(2, "pass" + i);
                pstmt.setString(3, "user" + i + "@test.com");
                pstmt.executeUpdate();
            }
        }

        // 2. Вставка функций (1/10 от общего количества)
        logger.info("Вставка функций...");
        int functionCount = recordsCount / 10;
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO functions (user_id, name, type) VALUES (?, ?, ?)")) {
            for (int i = 0; i < functionCount; i++) {
                pstmt.setInt(1, (i % 100) + 1); // user_id от 1 до 100
                pstmt.setString(2, "function_" + i);
                pstmt.setString(3, i % 2 == 0 ? "linear" : "quadratic");
                pstmt.executeUpdate();
            }
        }

        // 3. Вставка точек (основной объем - 10к+ записей)
        logger.info("Вставка точек...");
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO points (func_id, x_value, y_value) VALUES (?, ?, ?)")) {
            for (int funcId = 1; funcId <= functionCount; funcId++) {
                for (int point = 0; point < 10; point++) {
                    pstmt.setInt(1, funcId);
                    pstmt.setDouble(2, point * 0.5);
                    pstmt.setDouble(3, Math.sin(point * 0.5));
                    pstmt.executeUpdate();
                }
            }
        }

        // 4. Вставка результатов
        logger.info("Вставка результатов...");
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO result (result_id, result) VALUES (?, ?)")) {
            for (int funcId = 1; funcId <= functionCount; funcId++) {
                pstmt.setInt(1, funcId);
                pstmt.setString(2, "Result for function " + funcId);
                pstmt.executeUpdate();
            }
        }

        logger.info("Тестовые данные сгенерированы: {} функций, {} точек",
                functionCount, functionCount * 10);
    }

    public List<BenchmarkResult> runBenchmarks() throws SQLException {
        List<BenchmarkResult> results = new ArrayList<>();

        // 1. SELECT всех точек (самая большая таблица)
        logger.info("Тест 1: SELECT всех точек");
        long selectTime = measureTime(() -> {
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM points")) {
                while (rs.next()) {
                    rs.getDouble("x_value");
                    rs.getDouble("y_value");
                }
            } catch (SQLException e) {
                logger.error("Ошибка SELECT: {}", e.getMessage());
            }
        });
        results.add(new BenchmarkResult("SELECT * FROM points", selectTime, "manual"));

        // 2. SELECT с JOIN (функции + точки)
        logger.info("Тест 2: SELECT с JOIN");
        long joinTime = measureTime(() -> {
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(
                         "SELECT f.name, p.x_value, p.y_value " +
                                 "FROM functions f " +
                                 "JOIN points p ON f.id = p.func_id " +
                                 "WHERE f.user_id = 1 LIMIT 100")) {
                while (rs.next()) {
                    rs.getString("name");
                    rs.getDouble("x_value");
                    rs.getDouble("y_value");
                }
            } catch (SQLException e) {
                logger.error("Ошибка JOIN: {}", e.getMessage());
            }
        });
        results.add(new BenchmarkResult("JOIN functions + points", joinTime, "manual"));

        // 3. INSERT новой точки
        logger.info("Тест 3: INSERT точки");
        long insertTime = measureTime(() -> {
            try (PreparedStatement pstmt = connection.prepareStatement(
                    "INSERT INTO points (func_id, x_value, y_value) VALUES (1, 100.0, 200.0)")) {
                pstmt.executeUpdate();
            } catch (SQLException e) {
                logger.error("Ошибка INSERT: {}", e.getMessage());
            }
        });
        results.add(new BenchmarkResult("INSERT point", insertTime, "manual"));

        // 4. UPDATE точки
        logger.info("Тест 4: UPDATE точки");
        long updateTime = measureTime(() -> {
            try (PreparedStatement pstmt = connection.prepareStatement(
                    "UPDATE points SET y_value = 300.0 WHERE id = 1")) {
                pstmt.executeUpdate();
            } catch (SQLException e) {
                logger.error("Ошибка UPDATE: {}", e.getMessage());
            }
        });
        results.add(new BenchmarkResult("UPDATE point", updateTime, "manual"));

        // 5. DELETE точки
        logger.info("Тест 5: DELETE точки");
        long deleteTime = measureTime(() -> {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("DELETE FROM points WHERE id = 999999");
            } catch (SQLException e) {
                logger.error("Ошибка DELETE: {}", e.getMessage());
            }
        });
        results.add(new BenchmarkResult("DELETE point", deleteTime, "manual"));

        return results;
    }

    public static class BenchmarkResult {
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

    public static void main(String[] args) {
        try {
            Connection connection = ConnectionManager.getConnection();
            PerformanceBenchmark benchmark = new PerformanceBenchmark(connection);

            // Генерируем тестовые данные (минимум 10к точек)
            benchmark.generateTestData(10000);

            // Запускаем тесты
            List<BenchmarkResult> results = benchmark.runBenchmarks();

            // Выводим результаты
            System.out.println("\n=== РЕЗУЛЬТАТЫ ТЕСТИРОВАНИЯ (manual) ===");
            System.out.println("| Операция | Время (ms) |");
            System.out.println("|----------|------------|");
            for (BenchmarkResult result : results) {
                System.out.printf("| %s | %d |\n", result.getOperation(), result.getTimeMs());
            }

            // Сохраняем в файл
            try (java.io.FileWriter writer = new java.io.FileWriter("manual_performance.md")) {
                writer.write("# Результаты производительности (Manual JDBC)\n\n");
                writer.write("**Тестовые данные:** 10,000+ записей в таблице points\n\n");
                writer.write("| Операция | Время (ms) |\n");
                writer.write("|----------|------------|\n");
                for (BenchmarkResult result : results) {
                    writer.write(String.format("| %s | %d |\n", result.getOperation(), result.getTimeMs()));
                }
            }

            logger.info("Результаты сохранены в manual_performance.md");
            connection.close();

        } catch (Exception e) {
            logger.error("Ошибка: {}", e.getMessage(), e);
        }
    }
}