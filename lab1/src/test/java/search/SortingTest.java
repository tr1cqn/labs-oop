package search;

import entity.User;
import entity.Function;
import entity.Point;
import entity.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для сортировки данных
 */
public class SortingTest {
    private DataSearchService searchService;
    private List<User> users;
    private List<Function> functions;
    private List<Point> points;
    private List<Result> results;
    
    @BeforeEach
    void setUp() {
        searchService = new DataSearchService();
        
        // Создаем тестовые данные
        users = new ArrayList<>();
        users.add(new User("user3", "pass3", "user3@test.com"));
        users.add(new User("user1", "pass1", "user1@test.com"));
        users.add(new User("user2", "pass2", "user2@test.com"));
        
        functions = new ArrayList<>();
        User user1 = users.get(1);
        functions.add(new Function(user1, "function3", "type3"));
        functions.add(new Function(user1, "function1", "type1"));
        functions.add(new Function(user1, "function2", "type2"));
        
        points = new ArrayList<>();
        Function func1 = functions.get(1);
        points.add(new Point(func1, 30.0, 300.0));
        points.add(new Point(func1, 10.0, 100.0));
        points.add(new Point(func1, 20.0, 200.0));
        
        results = new ArrayList<>();
        results.add(new Result(func1, "result3"));
        results.add(new Result(func1, "result1"));
        results.add(new Result(func1, "result2"));
    }
    
    @Test
    @DisplayName("Тест сортировки пользователей по login (ASC)")
    void testSortUsersByLoginAsc() {
        SearchCriteria criteria = new SearchCriteria("login", "user", SearchCriteria.SearchOperator.CONTAINS);
        criteria.setSortField("login");
        criteria.setSortDirection(SearchCriteria.SortDirection.ASC);
        
        List<User> sorted = searchService.sortResults(users, criteria);
        
        assertEquals(3, sorted.size());
        assertEquals("user1", sorted.get(0).getLogin());
        assertEquals("user2", sorted.get(1).getLogin());
        assertEquals("user3", sorted.get(2).getLogin());
    }
    
    @Test
    @DisplayName("Тест сортировки пользователей по login (DESC)")
    void testSortUsersByLoginDesc() {
        SearchCriteria criteria = new SearchCriteria("login", "user", SearchCriteria.SearchOperator.CONTAINS);
        criteria.setSortField("login");
        criteria.setSortDirection(SearchCriteria.SortDirection.DESC);
        
        List<User> sorted = searchService.sortResults(users, criteria);
        
        assertEquals(3, sorted.size());
        assertEquals("user3", sorted.get(0).getLogin());
        assertEquals("user2", sorted.get(1).getLogin());
        assertEquals("user1", sorted.get(2).getLogin());
    }
    
    @Test
    @DisplayName("Тест сортировки пользователей по id (ASC)")
    void testSortUsersByIdAsc() {
        // Устанавливаем ID для тестирования
        users.get(0).setId(3L);
        users.get(1).setId(1L);
        users.get(2).setId(2L);
        
        SearchCriteria criteria = new SearchCriteria("id", 0L, SearchCriteria.SearchOperator.GREATER_THAN);
        criteria.setSortField("id");
        criteria.setSortDirection(SearchCriteria.SortDirection.ASC);
        
        List<User> sorted = searchService.sortResults(users, criteria);
        
        assertEquals(3, sorted.size());
        assertEquals(1L, sorted.get(0).getId());
        assertEquals(2L, sorted.get(1).getId());
        assertEquals(3L, sorted.get(2).getId());
    }
    
    @Test
    @DisplayName("Тест сортировки функций по name (ASC)")
    void testSortFunctionsByNameAsc() {
        SearchCriteria criteria = new SearchCriteria("name", "function", SearchCriteria.SearchOperator.CONTAINS);
        criteria.setSortField("name");
        criteria.setSortDirection(SearchCriteria.SortDirection.ASC);
        
        List<Function> sorted = searchService.sortResults(functions, criteria);
        
        assertEquals(3, sorted.size());
        assertEquals("function1", sorted.get(0).getName());
        assertEquals("function2", sorted.get(1).getName());
        assertEquals("function3", sorted.get(2).getName());
    }
    
    @Test
    @DisplayName("Тест сортировки точек по xValue (ASC)")
    void testSortPointsByXValueAsc() {
        SearchCriteria criteria = new SearchCriteria("xValue", 0.0, SearchCriteria.SearchOperator.GREATER_THAN);
        criteria.setSortField("xValue");
        criteria.setSortDirection(SearchCriteria.SortDirection.ASC);
        
        List<Point> sorted = searchService.sortResults(points, criteria);
        
        assertEquals(3, sorted.size());
        assertEquals(10.0, sorted.get(0).getXValue());
        assertEquals(20.0, sorted.get(1).getXValue());
        assertEquals(30.0, sorted.get(2).getXValue());
    }
    
    @Test
    @DisplayName("Тест сортировки точек по yValue (DESC)")
    void testSortPointsByYValueDesc() {
        SearchCriteria criteria = new SearchCriteria("yValue", 0.0, SearchCriteria.SearchOperator.GREATER_THAN);
        criteria.setSortField("yValue");
        criteria.setSortDirection(SearchCriteria.SortDirection.DESC);
        
        List<Point> sorted = searchService.sortResults(points, criteria);
        
        assertEquals(3, sorted.size());
        assertEquals(300.0, sorted.get(0).getYValue());
        assertEquals(200.0, sorted.get(1).getYValue());
        assertEquals(100.0, sorted.get(2).getYValue());
    }
    
    @Test
    @DisplayName("Тест сортировки результатов по result (ASC)")
    void testSortResultsByResultAsc() {
        SearchCriteria criteria = new SearchCriteria("result", "result", SearchCriteria.SearchOperator.CONTAINS);
        criteria.setSortField("result");
        criteria.setSortDirection(SearchCriteria.SortDirection.ASC);
        
        List<Result> sorted = searchService.sortResults(results, criteria);
        
        assertEquals(3, sorted.size());
        assertEquals("result1", sorted.get(0).getResult());
        assertEquals("result2", sorted.get(1).getResult());
        assertEquals("result3", sorted.get(2).getResult());
    }
    
    @Test
    @DisplayName("Тест сортировки пустого списка")
    void testSortEmptyList() {
        List<User> emptyList = new ArrayList<>();
        SearchCriteria criteria = new SearchCriteria("login", "user", SearchCriteria.SearchOperator.CONTAINS);
        criteria.setSortField("login");
        
        List<User> sorted = searchService.sortResults(emptyList, criteria);
        
        assertTrue(sorted.isEmpty());
    }
    
    @Test
    @DisplayName("Тест сортировки без указания поля")
    void testSortWithoutField() {
        SearchCriteria criteria = new SearchCriteria("login", "user", SearchCriteria.SearchOperator.CONTAINS);
        // sortField не установлен
        
        List<User> sorted = searchService.sortResults(users, criteria);
        
        // Должен вернуть исходный список без изменений
        assertEquals(3, sorted.size());
    }
}

