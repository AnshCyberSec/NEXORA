package com.nexora.auth.service.impl;

import com.nexora.auth.dto.request.LoginRequest;
import com.nexora.auth.dto.request.RefreshTokenRequest;
import com.nexora.auth.dto.request.RegisterRequest;
import com.nexora.auth.dto.response.AuthResponse;
import com.nexora.auth.dto.response.UserResponse;
import com.nexora.auth.entity.Role;
import com.nexora.auth.entity.User;
import com.nexora.auth.enums.RoleType;
import com.nexora.auth.exception.InvalidCredentialsException;
import com.nexora.auth.exception.ResourceNotFoundException;
import com.nexora.auth.exception.UserAlreadyExistsException;
import com.nexora.auth.mapper.UserMapper;
import com.nexora.auth.repository.RoleRepository;
import com.nexora.auth.repository.UserRepository;
import com.nexora.auth.security.CustomUserPrincipal;
import com.nexora.auth.security.JwtService;
import com.nexora.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;

    @Override
    public UserResponse register(RegisterRequest request) {
        // Check if user already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("username", request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("email", request.getEmail());
        }

        // Map request to entity
        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Assign default role USER
        Role defaultRole = roleRepository.findByName(RoleType.USER)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", RoleType.USER.name()));
        user.setRoles(Set.of(defaultRole));

        // Save user
        User savedUser = userRepository.save(user);

        return userMapper.toResponse(savedUser);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        // Authenticate
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();

        // Generate tokens
        String accessToken = jwtService.generateAccessToken(principal);
        String refreshToken = jwtService.generateRefreshToken(principal);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(900000L) // 15 minutes
                .build();
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        // Validate refresh token
        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new InvalidCredentialsException();
        }

        String username = jwtService.extractUsername(refreshToken);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        CustomUserPrincipal principal = new CustomUserPrincipal(user);

        if (!jwtService.isTokenValid(refreshToken, principal)) {
            throw new InvalidCredentialsException();
        }

        // Generate new access token
        String newAccessToken = jwtService.generateAccessToken(principal);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(900000L)
                .build();
    }

    @Override
    public void logout(String refreshToken) {
        // For now i just validate token exists
        // In Future i Add refresh token to blacklist in Redis
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new InvalidCredentialsException();
        }
        // Token will be invalidated by client removing it
        // Redis blacklist will be added later
    }
}