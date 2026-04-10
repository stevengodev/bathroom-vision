package com.foliaco.vision_bathroom.controller;

import com.foliaco.vision_bathroom.dto.MaintenanceRequest;
import com.foliaco.vision_bathroom.dto.MaintenanceResponse;
import com.foliaco.vision_bathroom.entity.Maintenance;
import com.foliaco.vision_bathroom.service.MaintenanceService;
import com.foliaco.vision_bathroom.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/maintenances")
@RequiredArgsConstructor
public class MaintenanceController {

    private final MaintenanceService maintenanceService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<MaintenanceResponse>> getAll() {
        return ResponseEntity.ok(maintenanceService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MaintenanceResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(maintenanceService.findById(id));
    }

    @GetMapping("/bathroom/{bathroomId}")
    public ResponseEntity<List<MaintenanceResponse>> getByBathroom(@PathVariable Long bathroomId) {
        return ResponseEntity.ok(maintenanceService.findByBathroom(bathroomId));
    }

    @GetMapping("/my")
    public ResponseEntity<List<MaintenanceResponse>> getMyTickets(Authentication auth) {
        String email = (String) auth.getPrincipal();
        Long currentUserId = userService.getCurrentUser(email).id();
        return ResponseEntity.ok(maintenanceService.findByCurrentUser(currentUserId));
    }

    @PostMapping
    public ResponseEntity<MaintenanceResponse> create(
            @Valid @RequestBody MaintenanceRequest request
    ) {

        MaintenanceResponse created = maintenanceService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<MaintenanceResponse> update(@PathVariable Long id,
            @Valid @RequestBody MaintenanceRequest request) {
        MaintenanceResponse updated = maintenanceService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<MaintenanceResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam Maintenance.Status status
    ) {
        MaintenanceResponse updated = maintenanceService.updateStatus(id, status);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        maintenanceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
