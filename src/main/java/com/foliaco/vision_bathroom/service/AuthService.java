package com.foliaco.vision_bathroom.service;

import com.foliaco.vision_bathroom.dto.AuthResponse;
import com.foliaco.vision_bathroom.dto.LoginRequest;
import com.foliaco.vision_bathroom.dto.UserRequest;

public interface AuthService {
    
    AuthResponse register(UserRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse authenticateWithGoogle(String googleIdToken);

}
