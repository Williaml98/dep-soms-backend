package com.dep.soms.repository;

import com.dep.soms.dto.user.SetPasswordRequest;
import com.dep.soms.model.Role;
import com.dep.soms.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
    Optional<User> findByUsernameAndEmail(String username, String email);
    //List<User> findByRoles_Name(Role.ERole roleName);
    // Use a custom query to handle the enum properly
    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r.name = :roleName AND u.active = true")
    List<User> findActiveUsersByRoleName(@Param("roleName") Role.ERole roleName);

    // Or if you want all users (active and inactive)
    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findByRoles_Name(@Param("roleName") Role.ERole roleName);




}
