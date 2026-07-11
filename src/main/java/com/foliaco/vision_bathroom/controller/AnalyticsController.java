package com.foliaco.vision_bathroom.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.foliaco.vision_bathroom.dto.DashboardOverviewResponse;
import com.foliaco.vision_bathroom.dto.IncidentStatisticsResponse;
import com.foliaco.vision_bathroom.dto.MaintenanceStatisticsResponse;
import com.foliaco.vision_bathroom.service.AnalyticsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping({"/dashboard/overview", "/analytics/dashboard"})
    public ResponseEntity<DashboardOverviewResponse> getDashboardOverview() {
        return ResponseEntity.ok(analyticsService.getDashboardOverview());
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping({"/statistics/incidents", "/analytics/incidents"})
    public ResponseEntity<IncidentStatisticsResponse> getIncidentStatistics(
            @RequestParam(defaultValue = "bathroom") String groupBy,
            @RequestParam(defaultValue = "desc") String sort) {
        return ResponseEntity.ok(analyticsService.getIncidentStatistics(groupBy, sort));
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping({"/statistics/maintenance", "/analytics/maintenance"})
    public ResponseEntity<MaintenanceStatisticsResponse> getMaintenanceStatistics(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long bathroomId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return ResponseEntity.ok(analyticsService.getMaintenanceStatistics(
                status,
                bathroomId,
                parseDateTime(startDate),
                parseDateTime(endDate)));
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME);
        } catch (DateTimeParseException ignored) {
            LocalDate date = LocalDate.parse(value, DateTimeFormatter.ISO_DATE);
            return date.atStartOfDay();
        }
    }
}
