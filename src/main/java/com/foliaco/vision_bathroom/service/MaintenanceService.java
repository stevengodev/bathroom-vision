package com.foliaco.vision_bathroom.service;

import com.foliaco.vision_bathroom.dto.MaintenanceRequest;
import com.foliaco.vision_bathroom.dto.MaintenanceResponse;
import com.foliaco.vision_bathroom.entity.Maintenance;

import java.util.List;

public interface MaintenanceService {

    List<MaintenanceResponse> findAll();
    MaintenanceResponse findById(Long id);
    List<MaintenanceResponse> findByBathroom(Long bathroomId);
    List<MaintenanceResponse> findByCurrentUser(Long userId);
    MaintenanceResponse create(MaintenanceRequest request);
    MaintenanceResponse update(Long id, MaintenanceRequest request);
    MaintenanceResponse updateStatus(Long id, Maintenance.Status status);
    void delete(Long id);


}

