package search;

import entity.User;
import entity.Function;
import entity.Point;
import entity.Result;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс для представления иерархических данных
 * Структура: User -> Functions -> Points/Results
 */
public class HierarchicalDataNode {
    private User user;
    private List<FunctionNode> functions;
    private NodeType type;
    
    /**
     * Тип узла в иерархии
     */
    public enum NodeType {
        USER,
        FUNCTION,
        POINT,
        RESULT
    }
    
    /**
     * Внутренний класс для представления функции с её дочерними элементами
     */
    public static class FunctionNode {
        private Function function;
        private List<Point> points;
        private List<Result> results;
        
        public FunctionNode(Function function) {
            this.function = function;
            this.points = new ArrayList<>();
            this.results = new ArrayList<>();
        }
        
        public Function getFunction() {
            return function;
        }
        
        public void setFunction(Function function) {
            this.function = function;
        }
        
        public List<Point> getPoints() {
            return points;
        }
        
        public void setPoints(List<Point> points) {
            this.points = points;
        }
        
        public List<Result> getResults() {
            return results;
        }
        
        public void setResults(List<Result> results) {
            this.results = results;
        }
        
        public void addPoint(Point point) {
            this.points.add(point);
        }
        
        public void addResult(Result result) {
            this.results.add(result);
        }
    }
    
    /**
     * Конструктор для создания узла пользователя
     */
    public HierarchicalDataNode(User user) {
        this.user = user;
        this.functions = new ArrayList<>();
        this.type = NodeType.USER;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public List<FunctionNode> getFunctions() {
        return functions;
    }
    
    public void setFunctions(List<FunctionNode> functions) {
        this.functions = functions;
    }
    
    public void addFunction(FunctionNode function) {
        this.functions.add(function);
    }
    
    public NodeType getType() {
        return type;
    }
    
    public void setType(NodeType type) {
        this.type = type;
    }
    
    /**
     * Получить все дочерние элементы (функции)
     */
    public List<FunctionNode> getChildren() {
        return new ArrayList<>(functions);
    }
    
    /**
     * Проверяет, является ли узел листовым (не имеет дочерних элементов)
     */
    public boolean isLeaf() {
        return functions.isEmpty();
    }
}

