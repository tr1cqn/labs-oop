package entity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity класс для таблицы functions
 */
@Entity
@Table(name = "functions")
public class Function {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "name", nullable = false, length = 200)
    private String name;
    
    @Column(name = "type", nullable = false, length = 50)
    private String type;
    
    @OneToMany(mappedBy = "function", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Point> points = new ArrayList<>();
    
    @OneToMany(mappedBy = "function", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Result> results = new ArrayList<>();
    
    // Конструкторы
    public Function() {
    }
    
    public Function(User user, String name, String type) {
        this.user = user;
        this.name = name;
        this.type = type;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
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
    
    // Вспомогательные методы
    public void addPoint(Point point) {
        points.add(point);
        point.setFunction(this);
    }
    
    public void removePoint(Point point) {
        points.remove(point);
        point.setFunction(null);
    }
    
    public void addResult(Result result) {
        results.add(result);
        result.setFunction(this);
    }
    
    public void removeResult(Result result) {
        results.remove(result);
        result.setFunction(null);
    }
    
    @Override
    public String toString() {
        return "Function{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}

