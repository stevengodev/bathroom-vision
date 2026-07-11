package com.foliaco.vision_bathroom.service;

import java.time.LocalDateTime;

import com.foliaco.vision_bathroom.dto.DashboardOverviewResponse;
import com.foliaco.vision_bathroom.dto.IncidentStatisticsResponse;
import com.foliaco.vision_bathroom.dto.MaintenanceStatisticsResponse;

public interface AnalyticsService {

    DashboardOverviewResponse getDashboardOverview();

    IncidentStatisticsResponse getIncidentStatistics(String groupBy, String sort);

    MaintenanceStatisticsResponse getMaintenanceStatistics(String status, Long bathroomId, LocalDateTime startDate, LocalDateTime endDate);

}
