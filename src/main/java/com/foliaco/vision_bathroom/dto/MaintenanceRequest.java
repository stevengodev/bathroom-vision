package com.foliaco.vision_bathroom.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MaintenanceRequest(
        @NotNull(message = "El baño es requerido")
        Long bathroomId,

        @NotBlank(message = "El nombre del tecnico es requerido")
        String technicianFullName,

        @NotNull(message = "La fecha programada es requerida")
        LocalDateTime scheduledAt,

        @NotBlank(message = "La descripción es requerida")
        @Size(min = 5, max = 500, message = "La descripción debe tener entre 5 y 500 caracteres")
        String description
) {
}
