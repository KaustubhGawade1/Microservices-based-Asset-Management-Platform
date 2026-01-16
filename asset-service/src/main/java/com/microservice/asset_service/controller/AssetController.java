package com.microservice.asset_service.controller;

import com.microservice.asset_service.model.asset;
import com.microservice.asset_service.payload.AssetDto;
import com.microservice.asset_service.service.assetservice;
import com.microservice.asset_service.service.assetserviceimp;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
//import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class AssetController {

    @Autowired
    public assetservice AssetService;

    @Autowired
    public assetserviceimp AssetServiceimp;
    @Autowired
    RestTemplate restTemplate;

    // @PreAuthorize("hasRole('ADMIN') or hasRole('EDITOR')")
    @PostMapping("/folder")
    public ResponseEntity<asset> createFolder(@RequestParam String name, @RequestParam(required = false) Long parentId,
            @RequestHeader(value = "Authorization") String authHeader) {
        String token = authHeader.substring(7);
        asset folder = AssetService.createFolder(name, parentId, token);
        return new ResponseEntity<>(folder, HttpStatus.CREATED);
    }

    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR', 'USER')")
    public ResponseEntity<?> uploadFile(
            @RequestParam MultipartFile file,
            @RequestParam(required = false) Long parentId,
            @RequestParam(required = false, defaultValue = "false") boolean isPublic,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @CookieValue(value = "springBootDAMS", required = false) String jwtCookie) throws IOException {

        String token;
        System.out.println("DEBUG: uploadFile - AuthHeader: " + authHeader);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7); // remove Bearer
            System.out.println("DEBUG: uploadFile - Token extracted from header");
        } else if (jwtCookie != null) {
            token = jwtCookie;
            System.out.println("DEBUG: uploadFile - Token extracted from cookie");
        } else {
            System.out.println("DEBUG: uploadFile - Missing authentication token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Missing authentication token");
        }

        AssetService.storeFile(file, token, parentId, isPublic);
        return ResponseEntity.ok("Uploaded");
    }

    // ... skipping folder endpoints ...

    // @CircuitBreaker(name ="CompanyBreaker",fallbackMethod =
    // "companyBreakerFallback")
    @Retry(name = "companyRetry", fallbackMethod = "fallbackResponse")
    @GetMapping("/public/asset")
    // @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR', 'USER')") // Public assets
    // should be truly public or just authenticated? User request says "Public
    // Assets". Often accessible to all. But keeping auth for now as safe default.
    public ResponseEntity<List<asset>> getPublicAssets() {
        List<asset> assetList = AssetService.getPublicAssets();
        return new ResponseEntity<>(assetList, HttpStatus.OK);
    }

    public List<String> companyBreakerFallback(Exception e) {
        List<String> list = new ArrayList<>();
        list.add("Dummy");
        return list;
    }

    @GetMapping("/asset/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<asset> getAllAssetById(@PathVariable Long id) {
        asset assetL = AssetService.getAllAssetById(id);
        return new ResponseEntity<>(assetL, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('EDITOR')")
    @PutMapping("/asset/update/{id}")

    public ResponseEntity<asset> updateAsset(@PathVariable Long id, @RequestBody asset Asset) {
        AssetService.updateAsset(id, Asset);
        return new ResponseEntity<>(Asset, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('EDITOR')")
    @DeleteMapping("/asset/{id}")

    public ResponseEntity<String> deleteAsset(@PathVariable Long id) {
        AssetService.deleteAsset(id);
        return new ResponseEntity<>("Asset Deleted Successfully", HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<List<asset>> searchAssets(@RequestParam String query) {
        List<asset> result = AssetService.searchAssets(query);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/asset/{id}/visibility")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR', 'USER')")
    public ResponseEntity<?> toggleVisibility(@PathVariable Long id,
            @RequestBody java.util.Map<String, Boolean> payload,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @CookieValue(value = "springBootDAMS", required = false) String jwtCookie) {
        String token;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        } else if (jwtCookie != null) {
            token = jwtCookie;
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing authentication token");
        }

        boolean makePublic = payload.getOrDefault("makePublic", false);
        AssetService.toggleVisibility(id, makePublic, token);
        return ResponseEntity.ok(java.util.Collections.singletonMap("message", "Visibility updated"));
    }

    @GetMapping("/asset/my-assets")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR', 'USER')")
    public ResponseEntity<List<asset>> getMyAssets(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @CookieValue(value = "springBootDAMS", required = false) String jwtCookie) {
        String token;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        } else if (jwtCookie != null) {
            token = jwtCookie;
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<asset> assets = AssetService.getMyAssets(token);
        return ResponseEntity.ok(assets);
    }

    @GetMapping("/asset/download/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR', 'USER')")
    public ResponseEntity<org.springframework.core.io.Resource> downloadAsset(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @CookieValue(value = "springBootDAMS", required = false) String jwtCookie)
            throws java.net.MalformedURLException {

        String token;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        } else if (jwtCookie != null) {
            token = jwtCookie;
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        org.springframework.core.io.Resource resource = AssetService.downloadAsset(id, token);

        // Try to determine file name (we could also look up the asset to get the
        // original filename)
        // For now, let's look up the asset briefly just for the filename to set
        // Content-Disposition
        // Or cleaner: modify interface to return a wrapper with filename, but let's do
        // a quick lookup here if needed
        // Actually, the resource.getFilename() will return the UUID name. We prefer
        // original name.
        // Let's rely on the service to handle permission, but we get the asset here for
        // headers.

        asset assetMeta = AssetService.getAllAssetById(id); // Using existing method
        String originalFilename = assetMeta.getFilename();
        String contentType = assetMeta.getContentType();

        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + originalFilename + "\"")
                .body(resource);
    }

}

// public ResponseEntity<ProductDTO> addProduct(@Valid @RequestBody ProductDTO
// productDTO,
// @PathVariable Long categoryId){
// ProductDTO savedProductDTO = productService.addProduct(categoryId,
// productDTO);
// return new ResponseEntity<>(savedProductDTO, HttpStatus.CREATED);
// }
