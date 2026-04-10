package com.foliaco.vision_bathroom.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.foliaco.vision_bathroom.dto.UserResponse;
import com.foliaco.vision_bathroom.entity.User;
import com.foliaco.vision_bathroom.entity.User.Role;
import com.foliaco.vision_bathroom.exception.NotFoundException;
import com.foliaco.vision_bathroom.repository.UserRepository;
import com.foliaco.vision_bathroom.service.UserService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserResponse getCurrentUser(String email) {

        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new NotFoundException("User not found with email " + email));

        return toUserResponse(user);

    }

    @Override
    public List<UserResponse> getUsersByRol(Role role) {
        List<User> users = userRepository.findByRole(role);
        return users.stream()
                .map(user -> toUserResponse(user))
                .toList();

    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole().toString());
    }

}
