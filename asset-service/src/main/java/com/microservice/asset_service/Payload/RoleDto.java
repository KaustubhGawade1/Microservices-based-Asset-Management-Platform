package com.microservice.asset_service.Payload;

public class RoleDto {
    private Long id;
    private String userRole; // "ROLE_USER", etc.

    public RoleDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    // Helper to get name
    public NameSchema getName() {
        // Mocking the behavior for isPublic logic
        return new NameSchema(userRole);
    }

    public static class NameSchema {
        private String name;

        public NameSchema(String name) {
            this.name = name;
        }

        public String name() {
            return name;
        }
    }
}
