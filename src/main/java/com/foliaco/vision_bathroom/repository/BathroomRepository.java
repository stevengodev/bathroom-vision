package com.foliaco.vision_bathroom.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.foliaco.vision_bathroom.entity.Bathroom;
import com.foliaco.vision_bathroom.entity.Bathroom.BathroomStatus;

@Repository
public interface BathroomRepository extends JpaRepository<Bathroom, Long>, 
                                            JpaSpecificationExecutor<Bathroom>  {
 
    @Query("SELECT b FROM Bathroom b JOIN FETCH b.block WHERE b.block.id = :blockId")
    List<Bathroom> findAllByBlockId(Long blockId);

    @Query("SELECT b FROM Bathroom b JOIN FETCH b.block WHERE b.id = :id")
    Optional<Bathroom> findByIdWithBlock(@Param("id") Long id);

    @Query("SELECT b FROM Bathroom b JOIN FETCH b.block ORDER BY b.block.name")
    List<Bathroom> findAllWithBlock();

    List<Bathroom> findByStatus(BathroomStatus status);

}
