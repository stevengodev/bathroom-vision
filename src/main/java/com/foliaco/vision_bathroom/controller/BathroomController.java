package com.foliaco.vision_bathroom.controller;

import com.foliaco.vision_bathroom.dto.UpdateBathroomStatusRequest;
import com.foliaco.vision_bathroom.entity.Bathroom.BathroomStatus;
import com.foliaco.vision_bathroom.entity.Bathroom.Gender;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.foliaco.vision_bathroom.dto.BathroomFilter;
import com.foliaco.vision_bathroom.dto.BathroomRequest;
import com.foliaco.vision_bathroom.dto.BathroomResponse;
import com.foliaco.vision_bathroom.service.BathroomService;

import java.util.List;


@RestController
@RequestMapping("/api/bathrooms")
@RequiredArgsConstructor
public class BathroomController {

    private final BathroomService bathroomService;

    @GetMapping("/block/{blockId}")
    public ResponseEntity<List<BathroomResponse>> getByBlockId(@PathVariable Long blockId) {
        return ResponseEntity.ok(bathroomService.findByBlockId(blockId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BathroomResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(bathroomService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BathroomResponse> create(@Valid @RequestBody BathroomRequest request) {
        BathroomResponse created = bathroomService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BathroomResponse> update(@PathVariable Long id,
            @Valid @RequestBody BathroomRequest request) {
        BathroomResponse updated = bathroomService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        bathroomService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<BathroomResponse> updateStatus(@PathVariable Long id,
            @Valid @RequestBody UpdateBathroomStatusRequest request) {

        BathroomResponse response = bathroomService.updateStatus(id, request.status());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<List<BathroomResponse>> search(
            @RequestParam(required = false) BathroomStatus status,
            @RequestParam(required = false) Gender gender,
            @RequestParam(required = false) Long blockId,
            @RequestParam(required = false) String query) {

        if (status == null && gender == null && blockId == null && query == null) {
            return ResponseEntity.ok(bathroomService.findAll());
        }

        BathroomFilter filter = new BathroomFilter(status, gender, blockId, query);
        List<BathroomResponse> bathrooms = bathroomService.searchBathrooms(filter);

        return ResponseEntity.ok(bathrooms);
    }

}
