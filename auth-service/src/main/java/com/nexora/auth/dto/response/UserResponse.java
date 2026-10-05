package com.nexora.auth.dto.response;

import com.nexora.auth.enums.AccountStatus;
import com.nexora.auth.enums.RoleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private String id;
    private String username;
    private String email;
    private AccountStatus accountStatus;
    private Set<RoleType> roles;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
