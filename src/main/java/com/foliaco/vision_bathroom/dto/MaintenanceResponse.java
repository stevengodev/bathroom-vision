package com.foliaco.vision_bathroom.dto;

import java.time.LocalDateTime;

import com.foliaco.vision_bathroom.entity.Maintenance.Status;

public record MaintenanceResponse(
    Long id,
    BathroomResponse bathroom,
    String technicianFullName,
    String description,
    Status status,
    LocalDateTime reportedAt,
    LocalDateTime scheduledAt,
    LocalDateTime resolvedAt
) {
}
