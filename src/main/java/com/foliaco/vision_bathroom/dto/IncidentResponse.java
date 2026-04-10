package com.foliaco.vision_bathroom.dto;

import java.time.LocalDateTime;

import com.foliaco.vision_bathroom.entity.Bathroom.Gender;

public record IncidentResponse(
    Long id,
    ReporterInfo reporter,
    IncidentMessageResponse incidentMessage,
    AffectedBathroom bathroom,
    String status,
    LocalDateTime reportedAt,
    LocalDateTime resolvedAt
) {

    public record AffectedBathroom(
        Long id,
        Gender gender, 
        Integer floor, 
        String blockName
    ) {}
    
}
