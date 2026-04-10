package com.foliaco.vision_bathroom.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.foliaco.vision_bathroom.dto.BlockRequest;
import com.foliaco.vision_bathroom.dto.BlockResponse;
import com.foliaco.vision_bathroom.service.BlockService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/blocks")
@RequiredArgsConstructor
public class BlockController {

    private final BlockService blockService;

    @GetMapping
    public ResponseEntity<List<BlockResponse>> getAll() {
        return ResponseEntity.ok(blockService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BlockResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(blockService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BlockResponse> create(@Valid @RequestBody BlockRequest request) {
        BlockResponse created = blockService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BlockResponse> update(@PathVariable Long id,
                                                @Valid @RequestBody BlockRequest request) {
        BlockResponse updated = blockService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        blockService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
