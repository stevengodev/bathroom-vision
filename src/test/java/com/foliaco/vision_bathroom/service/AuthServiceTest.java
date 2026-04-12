package com.foliaco.vision_bathroom.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.GeneralSecurityException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.foliaco.vision_bathroom.dto.AuthResponse;
import com.foliaco.vision_bathroom.dto.LoginRequest;
import com.foliaco.vision_bathroom.dto.UserRequest;
import com.foliaco.vision_bathroom.entity.User;
import com.foliaco.vision_bathroom.exception.ConflictException;
import com.foliaco.vision_bathroom.exception.InvalidGoogleTokenException;
import com.foliaco.vision_bathroom.exception.UnauthorizedException;
import com.foliaco.vision_bathroom.repository.UserRepository;
import com.foliaco.vision_bathroom.security.GoogleTokenValidator;
import com.foliaco.vision_bathroom.security.JwtService;
import com.foliaco.vision_bathroom.service.impl.AuthServiceImpl;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private GoogleTokenValidator googleTokenValidator;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private GoogleIdToken.Payload payload;

    @BeforeEach
    void setUp() {
        payload = mock(GoogleIdToken.Payload.class);
    }

    @Test
    @DisplayName("Debe registrar usuario correctamente")
    void register_success() {

        var request = new UserRequest("Test", "test@mail.com", "1234",  User.Role.USER);

        User savedUser = User.builder()
                .id(1L)
                .name("Test")
                .email("test@mail.com")
                .password("encoded-password")
                .role(User.Role.USER)
                .build();

        when(passwordEncoder.encode("1234")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.accessToken());
        assertEquals("test@mail.com", response.user().email());

        verify(passwordEncoder).encode("1234");
        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(any(User.class));
    }

    @Test
    @DisplayName("Debe fallar registro si el email ya existe")
    void register_emailAlreadyExists() {

        var request = new UserRequest("Test", "test@mail.com", "1234", User.Role.USER);

        when(userRepository.findByEmail("test@mail.com"))
                .thenReturn(Optional.of(new User()));

        assertThrows(ConflictException.class,
                () -> authService.register(request));

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe hacer login correctamente")
    void login_success() {

        var request = new LoginRequest("test@mail.com", "1234");

        User user = User.builder()
                .id(1L)
                .name("Test")
                .email("test@mail.com")
                .password("encoded-password")
                .role(User.Role.USER)
                .build();

        when(userRepository.findByEmail("test@mail.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("1234", "encoded-password")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.accessToken());

        verify(userRepository).findByEmail("test@mail.com");
        verify(passwordEncoder).matches("1234", "encoded-password");
        verify(jwtService).generateToken(user);
    }

    @Test
    @DisplayName("Debe lanzar excepción si el usuario no existe")
    void login_userNotFound() {

        var request = new LoginRequest("test@mail.com", "1234");

        when(userRepository.findByEmail("test@mail.com"))
                .thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class,
                () -> authService.login(request));

        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción si el password es incorrecto")
    void login_invalidPassword() {

        var request = new LoginRequest("test@mail.com", "wrong");

        User user = User.builder()
                .email("test@mail.com")
                .password("encoded-password")
                .build();

        when(userRepository.findByEmail("test@mail.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("wrong", "encoded-password"))
                .thenReturn(false);

        assertThrows(UnauthorizedException.class,
                () -> authService.login(request));

        verify(jwtService, never()).generateToken(any());
    }

    @Test
    @DisplayName("Debe autenticar cuando el usuario ya existe")
    void authenticate_existingUser_returnsJwt() throws Exception {

        String email = "test@gmail.com";
        String name = "Test User";
        String googleIdToken = "valid-token";

        User user = User.builder()
                .email(email)
                .name(name)
                .role(User.Role.USER)
                .build();

        when(googleTokenValidator.verify(googleIdToken)).thenReturn(payload);
        when(payload.getEmail()).thenReturn(email);
        when(payload.get("name")).thenReturn(name);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        AuthResponse response = authService.authenticateWithGoogle(googleIdToken);

        assertNotNull(response);
        assertEquals("jwt-token", response.accessToken());

        verify(userRepository).findByEmail(email);
        verify(jwtService).generateToken(user);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe crear usuario si no existe")
    void authenticate_newUser_createsAndReturnsJwt() throws Exception {

        String email = "new@gmail.com";
        String name = "New User";
        String googleToken = "valid-token";

        when(googleTokenValidator.verify(googleToken)).thenReturn(payload);
        when(payload.getEmail()).thenReturn(email);
        when(payload.get("name")).thenReturn(name);

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        User savedUser = User.builder()
                .email(email)
                .name(name)
                .role(User.Role.USER)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(savedUser)).thenReturn("jwt-token");

        AuthResponse response = authService.authenticateWithGoogle(googleToken);

        assertNotNull(response);
        assertEquals("jwt-token", response.accessToken());

        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(savedUser);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el token es inválido")
    void authenticate_invalidToken_throwsException() throws Exception {

        String googleToken = "invalid-token";

        when(googleTokenValidator.verify(googleToken))
                .thenThrow(new GeneralSecurityException());

        assertThrows(InvalidGoogleTokenException.class,
                () -> authService.authenticateWithGoogle(googleToken));

        verify(userRepository, never()).findByEmail(any());
        verify(jwtService, never()).generateToken(any());
    }

}
