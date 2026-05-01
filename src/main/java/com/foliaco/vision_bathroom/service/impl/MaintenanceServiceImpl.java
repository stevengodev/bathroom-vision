package com.foliaco.vision_bathroom.service.impl;

import com.foliaco.vision_bathroom.dto.BathroomResponse;
import com.foliaco.vision_bathroom.dto.MaintenanceRequest;
import com.foliaco.vision_bathroom.dto.MaintenanceResponse;
import com.foliaco.vision_bathroom.entity.Bathroom;
import com.foliaco.vision_bathroom.entity.Maintenance;
import com.foliaco.vision_bathroom.entity.Maintenance.Status;
import com.foliaco.vision_bathroom.exception.ConflictException;
import com.foliaco.vision_bathroom.exception.NotFoundException;
import com.foliaco.vision_bathroom.repository.BathroomRepository;
import com.foliaco.vision_bathroom.repository.MaintenanceRepository;
import com.foliaco.vision_bathroom.service.MaintenanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MaintenanceServiceImpl implements MaintenanceService {

    private final MaintenanceRepository maintenanceRepository;
    private final BathroomRepository bathroomRepository;

    @Override
    public List<MaintenanceResponse> findAll() {
        return maintenanceRepository.findAll().stream()
                .map(maintenance -> toMaintenanceResponse(maintenance))
                .toList();
    }

    @Override
    public List<MaintenanceResponse> findAllByStatus(Status status) {

        return maintenanceRepository.findAllByStatus(status).stream()
                .map(maintenance -> toMaintenanceResponse(maintenance))
                .toList();

    }

    @Override
    public MaintenanceResponse findById(Long id) {
        Maintenance maintenance = maintenanceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ticket de mantenimiento no encontrado con id: " + id));

        return toMaintenanceResponse(maintenance);

    }

    @Override
    public List<MaintenanceResponse> findByBathroom(Long bathroomId) {
        
        Bathroom bathroom = bathroomRepository.findById(bathroomId)
                .orElseThrow(() -> new NotFoundException("Baño no encontrado con id: " + bathroomId));

        return maintenanceRepository.findByBathroomId(bathroom.getId()).stream()
                .map(maintenance -> toMaintenanceResponse(maintenance))
                .toList();

    }

    @Override
    public List<MaintenanceResponse> findByCurrentUser(Long userId) {
        return List.of();
    }

    @Override
    public MaintenanceResponse create(MaintenanceRequest request) {

        Bathroom bathroom = bathroomRepository.findById(request.bathroomId()).orElseThrow(
                () -> new NotFoundException("Baño no encontrado con id " + request.bathroomId()));

        Maintenance maintenance = new Maintenance();
        maintenance.setBathroom(bathroom);
        maintenance.setTechnicianFullName(request.technicianFullName());
        maintenance.setDescription(request.description());
        maintenance.setStatus(Maintenance.Status.ABIERTO);

        return toMaintenanceResponse(maintenanceRepository.save(maintenance));
    }

    @Override
    public MaintenanceResponse update(Long id, MaintenanceRequest request) {

        Maintenance maintenance = maintenanceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Mantenimiento no encontrado con id: " + id));

        if (maintenance.getStatus() == Maintenance.Status.CERRADO) {
            throw new ConflictException("No se puede modificar un ticket en estado CERRADO");
        }

        Bathroom bathroom = bathroomRepository.findById(request.bathroomId()).orElseThrow(
                () -> new NotFoundException("Bathroom no encontrado con id: " + request.bathroomId()));

        maintenance.setBathroom(bathroom);
        maintenance.setDescription(request.description().trim());

        return toMaintenanceResponse(maintenanceRepository.save(maintenance));

    }

    @Override
    public MaintenanceResponse updateStatus(Long id, Maintenance.Status status) {

        Maintenance maintenance = maintenanceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Mantenimiento no encontrado con id: " + id));

        maintenance.setStatus(status);

        if (status == Maintenance.Status.CERRADO) {
            maintenance.setResolvedAt(LocalDateTime.now());
        }

        maintenanceRepository.save(maintenance);

        return toMaintenanceResponse(maintenance);

    }

    @Override
    public void delete(Long id) {

        Maintenance maintenance = maintenanceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Mantenimiento no encontrado con id: " + id));

        maintenanceRepository.delete(maintenance);
    }

    private MaintenanceResponse toMaintenanceResponse(Maintenance maintenance) {

        Bathroom bathroom = maintenance.getBathroom();

        return new MaintenanceResponse(
                maintenance.getId(), 
                toBathroomResponse(bathroom), 
                maintenance.getTechnicianFullName(), 
                maintenance.getDescription(),
                maintenance.getStatus(), 
                maintenance.getResolvedAt()
            );
    }


    private BathroomResponse toBathroomResponse(Bathroom bathroom) {
        return new BathroomResponse(
            bathroom.getId(),
            bathroom.getGender(),
            bathroom.getBlock().getId(),
            bathroom.getBlock().getName(),
            bathroom.getStatus(),
            bathroom.getFloor()
        );
    }



}
