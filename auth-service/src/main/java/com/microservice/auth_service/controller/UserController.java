package com.microservice.auth_service.controller;

import com.microservice.auth_service.Payload.UsersDto;
import com.microservice.auth_service.model.Users;
import com.microservice.auth_service.repository.UserRepository;
import com.microservice.auth_service.security.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class UserController {
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/{username}")
    public UsersDto getUserByUsername(@PathVariable String username) {
        Users user = (Users) userDetailsService.loadUserByUsername(username);
        return new UsersDto(user.getUser_id(), user.getUsername(), user.getEmail(), user.getRoles());
    }

    @GetMapping("/me")
    public UsersDto getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        Users user = userRepository.findByusername(username);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        return new UsersDto(user.getUser_id(), user.getUsername(), user.getEmail(), user.getRoles());
    }

}
