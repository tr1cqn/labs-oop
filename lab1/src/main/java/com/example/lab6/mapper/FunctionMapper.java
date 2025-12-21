package com.example.lab6.mapper;

import com.example.lab6.dto.FunctionDTO;
import entity.Function;
import entity.User;

/**
 * Mapper для преобразования Function Entity в FunctionDTO и обратно
 */
public class FunctionMapper {
    
    public static FunctionDTO toDTO(Function function) {
        if (function == null) {
            return null;
        }
        Long userId = function.getUser() != null ? function.getUser().getId() : null;
        return new FunctionDTO(function.getId(), userId, function.getName(), function.getType());
    }
    
    public static Function toEntity(FunctionDTO dto, User user) {
        if (dto == null) {
            return null;
        }
        Function function = new Function(user, dto.getName(), dto.getType());
        if (dto.getId() != null) {
            function.setId(dto.getId());
        }
        return function;
    }
}

