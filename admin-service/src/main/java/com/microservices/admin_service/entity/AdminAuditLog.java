package com.microservices.admin_service.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_audit_logs")
public class AdminAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String adminUsername;
    private String action;         // e.g., DELETE_ASSET, UPDATE_ROLES
    private String target;         // e.g., asset:42 or user:user1
    private LocalDateTime at;

    @Column(length = 2000)
    private String details;        // optional JSON or text

    public static AdminAuditLog of(String admin, String action, String target, String details) {
        AdminAuditLog log = new AdminAuditLog();
        log.adminUsername = admin;
        log.action = action;
        log.target = target;
        log.details = details;
        log.at = LocalDateTime.now();
        return log;
    }

    // getters/setters
    public Long getId() { return id; }
    public String getAdminUsername() { return adminUsername; }
    public String getAction() { return action; }
    public String getTarget() { return target; }
    public LocalDateTime getAt() { return at; }
    public String getDetails() { return details; }
    public void setId(Long id) { this.id = id; }
    public void setAdminUsername(String adminUsername) { this.adminUsername = adminUsername; }
    public void setAction(String action) { this.action = action; }
    public void setTarget(String target) { this.target = target; }
    public void setAt(LocalDateTime at) { this.at = at; }
    public void setDetails(String details) { this.details = details; }
}
