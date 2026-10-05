package com.nexora.auth.service;

import com.nexora.auth.dto.request.LoginRequest;
import com.nexora.auth.dto.request.RefreshTokenRequest;
import com.nexora.auth.dto.request.RegisterRequest;
import com.nexora.auth.dto.response.AuthResponse;
import com.nexora.auth.dto.response.UserResponse;

public interface AuthService {

    UserResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);

    void logout(String refreshToken);
}
