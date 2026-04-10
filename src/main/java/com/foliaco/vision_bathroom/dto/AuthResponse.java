package com.foliaco.vision_bathroom.dto;

public record AuthResponse(
    String accessToken,
    String tokenType,
    Long expiresIn,
    UserResponse user
) {

    public static AuthResponse of(String token, long expiresInMs, UserResponse user) {
        return new AuthResponse(
                token,
                "Bearer",
                expiresInMs / 1000,
                user
        );
    }

}
