package com.foliaco.vision_bathroom.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.foliaco.vision_bathroom.entity.CleaningSchedule;

import jakarta.validation.constraints.NotNull;

public record CleaningScheduleRequest(

        @NotNull(message = "El baño es requerido")
        Long bathroomId,

        @NotNull(message = "El usuario es requerido")
        Long userId,

        @NotNull(message = "La fecha de inicio es requerida")
        LocalDate startDate,

        @NotNull(message = "La fecha de fin es requerida")
        LocalDate endDate,

        @NotNull(message = "La frecuencia es requerida")
        CleaningSchedule.Frequency frequency,

        /*
         * Días de la semana aplicables.
         * Requerido cuando frequency = WEEKLY.
         * Valores válidos: MO,TU,WE,TH,FR,SA,SU
         */
        String daysOfWeek,

        @NotNull(message = "La hora de inicio es requerida")
        LocalTime startTime,

        @NotNull(message = "La hora de fin es requerida")
        LocalTime endTime) {

}
