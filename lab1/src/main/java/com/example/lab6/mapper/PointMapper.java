package com.example.lab6.mapper;

import com.example.lab6.dto.PointDTO;
import entity.Function;
import entity.Point;

/**
 * Mapper для преобразования Point Entity в PointDTO и обратно
 */
public class PointMapper {
    
    public static PointDTO toDTO(Point point) {
        if (point == null) {
            return null;
        }
        Long funcId = point.getFunction() != null ? point.getFunction().getId() : null;
        return new PointDTO(point.getId(), funcId, point.getXValue(), point.getYValue());
    }
    
    public static Point toEntity(PointDTO dto, Function function) {
        if (dto == null) {
            return null;
        }
        Point point = new Point(function, dto.getXValue(), dto.getYValue());
        if (dto.getId() != null) {
            point.setId(dto.getId());
        }
        return point;
    }
}

