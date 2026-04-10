package com.foliaco.vision_bathroom.service;

import java.util.List;

import com.foliaco.vision_bathroom.dto.*;
import com.foliaco.vision_bathroom.entity.Incident;
import com.foliaco.vision_bathroom.entity.IncidentMessage;

/**
 * HU-008: Reportar incidencia
 * HU-009: Ver lista de incidencias pendientes (STAFF/ADMIN)
 * HU-010: Actualizar estado de incidencia
 */
public interface IncidentService {
    
    /* Catálogo de tipos de incidencia */
    List<IncidentMessageResponse> findAllIncidentMessages();

    IncidentCreatedResponse report(Long reporterUserId, IncidentRequest request);

    int resolveIncidentTypeInBathroom(Long bathroomId, Long incidentMessageId);

    IncidentResponse findById(Long id);

    /* Listar incidencias */
    List<IncidentResponse> findAll(Incident.Status statusFilter);

    List<IncidentResponse> findAllByStatusAndCategory(Incident.Status statusFilter, IncidentMessage.Category categoryFilter);

    List<IncidentSummary> findByIncidentMessageIds(List<Long> incidentMessageIds);

    /** Incidencias reportadas por el propio usuario (para vista del STUDENT) */
    List<IncidentResponse> findByUser(Long userId);

    /** Historial de incidencias de un baño específico */
    List<IncidentResponse> findByBathroom(Long bathroomId);

}
