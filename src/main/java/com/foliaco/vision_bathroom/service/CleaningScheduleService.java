package com.foliaco.vision_bathroom.service;

import java.util.List;

import com.foliaco.vision_bathroom.dto.CleaningScheduleRequest;
import com.foliaco.vision_bathroom.dto.CleaningScheduleResponse;


public interface CleaningScheduleService {

    List<CleaningScheduleResponse> findAll();
    List<CleaningScheduleResponse> findByUser(String email);
    CleaningScheduleResponse findById(Long id);
    List<CleaningScheduleResponse> findByBathroom(Long bathroomId);
    CleaningScheduleResponse create(CleaningScheduleRequest request);
    CleaningScheduleResponse update(Long id, CleaningScheduleRequest request);
    void delete(Long id);
}
