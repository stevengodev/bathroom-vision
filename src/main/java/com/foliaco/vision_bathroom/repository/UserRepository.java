package com.foliaco.vision_bathroom.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.foliaco.vision_bathroom.entity.User;
import com.foliaco.vision_bathroom.entity.User.Role;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByRole(Role role);

    @Query("SELECT u FROM User u WHERE u.role IN :roles")
    List<User> findUsersByRoles(@Param("roles") List<Role> roles);

}
