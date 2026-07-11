package com.foliaco.vision_bathroom.service.impl;

import java.util.List;

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

                User user = userRepository.findByEmail(email).orElseThrow(() -> new NotFoundException("Usuario no encontrado con email: " + email));

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

                boolean hasOverlap = scheduleRepository.existsOverlap(
                                request.bathroomId(),
                                request.startTime(),
                                request.endTime(),
                                -1L);

                if (hasOverlap) {
                        throw new ConflictException(
                                        "El horario " + request.startTime() + " - " + request.endTime() +
                                                        " se solapa con otro horario activo de este baño");
                }

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
                                .orElseThrow(() -> new NotFoundException("Horario de limpieza no encontrado con id: " + id));

                scheduleRepository.delete(cleaningSchedule);
                log.info("Horario de limpieza eliminado: id={}, baño={}",
                                id, cleaningSchedule.getBathroom());
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
                                                cleaningSchedule.getBathroom().getFloor()
                                        ),
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
