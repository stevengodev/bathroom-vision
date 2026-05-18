package com.foliaco.vision_bathroom.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.foliaco.vision_bathroom.dto.CleaningScheduleRequest;
import com.foliaco.vision_bathroom.dto.CleaningScheduleResponse;
import com.foliaco.vision_bathroom.entity.Bathroom;
import com.foliaco.vision_bathroom.entity.Block;
import com.foliaco.vision_bathroom.entity.CleaningSchedule;
import com.foliaco.vision_bathroom.entity.User;
import com.foliaco.vision_bathroom.exception.BadRequestException;
import com.foliaco.vision_bathroom.exception.ConflictException;
import com.foliaco.vision_bathroom.exception.NotFoundException;
import com.foliaco.vision_bathroom.repository.BathroomRepository;
import com.foliaco.vision_bathroom.repository.CleaningScheduleRepository;
import com.foliaco.vision_bathroom.repository.UserRepository;
import com.foliaco.vision_bathroom.service.impl.CleaningScheduleServiceImpl;

@ExtendWith(MockitoExtension.class)
public class CleaningScheduleServiceTest {

    @Mock
    private CleaningScheduleRepository scheduleRepository;

    @Mock
    private BathroomRepository bathroomRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CleaningScheduleServiceImpl service;

    private Bathroom bathroom;
    private User user;
    private CleaningSchedule cleaningSchedule;
    private CleaningScheduleRequest request;

    @BeforeEach
    void setUp() {

        Block block = new Block();
        block.setId(1L);
        block.setName("Bloque A");

        bathroom = new Bathroom();
        bathroom.setId(1L);
        bathroom.setBlock(block);
        bathroom.setFloor(2);

        user = new User();
        user.setId(1L);
        user.setName("Juan");
        user.setEmail("juan@test.com");

        cleaningSchedule = new CleaningSchedule();
        cleaningSchedule.setId(1L);
        cleaningSchedule.setBathroom(bathroom);
        cleaningSchedule.setUser(user);
        cleaningSchedule.setStartDate(LocalDate.now());
        cleaningSchedule.setEndDate(LocalDate.now().plusDays(10));
        cleaningSchedule.setFrequency(CleaningSchedule.Frequency.DIARIO);
        cleaningSchedule.setStartTime(LocalTime.of(8, 0));
        cleaningSchedule.setEndTime(LocalTime.of(10, 0));

        request = new CleaningScheduleRequest(
                1L,
                1L,
                LocalDate.now(),
                LocalDate.now().plusDays(10),
                CleaningSchedule.Frequency.DIARIO,
                null,
                LocalTime.of(8, 0),
                LocalTime.of(10, 0)
        );
    }

    @Test
    @DisplayName("Debe retornar todos los horarios")
    void shouldFindAll() {

        when(scheduleRepository.findAllWithDetails())
                .thenReturn(List.of(cleaningSchedule));

        List<CleaningScheduleResponse> response = service.findAll();

        assertNotNull(response);
        assertEquals(1, response.size());

        verify(scheduleRepository).findAllWithDetails();
    }

    @Test
    @DisplayName("Debe buscar horarios por usuario")
    void shouldFindByUser() {

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));

        when(scheduleRepository.findByUserId(user.getId()))
                .thenReturn(List.of(cleaningSchedule));

        List<CleaningScheduleResponse> response =
                service.findByUser(user.getEmail());

        assertEquals(1, response.size());

        verify(userRepository).findByEmail(user.getEmail());
        verify(scheduleRepository).findByUserId(user.getId());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando usuario no existe")
    void shouldThrowWhenUserNotFound() {

        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.findByUser("fake@test.com"));
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando usuario no tiene horarios")
    void shouldReturnEmptyListWhenNoSchedules() {

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));

        when(scheduleRepository.findByUserId(user.getId()))
                .thenReturn(List.of());

        List<CleaningScheduleResponse> response =
                service.findByUser(user.getEmail());

        assertTrue(response.isEmpty());
    }

    @Test
    @DisplayName("Debe encontrar horario por id")
    void shouldFindById() {

        when(scheduleRepository.findByIdWithDetails(1L))
                .thenReturn(Optional.of(cleaningSchedule));

        CleaningScheduleResponse response = service.findById(1L);

        assertNotNull(response);
        assertEquals(1L, response.id());

        verify(scheduleRepository).findByIdWithDetails(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando horario no existe")
    void shouldThrowWhenScheduleNotFound() {

        when(scheduleRepository.findByIdWithDetails(1L))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.findById(1L));
    }

    @Test
    @DisplayName("Debe buscar horarios por baño")
    void shouldFindByBathroom() {

        when(scheduleRepository.findByBathroomId(1L))
                .thenReturn(List.of(cleaningSchedule));

        List<CleaningScheduleResponse> response =
                service.findByBathroom(1L);

        assertEquals(1, response.size());

        verify(scheduleRepository).findByBathroomId(1L);
    }

    @Test
    @DisplayName("Debe crear horario correctamente")
    void shouldCreateSchedule() {

        when(bathroomRepository.findById(1L))
                .thenReturn(Optional.of(bathroom));

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(scheduleRepository.existsOverlap(
                anyLong(),
                any(),
                any(),
                anyLong()))
                .thenReturn(false);

        when(scheduleRepository.save(any(CleaningSchedule.class)))
                .thenReturn(cleaningSchedule);

        CleaningScheduleResponse response = service.create(request);

        assertNotNull(response);
        assertEquals(1L, response.id());

        verify(scheduleRepository).save(any(CleaningSchedule.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando baño no existe")
    void shouldThrowWhenBathroomNotFound() {

        when(bathroomRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.create(request));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando usuario no existe en create")
    void shouldThrowWhenUserNotFoundInCreate() {

        when(bathroomRepository.findById(1L))
                .thenReturn(Optional.of(bathroom));

        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.create(request));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando frecuencia semanal no tiene días")
    void shouldThrowWhenWeeklyWithoutDays() {

        CleaningScheduleRequest weeklyRequest =
                new CleaningScheduleRequest(
                        1L,
                        1L,
                        LocalDate.now(),
                        LocalDate.now().plusDays(10),
                        CleaningSchedule.Frequency.SEMANAL,
                        "",
                        LocalTime.of(8, 0),
                        LocalTime.of(10, 0)
                );

        when(bathroomRepository.findById(1L))
                .thenReturn(Optional.of(bathroom));

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class,
                () -> service.create(weeklyRequest));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando existe solapamiento")
    void shouldThrowWhenOverlapExists() {

        when(bathroomRepository.findById(1L))
                .thenReturn(Optional.of(bathroom));

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(scheduleRepository.existsOverlap(
                anyLong(),
                any(),
                any(),
                anyLong()))
                .thenReturn(true);

        assertThrows(ConflictException.class,
                () -> service.create(request));
    }

    @Test
    @DisplayName("Debe actualizar horario correctamente")
    void shouldUpdateSchedule() {

        when(scheduleRepository.findByIdWithDetails(1L))
                .thenReturn(Optional.of(cleaningSchedule));

        when(bathroomRepository.findById(1L))
                .thenReturn(Optional.of(bathroom));

        when(scheduleRepository.save(any(CleaningSchedule.class)))
                .thenReturn(cleaningSchedule);

        CleaningScheduleResponse response =
                service.update(1L, request);

        assertNotNull(response);

        verify(scheduleRepository).save(any(CleaningSchedule.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando horario no existe en update")
    void shouldThrowWhenUpdateScheduleNotFound() {

        when(scheduleRepository.findByIdWithDetails(1L))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.update(1L, request));
    }

    @Test
    @DisplayName("Debe eliminar horario correctamente")
    void shouldDeleteSchedule() {

        when(scheduleRepository.findByIdWithDetails(1L))
                .thenReturn(Optional.of(cleaningSchedule));

        service.delete(1L);

        verify(scheduleRepository).delete(cleaningSchedule);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando horario no existe en delete")
    void shouldThrowWhenDeleteScheduleNotFound() {

        when(scheduleRepository.findByIdWithDetails(1L))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.delete(1L));
    }
    
}
