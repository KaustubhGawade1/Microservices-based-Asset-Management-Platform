package com.microservices.admin_service.payload;

import java.time.LocalDate;

public class UserRequestDto {
    private Long id;
    private String username;
    private String email;
    private String status;
    private LocalDate requestedAt;

    public UserRequestDto() {
    }

    public UserRequestDto(Long id, String username, String email, String status, LocalDate requestedAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.status = status;
        this.requestedAt = requestedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDate requestedAt) {
        this.requestedAt = requestedAt;
    }
}
