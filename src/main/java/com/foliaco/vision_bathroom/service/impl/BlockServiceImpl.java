package com.foliaco.vision_bathroom.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.foliaco.vision_bathroom.dto.BlockRequest;
import com.foliaco.vision_bathroom.dto.BlockResponse;
import com.foliaco.vision_bathroom.entity.Block;
import com.foliaco.vision_bathroom.exception.NotFoundException;
import com.foliaco.vision_bathroom.repository.BlockRepository;
import com.foliaco.vision_bathroom.service.BlockService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlockServiceImpl implements BlockService {

    private final BlockRepository blockRepository;

    @Override
    public List<BlockResponse> findAll() {
        log.info("Find all blocks");
        return blockRepository.findAll()
            .stream().map( block -> toBlockResponse(block) )
            .collect(Collectors.toList());
    }

    @Override
    public BlockResponse findById(Long id) {

        log.info("Find block by id: {}", id);

        Block block = blockRepository.findById(id).orElseThrow(
            () -> new NotFoundException("block not found with id: " + id )
        );

        return toBlockResponse(block);

    }

    @Override
    public BlockResponse findByName(String name) {
    
        log.info("Find block by name: {}", name);

        Block block = blockRepository.findByName(name).orElseThrow(
            () -> new NotFoundException("block not found with name: " + name )
        );

        return toBlockResponse(block);

    }

    @Override
    public BlockResponse create(BlockRequest request) {
        
        log.info("Creating block {}", request.name());

        Block block = Block.builder()
                .name(request.name().trim())
                .numberOfFloors(request.numberOfFloors())
                .build();

        return toBlockResponse( blockRepository.save(block) );

    }

    @Override
    public BlockResponse update(Long id, BlockRequest request) {

        log.info("Updating block {}", request.name());

        Block block= blockRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Bloque no encontrado con id: " + id));

        block.setName(request.name().trim());
        block.setNumberOfFloors(request.numberOfFloors());

        return toBlockResponse( blockRepository.save(block) );

    }

    @Override
    public void delete(Long id) {
        Block block= blockRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Bloque no encontrado con id: " + id));

        blockRepository.delete(block);
    }

    private BlockResponse toBlockResponse(Block block){

        int numberOfBathrooms = 0;

        if (block.getBathrooms() != null) {
            numberOfBathrooms = block.getBathrooms().size();
        }

        return new BlockResponse(block.getId(), block.getName(), block.getNumberOfFloors(), numberOfBathrooms);
    }
    
}
