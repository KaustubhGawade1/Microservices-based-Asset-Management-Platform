package com.microservices.admin_service.controller;

import com.microservices.admin_service.payload.AssetDto;
import com.microservices.admin_service.payload.ToggleVisibilityRequest;
import com.microservices.admin_service.service.AdminAssetService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/assets")
public class AdminAssetController {

    private final AdminAssetService service;

    public AdminAssetController(AdminAssetService service) {
        this.service = service;
    }

    @GetMapping
    // @PreAuthorize("hasRole('ADMIN')")
    public List<AssetDto> all() {
        return service.all();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PostMapping("/{id}/visibility")
    @PreAuthorize("hasRole('ADMIN')")
    public AssetDto toggleVisibility(@PathVariable Long id, @RequestBody ToggleVisibilityRequest req) {
        return service.toggleVisibility(id, req.isMakePublic());
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AssetDto> search(@RequestParam("q") String q) {
        return service.search(q);
    }

    @GetMapping("/user/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AssetDto> byUser(@PathVariable String username) {
        return service.byUser(username);
    }
}
