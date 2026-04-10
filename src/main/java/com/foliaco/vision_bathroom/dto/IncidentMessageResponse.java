package com.foliaco.vision_bathroom.dto;

public record IncidentMessageResponse(
    Long id,
    String code,
    String description,
    String category
) {
    
}
