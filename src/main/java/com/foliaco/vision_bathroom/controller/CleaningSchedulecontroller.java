package com.foliaco.vision_bathroom.controller;

import com.foliaco.vision_bathroom.dto.CleaningScheduleRequest;
import com.foliaco.vision_bathroom.dto.CleaningScheduleResponse;
import com.foliaco.vision_bathroom.service.CleaningScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schedules/cleaning")
@RequiredArgsConstructor
public class CleaningSchedulecontroller {

    private final CleaningScheduleService scheduleService;

    @GetMapping
    @PreAuthorize("hasAnyRole('CLEANER','ADMIN')")
    public ResponseEntity<List<CleaningScheduleResponse>> getAll() {
        return ResponseEntity.ok(scheduleService.findAll());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CleaningScheduleResponse> create(@Valid @RequestBody CleaningScheduleRequest request) {
        CleaningScheduleResponse created = scheduleService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CleaningScheduleResponse> update(@PathVariable Long id,
            @Valid @RequestBody CleaningScheduleRequest request) {
        CleaningScheduleResponse updated = scheduleService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        scheduleService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('CLEANER', 'ADMIN')")
    public ResponseEntity<List<CleaningScheduleResponse>> getByUser(Authentication auth) {
        String email = (String) auth.getPrincipal();
        return ResponseEntity.ok(scheduleService.findByUser(email));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLEANER', 'ADMIN')")
    public ResponseEntity<CleaningScheduleResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(scheduleService.findById(id));
    }

    @GetMapping("/bathroom/{bathroomId}")
    @PreAuthorize("hasAnyRole('CLEANER', 'ADMIN')")
    public ResponseEntity<List<CleaningScheduleResponse>> getByBathroom(@PathVariable Long bathroomId) {
        return ResponseEntity.ok(scheduleService.findByBathroom(bathroomId));
    }

}
