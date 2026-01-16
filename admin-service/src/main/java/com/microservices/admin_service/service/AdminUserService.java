package com.microservices.admin_service.service;

import com.microservices.admin_service.payload.RoleUpdateRequest;
import com.microservices.admin_service.payload.UserDto;
import com.microservices.admin_service.entity.AdminAuditLog;
import com.microservices.admin_service.feign.AuthServiceClient;
import com.microservices.admin_service.repository.AdminAuditLogRepository;
import java.util.List;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AdminUserService {

    private final AuthServiceClient authClient;
    private final AdminAuditLogRepository auditRepo;

    public AdminUserService(AuthServiceClient authClient, AdminAuditLogRepository auditRepo) {
        this.authClient = authClient;
        this.auditRepo = auditRepo;
    }

    public UserDto getUserRoles(String username) {
        return authClient.getUserRoles(username);
    }

    // Example: Update roles via auth-service endpoint you expose there
    public UserDto updateUserRoles(String username, RoleUpdateRequest req) {
        // You must implement an endpoint in auth-service to update roles
        // e.g., PUT /api/auth/users/{username}/roles
        // For now we just log the intent:
        String admin = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        auditRepo.save(AdminAuditLog.of(admin, "UPDATE_ROLES", "user:" + username, String.valueOf(req.getRoles())));
        // return authClient.updateUserRoles(username, req); // uncomment when available
        return new UserDto(null, username, req.getRoles());
    }

    public java.util.List<com.microservices.admin_service.payload.UserRequestDto> getRequests() {
        return authClient.getRequests();
    }

    public void approveRequest(String username) {
        // Find request by username logic or just pass username if API supported it.
        // But our Internal API uses ID. The frontend sends username.
        // We need lookup or change API.
        // Let's assume we can lookup by username in frontend or here?
        // Actually, frontend `handleApproveUser` takes `UserRequest` which has `id` and
        // `username`.
        // Frontend calls: `/users/${req.username}/approve`.
        // So Controller gets username. We need to find ID from username?
        // Or we should better change frontend to use ID?
        // Modifying frontend is expensive.
        // Let's Fetch all requests, find the one with username, and then approve by ID.
        // This is inefficient but works without changing frontend.
        List<com.microservices.admin_service.payload.UserRequestDto> requests = authClient.getRequests();
        Long id = requests.stream()
                .filter(r -> r.getUsername().equals(username))
                .findFirst()
                .map(com.microservices.admin_service.payload.UserRequestDto::getId)
                .orElseThrow(() -> new RuntimeException("Request not found for username: " + username));

        authClient.approveRequest(id);
        auditRepo.save(AdminAuditLog.of("admin", "APPROVE_USER", "user:" + username, "Approved registration"));
    }

    public void deleteRequest(String id) {
        authClient.deleteRequest(Long.parseLong(id));
        auditRepo.save(AdminAuditLog.of("admin", "REJECT_REQUEST", "request:" + id, "Rejected registration"));
    }

    public void deleteUser(String username) {
        authClient.deleteUser(username);
        auditRepo.save(AdminAuditLog.of("admin", "DELETE_USER", "user:" + username, "Deleted user account"));
    }

    public java.util.List<com.microservices.admin_service.payload.UserDto> getAllUsers() {
        return authClient.getAllUsers();
    }
}
