package com.foliaco.vision_bathroom.service.impl;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.foliaco.vision_bathroom.dto.DashboardOverviewResponse;
import com.foliaco.vision_bathroom.dto.IncidentStatisticsResponse;
import com.foliaco.vision_bathroom.dto.MaintenanceStatisticsResponse;
import com.foliaco.vision_bathroom.entity.Bathroom;
import com.foliaco.vision_bathroom.entity.Incident;
import com.foliaco.vision_bathroom.entity.Maintenance;
import com.foliaco.vision_bathroom.repository.BathroomRepository;
import com.foliaco.vision_bathroom.repository.IncidentRepository;
import com.foliaco.vision_bathroom.repository.MaintenanceRepository;
import com.foliaco.vision_bathroom.service.AnalyticsService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final BathroomRepository bathroomRepository;
    private final IncidentRepository incidentRepository;
    private final MaintenanceRepository maintenanceRepository;

    @Override
    public DashboardOverviewResponse getDashboardOverview() {
        long totalBathrooms = bathroomRepository.count();
        long availableBathrooms = bathroomRepository.findByStatus(Bathroom.BathroomStatus.DISPONIBLE).size();
        long occupiedBathrooms = bathroomRepository.findByStatus(Bathroom.BathroomStatus.EN_LIMPIEZA).size();
        long maintenanceBathrooms = bathroomRepository.findByStatus(Bathroom.BathroomStatus.EN_MANTENIMIENTO).size();

        long activeIncidents = incidentRepository.countByStatus(Incident.Status.PENDING);
        long openMaintenances = maintenanceRepository.countByStatus(Maintenance.Status.ABIERTO);
        long closedMaintenances = maintenanceRepository.countByStatus(Maintenance.Status.CERRADO);

        return new DashboardOverviewResponse(
                totalBathrooms,
                availableBathrooms,
                occupiedBathrooms,
                maintenanceBathrooms,
                activeIncidents,
                openMaintenances,
                closedMaintenances);
    }

    @Override
    public IncidentStatisticsResponse getIncidentStatistics(String groupBy, String sort) {
        List<Incident> incidents = incidentRepository.findAll();

        Map<String, Long> grouped = incidents.stream()
                .filter(incident -> incident.getBathroom() != null)
                .collect(Collectors.groupingBy(incident -> resolveGroupKey(incident, groupBy), Collectors.counting()));

        List<IncidentStatisticsResponse.Item> items = grouped.entrySet().stream()
                .map(entry -> toItem(entry.getKey(), entry.getValue(), groupBy))
                .filter(item -> item != null)
                .sorted(resolveComparator(sort))
                .toList();

        return new IncidentStatisticsResponse(groupBy, sort, items);
    }

    @Override
    public MaintenanceStatisticsResponse getMaintenanceStatistics(String status, Long bathroomId,
            LocalDateTime startDate, LocalDateTime endDate) {
        List<Maintenance> maintenances = maintenanceRepository.findAll().stream()
                .filter(maintenance -> matchesStatus(maintenance, status))
                .filter(maintenance -> matchesBathroom(maintenance, bathroomId))
                .filter(maintenance -> matchesDateRange(maintenance, startDate, endDate))
                .sorted(Comparator.comparing(Maintenance::getReportedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .toList();

        long openCount = maintenanceRepository.findAll().stream()
                .filter(maintenance -> maintenance.getStatus() == Maintenance.Status.ABIERTO)
                .filter(maintenance -> matchesBathroom(maintenance, bathroomId))
                .filter(maintenance -> matchesDateRange(maintenance, startDate, endDate))
                .count();

        long closedCount = maintenanceRepository.findAll().stream()
                .filter(maintenance -> maintenance.getStatus() == Maintenance.Status.CERRADO)
                .filter(maintenance -> matchesBathroom(maintenance, bathroomId))
                .filter(maintenance -> matchesDateRange(maintenance, startDate, endDate))
                .count();

        List<MaintenanceStatisticsResponse.HistoryItem> history = maintenances.stream()
                .map(this::toHistoryItem)
                .toList();

        return new MaintenanceStatisticsResponse(status, bathroomId, startDate, endDate, openCount, closedCount, history);
    }

    private boolean matchesStatus(Maintenance maintenance, String status) {
        if (status == null || status.isBlank()) {
            return true;
        }
        return switch (status.toLowerCase()) {
            case "open" -> maintenance.getStatus() == Maintenance.Status.ABIERTO;
            case "closed" -> maintenance.getStatus() == Maintenance.Status.CERRADO;
            default -> true;
        };
    }

    private boolean matchesBathroom(Maintenance maintenance, Long bathroomId) {
        return bathroomId == null || maintenance.getBathroom() != null && maintenance.getBathroom().getId().equals(bathroomId);
    }

    private boolean matchesDateRange(Maintenance maintenance, LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null && endDate == null) {
            return true;
        }
        LocalDateTime reportedAt = maintenance.getReportedAt();
        if (reportedAt == null) {
            return false;
        }
        boolean afterStart = startDate == null || !reportedAt.isBefore(startDate);
        boolean beforeEnd = endDate == null || !reportedAt.isAfter(endDate);
        return afterStart && beforeEnd;
    }

    private String resolveGroupKey(Incident incident, String groupBy) {
        return switch (groupBy != null ? groupBy.toLowerCase() : "bathroom") {
            case "block" -> incident.getBathroom().getBlock() != null ? incident.getBathroom().getBlock().getName() : "Sin bloque";
            case "category" -> incident.getIncidentMessage() != null && incident.getIncidentMessage().getCategory() != null
                    ? incident.getIncidentMessage().getCategory().name()
                    : "SIN_CATEGORIA";
            case "bathroom" -> String.valueOf(incident.getBathroom().getId());
            default -> String.valueOf(incident.getBathroom().getId());
        };
    }

    private IncidentStatisticsResponse.Item toItem(String key, Long count, String groupBy) {
        if ("bathroom".equalsIgnoreCase(groupBy)) {
            Long bathroomId = Long.parseLong(key);
            return new IncidentStatisticsResponse.Item(bathroomId, null, null, count);
        }
        if ("block".equalsIgnoreCase(groupBy)) {
            return new IncidentStatisticsResponse.Item(null, key, null, count);
        }
        if ("category".equalsIgnoreCase(groupBy)) {
            return new IncidentStatisticsResponse.Item(null, null, key, count);
        }
        return null;
    }

    private Comparator<IncidentStatisticsResponse.Item> resolveComparator(String sort) {
        boolean descending = "desc".equalsIgnoreCase(sort);
        return (left, right) -> descending ? Long.compare(right.count(), left.count()) : Long.compare(left.count(), right.count());
    }

    private MaintenanceStatisticsResponse.HistoryItem toHistoryItem(Maintenance maintenance) {
        return new MaintenanceStatisticsResponse.HistoryItem(
                maintenance.getId(),
                maintenance.getBathroom() != null ? maintenance.getBathroom().getId() : null,
                maintenance.getBathroom() != null && maintenance.getBathroom().getBlock() != null ? maintenance.getBathroom().getBlock().getName() : null,
                maintenance.getStatus(),
                maintenance.getReportedAt(),
                maintenance.getResolvedAt());
    }
}
