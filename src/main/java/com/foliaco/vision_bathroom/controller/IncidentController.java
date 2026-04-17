package com.foliaco.vision_bathroom.controller;

import java.util.List;

import com.foliaco.vision_bathroom.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.foliaco.vision_bathroom.entity.Incident.Status;
import com.foliaco.vision_bathroom.entity.IncidentMessage.Category;
import com.foliaco.vision_bathroom.service.IncidentService;
import com.foliaco.vision_bathroom.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
public class IncidentController {

    private final IncidentService incidentService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<IncidentResponse>> getAll(@RequestParam Status status, 
                                                        @RequestParam(required = false) Category category) {

        if (category != null) {
            return ResponseEntity.ok(incidentService.findAllByStatusAndCategory(status, category));
        }

        return ResponseEntity.ok(incidentService.findAll(status));
    }

    @PatchMapping(value = "/{messageId}/status", params = "bathroomId")
    public ResponseEntity<Integer> updateStatus(@PathVariable Long messageId, @RequestParam Long bathroomId) {
        int rowsUpdated = incidentService.resolveIncidentTypeInBathroom(bathroomId, messageId);
        return ResponseEntity.ok(rowsUpdated);
    }

    @PostMapping
    public ResponseEntity<IncidentCreatedResponse> report(@Valid @RequestBody IncidentRequest request) {
        Long currentUserId = userService.getCurrentUser(request.email()).id();
        IncidentCreatedResponse response = incidentService.report(currentUserId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<List<IncidentResponse>> getMyIncidents(Authentication authentication) {
        String email = (String) authentication.getPrincipal();
        Long currentUserId = userService.getCurrentUser(email).id();
        return ResponseEntity.ok(incidentService.findByUser(currentUserId));
    }

    @GetMapping("/messages")
    public ResponseEntity<List<IncidentMessageResponse>> getMessages() {
        return ResponseEntity.ok(incidentService.findAllIncidentMessages());
    }

    @GetMapping("/{id}")
    public ResponseEntity<IncidentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(incidentService.findById(id));
    }
    
    @GetMapping("/bathroom/{bathroomId}")
    public ResponseEntity<List<IncidentResponse>> getByBathroom(@PathVariable Long bathroomId) {
        return ResponseEntity.ok(incidentService.findByBathroom(bathroomId));
    }

}
