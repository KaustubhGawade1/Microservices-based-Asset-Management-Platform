package com.microservices.admin_service.controller;

import com.microservices.admin_service.payload.RoleUpdateRequest;
import com.microservices.admin_service.payload.UserDto;
import com.microservices.admin_service.service.AdminUserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService service;

    public AdminUserController(AdminUserService service) {
        this.service = service;
    }

    @GetMapping("/{username}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public UserDto roles(@PathVariable String username) {
        return service.getUserRoles(username);
    }

    @PutMapping("/{username}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public UserDto updateRoles(@PathVariable String username, @RequestBody RoleUpdateRequest req) {
        return service.updateUserRoles(username, req);
    }

    @GetMapping("/requests")
    @PreAuthorize("hasRole('ADMIN')")
    public java.util.List<com.microservices.admin_service.payload.UserRequestDto> getRequests() {
        return service.getRequests();
    }

    @PostMapping("/{username}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public void approveUser(@PathVariable String username) {
        service.approveRequest(username);
    }

    @DeleteMapping("/requests/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteRequest(@PathVariable String id) { // Frontend sends ID as string
        service.deleteRequest(id);
    }

    @DeleteMapping("/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(@PathVariable String username) {
        service.deleteUser(username);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public java.util.List<com.microservices.admin_service.payload.UserDto> getAllUsers() {
        return service.getAllUsers();
    }
}
