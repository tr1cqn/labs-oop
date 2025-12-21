package database.search;

import database.dto.UserDTO;
import database.dto.FunctionDTO;
import database.dto.PointDTO;
import database.dto.ResultDTO;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс для представления иерархических данных
 * Структура: User -> Functions -> Points/Results
 */
public class HierarchicalDataNode {
    private UserDTO user;
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
        private FunctionDTO function;
        private List<PointDTO> points;
        private List<ResultDTO> results;
        
        public FunctionNode(FunctionDTO function) {
            this.function = function;
            this.points = new ArrayList<>();
            this.results = new ArrayList<>();
        }
        
        public FunctionDTO getFunction() {
            return function;
        }
        
        public void setFunction(FunctionDTO function) {
            this.function = function;
        }
        
        public List<PointDTO> getPoints() {
            return points;
        }
        
        public void setPoints(List<PointDTO> points) {
            this.points = points;
        }
        
        public List<ResultDTO> getResults() {
            return results;
        }
        
        public void setResults(List<ResultDTO> results) {
            this.results = results;
        }
        
        public void addPoint(PointDTO point) {
            this.points.add(point);
        }
        
        public void addResult(ResultDTO result) {
            this.results.add(result);
        }
    }
    
    /**
     * Конструктор для создания узла пользователя
     */
    public HierarchicalDataNode(UserDTO user) {
        this.user = user;
        this.functions = new ArrayList<>();
        this.type = NodeType.USER;
    }
    
    public UserDTO getUser() {
        return user;
    }
    
    public void setUser(UserDTO user) {
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

