package com.foliaco.vision_bathroom.repository;

import com.foliaco.vision_bathroom.entity.Maintenance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MaintenanceRepository extends JpaRepository<Maintenance, Long> {

    @Query("""
        SELECT m FROM Maintenance m
        JOIN FETCH m.bathroom b
        JOIN FETCH b.block
        WHERE m.id = :id
    """)
    Optional<Maintenance> findByIdWithDetails(@Param("id") Long id);

    /**
     * Listado filtrable por estado y/o prioridad.
     * Ambos filtros son opcionales (null = sin filtro).
     */
    @Query("""
        SELECT m FROM Maintenance m
        JOIN FETCH m.bathroom b
        JOIN FETCH b.block
        WHERE m.status = :status
        ORDER BY m.reportedAt DESC
    """)
    List<Maintenance> findAllFiltered(@Param("status") Maintenance.Status status);

    /** Tickets de un baño específico (historial) */
    @Query("""
        SELECT mt FROM Maintenance mt
        WHERE mt.bathroom.id = :bathroomId
        ORDER BY mt.reportedAt DESC
    """)
    List<Maintenance> findByBathroomId(@Param("bathroomId") Long bathroomId);

    /** Conteos para dashboard */
    long countByStatus(Maintenance.Status status);

}
