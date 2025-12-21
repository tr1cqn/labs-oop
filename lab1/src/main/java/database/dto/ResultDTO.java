package database.dto;

/**
 * DTO класс для таблицы result
 */
public class ResultDTO {
    private Long id;
    private Long resultId;
    private String result;
    
    // Конструкторы
    public ResultDTO() {
    }
    
    public ResultDTO(Long id, Long resultId, String result) {
        this.id = id;
        this.resultId = resultId;
        this.result = result;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getResultId() {
        return resultId;
    }
    
    public void setResultId(Long resultId) {
        this.resultId = resultId;
    }
    
    public String getResult() {
        return result;
    }
    
    public void setResult(String result) {
        this.result = result;
    }
    
    @Override
    public String toString() {
        return "ResultDTO{" +
                "id=" + id +
                ", resultId=" + resultId +
                ", result='" + result + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        ResultDTO resultDTO = (ResultDTO) o;
        
        if (id != null ? !id.equals(resultDTO.id) : resultDTO.id != null) return false;
        if (resultId != null ? !resultId.equals(resultDTO.resultId) : resultDTO.resultId != null) return false;
        return result != null ? result.equals(resultDTO.result) : resultDTO.result == null;
    }
    
    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (resultId != null ? resultId.hashCode() : 0);
        result = 31 * result + (this.result != null ? this.result.hashCode() : 0);
        return result;
    }
}

