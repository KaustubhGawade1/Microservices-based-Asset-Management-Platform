package com.microservice.asset_service.service;

import com.microservice.asset_service.Payload.UsersDto;
import com.microservice.asset_service.model.asset;
import com.microservice.asset_service.payload.AssetDto;
import com.microservice.asset_service.repository.AssetRepository;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
//import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
// import java.util.Optional; // Removed unused
import java.util.UUID;

@Service
public class assetserviceimp implements assetservice {

    @Value("${upload.path}")
    private String uploadDir;

    @Autowired
    public AssetRepository assetrepo;

    @Autowired
    private ModelMapper modelMapper;

    @Value("${auth.service.url}")
    private String authServiceUrl;
    @Autowired
    private RestTemplate restTemplate;

    // public asset storeFile(MultipartFile file) throws IOException {
    //
    // Authentication authentication =
    // SecurityContextHolder.getContext().getAuthentication();
    // String username = authentication.getName();
    // Users userr = userRepository.findByusername(username);
    //
    // // Create the directory if it doesn't exist
    // File dir = new File(uploadDir);
    // if (!dir.exists()) dir.mkdirs();
    //
    // // Save file to disk
    // String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
    // Path filepath = Paths.get(uploadDir, filename);
    // Files.copy(file.getInputStream(), filepath);
    //
    // // Save metadata to DB
    // asset aasset = new asset();
    // aasset.setFilename(file.getOriginalFilename());
    // aasset.setContentType(file.getContentType());
    // aasset.setSize(file.getSize());
    // aasset.setFilePath(filepath.toString());
    // aasset.setUploadedAt(LocalDateTime.now());
    // aasset.setUploadedBy(userr);
    // return assetrepo.save(aasset);
    // }
    @Override
    public asset storeFile(MultipartFile file, String token, Long parentId, boolean isPublic) throws IOException {
        // Authenticate user
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = authServiceUrl + "/api/auth/user";
        ResponseEntity<java.util.Map> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, java.util.Map.class);

        java.util.Map<String, Object> user = response.getBody();
        if (user == null || user.get("id") == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }

        Long userId = ((Number) user.get("id")).longValue();

        // Save file physically
        File dir = new File(uploadDir);
        if (!dir.exists())
            dir.mkdirs();
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filepath = Paths.get(uploadDir, filename);

        // Ensure parent exists and is a folder if parentId is provided
        if (parentId != null) {
            asset parent = assetrepo.findById(parentId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parent folder not found"));
            if (!parent.isFolder()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parent must be a folder");
            }
        }

        Files.copy(file.getInputStream(), filepath);

        // Save metadata
        asset newAsset = new asset();
        newAsset.setFilename(file.getOriginalFilename());
        newAsset.setContentType(file.getContentType());
        newAsset.setSize(file.getSize());
        newAsset.setFilePath(filepath.toString());
        newAsset.setUploadedAt(LocalDateTime.now());
        newAsset.setUploadedById(userId);
        newAsset.setParentId(parentId);
        newAsset.setPublic(isPublic);

        return assetrepo.save(newAsset);
    }

    @Override
    public List<asset> getPublicAssets() {
        return assetrepo.findByIsPublicTrue();
    }

    @Override
    public asset createFolder(String folderName, Long parentId, String token) {
        // Auth check (reuse same token logic or extract to method)
        // For brevity assuming valid token or just using user ID if we extract it
        // differently

        // ... (Auth Logic similar to storeFile) ...

        // Ensure parent exists
        if (parentId != null) {
            asset parent = assetrepo.findById(parentId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
            if (!parent.isFolder())
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parent is not a folder");
        }

        asset folder = new asset();
        folder.setFilename(folderName);
        folder.setFolder(true);
        folder.setParentId(parentId);
        folder.setUploadedAt(LocalDateTime.now());
        // folder.setUploadedById(userId); // Need user ID

        return assetrepo.save(folder);
    }

    @Override
    public List<asset> getAssetsByFolder(Long parentId) {
        if (parentId == null) {
            return assetrepo.findByParentIdIsNullAndIsDeletedFalse();
        }
        return assetrepo.findByParentIdAndIsDeletedFalse(parentId);
    }

    @Override
    public List<asset> getStarredAssets() {
        return assetrepo.findByIsStarredTrueAndIsDeletedFalse();
    }

    @Override
    public List<asset> getTrashedAssets() {
        return assetrepo.findByIsDeletedTrue();
    }

    @Override
    public asset softDeleteAsset(Long id) {
        asset asset = assetrepo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        asset.setDeleted(true);
        return assetrepo.save(asset);
    }

    @Override
    public void restoreAsset(Long id) {
        asset asset = assetrepo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        asset.setDeleted(false);
        assetrepo.save(asset);
    }

    @Override
    public asset starAsset(Long id, boolean isStarred) {
        asset asset = assetrepo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        asset.setStarred(isStarred);
        return assetrepo.save(asset);
    }

    @Override
    public List<asset> getAllAsset() {
        return assetrepo.findByIsDeletedFalse();
    }

    @Override
    public AssetDto addAsset(AssetDto Asset) {
        // This seems redundant with storeFile but keeping for backward compat if used
        asset assetL = modelMapper.map(Asset, asset.class);
        asset savedasset = assetrepo.save(assetL);
        return modelMapper.map(savedasset, AssetDto.class);
    }

    @Override
    public asset getAllAssetById(Long id) {
        return assetrepo.findById(id).orElse(null);
    }

    @Override
    public asset updateAsset(Long id, asset Asset) {
        asset existing = assetrepo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        existing.setFilename(Asset.getFilename());
        return assetrepo.save(existing);
    }

    @Override
    public String deleteAsset(Long id) {
        // Permanent delete
        asset existing = assetrepo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        // Delete physical file if it exists
        if (!existing.isFolder() && existing.getFilePath() != null) {
            try {
                Files.deleteIfExists(Paths.get(existing.getFilePath()));
            } catch (IOException e) {
                // log error
            }
        }
        assetrepo.delete(existing);
        return "Asset Deleted Permanently";
    }

    @Override
    public List<asset> searchAssets(String query) {
        return assetrepo.searchAllFields(query);
    }

    @Override
    public asset toggleVisibility(Long id, boolean makePublic, String token) {
        // Auth Check
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        String url = authServiceUrl + "/api/auth/me";
        ResponseEntity<UsersDto> response = restTemplate.exchange(url, HttpMethod.GET, entity, UsersDto.class);
        UsersDto user = response.getBody();
        if (user == null)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        asset asset = assetrepo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // Ownership Check (Allow Admins too? Frontend says Owners and Admins)
        // We need roles. UsersDto has roles?
        // Let's check UsersDto in asset-service
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(r -> r.getName().name().equals("ROLE_ADMIN") || r.getName().name().equals("ADMIN"));

        if (!asset.getUploadedById().equals(user.getUser_id()) && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not owner or admin");
        }

        asset.setPublic(makePublic);
        return assetrepo.save(asset);
    }

    @Override
    public List<asset> getMyAssets(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = authServiceUrl + "/api/auth/me";
        ResponseEntity<UsersDto> response = restTemplate.exchange(url, HttpMethod.GET, entity, UsersDto.class);
        UsersDto user = response.getBody();

        if (user == null || user.getUser_id() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }

        return assetrepo.findByUser_id(user.getUser_id());
    }

    @Override
    public org.springframework.core.io.Resource downloadAsset(Long id, String token)
            throws java.net.MalformedURLException {
        // Auth Check (Same logic as toggleVisibility or storeFile, could be refactored)
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        String url = authServiceUrl + "/api/auth/me";

        ResponseEntity<UsersDto> response;
        try {
            response = restTemplate.exchange(url, HttpMethod.GET, entity, UsersDto.class);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }

        UsersDto user = response.getBody();
        if (user == null)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        asset asset = assetrepo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // Access Control: Public OR Owner OR Admin
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(r -> r.getName().name().equals("ROLE_ADMIN") || r.getName().name().equals("ADMIN"));
        boolean isOwner = asset.getUploadedById().equals(user.getUser_id());

        System.out.println("DEBUG: Download Access Check for Asset ID: " + id);
        System.out.println("DEBUG: User ID: " + user.getUser_id() + ", Roles: " + user.getRolesAsString());
        System.out.println("DEBUG: Asset Owner ID: " + asset.getUploadedById() + ", IsPublic: " + asset.isPublic());
        System.out.println("DEBUG: IsAdmin: " + isAdmin + ", IsOwner: " + isOwner);

        if (!asset.isPublic() && !isOwner && !isAdmin) {
            System.out.println("DEBUG: Access DENIED");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied");
        }

        Path filePath = Paths.get(asset.getFilePath());
        org.springframework.core.io.Resource resource = new org.springframework.core.io.UrlResource(filePath.toUri());

        if (resource.exists() || resource.isReadable()) {
            return resource;
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found on server");
        }
    }
}