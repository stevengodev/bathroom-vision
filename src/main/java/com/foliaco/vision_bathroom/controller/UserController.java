package com.foliaco.vision_bathroom.controller;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.foliaco.vision_bathroom.dto.UserResponse;
import com.foliaco.vision_bathroom.entity.User.Role;
import com.foliaco.vision_bathroom.service.UserService;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;

    @GetMapping("profile")
    public ResponseEntity<UserResponse> getById(Authentication auth) {
        String email = (String) auth.getPrincipal();
        return ResponseEntity.ok(userService.getCurrentUser(email));
    }

    @GetMapping(params = "role")
    public ResponseEntity<List<UserResponse>> getByRole(@RequestParam Role role){
        return ResponseEntity.ok(userService.getUsersByRol(role));
    }

}
