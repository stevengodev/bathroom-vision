package com.foliaco.vision_bathroom.dto;

import com.foliaco.vision_bathroom.entity.Bathroom.BathroomStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateBathroomStatusRequest(
        @NotNull
        BathroomStatus status
) {
}
