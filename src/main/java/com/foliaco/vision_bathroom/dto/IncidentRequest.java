package com.foliaco.vision_bathroom.dto;

import java.util.List;

import jakarta.validation.constraints.NotNull;

public record IncidentRequest(

    @NotNull(message = "Debe indicar su email")
    String email,

    @NotNull(message = "Debe indicar al menos un tipo de incidencia")
    List<Long> incidentMessageIds,

    @NotNull
    Long bathroomId
) {
    
}
