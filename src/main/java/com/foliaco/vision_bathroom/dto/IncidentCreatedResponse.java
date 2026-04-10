package com.foliaco.vision_bathroom.dto;

import java.time.LocalDateTime;
import java.util.List;

public record IncidentCreatedResponse(
        List<Long> incidentIds,
        String status,
        LocalDateTime reportedAt
) {
}
