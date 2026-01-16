package com.microservice.auth_service.controller;

import com.microservice.auth_service.model.Users;
import com.microservice.auth_service.model.acces;
import com.microservice.auth_service.model.user_role;
import com.microservice.auth_service.repository.UserRepository;
import com.microservice.auth_service.repository.userRoleRepo;
import com.microservice.auth_service.security.jwt.JwtUtils;
import com.microservice.auth_service.security.request.Loginrequest;
import com.microservice.auth_service.security.request.SignupRequest;
import com.microservice.auth_service.security.response.MessageResponse;
import com.microservice.auth_service.security.services.RefreshTokenService;
import com.microservice.auth_service.model.RefreshToken;
import com.microservice.auth_service.security.response.userinforesponse;
import com.microservice.auth_service.security.services.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    userRoleRepo roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    RefreshTokenService refreshTokenService;

    @Autowired
    com.microservice.auth_service.repository.UserRequestRepository userRequestRepository;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody Loginrequest loginRequest) {
        System.out.println("DEBUG: Entering authenticateUser for " + loginRequest.getUsername());
        Authentication authentication;
        try {
            System.out.println("DEBUG: Attempting authenticationManager.authenticate");
            authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(),
                            loginRequest.getPassword()));
        } catch (AuthenticationException exception) {
            Map<String, Object> map = new HashMap<>();
            map.put("message", "Bad credentials");
            map.put("status", false);
            return new ResponseEntity<Object>(map, HttpStatus.NOT_FOUND);
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

        userinforesponse response = new userinforesponse(userDetails.getId(),
                userDetails.getUsername(), roles, jwtCookie.toString());

        // Note: Ideally response should include refreshToken, but userinforesponse
        // might need update.
        // For now we will return it in a separate field if needed or assume client gets
        // it from side-channel.
        // But the requirement says return it. Let's send a map for now or update
        // userinforesponse.
        // Actually, let's keep userinforesponse as is and maybe add it there?
        // The original code uses userinforesponse. Let's see if we can modify it or
        // just return a map.
        // Waiting, the plan said "Update /signin: Create and return Refresh Token".
        // Let's modify the response body to be a Map or a new DTO if userinforesponse
        // doesn't support it.
        // Or better, let's just properly map it.

        // Actually, to avoid breaking userinforesponse immediately, let's return a Map
        // which is flexible
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("id", userDetails.getId());
        responseMap.put("username", userDetails.getUsername());
        responseMap.put("roles", roles);
        responseMap.put("accessToken", jwtCookie.getValue()); // usually sent as cookie but also good to have in body
        responseMap.put("refreshToken", refreshToken.getToken());

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(responseMap);
    }

    @PostMapping("/refreshtoken")
    public ResponseEntity<?> refreshtoken(
            @Valid @RequestBody com.microservice.auth_service.security.request.TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtUtils.generateTokenFromUsername(user.getUsername());
                    return ResponseEntity.ok(new com.microservice.auth_service.security.response.TokenRefreshResponse(
                            token, requestRefreshToken));
                })
                .orElseThrow(() -> new RuntimeException("Refresh token is not in database!"));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByusername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
        }

        // Create new user's account
        Users user = new Users(signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()));

        Set<String> strRoles = signUpRequest.getRole();
        Set<user_role> roles = new HashSet<>();

        if (strRoles == null) {
            user_role userRole = roleRepository.findByUserRole(acces.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        user_role adminRole = roleRepository.findByUserRole(acces.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(adminRole);

                        break;
                    case "editor":
                        user_role modRole = roleRepository.findByUserRole(acces.ROLE_EDITOR)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(modRole);

                        break;
                    default:
                        user_role userRole = roleRepository.findByUserRole(acces.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(userRole);
                }
            });
        }

        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    @GetMapping("/username")
    public String currentUserName(Authentication authentication) {
        if (authentication != null)
            return authentication.getName();
        else
            return "";
    }

    @GetMapping("/user")
    public ResponseEntity<?> getUserDetails(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        userinforesponse response = new userinforesponse(userDetails.getId(),
                userDetails.getUsername(), roles);

        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/signout")
    public ResponseEntity<?> signoutUser() {
        ResponseCookie cookie = jwtUtils.getCleanJwtCookie();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,
                cookie.toString())
                .body(new MessageResponse("You've been signed out!"));
    }

    @PostMapping("/signup-request")
    public ResponseEntity<?> registerUserRequest(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByusername(signUpRequest.getUsername())
                || userRequestRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())
                || userRequestRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
        }

        // Create new user request
        com.microservice.auth_service.model.UserRequest userRequest = new com.microservice.auth_service.model.UserRequest(
                signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()));

        userRequestRepository.save(userRequest);

        return ResponseEntity.ok(new MessageResponse("User registration request sent to Admin!"));
    }
}
