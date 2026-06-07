package com.foliaco.vision_bathroom.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.foliaco.vision_bathroom.dto.MaintenanceRequest;
import com.foliaco.vision_bathroom.dto.MaintenanceResponse;
import com.foliaco.vision_bathroom.entity.Bathroom;
import com.foliaco.vision_bathroom.entity.Block;
import com.foliaco.vision_bathroom.entity.Maintenance;
import com.foliaco.vision_bathroom.exception.ConflictException;
import com.foliaco.vision_bathroom.exception.NotFoundException;
import com.foliaco.vision_bathroom.repository.BathroomRepository;
import com.foliaco.vision_bathroom.repository.MaintenanceRepository;
import com.foliaco.vision_bathroom.service.impl.MaintenanceServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MaintenanceServiceTest {

    @Mock
    private MaintenanceRepository maintenanceRepository;

    @Mock
    private BathroomRepository bathroomRepository;

    @InjectMocks
    private MaintenanceServiceImpl service;

    private Bathroom bathroom;
    private Maintenance maintenance;
    private MaintenanceRequest request;

    @BeforeEach
    void setUp() {

        Block block = new Block();
        block.setId(1L);
        block.setName("Bloque A");

        bathroom = new Bathroom();
        bathroom.setId(1L);
        bathroom.setBlock(block);
        bathroom.setFloor(2);

        maintenance = new Maintenance();
        maintenance.setId(1L);
        maintenance.setBathroom(bathroom);
        maintenance.setTechnicianFullName("Carlos Perez");
        maintenance.setDescription("Fuga de agua");
        maintenance.setStatus(Maintenance.Status.ABIERTO);

        request = new MaintenanceRequest(
                1L,
                "Carlos Perez",
                LocalDateTime.now().plusDays(1),
                "Fuga de agua"
                
        );
    }

    @Test
    @DisplayName("Debe retornar todos los mantenimientos")
    void shouldFindAll() {

        when(maintenanceRepository.findAll())
                .thenReturn(List.of(maintenance));

        List<MaintenanceResponse> response = service.findAll();

        assertNotNull(response);
        assertEquals(1, response.size());

        verify(maintenanceRepository).findAll();
    }

    @Test
    @DisplayName("Debe retornar mantenimientos por estado")
    void shouldFindAllByStatus() {

        when(maintenanceRepository.findAllByStatus(Maintenance.Status.ABIERTO))
                .thenReturn(List.of(maintenance));

        List<MaintenanceResponse> response =
                service.findAllByStatus(Maintenance.Status.ABIERTO);

        assertEquals(1, response.size());

        verify(maintenanceRepository)
                .findAllByStatus(Maintenance.Status.ABIERTO);
    }

    @Test
    @DisplayName("Debe encontrar mantenimiento por id")
    void shouldFindById() {

        when(maintenanceRepository.findById(1L))
                .thenReturn(Optional.of(maintenance));

        MaintenanceResponse response = service.findById(1L);

        assertNotNull(response);
        assertEquals(1L, response.id());

        verify(maintenanceRepository).findById(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando mantenimiento no existe")
    void shouldThrowWhenMaintenanceNotFound() {

        when(maintenanceRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.findById(1L));
    }

    @Test
    @DisplayName("Debe encontrar mantenimientos por baño")
    void shouldFindByBathroom() {

        when(bathroomRepository.findById(1L))
                .thenReturn(Optional.of(bathroom));

        when(maintenanceRepository.findByBathroomId(1L))
                .thenReturn(List.of(maintenance));

        List<MaintenanceResponse> response =
                service.findByBathroom(1L);

        assertEquals(1, response.size());

        verify(bathroomRepository).findById(1L);
        verify(maintenanceRepository).findByBathroomId(1L);
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
    @DisplayName("Debe retornar lista vacía en findByCurrentUser")
    void shouldReturnEmptyListInFindByCurrentUser() {

        List<MaintenanceResponse> response =
                service.findByCurrentUser(1L);

        assertNotNull(response);
        assertTrue(response.isEmpty());
    }

    @Test
    @DisplayName("Debe crear mantenimiento correctamente")
    void shouldCreateMaintenance() {

        when(bathroomRepository.findById(1L))
                .thenReturn(Optional.of(bathroom));

        when(maintenanceRepository.save(any(Maintenance.class)))
                .thenReturn(maintenance);

        MaintenanceResponse response = service.create(request);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals(Maintenance.Status.ABIERTO, response.status());

        verify(maintenanceRepository).save(any(Maintenance.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando baño no existe en create")
    void shouldThrowWhenBathroomNotFoundInCreate() {

        when(bathroomRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.create(request));
    }

    @Test
    @DisplayName("Debe actualizar mantenimiento correctamente")
    void shouldUpdateMaintenance() {

        when(maintenanceRepository.findById(1L))
                .thenReturn(Optional.of(maintenance));

        when(bathroomRepository.findById(1L))
                .thenReturn(Optional.of(bathroom));

        when(maintenanceRepository.save(any(Maintenance.class)))
                .thenReturn(maintenance);

        MaintenanceResponse response =
                service.update(1L, request);

        assertNotNull(response);

        verify(maintenanceRepository).save(any(Maintenance.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando mantenimiento no existe en update")
    void shouldThrowWhenMaintenanceNotFoundInUpdate() {

        when(maintenanceRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.update(1L, request));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando mantenimiento está cerrado")
    void shouldThrowWhenMaintenanceClosed() {

        maintenance.setStatus(Maintenance.Status.CERRADO);

        when(maintenanceRepository.findById(1L))
                .thenReturn(Optional.of(maintenance));

        assertThrows(ConflictException.class,
                () -> service.update(1L, request));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando baño no existe en update")
    void shouldThrowWhenBathroomNotFoundInUpdate() {

        when(maintenanceRepository.findById(1L))
                .thenReturn(Optional.of(maintenance));

        when(bathroomRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.update(1L, request));
    }

    @Test
    @DisplayName("Debe actualizar estado correctamente")
    void shouldUpdateStatus() {

        when(maintenanceRepository.findById(1L))
                .thenReturn(Optional.of(maintenance));

        when(maintenanceRepository.save(any(Maintenance.class)))
                .thenReturn(maintenance);

        MaintenanceResponse response =
                service.updateStatus(1L, Maintenance.Status.CERRADO);

        assertNotNull(response);
        assertEquals(Maintenance.Status.CERRADO, response.status());

        verify(maintenanceRepository).save(any(Maintenance.class));
    }

    @Test
    @DisplayName("Debe asignar resolvedAt cuando estado es CERRADO")
    void shouldSetResolvedAtWhenClosed() {

        when(maintenanceRepository.findById(1L))
                .thenReturn(Optional.of(maintenance));

        when(maintenanceRepository.save(any(Maintenance.class)))
                .thenReturn(maintenance);

        MaintenanceResponse response =
                service.updateStatus(1L, Maintenance.Status.CERRADO);

        assertNotNull(response);
        assertEquals(Maintenance.Status.CERRADO, response.status());
        assertNotNull(response.resolvedAt());

        verify(maintenanceRepository).save(any(Maintenance.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando mantenimiento no existe en updateStatus")
    void shouldThrowWhenMaintenanceNotFoundInUpdateStatus() {

        when(maintenanceRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.updateStatus(1L, Maintenance.Status.CERRADO));
    }

    @Test
    @DisplayName("Debe eliminar mantenimiento correctamente")
    void shouldDeleteMaintenance() {

        when(maintenanceRepository.findById(1L))
                .thenReturn(Optional.of(maintenance));

        service.delete(1L);

        verify(maintenanceRepository).delete(maintenance);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando mantenimiento no existe en delete")
    void shouldThrowWhenMaintenanceNotFoundInDelete() {

        when(maintenanceRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.delete(1L));
    }

    @Test
    @DisplayName("Debe hacer trim en la descripción al actualizar")
    void shouldTrimDescriptionOnUpdate() {

        MaintenanceRequest requestWithSpaces =
                new MaintenanceRequest(
                        1L,
                        "Carlos Perez",
                        LocalDateTime.now().plusDays(1),
                        "   Nueva descripción   "
                );

        when(maintenanceRepository.findById(1L))
                .thenReturn(Optional.of(maintenance));

        when(bathroomRepository.findById(1L))
                .thenReturn(Optional.of(bathroom));

        when(maintenanceRepository.save(any(Maintenance.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.update(1L, requestWithSpaces);

        assertEquals("Nueva descripción",
                maintenance.getDescription());
    }
}
