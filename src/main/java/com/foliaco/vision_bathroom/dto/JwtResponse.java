package com.foliaco.vision_bathroom.dto;

public record JwtResponse(
    String accessToken,
    String type
) {
    
    public static JwtResponse of(String accessToken){
        return new JwtResponse(accessToken, "Bearer");
    }

}
