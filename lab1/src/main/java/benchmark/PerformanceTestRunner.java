package benchmark;

/**
 * Класс для запуска бенчмарков производительности
 */
public class PerformanceTestRunner {
    
    public static void main(String[] args) {
        FrameworkPerformanceBenchmark benchmark = new FrameworkPerformanceBenchmark();
        
        System.out.println("=== Бенчмарк производительности Framework (Hibernate) ===\n");
        
        // Генерация тестовых данных
        benchmark.generateTestData();
        
        System.out.println("\n=== Измерение производительности ===\n");
        
        // Запуск бенчмарков
        FrameworkPerformanceBenchmark.PerformanceResults results = benchmark.runBenchmarks();
        
        // Вывод результатов
        System.out.println("\n=== Результаты ===\n");
        System.out.println("Подход: " + results.getApproach());
        System.out.println("Поиск по ID (100 операций): " + results.getFindById() + " мс");
        System.out.println("Поиск всех записей: " + results.getFindAll() + " мс");
        System.out.println("Поиск с условиями (50 операций): " + results.getFindWithConditions() + " мс");
        System.out.println("Вставка (100 записей): " + results.getInsert() + " мс");
        System.out.println("Обновление (100 записей): " + results.getUpdate() + " мс");
        System.out.println("Удаление (100 записей): " + results.getDelete() + " мс");
        
        // Создание таблицы результатов
        PerformanceComparison.createFrameworkTable(results);
        
        System.out.println("\nДля полного сравнения необходимо запустить аналогичный бенчмарк в ветке manual");
    }
}

