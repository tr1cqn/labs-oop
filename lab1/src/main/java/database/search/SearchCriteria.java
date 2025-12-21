package database.search;

import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * Класс для критериев поиска
 */
public class SearchCriteria {
    private String fieldName;
    private Object value;
    private SearchOperator operator;
    private List<SearchCriteria> multipleCriteria;
    private boolean isMultiple;
    private String sortField;
    private SortDirection sortDirection;
    
    /**
     * Операторы сравнения для поиска
     */
    public enum SearchOperator {
        EQUALS,
        CONTAINS,
        STARTS_WITH,
        ENDS_WITH,
        GREATER_THAN,
        LESS_THAN,
        GREATER_OR_EQUAL,
        LESS_OR_EQUAL
    }
    
    /**
     * Направление сортировки
     */
    public enum SortDirection {
        ASC,
        DESC
    }
    
    /**
     * Конструктор для одиночного поиска
     */
    public SearchCriteria(String fieldName, Object value, SearchOperator operator) {
        this.fieldName = fieldName;
        this.value = value;
        this.operator = operator;
        this.isMultiple = false;
        this.multipleCriteria = new ArrayList<>();
        this.sortDirection = SortDirection.ASC;
    }
    
    /**
     * Конструктор для множественного поиска
     */
    public SearchCriteria(List<SearchCriteria> criteria) {
        this.multipleCriteria = new ArrayList<>(criteria);
        this.isMultiple = true;
        this.sortDirection = SortDirection.ASC;
    }
    
    public String getFieldName() {
        return fieldName;
    }
    
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
    
    public Object getValue() {
        return value;
    }
    
    public void setValue(Object value) {
        this.value = value;
    }
    
    public SearchOperator getOperator() {
        return operator;
    }
    
    public void setOperator(SearchOperator operator) {
        this.operator = operator;
    }
    
    public List<SearchCriteria> getMultipleCriteria() {
        return multipleCriteria;
    }
    
    public void setMultipleCriteria(List<SearchCriteria> multipleCriteria) {
        this.multipleCriteria = multipleCriteria;
    }
    
    public boolean isMultiple() {
        return isMultiple;
    }
    
    public void setMultiple(boolean multiple) {
        isMultiple = multiple;
    }
    
    public String getSortField() {
        return sortField;
    }
    
    public void setSortField(String sortField) {
        this.sortField = sortField;
    }
    
    public SortDirection getSortDirection() {
        return sortDirection;
    }
    
    public void setSortDirection(SortDirection sortDirection) {
        this.sortDirection = sortDirection;
    }
    
    /**
     * Создает компаратор для сортировки на основе настроек
     */
    @SuppressWarnings("unchecked")
    public <T> Comparator<T> createComparator() {
        if (sortField == null) {
            return null;
        }
        
        return (a, b) -> {
            try {
                java.lang.reflect.Field fieldA = a.getClass().getDeclaredField(sortField);
                java.lang.reflect.Field fieldB = b.getClass().getDeclaredField(sortField);
                fieldA.setAccessible(true);
                fieldB.setAccessible(true);
                
                Object valueA = fieldA.get(a);
                Object valueB = fieldB.get(b);
                
                if (valueA == null && valueB == null) return 0;
                if (valueA == null) return sortDirection == SortDirection.ASC ? -1 : 1;
                if (valueB == null) return sortDirection == SortDirection.ASC ? 1 : -1;
                
                Comparable<Object> compA = (Comparable<Object>) valueA;
                int result = compA.compareTo(valueB);
                
                return sortDirection == SortDirection.ASC ? result : -result;
            } catch (Exception e) {
                return 0;
            }
        };
    }
    
    @Override
    public String toString() {
        if (isMultiple) {
            return "SearchCriteria{multiple=" + multipleCriteria.size() + 
                   ", sortField=" + sortField + 
                   ", sortDirection=" + sortDirection + "}";
        } else {
            return "SearchCriteria{fieldName='" + fieldName + 
                   "', operator=" + operator + 
                   ", value=" + value + 
                   ", sortField=" + sortField + 
                   ", sortDirection=" + sortDirection + "}";
        }
    }
}

