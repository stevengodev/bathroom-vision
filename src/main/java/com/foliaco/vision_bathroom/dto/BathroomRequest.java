package com.foliaco.vision_bathroom.dto;

import com.foliaco.vision_bathroom.entity.Bathroom.BathroomStatus;
import com.foliaco.vision_bathroom.entity.Bathroom.Gender;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record BathroomRequest(

    @NotNull(message = "El genero es requerido")
    Gender gender,

    @NotNull(message = "El bloque es requerido")
    Long blockId,

    BathroomStatus status,

    @Min(value = 0, message = "El piso debe ser 0 o mayor")
    Integer floor
) {
    
}
