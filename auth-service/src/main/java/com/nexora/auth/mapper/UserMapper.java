package com.nexora.auth.mapper;

import com.nexora.auth.dto.request.RegisterRequest;
import com.nexora.auth.dto.response.UserResponse;
import com.nexora.auth.entity.Role;
import com.nexora.auth.entity.User;
import com.nexora.auth.enums.RoleType;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserMapper {
    public User toEntity(RegisterRequest request){
        return User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(request.getPassword())
                .build();
    }

    public UserResponse toResponse(User user){
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .accountStatus(user.getAccountStatus())
                .roles(mapRoles(user.getRoles()))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private Set<RoleType> mapRoles(Set<Role> roles){
        if (roles == null){
            return Set.of();
        }
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }
}
