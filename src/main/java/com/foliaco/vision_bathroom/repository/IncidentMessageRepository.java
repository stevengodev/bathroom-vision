package com.foliaco.vision_bathroom.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.foliaco.vision_bathroom.entity.IncidentMessage;

public interface IncidentMessageRepository extends JpaRepository<IncidentMessage, Long> {

    Optional<IncidentMessage> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    /** Solo los activos para mostrar al usuario en el formulario de reporte */
    List<IncidentMessage> findAllByOrderByDescriptionAsc();
}
