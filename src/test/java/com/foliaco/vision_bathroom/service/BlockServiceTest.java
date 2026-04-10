package com.foliaco.vision_bathroom.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
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

import com.foliaco.vision_bathroom.dto.BlockRequest;
import com.foliaco.vision_bathroom.dto.BlockResponse;
import com.foliaco.vision_bathroom.entity.Block;
import com.foliaco.vision_bathroom.exception.NotFoundException;
import com.foliaco.vision_bathroom.repository.BlockRepository;
import com.foliaco.vision_bathroom.service.impl.BlockServiceImpl;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class BlockServiceTest {
    
    @Mock
    private BlockRepository blockRepository;

    @InjectMocks
    private BlockServiceImpl blockService;

    private Block block;

    @BeforeEach
    void setUp() {
        block = Block.builder()
                .id(1L)
                .name("Bloque A")
                .numberOfFloors(2)
                .build();
    }

    @Test
    @DisplayName("Retorna lista de bloques")
    void findAll_returnsBlockList() {

        when( blockRepository.findAll() ).thenReturn(List.of(block));

        List<BlockResponse> result = blockService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Bloque A");
        verify(blockRepository, atLeast(1)).findAll();
    }


    @Test
    @DisplayName("Retorna bloque cuando existe por id")
    void findById_whenExists_returnsBlock(){

        when( blockRepository.findById(anyLong()) ).thenReturn((Optional.of(block)));

        BlockResponse result = blockService.findById(anyLong());
        assertNotNull(result);
        assertEquals("Bloque A", result.name());
        assertThat(result.id()).isEqualTo(1L);

    }

    @Test
    @DisplayName("Lanza NotFoundException cuando no existe por id")
    void findById_whenNotFound_throwsNotFoundException() {
        when(blockRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> blockService.findById(anyLong()));

        assertThatThrownBy(() -> blockService.findById(anyLong()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("Retorna bloque cuando existe por nombre")
    void findByName_whenExists_returnsBlock(){

        when( blockRepository.findByName(anyString()) ).thenReturn((Optional.of(block)));

        BlockResponse result = blockService.findByName(anyString());
        assertNotNull(result);
        assertEquals("Bloque A", result.name());
        assertThat(result.id()).isEqualTo(1L);

    }

    @Test
    @DisplayName("Lanza NotFoundException cuando no existe por nombre")
    void findByName_whenNotFound_throwsNotFoundException() {
        when(blockRepository.findByName(anyString())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> blockService.findByName(anyString()));

        assertThatThrownBy(() -> blockService.findByName(anyString()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("Crea un bloque")
    void create_savesBlock() {

        when(blockRepository.save(any(Block.class))).thenReturn(block);

        BlockResponse result = blockService.create(new BlockRequest("Bloque A", 2));

        assertNotNull(result);
        assertThat(result.name()).isEqualTo("Bloque A");
        verify(blockRepository, atLeast(1)).save(any(Block.class));
    }

    @Test
    @DisplayName("Actualiza un bloque cuando existe")
    void update_whenExists_returnsUpdatedBlock() {

        when(blockRepository.findById(anyLong())).thenReturn(Optional.of(block));
        when(blockRepository.save(any(Block.class))).thenReturn(block);

        BlockResponse result = blockService.update(1L, new BlockRequest("Bloque A", 2));

        assertNotNull(result);
        assertThat(result.name()).isEqualTo("Bloque A");
        verify(blockRepository, atLeast(1)).save(any(Block.class));
        verify(blockRepository, atLeast(1)).findById(anyLong());

    }

    @Test
    @DisplayName("Lanza NotFoundException cuando actualiza")
    void update_whenNotFound_throwsNotFoundException() {

        when(blockRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            blockService.update(2L, new BlockRequest("Bloque A", 2));
        });


        assertEquals("Bloque no encontrado con id: 2", exception.getMessage());
        verify(blockRepository, atLeast(1)).findById(anyLong());
        verify(blockRepository, never()).save(any(Block.class));


    }


    @Test
    @DisplayName("Elimina un bloque cuando existe")
    void delete_whenExists_deletesBlock() {
        
        when(blockRepository.findById(anyLong())).thenReturn(Optional.of(block));
        doNothing().when(blockRepository).delete(block);

        blockService.delete(block.getId());

        verify(blockRepository, times(1)).findById(anyLong());
        verify(blockRepository, times(1)).delete(any(Block.class));
    }


    
    @Test
    @DisplayName("Lanza NotFoundException cuando elimina")
    void delete_whenNotFound_throwsNotFoundException() {
        
        when(blockRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> blockService.delete(2L));

        verify(blockRepository, times(1)).findById(anyLong());
        verify(blockRepository, never()).delete(any(Block.class));
    }


}
