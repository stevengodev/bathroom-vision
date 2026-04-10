package com.foliaco.vision_bathroom.service;

import java.util.List;

import com.foliaco.vision_bathroom.dto.UserResponse;
import com.foliaco.vision_bathroom.entity.User.Role;

public interface UserService {
    
    UserResponse getCurrentUser(String email);

    List<UserResponse> getUsersByRol(Role role);
    
}
