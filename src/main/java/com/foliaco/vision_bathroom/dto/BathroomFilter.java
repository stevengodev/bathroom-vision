package com.foliaco.vision_bathroom.dto;

import com.foliaco.vision_bathroom.entity.Bathroom.BathroomStatus;
import com.foliaco.vision_bathroom.entity.Bathroom.Gender;

public record BathroomFilter(
    BathroomStatus status,
    Gender gender,
    Long blockId,
    String query
) {}
