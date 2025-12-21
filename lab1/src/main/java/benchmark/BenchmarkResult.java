package benchmark;

/**
 * Класс для хранения результатов бенчмарка
 */
public class BenchmarkResult {
    private final long duration; // время выполнения в миллисекундах
    private final String queryName; // название операции
    private final int recordsCount; // количество записей
    
    public BenchmarkResult(long duration, String queryName, int recordsCount) {
        this.duration = duration;
        this.queryName = queryName;
        this.recordsCount = recordsCount;
    }
    
    public long getDuration() {
        return duration;
    }
    
    public String getQueryName() {
        return queryName;
    }
    
    public int getRecordsCount() {
        return recordsCount;
    }
    
    @Override
    public String toString() {
        return String.format("%s: %d ms (%d records)", queryName, duration, recordsCount);
    }
}

