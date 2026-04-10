package com.foliaco.vision_bathroom.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.foliaco.vision_bathroom.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.foliaco.vision_bathroom.entity.Bathroom;
import com.foliaco.vision_bathroom.entity.Incident;
import com.foliaco.vision_bathroom.entity.IncidentMessage;
import com.foliaco.vision_bathroom.entity.IncidentMessage.Category;
import com.foliaco.vision_bathroom.entity.User;
import com.foliaco.vision_bathroom.exception.NotFoundException;
import com.foliaco.vision_bathroom.entity.Incident.Status;
import com.foliaco.vision_bathroom.repository.BathroomRepository;
import com.foliaco.vision_bathroom.repository.IncidentMessageRepository;
import com.foliaco.vision_bathroom.repository.IncidentRepository;
import com.foliaco.vision_bathroom.repository.UserRepository;
import com.foliaco.vision_bathroom.service.IncidentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class IncidentServiceImpl implements IncidentService {

    private final IncidentRepository incidentRepository;
    private final IncidentMessageRepository incidentMessageRepository;
    private final BathroomRepository bathroomRepository;
    private final UserRepository userRepository;

    @Override
    public List<IncidentMessageResponse> findAllIncidentMessages() {

        return incidentMessageRepository.findAllByOrderByDescriptionAsc()
                .stream()
                .map(incidentMessaje -> toIncidentMessageResponse(incidentMessaje))
                .toList();

    }

    /*
     * 
     * Este metodo sirve para no reportar el mismo problema dos veces en el mismo
     * baño por el mismo usuario mientras está pendiente
     * 
     * Permite reportar múltiples problemas en un baño, evitando duplicados si ya
     * están pendientes
     * 
     */

    @Override
    public IncidentCreatedResponse report(Long reporterUserId, IncidentRequest request) {

        User user = userRepository.findById(reporterUserId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado con id: " + reporterUserId));

        Bathroom bathroom = bathroomRepository.findById(request.bathroomId())
                .orElseThrow(() -> new NotFoundException("Baño no encontrado con id: " + request.bathroomId()));

        List<IncidentMessage> messages = incidentMessageRepository.findAllById(request.incidentMessageIds());

        if (messages.size() != request.incidentMessageIds().size()) {
            throw new NotFoundException("Uno o más tipos de incidente no existen");
        }

        List<Incident> existingIncidents = incidentRepository.findByUserIdAndBathroomIdAndStatus(
                reporterUserId,
                request.bathroomId(),
                Incident.Status.PENDING);

        Set<Long> existingMessageIds = existingIncidents.stream()
                .map(i -> i.getIncidentMessage().getId())
                .collect(Collectors.toSet());

        List<Incident> incidentsToSave = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (IncidentMessage message : messages) {

            if (existingMessageIds.contains(message.getId())) {
                log.info("Incidente {} ya existe para el baño {}", message.getCode(), bathroom.getId());
                continue;
            }

            log.info("Creando incidente {} para baño {}", message.getCode(), bathroom.getId());

            Incident incident = new Incident();
            incident.setUser(user);
            incident.setBathroom(bathroom);
            incident.setIncidentMessage(message);
            incident.setStatus(Incident.Status.PENDING);
            incident.setReportedAt(now);
            incident.setResolvedAt(null);

            incidentsToSave.add(incident);
        }

        if (incidentsToSave.isEmpty()) {
            log.info("No se crearon nuevos incidentes (todos ya existían)");
        }

        List<Incident> savedIncidents = incidentRepository.saveAll(incidentsToSave);

        List<Long> ids = savedIncidents.stream()
                .map(Incident::getId)
                .toList();

        return new IncidentCreatedResponse(
                ids,
                Incident.Status.PENDING.name(),
                now);

    }

    @Transactional
    @Override
    public int resolveIncidentTypeInBathroom(Long bathroomId, Long incidentMessageId) {
        LocalDateTime now = LocalDateTime.now();

        return incidentRepository.updateStatusByBathroomAndIncidentMessage(
                bathroomId,
                incidentMessageId,
                Incident.Status.RESOLVED,
                now);
    }

    @Override
    public IncidentResponse findById(Long id) {
        Incident incident = incidentRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Incident not found with id: " + id));

        return toIncidentResponse(incident);

    }

    @Override
    public List<IncidentResponse> findAll(Status statusFilter) {
        return incidentRepository.findAllWithDetails(statusFilter)
                .stream()
                .map(incident -> toIncidentResponse(incident))
                .toList();
    }

    @Override
    public List<IncidentResponse> findAllByStatusAndCategory(Status statusFilter, Category categoryFilter) {

        return incidentRepository.findAllByStatusAndMessageCategory(statusFilter, categoryFilter)
                .stream()
                .map(incident -> toIncidentResponse(incident))
                .toList();

    }

    @Override
    public List<IncidentSummary> findByIncidentMessageIds(List<Long> incidentMessageIds) {
        return incidentRepository.findPendingIncidentSummaryByMessages(incidentMessageIds);
    }

    @Override
    public List<IncidentResponse> findByUser(Long userId) {
        return incidentRepository.findByUserId(userId)
                .stream()
                .map(incident -> toIncidentResponse(incident))
                .toList();
    }

    @Override
    public List<IncidentResponse> findByBathroom(Long bathroomId) {
        bathroomRepository.findById(bathroomId)
                .orElseThrow(() -> new NotFoundException("Baño no encontrado con id: " + bathroomId));

        return incidentRepository.findByBathroomIdAndPendingStatus(bathroomId)
                .stream()
                .map(incident -> toIncidentResponse(incident))
                .toList();
    }

    private IncidentMessageResponse toIncidentMessageResponse(IncidentMessage incidentMessage) {
        return new IncidentMessageResponse(
                incidentMessage.getId(),
                incidentMessage.getCode(),
                incidentMessage.getDescription(),
                incidentMessage.getCategory().name()

        );
    }

    private IncidentResponse.AffectedBathroom toAffectedBathroom(Bathroom bathroom) {
        return new IncidentResponse.AffectedBathroom(bathroom.getId(), bathroom.getGender(), bathroom.getFloor(),
                bathroom.getBlock().getName());
    }

    private ReporterInfo toReporterInfo(Incident incident) {
        return new ReporterInfo(incident.getUser().getName(), incident.getUser().getEmail());
    }

    private IncidentMessageResponse toIncidentMessageResponse(Incident incident) {
        return new IncidentMessageResponse(
                incident.getIncidentMessage().getId(),
                incident.getIncidentMessage().getCode(),
                incident.getIncidentMessage().getDescription(),
                incident.getIncidentMessage().getCategory().name());
    }

    private IncidentResponse toIncidentResponse(Incident incident) {
        return new IncidentResponse(incident.getId(),
                toReporterInfo(incident),
                toIncidentMessageResponse(incident),
                toAffectedBathroom(incident.getBathroom()),
                incident.getStatus().toString(),
                incident.getReportedAt(),
                incident.getResolvedAt());
    }

}
