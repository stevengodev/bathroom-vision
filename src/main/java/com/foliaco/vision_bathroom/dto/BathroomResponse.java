package com.foliaco.vision_bathroom.dto;

import com.foliaco.vision_bathroom.entity.Bathroom.BathroomStatus;
import com.foliaco.vision_bathroom.entity.Bathroom.Gender;

public record BathroomResponse(
    Long id,
    Gender gender,
    Long blockId,
    String nameBlock,
    BathroomStatus status,
    Integer floor 
) {
    
}
