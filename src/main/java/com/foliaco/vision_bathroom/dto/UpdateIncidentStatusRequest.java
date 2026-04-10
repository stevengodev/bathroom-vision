package com.foliaco.vision_bathroom.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateIncidentStatusRequest(
        @NotNull Long bathroomId
) {
}
