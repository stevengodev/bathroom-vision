package com.foliaco.vision_bathroom.service.impl;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.foliaco.vision_bathroom.dto.AuthResponse;
import com.foliaco.vision_bathroom.dto.LoginRequest;
import com.foliaco.vision_bathroom.dto.UserRequest;
import com.foliaco.vision_bathroom.dto.UserResponse;
import com.foliaco.vision_bathroom.entity.User;
import com.foliaco.vision_bathroom.entity.User.Role;
import com.foliaco.vision_bathroom.exception.ConflictException;
import com.foliaco.vision_bathroom.exception.InvalidGoogleTokenException;
import com.foliaco.vision_bathroom.exception.UnauthorizedException;
import com.foliaco.vision_bathroom.repository.UserRepository;
import com.foliaco.vision_bathroom.security.GoogleTokenValidator;
import com.foliaco.vision_bathroom.security.JwtService;
import com.foliaco.vision_bathroom.service.AuthService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final GoogleTokenValidator googleTokenValidator;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.security.jwt.expiration-ms}")
    private long jwtExpirationMs;

    @Override
    public AuthResponse register(UserRequest request) {

        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new ConflictException("Email already exists");
        }

        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);
        user.setCreatedAt(LocalDateTime.now());

        String jwt = jwtService.generateToken(user);

        user = userRepository.save(user);

        return AuthResponse.of(jwt, jwtExpirationMs, toUserResponse(user));

    }

    @Override
    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.email()).orElseThrow(
                () -> new UnauthorizedException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        String jwt = jwtService.generateToken(user);

        return AuthResponse.of(jwt, jwtExpirationMs, toUserResponse(user));

    }

    @Override
    public AuthResponse authenticateWithGoogle(String googleIdToken) {

        // 1. Validar token de Google
        GoogleIdToken.Payload payload;

        try {
            payload = googleTokenValidator.verify(googleIdToken);
        } catch (GeneralSecurityException | IOException e) {
            throw new InvalidGoogleTokenException("Token no valido");
        }

        String email = payload.getEmail();
        String name = (String) payload.get("name");

        log.info("Autenticación Google exitosa para: {}", email);

        // 2. Buscar o crear usuario (registro automático en primer login)
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> createUser(email, name));

        // 3. Generar JWT propio
        String jwt = jwtService.generateToken(user);

        return AuthResponse.of(jwt, jwtExpirationMs, toUserResponse(user));

    }

    private User createUser(String email, String name) {
        log.info("Registrando nuevo usuario: {}", email);
        User newUser = User.builder()
                .email(email)
                .name(name)
                .role(User.Role.USER) // Rol por defecto
                .build();
        return userRepository.save(newUser);
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole().toString());
    }

}
