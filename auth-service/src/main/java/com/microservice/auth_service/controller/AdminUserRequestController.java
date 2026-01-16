package com.microservice.auth_service.controller;

import com.microservice.auth_service.model.UserRequest;
import com.microservice.auth_service.model.Users;
import com.microservice.auth_service.model.acces;
import com.microservice.auth_service.model.user_role;
import com.microservice.auth_service.repository.UserRequestRepository;
import com.microservice.auth_service.repository.UserRepository;
import com.microservice.auth_service.repository.userRoleRepo;
import com.microservice.auth_service.security.response.MessageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/internal/requests")
// @PreAuthorize("hasRole('ADMIN')") // Internal, maybe secured by network or
// shared secret in real life
public class AdminUserRequestController {

    @Autowired
    UserRequestRepository userRequestRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    userRoleRepo roleRepository;

    @GetMapping
    public List<UserRequest> getAllRequests() {
        return userRequestRepository.findAll();
    }

    @GetMapping("/users")
    public List<Users> getAllUsers() {
        return userRepository.findAll();
    }

    @DeleteMapping("/users/{username}")
    public ResponseEntity<?> deleteUser(@PathVariable String username) {
        if (!userRepository.existsByusername(username)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: User not found!"));
        }
        Users user = userRepository.findByusername(username);
        userRepository.delete(user);
        return ResponseEntity.ok(new MessageResponse("User deleted successfully!"));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approveRequest(@PathVariable Long id) {
        UserRequest request = userRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Request not found."));

        if (userRepository.existsByusername(request.getUsername())) {
            userRequestRepository.delete(request); // Clean up if already exists
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Username already exists!"));
        }

        // Create User from Request
        Users user = new Users(request.getUsername(), request.getEmail(), request.getPassword());
        Set<user_role> roles = new HashSet<>();
        user_role userRole = roleRepository.findByUserRole(acces.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        roles.add(userRole);
        user.setRoles(roles);

        userRepository.save(user);
        userRequestRepository.delete(request);

        return ResponseEntity.ok(new MessageResponse("User approved and registered successfully!"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRequest(@PathVariable Long id) {
        userRequestRepository.deleteById(id);
        return ResponseEntity.ok(new MessageResponse("Request deleted successfully!"));
    }
}
