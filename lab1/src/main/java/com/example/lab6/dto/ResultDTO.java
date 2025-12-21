package com.example.lab6.dto;

/**
 * DTO класс для результата
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
}

