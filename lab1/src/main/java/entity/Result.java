package entity;

import javax.persistence.*;

/**
 * Entity класс для таблицы result
 */
@Entity
@Table(name = "result")
public class Result {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "result_id", nullable = false)
    private Function function;
    
    @Column(name = "result", nullable = false, columnDefinition = "TEXT")
    private String result;
    
    // Конструкторы
    public Result() {
    }
    
    public Result(Function function, String result) {
        this.function = function;
        this.result = result;
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
    
    public String getResult() {
        return result;
    }
    
    public void setResult(String result) {
        this.result = result;
    }
    
    @Override
    public String toString() {
        return "Result{" +
                "id=" + id +
                ", result='" + result + '\'' +
                '}';
    }
}

