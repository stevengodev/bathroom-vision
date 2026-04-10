package com.foliaco.vision_bathroom.dto;

public record BlockResponse(
    Long id,
    String name,
    Integer numberOfFloors,
    Integer numberOfBathrooms
) {
    
}
