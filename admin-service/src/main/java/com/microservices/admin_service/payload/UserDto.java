package com.microservices.admin_service.payload;

import java.util.List;

public class UserDto {
    private Long id;
    private String username;
    private List<String> roles;

    public UserDto() {}
    public UserDto(Long id, String username, List<String> roles) {
        this.id = id; this.username = username; this.roles = roles;
    }
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public List<String> getRoles() { return roles; }
    public void setId(Long id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setRoles(List<String> roles) { this.roles = roles; }
}

