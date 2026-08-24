package com.foliaco.vision_bathroom.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import com.foliaco.vision_bathroom.dto.BathroomFilter;
import com.foliaco.vision_bathroom.dto.BathroomRequest;
import com.foliaco.vision_bathroom.dto.BathroomResponse;
import com.foliaco.vision_bathroom.entity.Bathroom;
import com.foliaco.vision_bathroom.entity.Bathroom.BathroomStatus;
import com.foliaco.vision_bathroom.entity.Bathroom.Gender;
import com.foliaco.vision_bathroom.entity.Block;
import com.foliaco.vision_bathroom.exception.ConflictException;
import com.foliaco.vision_bathroom.exception.NotFoundException;
import com.foliaco.vision_bathroom.firebase.NotificationService;
import com.foliaco.vision_bathroom.repository.BathroomRepository;
import com.foliaco.vision_bathroom.repository.BlockRepository;
import com.foliaco.vision_bathroom.repository.IncidentRepository;
import com.foliaco.vision_bathroom.service.impl.BathroomServiceImpl;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class BathroomServiceTest {

    @Mock
    private BathroomRepository bathroomRepository;

    @Mock
    private BlockRepository blockRepository;

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private BathroomServiceImpl bathroomService;

    private Bathroom bathroom;
    private Block block;

    @BeforeEach
    void setUp() {

        block = Block.builder()
                .id(1L)
                .name("Bloque A")
                .numberOfFloors(2)
                .build();

        bathroom = new Bathroom();
        bathroom.setId(1L);
        bathroom.setBlock(block);
        bathroom.setFloor(1);
        bathroom.setGender(Gender.MASCULINO);
        bathroom.setStatus(BathroomStatus.DISPONIBLE);
    }

    @Test
    @DisplayName("Retorna lista de baños")
    void findAll_returnsBathroomList() {

        when(bathroomRepository.findAll()).thenReturn(List.of(bathroom));

        List<BathroomResponse> result = bathroomService.findAll();

        assertThat(result).hasSize(1);
        assertEquals(Gender.MASCULINO, result.get(0).gender());
        assertEquals("Bloque A", result.get(0).nameBlock());
        verify(bathroomRepository, atLeast(1)).findAll();
    }

    @Test
    @DisplayName("Retorna baño cuando existe por id")
    void findById_whenExists_returnsBathroom() {

        when(bathroomRepository.findById(anyLong()))
                .thenReturn(Optional.of(bathroom));

        BathroomResponse result = bathroomService.findById(1L);

        assertNotNull(result);
        assertEquals("Bloque A", result.nameBlock());
        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Retorna baños por id de bloque")
    void findByBlockId_returnsBathrooms() {

        when(bathroomRepository.findAllByBlockId(anyLong()))
                .thenReturn(List.of(bathroom));

        List<BathroomResponse> result = bathroomService.findByBlockId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).nameBlock())
                .isEqualTo("Bloque A");

        verify(bathroomRepository, atLeast(1))
                .findAllByBlockId(anyLong());
    }

    @Test
    @DisplayName("Lanza NotFoundException cuando no existe baño por id")
    void findById_whenNotFound_throwsNotFoundException() {

        when(bathroomRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bathroomService.findById(1L));

        verify(bathroomRepository, atLeast(1)).findById(anyLong());
    }

    @Test
    @DisplayName("Crea baño cuando bloque existe")
    void create_whenBlockExists_returnsBathroom() {

        BathroomRequest request = new BathroomRequest(Gender.MASCULINO, 1L, BathroomStatus.DISPONIBLE, 1);

        when(blockRepository.findById(anyLong())).thenReturn(Optional.of(block));
        when(bathroomRepository.save(any(Bathroom.class))).thenReturn(bathroom);

        BathroomResponse result = bathroomService.create(request);

        assertNotNull(result);
        assertEquals("Bloque A", result.nameBlock());
        verify(blockRepository, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("Lanza NotFoundException cuando bloque no existe al crear")
    void create_whenBlockNotFound_throwsException() {

        BathroomRequest request = new BathroomRequest(Gender.MASCULINO, 1L, BathroomStatus.DISPONIBLE, 1);

        when(blockRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bathroomService.create(request));

        verify(blockRepository, atLeast(1)).findById(anyLong());
    }

    @Test
    @DisplayName("Actualiza baño cuando existe")
    void update_whenExists_returnsUpdatedBathroom() {

        BathroomRequest request = new BathroomRequest(Gender.FEMENINO, 1L, BathroomStatus.EN_LIMPIEZA, 2);

        Bathroom updatedBathroom = new Bathroom();
        updatedBathroom.setBlock(block);
        updatedBathroom.setFloor(request.floor());
        updatedBathroom.setGender(request.gender());
        updatedBathroom.setStatus(request.status());

        when(bathroomRepository.findById(anyLong())).thenReturn(Optional.of(bathroom));
        when(blockRepository.findById(anyLong())).thenReturn(Optional.of(block));
        when(bathroomRepository.save(any(Bathroom.class))).thenReturn(updatedBathroom);

        BathroomResponse result = bathroomService.update(1L, request);

        assertNotNull(result);
        assertEquals(BathroomStatus.EN_LIMPIEZA, result.status());

        verify(bathroomRepository, atLeast(1)).findById(anyLong());
        verify(blockRepository, atLeast(1)).findById(anyLong());
    }

    @Test
    @DisplayName("Elimina baño cuando existe")
    void delete_whenExists_deletesBathroom() {

        when(bathroomRepository.findById(anyLong())).thenReturn(Optional.of(bathroom));
        doNothing().when(bathroomRepository).delete(bathroom);

        bathroomService.delete(1L);

        verify(bathroomRepository, times(1)).findById(anyLong());
        verify(bathroomRepository, times(1)).delete(any(Bathroom.class));
    }

    @Test
    @DisplayName("Lanza ConflictException cuando el baño tiene incidentes asociados")
    void delete_whenBathroomHasIncidents_throwsConflictException() {

        when(bathroomRepository.findById(anyLong())).thenReturn(Optional.of(bathroom));
        when(incidentRepository.existsByBathroomId(1L)).thenReturn(true);

        ConflictException exception = assertThrows(ConflictException.class, () -> bathroomService.delete(1L));

        assertThat(exception.getMessage()).isEqualTo("No se puede eliminar el baño porque tiene incidentes asociados");
        verify(bathroomRepository, never()).delete(any(Bathroom.class));
    }

    @Test
    @DisplayName("Lanza NotFoundException cuando elimina baño no existente")
    void delete_whenNotFound_throwsException() {

        when(bathroomRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bathroomService.delete(2L));

        verify(bathroomRepository, times(1)).findById(anyLong());
        verify(bathroomRepository, never()).delete(any(Bathroom.class));
    }

    @Test
    @DisplayName("Busca baños usando filtros")
    void searchBathrooms_returnsFilteredBathrooms() {

        BathroomFilter filter = new BathroomFilter(
                BathroomStatus.DISPONIBLE,
                Gender.MASCULINO,
                1L,
                "Bloque A");

        when(bathroomRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(bathroom));

        List<BathroomResponse> result = bathroomService.searchBathrooms(filter);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Gender.MASCULINO, result.get(0).gender());

        verify(bathroomRepository, times(1))
                .findAll(any(Specification.class));
    }

    @Test
    @DisplayName("Actualiza estado del baño y envía notificación")
    void updateStatus_whenStatusChanges_sendsNotification() {

        Bathroom updatedBathroom = new Bathroom();
        updatedBathroom.setId(1L);
        updatedBathroom.setBlock(block);
        updatedBathroom.setFloor(1);
        updatedBathroom.setGender(Gender.MASCULINO);
        updatedBathroom.setStatus(BathroomStatus.EN_LIMPIEZA);

        when(bathroomRepository.findById(anyLong()))
                .thenReturn(Optional.of(bathroom));

        when(bathroomRepository.save(any(Bathroom.class)))
                .thenReturn(updatedBathroom);

        BathroomResponse result = bathroomService.updateStatus(
                1L,
                BathroomStatus.EN_LIMPIEZA);

        assertNotNull(result);
        assertEquals(BathroomStatus.EN_LIMPIEZA, result.status());

        verify(notificationService, times(1))
                .notifyBathroomStatusChanged(any());
    }

    @Test
    @DisplayName("No envía notificación cuando el estado no cambia")
    void updateStatus_whenSameStatus_doesNotSendNotification() {

        bathroom.setStatus(BathroomStatus.DISPONIBLE);

        when(bathroomRepository.findById(anyLong()))
                .thenReturn(Optional.of(bathroom));

        BathroomResponse result = bathroomService.updateStatus(
                1L,
                BathroomStatus.DISPONIBLE);

        assertNotNull(result);
        assertEquals(BathroomStatus.DISPONIBLE, result.status());

        verify(bathroomRepository, never())
                .save(any(Bathroom.class));

        verify(notificationService, never())
                .notifyBathroomStatusChanged(any());
    }

    @Test
    @DisplayName("Lanza excepción cuando baño no existe en update")
    void update_whenBathroomNotFound_throwsException() {

        BathroomRequest request = new BathroomRequest(
                Gender.FEMENINO,
                1L,
                BathroomStatus.EN_LIMPIEZA,
                2);

        when(bathroomRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> bathroomService.update(1L, request));

        verify(bathroomRepository, never())
                .save(any(Bathroom.class));
    }

    @Test
    @DisplayName("Lanza excepción cuando bloque no existe en update")
    void update_whenBlockNotFound_throwsException() {

        BathroomRequest request = new BathroomRequest(
                Gender.FEMENINO,
                1L,
                BathroomStatus.EN_LIMPIEZA,
                2);

        when(bathroomRepository.findById(anyLong()))
                .thenReturn(Optional.of(bathroom));

        when(blockRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> bathroomService.update(1L, request));

        verify(bathroomRepository, never())
                .save(any(Bathroom.class));
    }

    @Test
    @DisplayName("Envía notificación cuando update cambia estado")
    void update_whenStatusChanges_sendsNotification() {

        BathroomRequest request = new BathroomRequest(
                Gender.MASCULINO,
                1L,
                BathroomStatus.EN_LIMPIEZA,
                1);

        Bathroom updatedBathroom = new Bathroom();
        updatedBathroom.setId(1L);
        updatedBathroom.setBlock(block);
        updatedBathroom.setFloor(1);
        updatedBathroom.setGender(Gender.MASCULINO);
        updatedBathroom.setStatus(BathroomStatus.EN_LIMPIEZA);

        when(bathroomRepository.findById(anyLong()))
                .thenReturn(Optional.of(bathroom));

        when(blockRepository.findById(anyLong()))
                .thenReturn(Optional.of(block));

        when(bathroomRepository.save(any(Bathroom.class)))
                .thenReturn(updatedBathroom);

        bathroomService.update(1L, request);

        verify(notificationService, times(1))
                .notifyBathroomStatusChanged(any());
    }

    @Test
    @DisplayName("No envía notificación cuando update no cambia estado")
    void update_whenStatusDoesNotChange_doesNotSendNotification() {

        BathroomRequest request = new BathroomRequest(
                Gender.MASCULINO,
                1L,
                BathroomStatus.DISPONIBLE,
                1);

        when(bathroomRepository.findById(anyLong()))
                .thenReturn(Optional.of(bathroom));

        when(blockRepository.findById(anyLong()))
                .thenReturn(Optional.of(block));

        when(bathroomRepository.save(any(Bathroom.class)))
                .thenReturn(bathroom);

        bathroomService.update(1L, request);

        verify(notificationService, never())
                .notifyBathroomStatusChanged(any());
    }

    @Test
    @DisplayName("Lanza excepción cuando baño no existe en updateStatus")
    void updateStatus_whenBathroomNotFound_throwsException() {

        when(bathroomRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> bathroomService.updateStatus(
                        1L,
                        BathroomStatus.EN_LIMPIEZA));

        verify(notificationService, never())
                .notifyBathroomStatusChanged(any());
    }

}
