package com.microservices.admin_service.feign;

import com.microservices.admin_service.payload.AssetDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "asset-service", url = "${asset.service.url}", configuration = com.microservices.admin_service.config.FeignConfig.class)
public interface AssetServiceClient {

    @GetMapping("/api/public/asset")
    List<AssetDto> getAllAsset();

    @DeleteMapping("/api/asset/{id}")
    void deleteAsset(@PathVariable("id") Long id);

    @PostMapping("/api/asset/{id}/visibility")
    AssetDto toggleVisibility(@PathVariable("id") Long id, @RequestParam("public") boolean makePublic);

    @GetMapping("/api/asset/search")
    List<AssetDto> search(@RequestParam("q") String query);

    @GetMapping("/api/asset/user/{username}")
    List<AssetDto> assetsByUser(@PathVariable("username") String username);
}
