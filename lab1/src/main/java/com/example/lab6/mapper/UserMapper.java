package com.example.lab6.mapper;

import com.example.lab6.dto.UserDTO;
import entity.User;

/**
 * Mapper для преобразования User Entity в UserDTO и обратно
 */
public class UserMapper {
    
    public static UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }
        return new UserDTO(user.getId(), user.getLogin(), user.getPassword(), user.getEmail(), user.getRole());
    }
    
    public static User toEntity(UserDTO dto) {
        if (dto == null) {
            return null;
        }
        User user = new User(dto.getLogin(), dto.getPassword(), dto.getEmail());
        if (dto.getRole() != null && !dto.getRole().isBlank()) {
            user.setRole(dto.getRole());
        }
        if (dto.getId() != null) {
            user.setId(dto.getId());
        }
        return user;
    }
    
    public static UserDTO toDTOSafe(User user) {
        if (user == null) {
            return null;
        }
        // Безопасная версия без пароля
        return new UserDTO(user.getId(), user.getLogin(), null, user.getEmail(), user.getRole());
    }
}

