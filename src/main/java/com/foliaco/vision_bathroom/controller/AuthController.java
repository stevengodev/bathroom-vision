package com.foliaco.vision_bathroom.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.foliaco.vision_bathroom.dto.AuthResponse;
import com.foliaco.vision_bathroom.dto.GoogleIdTokenRequest;
import com.foliaco.vision_bathroom.dto.UserRequest;
import com.foliaco.vision_bathroom.service.AuthService;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> googleAuth(@RequestBody GoogleIdTokenRequest request) {
        AuthResponse jwtResponse = authService.authenticateWithGoogle(request.idToken());
        return ResponseEntity.ok(jwtResponse);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody UserRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody UserRequest request) {
        AuthResponse response =  authService.login(request);
        return ResponseEntity.ok(response);
    }
    
}