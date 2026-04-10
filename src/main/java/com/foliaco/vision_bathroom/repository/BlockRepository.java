package com.foliaco.vision_bathroom.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.foliaco.vision_bathroom.entity.Block;

public interface BlockRepository extends JpaRepository<Block, Long> {

    boolean existsByNameIgnoreCase(String name);

    Optional<Block> findByName(String name);
    
}
