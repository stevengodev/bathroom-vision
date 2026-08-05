package com.foliaco.vision_bathroom.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.foliaco.vision_bathroom.entity.CleaningSchedule;

public interface CleaningScheduleRepository extends JpaRepository<CleaningSchedule, Long> {

    /** Horario con su baño y bloque cargados (evita N+1) */
    @Query("""
        SELECT cs FROM CleaningSchedule cs
        JOIN FETCH cs.bathroom b
        JOIN FETCH b.block
        WHERE cs.id = :id
    """)
    Optional<CleaningSchedule> findByIdWithDetails(@Param("id") Long id);

    /** Todos los horarios de un baño específico */
    @Query("""
        SELECT cs FROM CleaningSchedule cs
        JOIN FETCH cs.bathroom b
        JOIN FETCH b.block
        WHERE b.id = :bathroomId
        ORDER BY cs.startTime ASC
    """)
    List<CleaningSchedule> findByBathroomId(@Param("bathroomId") Long bathroomId);

    /** Todos los horarios de un usuario específico */
    List<CleaningSchedule> findByUserId(Long userId);

    /** Listado completo para admin con JOIN FETCH */
    @Query("""
        SELECT cs FROM CleaningSchedule cs
        JOIN FETCH cs.bathroom b
        JOIN FETCH b.block
        ORDER BY b.block.name, cs.startTime
    """)
    List<CleaningSchedule> findAllWithDetails();

    /** Horarios activos vigentes a una fecha dada (para consultas de programación) */
    @Query("""
        SELECT cs FROM CleaningSchedule cs
        JOIN FETCH cs.bathroom b
        JOIN FETCH b.block
        WHERE cs.startDate <= :date AND cs.endDate >= :date
        ORDER BY cs.startTime
    """)
    List<CleaningSchedule> findActiveSchedulesForDate(@Param("date") LocalDate date);

}

