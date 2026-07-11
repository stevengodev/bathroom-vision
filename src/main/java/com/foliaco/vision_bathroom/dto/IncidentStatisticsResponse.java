package com.foliaco.vision_bathroom.dto;

import java.util.List;

public record IncidentStatisticsResponse(
        String groupBy,
        String sort,
        List<Item> items
) {
    public record Item(
            Long bathroomId,
            String blockName,
            String category,
            Long count
    ) {
    }
}
