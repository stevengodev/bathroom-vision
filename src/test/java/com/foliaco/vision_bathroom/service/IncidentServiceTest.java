package com.foliaco.vision_bathroom.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.foliaco.vision_bathroom.dto.IncidentCreatedResponse;
import com.foliaco.vision_bathroom.dto.IncidentMessageResponse;
import com.foliaco.vision_bathroom.dto.IncidentRequest;
import com.foliaco.vision_bathroom.dto.IncidentResponse;
import com.foliaco.vision_bathroom.dto.IncidentSummary;
import com.foliaco.vision_bathroom.entity.Bathroom;
import com.foliaco.vision_bathroom.entity.Block;
import com.foliaco.vision_bathroom.entity.Incident;
import com.foliaco.vision_bathroom.entity.IncidentMessage;
import com.foliaco.vision_bathroom.entity.User;
import com.foliaco.vision_bathroom.exception.NotFoundException;
import com.foliaco.vision_bathroom.repository.BathroomRepository;
import com.foliaco.vision_bathroom.repository.IncidentMessageRepository;
import com.foliaco.vision_bathroom.repository.IncidentRepository;
import com.foliaco.vision_bathroom.repository.UserRepository;
import com.foliaco.vision_bathroom.service.impl.IncidentServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IncidentServiceTest {

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private IncidentMessageRepository incidentMessageRepository;

    @Mock
    private BathroomRepository bathroomRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private IncidentServiceImpl service;

    private User user;
    private Bathroom bathroom;
    private IncidentMessage incidentMessage;
    private Incident incident;
    private IncidentRequest request;

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

        incidentMessage = new IncidentMessage();
        incidentMessage.setId(1L);
        incidentMessage.setCode("WC001");
        incidentMessage.setDescription("Baño tapado");
        incidentMessage.setCategory(IncidentMessage.Category.LIMPIEZA);

        incident = new Incident();
        incident.setId(1L);
        incident.setUser(user);
        incident.setBathroom(bathroom);
        incident.setIncidentMessage(incidentMessage);
        incident.setStatus(Incident.Status.PENDING);
        incident.setReportedAt(LocalDateTime.now());

        request = new IncidentRequest(user.getEmail(),
                List.of(1L),
                1L
        );
    }

    @Test
    @DisplayName("Debe retornar todos los mensajes de incidentes")
    void shouldFindAllIncidentMessages() {

        when(incidentMessageRepository.findAllByOrderByDescriptionAsc())
                .thenReturn(List.of(incidentMessage));

        List<IncidentMessageResponse> response =
                service.findAllIncidentMessages();

        assertNotNull(response);
        assertEquals(1, response.size());

        verify(incidentMessageRepository)
                .findAllByOrderByDescriptionAsc();
    }

    @Test
    @DisplayName("Debe reportar incidente correctamente")
    void shouldReportIncident() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(bathroomRepository.findById(1L))
                .thenReturn(Optional.of(bathroom));

        when(incidentMessageRepository.findAllById(List.of(1L)))
                .thenReturn(List.of(incidentMessage));

        when(incidentRepository.findByUserIdAndBathroomIdAndStatus(
                1L,
                1L,
                Incident.Status.PENDING))
                .thenReturn(List.of());

        when(incidentRepository.saveAll(anyList()))
                .thenAnswer(invocation -> {
                    List<Incident> incidents = invocation.getArgument(0);
                    incidents.get(0).setId(1L);
                    return incidents;
                });

        IncidentCreatedResponse response =
                service.report(1L, request);

        assertNotNull(response);
        assertEquals(1, response.incidentIds().size());
        assertEquals("PENDING", response.status());

        verify(incidentRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando usuario no existe")
    void shouldThrowWhenUserNotFound() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.report(1L, request));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando baño no existe")
    void shouldThrowWhenBathroomNotFound() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(bathroomRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.report(1L, request));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando incident message no existe")
    void shouldThrowWhenIncidentMessageNotFound() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(bathroomRepository.findById(1L))
                .thenReturn(Optional.of(bathroom));

        when(incidentMessageRepository.findAllById(List.of(1L)))
                .thenReturn(List.of());

        assertThrows(NotFoundException.class,
                () -> service.report(1L, request));
    }

    @Test
    @DisplayName("No debe crear incidentes duplicados pendientes")
    void shouldNotCreateDuplicatePendingIncidents() {

        Incident existingIncident = new Incident();
        existingIncident.setIncidentMessage(incidentMessage);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(bathroomRepository.findById(1L))
                .thenReturn(Optional.of(bathroom));

        when(incidentMessageRepository.findAllById(List.of(1L)))
                .thenReturn(List.of(incidentMessage));

        when(incidentRepository.findByUserIdAndBathroomIdAndStatus(
                1L,
                1L,
                Incident.Status.PENDING))
                .thenReturn(List.of(existingIncident));

        when(incidentRepository.saveAll(anyList()))
                .thenReturn(List.of());

        IncidentCreatedResponse response =
                service.report(1L, request);

        assertNotNull(response);
        assertTrue(response.incidentIds().isEmpty());

        verify(incidentRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Debe resolver incidentes por tipo y baño")
    void shouldResolveIncidentTypeInBathroom() {

        when(incidentRepository.updateStatusByBathroomAndIncidentMessage(
                eq(1L),
                eq(1L),
                eq(Incident.Status.RESOLVED),
                any(LocalDateTime.class)))
                .thenReturn(2);

        int updated =
                service.resolveIncidentTypeInBathroom(1L, 1L);

        assertEquals(2, updated);
    }

    @Test
    @DisplayName("Debe encontrar incidente por id")
    void shouldFindById() {

        when(incidentRepository.findById(1L))
                .thenReturn(Optional.of(incident));

        IncidentResponse response =
                service.findById(1L);

        assertNotNull(response);
        assertEquals(1L, response.id());

        verify(incidentRepository).findById(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando incidente no existe")
    void shouldThrowWhenIncidentNotFound() {

        when(incidentRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.findById(1L));
    }

    @Test
    @DisplayName("Debe retornar todos los incidentes")
    void shouldFindAll() {

        when(incidentRepository.findAllWithDetails(Incident.Status.PENDING))
                .thenReturn(List.of(incident));

        List<IncidentResponse> response =
                service.findAll(Incident.Status.PENDING);

        assertEquals(1, response.size());

        verify(incidentRepository)
                .findAllWithDetails(Incident.Status.PENDING);
    }

    @Test
    @DisplayName("Debe retornar incidentes por estado y categoría")
    void shouldFindAllByStatusAndCategory() {

        when(incidentRepository.findAllByStatusAndMessageCategory(
                Incident.Status.PENDING,
                IncidentMessage.Category.LIMPIEZA))
                .thenReturn(List.of(incident));

        List<IncidentResponse> response =
                service.findAllByStatusAndCategory(
                        Incident.Status.PENDING,
                        IncidentMessage.Category.LIMPIEZA);

        assertEquals(1, response.size());

        verify(incidentRepository)
                .findAllByStatusAndMessageCategory(
                        Incident.Status.PENDING,
                        IncidentMessage.Category.LIMPIEZA);
    }

    @Test
    @DisplayName("Debe retornar resumen de incidentes")
    void shouldFindByIncidentMessageIds() {

        IncidentSummary summary = mock(IncidentSummary.class);

        when(incidentRepository.findPendingIncidentSummaryByMessages(
                List.of(1L)))
                .thenReturn(List.of(summary));

        List<IncidentSummary> response =
                service.findByIncidentMessageIds(List.of(1L));

        assertEquals(1, response.size());

        verify(incidentRepository)
                .findPendingIncidentSummaryByMessages(List.of(1L));
    }

    @Test
    @DisplayName("Debe retornar incidentes por usuario")
    void shouldFindByUser() {

        when(incidentRepository.findByUserId(1L))
                .thenReturn(List.of(incident));

        List<IncidentResponse> response =
                service.findByUser(1L);

        assertEquals(1, response.size());

        verify(incidentRepository).findByUserId(1L);
    }

    @Test
    @DisplayName("Debe retornar incidentes por baño")
    void shouldFindByBathroom() {

        when(bathroomRepository.findById(1L))
                .thenReturn(Optional.of(bathroom));

        when(incidentRepository.findByBathroomIdAndPendingStatus(1L))
                .thenReturn(List.of(incident));

        List<IncidentResponse> response =
                service.findByBathroom(1L);

        assertEquals(1, response.size());

        verify(incidentRepository)
                .findByBathroomIdAndPendingStatus(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando baño no existe en findByBathroom")
    void shouldThrowWhenBathroomNotFoundInFindByBathroom() {

        when(bathroomRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.findByBathroom(1L));
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay incidentes")
    void shouldReturnEmptyListWhenNoIncidents() {

        when(incidentRepository.findAllWithDetails(Incident.Status.PENDING))
                .thenReturn(List.of());

        List<IncidentResponse> response =
                service.findAll(Incident.Status.PENDING);

        assertNotNull(response);
        assertTrue(response.isEmpty());
    }
}
