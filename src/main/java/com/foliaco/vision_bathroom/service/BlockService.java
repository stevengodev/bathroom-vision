package com.foliaco.vision_bathroom.service;

import java.util.List;

import com.foliaco.vision_bathroom.dto.BlockRequest;
import com.foliaco.vision_bathroom.dto.BlockResponse;

/**
 * HU-012: Crear bloque
 * HU-013: Visualizar bloques
 * HU-014: Editar bloque
 * HU-015: Eliminar bloque
 */
public interface BlockService {

    List<BlockResponse> findAll();

    BlockResponse findById(Long id);

    BlockResponse findByName(String name);

    BlockResponse create(BlockRequest request);

    BlockResponse update(Long id, BlockRequest request);

    void delete(Long id);
    
}
