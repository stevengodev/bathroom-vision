package com.foliaco.vision_bathroom.dto;

import com.foliaco.vision_bathroom.entity.User;

public record UserRequest(
    String name,
    String email,
    String password,
    User.Role role
) {
    
}
