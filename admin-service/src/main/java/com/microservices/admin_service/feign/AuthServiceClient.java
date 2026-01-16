package com.microservices.admin_service.feign;

import com.microservices.admin_service.payload.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;

// Point to service name registered in Eureka OR to gateway route if you prefer gateway
@FeignClient(name = "auth-service", url = "${auth.service.url}", configuration = com.microservices.admin_service.config.FeignConfig.class)
public interface AuthServiceClient {

    // You need an endpoint like: GET /api/auth/users/{username}/roles
    // Return at least ["ROLE_ADMIN", "ROLE_EDITOR", ...]
    @GetMapping("/api/auth/users/{username}/roles")
    UserDto getUserRoles(@PathVariable("username") String username);

    @GetMapping("/api/internal/requests")
    java.util.List<com.microservices.admin_service.payload.UserRequestDto> getRequests();

    @PostMapping("/api/internal/requests/{id}/approve")
    void approveRequest(@PathVariable("id") Long id);

    @DeleteMapping("/api/internal/requests/{id}")
    void deleteRequest(@PathVariable("id") Long id);

    @DeleteMapping("/api/internal/requests/users/{username}")
    void deleteUser(@PathVariable("username") String username);

    @GetMapping("/api/internal/requests/users")
    java.util.List<com.microservices.admin_service.payload.UserDto> getAllUsers();
}
