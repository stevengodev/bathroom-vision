package com.foliaco.vision_bathroom.dto;

import java.time.LocalDateTime;

public record UserIncidentResponse(
        Long id,
        String incidentType,
        String bathroom,
        String status,
        LocalDateTime reportedAt
) {
}
