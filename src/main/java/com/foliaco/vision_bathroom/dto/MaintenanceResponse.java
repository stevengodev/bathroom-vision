package com.foliaco.vision_bathroom.dto;

import java.time.LocalDateTime;

import com.foliaco.vision_bathroom.entity.Maintenance.Status;

public record MaintenanceResponse(
    Long id,
    String bathroom,
    String technicianFullName,
    String description,
    Status status,
    LocalDateTime resolvedAt
) {
}
