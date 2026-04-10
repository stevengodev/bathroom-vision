package com.foliaco.vision_bathroom.repository;

import org.springframework.data.jpa.domain.Specification;

import com.foliaco.vision_bathroom.entity.Bathroom;
import com.foliaco.vision_bathroom.entity.Bathroom.BathroomStatus;
import com.foliaco.vision_bathroom.entity.Bathroom.Gender;

public class BathroomSpecification {
    
    public static Specification<Bathroom> hasStatus(BathroomStatus status) {
        return (root, query, cb) -> {
            if (status == null) return null;
            return cb.equal(root.get("status"), status);
        };
    }

    public static Specification<Bathroom> hasGender(Gender gender) {
        return (root, query, cb) -> {
            if (gender == null) return null;
            return cb.equal(root.get("gender"), gender);
        };
    }

    public static Specification<Bathroom> hasBlockId(Long blockId) {
        return (root, query, cb) -> {
            if (blockId == null) return null;
            return cb.equal(root.get("block").get("id"), blockId);
        };
    }

    public static Specification<Bathroom> searchText(String queryText) {
        return (root, query, cb) -> {
            if (queryText == null || queryText.isBlank()) return null;

            String like = "%" + queryText.toLowerCase() + "%";

            return cb.like(cb.lower(root.get("block").get("name")), like);

        };
    }

}
