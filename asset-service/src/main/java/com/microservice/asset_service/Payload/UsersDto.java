package com.microservice.asset_service.Payload;

public class UsersDto {
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public java.util.Set<String> getRolesAsString() {
        return roles != null ? roles.stream().map(RoleDto::getUserRole).collect(java.util.stream.Collectors.toSet())
                : new java.util.HashSet<>();
    }

    private String username;
    private String password;
    private java.util.Set<RoleDto> roles;
    private String email;

    public java.util.Set<RoleDto> getRoles() {
        return roles;
    }

    public void setRoles(java.util.Set<RoleDto> roles) {
        this.roles = roles;
    }

    public Long getUser_id() {
        return user_id;
    }

    public void setUser_id(Long user_id) {
        this.user_id = user_id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    private Long user_id;

    public UsersDto(Long user_id, String username, String email) {
        this.username = username;
        this.user_id = user_id;
        this.email = email;
    }
}
