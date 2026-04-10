package com.foliaco.vision_bathroom.dto;

import com.foliaco.vision_bathroom.entity.CleaningSchedule;

import java.time.LocalDate;
import java.time.LocalTime;

public record CleaningScheduleResponse(
        Long id,
        Long bathroomId,
        LocalDate startDate,
        LocalDate endDate,
        CleaningSchedule.Frequency frequency,
        String daysOfWeek,
        LocalTime startTime,
        LocalTime endTime
) {
    
}
