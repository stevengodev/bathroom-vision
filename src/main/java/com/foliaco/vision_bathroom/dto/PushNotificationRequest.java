package com.foliaco.vision_bathroom.dto;

import java.util.Map;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class PushNotificationRequest {

    private String title;
    private String body;

    // Datos adicionales que recibe la app
    private Map<String, String> data;

}
