package com.example.lab6.mapper;

import com.example.lab6.dto.ResultDTO;
import entity.Function;
import entity.Result;

/**
 * Mapper для преобразования Result Entity в ResultDTO и обратно
 */
public class ResultMapper {
    
    public static ResultDTO toDTO(Result result) {
        if (result == null) {
            return null;
        }
        Long resultId = result.getFunction() != null ? result.getFunction().getId() : null;
        return new ResultDTO(result.getId(), resultId, result.getResult());
    }
    
    public static Result toEntity(ResultDTO dto, Function function) {
        if (dto == null) {
            return null;
        }
        Result result = new Result(function, dto.getResult());
        if (dto.getId() != null) {
            result.setId(dto.getId());
        }
        return result;
    }
}

