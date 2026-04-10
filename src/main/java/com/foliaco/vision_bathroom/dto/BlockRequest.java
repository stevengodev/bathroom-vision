package com.foliaco.vision_bathroom.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BlockRequest(
    @NotBlank(message = "El nombre del bloque es requerido")
    @Size(min = 1, max = 255, message = "El nombre debe tener entre 1 y 255 caracteres")
    String name,

    @Min(value = 1, message = "El numero de pisos debe ser 1 o mayor")
    Integer numberOfFloors
) {
    
}
