package benchmark;

import java.util.List;

/**
 * Класс для объединения результатов бенчмарков Manual и Framework
 */
public class PerformanceComparison {
    
    /**
     * Объединяет результаты Manual и Framework в одну таблицу сравнения
     */
    public static void createComparisonTable(List<BenchmarkResult> manualResults, 
                                            List<BenchmarkResult> frameworkResults) {
        ExcelWriter excelWriter = new ExcelWriter();
        excelWriter.saveComparisonToExcel(manualResults, frameworkResults, "performance_comparison.xlsx");
        
        System.out.println("\n=== Сравнение результатов ===\n");
        System.out.println("Операция | Manual (JDBC), мс | Framework (Hibernate), мс | Разница, мс");
        System.out.println("---------|-------------------|---------------------------|-------------");
        
        for (int i = 0; i < manualResults.size() && i < frameworkResults.size(); i++) {
            BenchmarkResult manual = manualResults.get(i);
            BenchmarkResult framework = frameworkResults.get(i);
            
            if (manual.getQueryName().equals(framework.getQueryName())) {
                long diff = manual.getDuration() - framework.getDuration();
                System.out.printf("%-30s | %18d | %26d | %12d%n", 
                    manual.getQueryName(), 
                    manual.getDuration(), 
                    framework.getDuration(), 
                    diff);
            }
        }
    }
}

