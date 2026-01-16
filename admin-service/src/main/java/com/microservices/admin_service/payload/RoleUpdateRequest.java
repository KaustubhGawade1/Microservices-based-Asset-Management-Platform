package com.microservices.admin_service.payload;

import java.util.List;

public class RoleUpdateRequest {
    private List<String> roles;
    public RoleUpdateRequest() {}
    public RoleUpdateRequest(List<String> roles) { this.roles = roles; }
    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
}
