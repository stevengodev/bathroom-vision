package com.foliaco.vision_bathroom.service;

import java.util.List;

import com.foliaco.vision_bathroom.dto.BathroomFilter;
import com.foliaco.vision_bathroom.dto.BathroomRequest;
import com.foliaco.vision_bathroom.dto.BathroomResponse;
import com.foliaco.vision_bathroom.entity.Bathroom.BathroomStatus;

/**
 * HU-001: Listar baños disponibles (usuarios)
 * HU-016: Crear baños
 * HU-017: Visualizar baños registrados
 * HU-018: Editar información de baños
 * HU-019: Eliminar baños
 */
public interface BathroomService {
    
    List<BathroomResponse> findAll();

    BathroomResponse findById(Long id);
    
    List<BathroomResponse> findByBlockId(Long blockId);
    
    List<BathroomResponse> searchBathrooms(BathroomFilter filter);

    BathroomResponse create(BathroomRequest request);
    
    BathroomResponse update(Long id, BathroomRequest bathroom);
    
    BathroomResponse updateStatus(Long id, BathroomStatus status);
    
    void delete(Long id);



}
