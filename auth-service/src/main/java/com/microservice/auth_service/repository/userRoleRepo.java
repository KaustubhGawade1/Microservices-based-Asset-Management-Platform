package com.microservice.auth_service.repository;

import com.microservice.auth_service.model.acces;
import com.microservice.auth_service.model.user_role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface userRoleRepo extends JpaRepository<user_role,Long> {



    Optional<user_role> findByUserRole(acces userRole);
}
