package com.foliaco.vision_bathroom.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.foliaco.vision_bathroom.dto.BathroomFilter;
import com.foliaco.vision_bathroom.dto.BathroomRequest;
import com.foliaco.vision_bathroom.dto.BathroomResponse;
import com.foliaco.vision_bathroom.dto.PushNotificationRequest;
import com.foliaco.vision_bathroom.entity.Bathroom;
import com.foliaco.vision_bathroom.entity.Bathroom.BathroomStatus;
import com.foliaco.vision_bathroom.entity.Block;
import com.foliaco.vision_bathroom.exception.NotFoundException;
import com.foliaco.vision_bathroom.firebase.NotificationService;
import com.foliaco.vision_bathroom.repository.BathroomRepository;
import com.foliaco.vision_bathroom.repository.BathroomSpecification;
import com.foliaco.vision_bathroom.repository.BlockRepository;
import com.foliaco.vision_bathroom.service.BathroomService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BathroomServiceImpl implements BathroomService {

    private final BathroomRepository bathroomRepository;
    private final BlockRepository blockRepository;
    private final NotificationService notificationService;

    @Override
    public List<BathroomResponse> findAll() {
        return bathroomRepository.findAll()
                .stream().map(bathroom -> toBathroomResponse(bathroom))
                .toList();
    }

    @Override
    public BathroomResponse findById(Long id) {

        Bathroom bathroom = bathroomRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Bathroom not found with id: " + id));

        return toBathroomResponse(bathroom);

    }

    @Override
    public List<BathroomResponse> findByBlockId(Long blockId) {

        return bathroomRepository.findAllByBlockId(blockId).stream()
                .map(bathroom -> toBathroomResponse(bathroom)).toList();

    }

    @Override
    public List<BathroomResponse> searchBathrooms(BathroomFilter filter) {

        Specification<Bathroom> spec = Specification
                .where(BathroomSpecification.hasStatus(filter.status()))
                .and(BathroomSpecification.hasGender(filter.gender()))
                .and(BathroomSpecification.hasBlockId(filter.blockId()))
                .and(BathroomSpecification.searchText(filter.query()));

        return bathroomRepository.findAll(spec).stream()
                .map(bathroom -> toBathroomResponse(bathroom))
                .toList();

    }

    @Override
    public BathroomResponse create(BathroomRequest request) {

        Block block = blockRepository.findById(request.blockId()).orElseThrow(
                () -> new NotFoundException("Block not found with id: " + request.blockId()));

        Bathroom bathroom = new Bathroom();
        bathroom.setBlock(block);
        bathroom.setFloor(request.floor());
        bathroom.setGender(request.gender());
        bathroom.setStatus(request.status());

        return toBathroomResponse(bathroomRepository.save(bathroom));

    }

    @Override
    public BathroomResponse update(Long id, BathroomRequest request) {
        Bathroom bathroom = bathroomRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Bathroom not found with id: " + id));

        Block block = blockRepository.findById(request.blockId()).orElseThrow(
                () -> new NotFoundException("Block not found with id: " + request.blockId()));

        bathroom.setBlock(block);
        bathroom.setFloor(request.floor());
        bathroom.setGender(request.gender());
        bathroom.setStatus(request.status());

        return toBathroomResponse(bathroomRepository.save(bathroom));

    }

    @Override
    public BathroomResponse updateStatus(Long id, BathroomStatus status) {
        Bathroom bathroom = bathroomRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Bathroom not found with id " + id));

        // Evita enviar notificación si el estado no cambió.
        if (bathroom.getStatus() == status) {
            return toBathroomResponse(bathroom);
        }

        bathroom.setStatus(status);

        Bathroom savedBathroom = bathroomRepository.save(bathroom);

        notificationService.notifyBathroomStatusChanged(buildPushNotificationRequest(savedBathroom));

        return toBathroomResponse(savedBathroom);
    }

    @Override
    public void delete(Long id) {
        Bathroom bathroom = bathroomRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Bathroom not found with id: " + id));

        bathroomRepository.delete(bathroom);
    }

    private BathroomResponse toBathroomResponse(Bathroom bathroom) {
        return new BathroomResponse(
                bathroom.getId(),
                bathroom.getGender(),
                bathroom.getBlock().getId(),
                bathroom.getBlock().getName(),
                bathroom.getStatus(),
                bathroom.getFloor());
    }

    private PushNotificationRequest buildPushNotificationRequest(Bathroom bathroom) {

        String title = "Baño " + bathroom.getStatus().toDisplayString();
        String body = String.format(
                "El baño (%s) del piso %d está %s",
                bathroom.getGender().toString().toLowerCase(),
                bathroom.getFloor(),
                bathroom.getStatus().toString().toLowerCase());

        Map<String, String> data = new HashMap<>();
        data.put("blockName", bathroom.getBlock().getName());
        data.put("status", bathroom.getStatus().toString());
        data.put("gender", bathroom.getGender().toString());
        data.put("floor", bathroom.getFloor().toString());

        return new PushNotificationRequest(title, body, data);
    }

}
