package com.nexora.auth.repository;

import com.nexora.auth.entity.Role;
import com.nexora.auth.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role,String> {
    Optional<Role> findByName(RoleType name);
}
