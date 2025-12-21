package benchmark;

import java.util.List;

/**
 * Класс для запуска бенчмарков Framework (Hibernate Repository)
 */
public class FrameworkBenchmarkRunner {
    
    public static void main(String[] args) {
        System.out.println("=== Бенчмарк производительности Framework (Hibernate) ===\n");
        
        FrameworkBenchmarkService benchmarkService = new FrameworkBenchmarkService();
        
        System.out.println("Генерация 10,000+ записей...");
        System.out.println("Запуск бенчмарков...\n");
        
        List<BenchmarkResult> results = benchmarkService.runBenchmarks();
        
        // Вывод результатов в консоль
        System.out.println("=== Результаты ===\n");
        for (BenchmarkResult result : results) {
            System.out.println(result);
        }
        
        // Сохранение в Excel
        ExcelWriter excelWriter = new ExcelWriter();
        excelWriter.saveToExcel(results, "performance_framework.xlsx");
        
        System.out.println("\nБенчмарк завершен. Результаты сохранены в performance_framework.xlsx");
    }
}

