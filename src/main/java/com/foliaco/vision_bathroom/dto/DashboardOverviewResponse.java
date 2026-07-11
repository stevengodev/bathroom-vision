package com.foliaco.vision_bathroom.dto;

public record DashboardOverviewResponse(
        Long totalBathrooms,
        Long availableBathrooms,
        Long occupiedBathrooms,
        Long maintenanceBathrooms,
        Long activeIncidents,
        Long openMaintenances,
        Long closedMaintenances
) {
}
