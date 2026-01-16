package com.microservice.auth_service.Payload;

import com.microservice.auth_service.model.user_role;

import java.util.Set;

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



    private String username;
    private String password;



    public Set<user_role> getRoles() {
        return roles;
    }

    public void setRoles(Set<user_role> roles) {
        this.roles = roles;
    }

    private Set<user_role> roles;

    public Long getUser_id() {
        return user_id;
    }

    public void setUser_id(Long user_id) {
        this.user_id = user_id;
    }

    public String getemail() {
        return email;
    }

    public void setemail(String email) {
        this.email = email;
    }

    private String email;
    private Long user_id;
    public UsersDto(Long user_id, String username, String email, Set<user_role> roles) {
        this.username = username;
        this.user_id = user_id;
        this.email = email;
        this.roles = roles;
    }
}
