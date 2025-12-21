package database.search;

import database.dto.UserDTO;
import database.dto.FunctionDTO;
import database.dto.PointDTO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Бенчмарк для сравнения скорости выполнения сортировки (Manual ветка)
 */
public class SortingPerformanceBenchmark {
    private static DataSearchService searchService;
    private static List<UserDTO> largeUserList;
    private static List<FunctionDTO> largeFunctionList;
    private static List<PointDTO> largePointList;
    private static final int DATA_SIZE = 10000;
    
    @BeforeAll
    static void setUp() {
        searchService = new DataSearchService();
        Random random = new Random();
        
        // Генерируем большие списки данных
        largeUserList = new ArrayList<>();
        for (int i = 0; i < DATA_SIZE; i++) {
            largeUserList.add(new UserDTO((long) i, "user" + random.nextInt(100000), 
                "pass" + i, "user" + i + "@test.com"));
        }
        
        largeFunctionList = new ArrayList<>();
        for (int i = 0; i < DATA_SIZE; i++) {
            largeFunctionList.add(new FunctionDTO((long) i, 1L, "function" + random.nextInt(100000), 
                "type" + (i % 10)));
        }
        
        largePointList = new ArrayList<>();
        for (int i = 0; i < DATA_SIZE; i++) {
            largePointList.add(new PointDTO((long) i, 1L, random.nextDouble() * 1000, 
                random.nextDouble() * 1000));
        }
    }
    
    @Test
    @DisplayName("Бенчмарк: сортировка пользователей по login")
    void benchmarkSortUsersByLogin() {
        SearchCriteria criteria = new SearchCriteria("login", "user", SearchCriteria.SearchOperator.CONTAINS);
        criteria.setSortField("login");
        criteria.setSortDirection(SearchCriteria.SortDirection.ASC);
        
        long startTime = System.currentTimeMillis();
        List<UserDTO> sorted = searchService.sortResults(new ArrayList<>(largeUserList), criteria);
        long endTime = System.currentTimeMillis();
        
        long duration = endTime - startTime;
        System.out.println("Сортировка " + DATA_SIZE + " пользователей по login: " + duration + " ms");
        
        // Проверяем, что сортировка работает
        assertTrue(sorted.size() > 0);
        for (int i = 1; i < Math.min(100, sorted.size()); i++) {
            assertTrue(sorted.get(i).getLogin().compareTo(sorted.get(i-1).getLogin()) >= 0);
        }
    }
    
    @Test
    @DisplayName("Бенчмарк: сортировка функций по name")
    void benchmarkSortFunctionsByName() {
        SearchCriteria criteria = new SearchCriteria("name", "function", SearchCriteria.SearchOperator.CONTAINS);
        criteria.setSortField("name");
        criteria.setSortDirection(SearchCriteria.SortDirection.ASC);
        
        long startTime = System.currentTimeMillis();
        List<FunctionDTO> sorted = searchService.sortResults(new ArrayList<>(largeFunctionList), criteria);
        long endTime = System.currentTimeMillis();
        
        long duration = endTime - startTime;
        System.out.println("Сортировка " + DATA_SIZE + " функций по name: " + duration + " ms");
        
        assertTrue(sorted.size() > 0);
    }
    
    @Test
    @DisplayName("Бенчмарк: сортировка точек по xValue")
    void benchmarkSortPointsByXValue() {
        SearchCriteria criteria = new SearchCriteria("xValue", 0.0, SearchCriteria.SearchOperator.GREATER_THAN);
        criteria.setSortField("xValue");
        criteria.setSortDirection(SearchCriteria.SortDirection.ASC);
        
        long startTime = System.currentTimeMillis();
        List<PointDTO> sorted = searchService.sortResults(new ArrayList<>(largePointList), criteria);
        long endTime = System.currentTimeMillis();
        
        long duration = endTime - startTime;
        System.out.println("Сортировка " + DATA_SIZE + " точек по xValue: " + duration + " ms");
        
        assertTrue(sorted.size() > 0);
        for (int i = 1; i < Math.min(100, sorted.size()); i++) {
            assertTrue(sorted.get(i).getXValue() >= sorted.get(i-1).getXValue());
        }
    }
    
    @Test
    @DisplayName("Бенчмарк: сортировка точек по yValue (DESC)")
    void benchmarkSortPointsByYValueDesc() {
        SearchCriteria criteria = new SearchCriteria("yValue", 0.0, SearchCriteria.SearchOperator.GREATER_THAN);
        criteria.setSortField("yValue");
        criteria.setSortDirection(SearchCriteria.SortDirection.DESC);
        
        long startTime = System.currentTimeMillis();
        List<PointDTO> sorted = searchService.sortResults(new ArrayList<>(largePointList), criteria);
        long endTime = System.currentTimeMillis();
        
        long duration = endTime - startTime;
        System.out.println("Сортировка " + DATA_SIZE + " точек по yValue (DESC): " + duration + " ms");
        
        assertTrue(sorted.size() > 0);
        for (int i = 1; i < Math.min(100, sorted.size()); i++) {
            assertTrue(sorted.get(i).getYValue() <= sorted.get(i-1).getYValue());
        }
    }
    
    @Test
    @DisplayName("Бенчмарк: сравнение ASC vs DESC")
    void benchmarkAscVsDesc() {
        SearchCriteria criteriaAsc = new SearchCriteria("xValue", 0.0, SearchCriteria.SearchOperator.GREATER_THAN);
        criteriaAsc.setSortField("xValue");
        criteriaAsc.setSortDirection(SearchCriteria.SortDirection.ASC);
        
        SearchCriteria criteriaDesc = new SearchCriteria("xValue", 0.0, SearchCriteria.SearchOperator.GREATER_THAN);
        criteriaDesc.setSortField("xValue");
        criteriaDesc.setSortDirection(SearchCriteria.SortDirection.DESC);
        
        long startAsc = System.currentTimeMillis();
        List<PointDTO> sortedAsc = searchService.sortResults(new ArrayList<>(largePointList), criteriaAsc);
        long endAsc = System.currentTimeMillis();
        
        long startDesc = System.currentTimeMillis();
        List<PointDTO> sortedDesc = searchService.sortResults(new ArrayList<>(largePointList), criteriaDesc);
        long endDesc = System.currentTimeMillis();
        
        long durationAsc = endAsc - startAsc;
        long durationDesc = endDesc - startDesc;
        
        System.out.println("Сортировка ASC: " + durationAsc + " ms");
        System.out.println("Сортировка DESC: " + durationDesc + " ms");
        
        assertTrue(sortedAsc.size() > 0);
        assertTrue(sortedDesc.size() > 0);
    }
}

