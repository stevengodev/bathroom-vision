package com.foliaco.vision_bathroom.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.foliaco.vision_bathroom.entity.Maintenance;

public record MaintenanceStatisticsResponse(
        String status,
        Long bathroomId,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Long openCount,
        Long closedCount,
        List<HistoryItem> history
) {
    public record HistoryItem(
            Long id,
            Long bathroomId,
            String blockName,
            Maintenance.Status status,
            LocalDateTime reportedAt,
            LocalDateTime resolvedAt
    ) {
    }
}
