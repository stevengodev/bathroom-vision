package com.foliaco.vision_bathroom.service;

import java.util.List;

import com.foliaco.vision_bathroom.dto.UserRequest;
import com.foliaco.vision_bathroom.dto.UserResponse;
import com.foliaco.vision_bathroom.entity.User.Role;

public interface UserService {
    
    List<UserResponse> getAllUsersByRoles(List<Role> roles);

    UserResponse getCurrentUser(String email);

    List<UserResponse> getUsersByRol(Role role);

    UserResponse updateUser(Long id, UserRequest request);

}
