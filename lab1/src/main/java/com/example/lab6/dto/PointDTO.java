package com.example.lab6.dto;

/**
 * DTO класс для точки
 */
public class PointDTO {
    private Long id;
    private Long funcId;
    private Double xValue;
    private Double yValue;

    // Конструкторы
    public PointDTO() {
    }

    public PointDTO(Long id, Long funcId, Double xValue, Double yValue) {
        this.id = id;
        this.funcId = funcId;
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

    public Long getFuncId() {
        return funcId;
    }

    public void setFuncId(Long funcId) {
        this.funcId = funcId;
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
        return "PointDTO{" +
                "id=" + id +
                ", funcId=" + funcId +
                ", xValue=" + xValue +
                ", yValue=" + yValue +
                '}';
    }
}

