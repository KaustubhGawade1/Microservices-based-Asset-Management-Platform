package com.microservice.auth_service.model;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
public class Users {
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<user_role> getRoles() {
        return roles;
    }

    public void setRoles(Set<user_role> roles) {
        this.roles = roles;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user__id")

    private Long user_id;

    @Column(nullable = false, unique = true)

    private String username;

    @Column(nullable = false, unique = true)

    private String email;

    private String password;

    @ManyToMany(fetch = FetchType.EAGER, cascade = { CascadeType.MERGE })
    @JoinTable(name = "user_roles_mapping", // avoid same name as entity table
            joinColumns = @JoinColumn(name = "user__id"), inverseJoinColumns = @JoinColumn(name = "role__id"))
    private Set<user_role> roles = new HashSet<>();

    public Users() {
    }

    public Users(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;

    }

    public Long getUser_id() {
        return user_id;
    }
}
