package com.microservices.admin_service.service;

import com.microservices.admin_service.payload.AssetDto;
import com.microservices.admin_service.entity.AdminAuditLog;
import com.microservices.admin_service.feign.AssetServiceClient;
import com.microservices.admin_service.repository.AdminAuditLogRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminAssetService {

    private final AssetServiceClient assetClient;
    private final AdminAuditLogRepository auditRepo;

    public AdminAssetService(AssetServiceClient assetClient, AdminAuditLogRepository auditRepo) {
        this.assetClient = assetClient;
        this.auditRepo = auditRepo;
    }

    public List<AssetDto> all() {
        return assetClient.getAllAsset();
    }

    public void delete(Long id) {
        assetClient.deleteAsset(id);
        String admin = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        auditRepo.save(AdminAuditLog.of(admin, "DELETE_ASSET", "asset:" + id, null));
    }

    public AssetDto toggleVisibility(Long id, boolean makePublic) {
        AssetDto dto = assetClient.toggleVisibility(id, makePublic);
        String admin = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        auditRepo.save(AdminAuditLog.of(admin, "TOGGLE_VISIBILITY", "asset:" + id, "public=" + makePublic));
        return dto;
    }

    public List<AssetDto> search(String q) {
        return assetClient.search(q);
    }

    public List<AssetDto> byUser(String username) {
        return assetClient.assetsByUser(username);
    }
}

