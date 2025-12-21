package database.dto;

/**
 * DTO класс для таблицы points
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
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        PointDTO pointDTO = (PointDTO) o;
        
        if (id != null ? !id.equals(pointDTO.id) : pointDTO.id != null) return false;
        if (funcId != null ? !funcId.equals(pointDTO.funcId) : pointDTO.funcId != null) return false;
        if (xValue != null ? !xValue.equals(pointDTO.xValue) : pointDTO.xValue != null) return false;
        return yValue != null ? yValue.equals(pointDTO.yValue) : pointDTO.yValue == null;
    }
    
    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (funcId != null ? funcId.hashCode() : 0);
        result = 31 * result + (xValue != null ? xValue.hashCode() : 0);
        result = 31 * result + (yValue != null ? yValue.hashCode() : 0);
        return result;
    }
}

