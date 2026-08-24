package com.foliaco.vision_bathroom.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.foliaco.vision_bathroom.dto.IncidentSummary;
import com.foliaco.vision_bathroom.entity.Bathroom;
import com.foliaco.vision_bathroom.entity.Incident;
import com.foliaco.vision_bathroom.entity.Incident.Status;
import com.foliaco.vision_bathroom.entity.IncidentMessage;
import com.foliaco.vision_bathroom.entity.User;

public interface IncidentRepository extends JpaRepository<Incident, Long> {

        /**
         * Incidente con todas sus relaciones cargadas (evita N+1).
         * Usado para el detalle de una sola incidencia.
         */
        @Query("""
                            SELECT DISTINCT i FROM Incident i
                            JOIN FETCH i.user
                            JOIN FETCH i.incidentMessage
                            LEFT JOIN FETCH i.bathroom b
                            LEFT JOIN FETCH b.block
                            WHERE i.id = :id
                        """)
        Optional<Incident> findByIdWithDetails(@Param("id") Long id);

        @Query("""
                            SELECT DISTINCT i FROM Incident i
                            JOIN FETCH i.user
                            JOIN FETCH i.incidentMessage
                            LEFT JOIN FETCH i.bathroom b
                            LEFT JOIN FETCH b.block
                            WHERE i.status = :status
                            ORDER BY i.reportedAt DESC
                        """)
        List<Incident> findAllWithDetails(@Param("status") Incident.Status status);

        @Query("""
                            SELECT DISTINCT i FROM Incident i
                            JOIN FETCH i.user
                            JOIN FETCH i.incidentMessage
                            LEFT JOIN FETCH i.bathroom b
                            LEFT JOIN FETCH b.block
                            WHERE i.status = :status AND i.incidentMessage.category = :category
                            ORDER BY i.reportedAt DESC
                        """)
        List<Incident> findAllByStatusAndMessageCategory(@Param("status") Incident.Status status,
                        @Param("category") IncidentMessage.Category category);

        /**
         * Incidencias reportadas por un usuario específico.
         */
        @Query("""
                            SELECT i FROM Incident i
                            JOIN FETCH i.incidentMessage
                            LEFT JOIN FETCH i.bathroom
                            WHERE i.user.id = :userId
                            ORDER BY i.reportedAt DESC
                        """)
        List<Incident> findByUserId(@Param("userId") Long userId);

        /**
         * Incidencias de un baño específico pendientes
         */
        @Query("""
                            SELECT DISTINCT i FROM Incident i
                            JOIN FETCH i.user
                            JOIN FETCH i.incidentMessage
                            JOIN i.bathroom b
                            WHERE b.id = :bathroomId
                            AND i.status = 'PENDING'
                            ORDER BY i.reportedAt DESC
                        """)
        List<Incident> findByBathroomIdAndPendingStatus(@Param("bathroomId") Long bathroomId);

        List<Incident> findByUserIdAndBathroomIdAndStatus(Long userId, Long bathroomId, Status status);

        boolean existsByBathroomId(Long bathroomId);

        boolean existsByUserAndBathroomAndIncidentMessageAndStatus(
                        User user,
                        Bathroom bathroom,
                        IncidentMessage incidentMessage,
                        Incident.Status status);

        /** Conteo por estado para dashboard */
        long countByStatus(Incident.Status status);

        @Modifying
        @Query("""
                        UPDATE Incident i
                        SET i.status = :status,
                            i.resolvedAt = :resolvedAt
                        WHERE i.bathroom.id = :bathroomId
                        AND i.incidentMessage.id = :messageId
                        AND i.status = 'PENDING'
                        """)
        int updateStatusByBathroomAndIncidentMessage(
                        Long bathroomId,
                        Long messageId,
                        Incident.Status status,
                        LocalDateTime resolvedAt);

        @Query("""
                        SELECT
                            i.bathroom.id AS bathroomId,
                            i.incidentMessage.id AS incidentMessageId,
                            COUNT(DISTINCT i.user.id) AS totalReports
                        FROM Incident i
                        WHERE i.status = 'PENDING'
                        AND i.incidentMessage.id IN :messageIds
                        GROUP BY i.bathroom.id, i.incidentMessage.id
                        """)
        List<IncidentSummary> findPendingIncidentSummaryByMessages(List<Long> messageIds);
}