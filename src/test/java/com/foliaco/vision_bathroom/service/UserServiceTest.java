package com.foliaco.vision_bathroom.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import com.foliaco.vision_bathroom.dto.UserRequest;
import com.foliaco.vision_bathroom.dto.UserResponse;
import com.foliaco.vision_bathroom.entity.User;
import com.foliaco.vision_bathroom.exception.NotFoundException;
import com.foliaco.vision_bathroom.repository.UserRepository;
import com.foliaco.vision_bathroom.service.impl.UserServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl service;

    private User user;
    private UserRequest request;

    @BeforeEach
    void setUp() {

        user = new User();
        user.setId(1L);
        user.setName("Juan Perez");
        user.setEmail("juan@test.com");
        user.setPassword("123456");
        user.setRole(User.Role.ADMIN);

        request = new UserRequest(
                "Juan Actualizado",
                "nuevo@test.com",
                "newpassword",
                User.Role.USER
        );
    }

    @Test
    @DisplayName("Debe obtener usuarios por múltiples roles")
    void shouldGetAllUsersByRoles() {

        List<User.Role> roles = List.of(
                User.Role.ADMIN,
                User.Role.USER
        );

        when(userRepository.findUsersByRoles(roles))
                .thenReturn(List.of(user));

        List<UserResponse> response =
                service.getAllUsersByRoles(roles);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(user.getName(), response.get(0).name());

        verify(userRepository).findUsersByRoles(roles);
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay usuarios por roles")
    void shouldReturnEmptyListWhenNoUsersByRoles() {

        List<User.Role> roles = List.of(User.Role.ADMIN);

        when(userRepository.findUsersByRoles(roles))
                .thenReturn(List.of());

        List<UserResponse> response =
                service.getAllUsersByRoles(roles);

        assertNotNull(response);
        assertTrue(response.isEmpty());
    }

    @Test
    @DisplayName("Debe actualizar usuario correctamente")
    void shouldUpdateUser() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.encode("newpassword"))
                .thenReturn("encoded-password");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response =
                service.updateUser(1L, request);

        assertNotNull(response);
        assertEquals("Juan Actualizado", response.name());
        assertEquals("nuevo@test.com", response.email());
        assertEquals(User.Role.USER.toString(), response.role());

        assertEquals("encoded-password", user.getPassword());

        verify(passwordEncoder).encode("newpassword");
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Debe actualizar usuario sin cambiar password")
    void shouldUpdateUserWithoutChangingPassword() {

        UserRequest requestWithoutPassword =
                new UserRequest(
                        "Juan Actualizado",
                        "nuevo@test.com",
                        "",
                        User.Role.USER
                );

        String oldPassword = user.getPassword();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response =
                service.updateUser(1L, requestWithoutPassword);

        assertNotNull(response);
        assertEquals(oldPassword, user.getPassword());

        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("Debe actualizar usuario cuando password es null")
    void shouldUpdateUserWhenPasswordIsNull() {

        UserRequest requestWithoutPassword =
                new UserRequest(
                        "Juan Actualizado",
                        "nuevo@test.com",
                        null,
                        User.Role.USER
                );

        String oldPassword = user.getPassword();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.updateUser(1L, requestWithoutPassword);

        assertEquals(oldPassword, user.getPassword());

        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando usuario no existe en update")
    void shouldThrowWhenUserNotFoundInUpdate() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.updateUser(1L, request));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Debe obtener usuario actual por email")
    void shouldGetCurrentUser() {

        when(userRepository.findByEmail("juan@test.com"))
                .thenReturn(Optional.of(user));

        UserResponse response =
                service.getCurrentUser("juan@test.com");

        assertNotNull(response);
        assertEquals(user.getEmail(), response.email());

        verify(userRepository).findByEmail("juan@test.com");
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando usuario actual no existe")
    void shouldThrowWhenCurrentUserNotFound() {

        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.getCurrentUser("fake@test.com"));
    }

    @Test
    @DisplayName("Debe obtener usuarios por rol")
    void shouldGetUsersByRol() {

        when(userRepository.findByRole(User.Role.ADMIN))
                .thenReturn(List.of(user));

        List<UserResponse> response =
                service.getUsersByRol(User.Role.ADMIN);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(user.getName(), response.get(0).name());

        verify(userRepository).findByRole(User.Role.ADMIN);
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay usuarios por rol")
    void shouldReturnEmptyListWhenNoUsersByRol() {

        when(userRepository.findByRole(User.Role.ADMIN))
                .thenReturn(List.of());

        List<UserResponse> response =
                service.getUsersByRol(User.Role.ADMIN);

        assertNotNull(response);
        assertTrue(response.isEmpty());
    }
}
