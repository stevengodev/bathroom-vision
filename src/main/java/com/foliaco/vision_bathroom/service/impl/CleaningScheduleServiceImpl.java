package com.foliaco.vision_bathroom.service.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.foliaco.vision_bathroom.dto.BathroomResponse;
import com.foliaco.vision_bathroom.dto.CleaningScheduleRequest;
import com.foliaco.vision_bathroom.dto.CleaningScheduleResponse;
import com.foliaco.vision_bathroom.entity.Bathroom;
import com.foliaco.vision_bathroom.entity.CleaningSchedule;
import com.foliaco.vision_bathroom.entity.User;
import com.foliaco.vision_bathroom.exception.BadRequestException;
import com.foliaco.vision_bathroom.exception.ConflictException;
import com.foliaco.vision_bathroom.exception.NotFoundException;
import com.foliaco.vision_bathroom.repository.BathroomRepository;
import com.foliaco.vision_bathroom.repository.CleaningScheduleRepository;
import com.foliaco.vision_bathroom.repository.UserRepository;
import com.foliaco.vision_bathroom.service.CleaningScheduleService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CleaningScheduleServiceImpl implements CleaningScheduleService {

        private final CleaningScheduleRepository scheduleRepository;
        private final BathroomRepository bathroomRepository;
        private final UserRepository userRepository;

        @Override
        public List<CleaningScheduleResponse> findAll() {
                return scheduleRepository.findAllWithDetails()
                                .stream()
                                .map(cleaning -> toCleaningScheduleResponse(cleaning))
                                .toList();
        }

        @Override
        public List<CleaningScheduleResponse> findByUser(String email) {

                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new NotFoundException("Usuario no encontrado con email: " + email));

                List<CleaningSchedule> cleaningSchedules = scheduleRepository.findByUserId(user.getId());

                if (cleaningSchedules.isEmpty()) {
                        return List.of();
                }

                return cleaningSchedules.stream()
                                .map(cleaningSchedule -> toCleaningScheduleResponse(cleaningSchedule))
                                .toList();

        }

        @Override
        public CleaningScheduleResponse findById(Long id) {
                CleaningSchedule cleaningSchedule = scheduleRepository.findByIdWithDetails(id)
                                .orElseThrow(() -> new NotFoundException(
                                                "Horario de limpieza no encontrado con id: " + id));

                return toCleaningScheduleResponse(cleaningSchedule);
        }

        @Override
        public List<CleaningScheduleResponse> findByBathroom(Long bathroomId) {
                return scheduleRepository.findByBathroomId(bathroomId)
                                .stream()
                                .map(cleaning -> toCleaningScheduleResponse(cleaning))
                                .toList();
        }

        @Override
        public CleaningScheduleResponse create(CleaningScheduleRequest request) {

                Bathroom bathroom = bathroomRepository.findById(request.bathroomId()).orElseThrow(
                                () -> new NotFoundException("Baño no encontrado con id: " + request.bathroomId()));

                User user = userRepository.findById(request.userId()).orElseThrow(
                                () -> new NotFoundException("Usuario no encontrado con id: " + request.userId()));

                CleaningSchedule cleaningSchedule = new CleaningSchedule();

                if (request.frequency() == CleaningSchedule.Frequency.SEMANAL
                                && (request.daysOfWeek() == null || request.daysOfWeek().isBlank())) {
                        throw new BadRequestException("Para frecuencia WEEKLY, los días de la semana son requeridos");
                }

                if (!request.startTime().isBefore(request.endTime())) {
                        throw new BadRequestException(
                                        "La hora de inicio debe ser menor que la hora de fin.");
                }

                if (request.startDate().isAfter(request.endDate())) {
                        throw new BadRequestException(
                                        "La fecha de inicio no puede ser posterior a la fecha de fin.");
                }

                // Validar que no exista conflicto
                validateOverlap(request.bathroomId(), request, null);

                cleaningSchedule.setBathroom(bathroom);
                cleaningSchedule.setUser(user);
                cleaningSchedule.setStartDate(request.startDate());
                cleaningSchedule.setEndDate(request.endDate());
                cleaningSchedule.setFrequency(request.frequency());
                cleaningSchedule.setDaysOfWeek(request.daysOfWeek());
                cleaningSchedule.setStartTime(request.startTime());
                cleaningSchedule.setEndTime(request.endTime());

                return toCleaningScheduleResponse(scheduleRepository.save(cleaningSchedule));

        }

        @Override
        public CleaningScheduleResponse update(Long id, CleaningScheduleRequest request) {

                CleaningSchedule cleaningSchedule = scheduleRepository.findByIdWithDetails(id)
                                .orElseThrow(() -> new NotFoundException(
                                                "Horario de limpieza no encontrado con id: " + id));

                Bathroom bathroom = bathroomRepository.findById(request.bathroomId()).orElseThrow(
                                () -> new NotFoundException("Baño no encontrado con id: " + request.bathroomId()));

                if (request.frequency() == CleaningSchedule.Frequency.SEMANAL
                                && (request.daysOfWeek() == null || request.daysOfWeek().isBlank())) {

                        throw new BadRequestException("Para frecuencia SEMANAL los días de la semana son requeridos");
                }

                if (!request.startTime().isBefore(request.endTime())) {
                        throw new BadRequestException(
                                        "La hora de inicio debe ser menor que la hora de fin.");
                }

                if (request.startDate().isAfter(request.endDate())) {
                        throw new BadRequestException(
                                        "La fecha de inicio no puede ser posterior a la fecha de fin.");
                }

                // Validar conflicto ignorando el mismo horario
                validateOverlap(request.bathroomId(), request, id);

                cleaningSchedule.setBathroom(bathroom);
                cleaningSchedule.setStartDate(request.startDate());
                cleaningSchedule.setEndDate(request.endDate());
                cleaningSchedule.setFrequency(request.frequency());
                cleaningSchedule.setDaysOfWeek(request.daysOfWeek());
                cleaningSchedule.setStartTime(request.startTime());
                cleaningSchedule.setEndTime(request.endTime());

                CleaningSchedule saved = scheduleRepository.save(cleaningSchedule);
                log.info("Horario de limpieza actualizado: id={}", saved.getId());

                return toCleaningScheduleResponse(saved);
        }

        @Override
        public void delete(Long id) {

                CleaningSchedule cleaningSchedule = scheduleRepository.findByIdWithDetails(id)
                                .orElseThrow(() -> new NotFoundException(
                                                "Horario de limpieza no encontrado con id: " + id));

                scheduleRepository.delete(cleaningSchedule);
                log.info("Horario de limpieza eliminado: id={}, baño={}",
                                id, cleaningSchedule.getBathroom());
        }

        private boolean datesOverlap(CleaningSchedule schedule,
                        CleaningScheduleRequest request) {

                return !request.startDate().isAfter(schedule.getEndDate())
                                &&
                                !request.endDate().isBefore(schedule.getStartDate());
        }

        private boolean timesOverlap(CleaningSchedule schedule,
                        CleaningScheduleRequest request) {

                return request.startTime().isBefore(schedule.getEndTime())
                                &&
                                request.endTime().isAfter(schedule.getStartTime());
        }

        private Set<String> parseDays(String days) {

                if (days == null || days.isBlank()) {
                        return Collections.emptySet();
                }

                return Arrays.stream(days.split(","))
                                .map(String::trim)
                                .collect(Collectors.toSet());
        }

        private boolean daysOverlap(CleaningSchedule schedule,
                        CleaningScheduleRequest request) {

                // Si alguno es diario, siempre comparte días
                if (schedule.getFrequency() == CleaningSchedule.Frequency.DIARIO
                                || request.frequency() == CleaningSchedule.Frequency.DIARIO) {

                        return true;
                }

                Set<String> existingDays = parseDays(schedule.getDaysOfWeek());
                Set<String> requestDays = parseDays(request.daysOfWeek());

                return existingDays.stream().anyMatch(requestDays::contains);
        }

        private void validateOverlap(Long bathroomId,
                        CleaningScheduleRequest request,
                        Long excludeId) {

                List<CleaningSchedule> schedules = scheduleRepository.findByBathroomId(bathroomId);

                for (CleaningSchedule schedule : schedules) {

                        // Ignorar el mismo registro cuando se actualiza
                        if (schedule.getId().equals(excludeId)) {
                                continue;
                        }

                        // 1. Validar fechas
                        if (!datesOverlap(schedule, request)) {
                                continue;
                        }

                        // 2. Validar días
                        if (!daysOverlap(schedule, request)) {
                                continue;
                        }

                        // 3. Validar horas
                        if (!timesOverlap(schedule, request)) {
                                continue;
                        }

                        throw new ConflictException(
                                        String.format(
                                                        "El horario %s - %s se solapa con otro horario activo de este baño.",
                                                        request.startTime(),
                                                        request.endTime()));
                }
        }

        private CleaningScheduleResponse toCleaningScheduleResponse(CleaningSchedule cleaningSchedule) {
                return new CleaningScheduleResponse(
                                cleaningSchedule.getId(),
                                new BathroomResponse(
                                                cleaningSchedule.getBathroom().getId(),
                                                cleaningSchedule.getBathroom().getGender(),
                                                cleaningSchedule.getBathroom().getBlock().getId(),
                                                cleaningSchedule.getBathroom().getBlock().getName(),
                                                cleaningSchedule.getBathroom().getStatus(),
                                                cleaningSchedule.getBathroom().getFloor()),
                                cleaningSchedule.getUser().getId(),
                                cleaningSchedule.getUser().getName(),
                                cleaningSchedule.getStartDate(),
                                cleaningSchedule.getEndDate(),
                                cleaningSchedule.getFrequency(),
                                cleaningSchedule.getDaysOfWeek(),
                                cleaningSchedule.getStartTime(),
                                cleaningSchedule.getEndTime());
        }

}
