package entity;

import javax.persistence.*;

/**
 * Entity класс для таблицы points
 */
@Entity
@Table(name = "points")
public class Point {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "func_id", nullable = false)
    private Function function;
    
    @Column(name = "x_value", nullable = false)
    private Double xValue;
    
    @Column(name = "y_value", nullable = false)
    private Double yValue;
    
    // Конструкторы
    public Point() {
    }
    
    public Point(Function function, Double xValue, Double yValue) {
        this.function = function;
        this.xValue = xValue;
        this.yValue = yValue;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Function getFunction() {
        return function;
    }
    
    public void setFunction(Function function) {
        this.function = function;
    }
    
    public Double getXValue() {
        return xValue;
    }
    
    public void setXValue(Double xValue) {
        this.xValue = xValue;
    }
    
    public Double getYValue() {
        return yValue;
    }
    
    public void setYValue(Double yValue) {
        this.yValue = yValue;
    }
    
    @Override
    public String toString() {
        return "Point{" +
                "id=" + id +
                ", xValue=" + xValue +
                ", yValue=" + yValue +
                '}';
    }
}

