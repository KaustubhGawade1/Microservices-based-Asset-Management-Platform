package com.microservice.asset_service.service;

import com.microservice.asset_service.model.asset;
import com.microservice.asset_service.payload.AssetDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface assetservice {

  // static asset storeFile(MultipartFile file) throws IOException ;

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
  asset storeFile(MultipartFile file, String token, Long parentId, boolean isPublic) throws IOException;

  List<asset> getAllAsset(); // Now returns flat list, maybe should rename or keep for admin

  // New methods
  List<asset> getAssetsByFolder(Long parentId);

  List<asset> getStarredAssets();

  List<asset> getTrashedAssets();

  List<asset> getPublicAssets();

  asset createFolder(String folderName, Long parentId, String token);

  asset starAsset(Long id, boolean isStarred);

  asset softDeleteAsset(Long id);

  void restoreAsset(Long id);

  AssetDto addAsset(AssetDto Asset);

  asset getAllAssetById(Long id);

  asset updateAsset(Long id, asset Asset);

  String deleteAsset(Long id); // Permanent delete

  List<asset> searchAssets(String query);

  asset toggleVisibility(Long id, boolean makePublic, String token);

  List<asset> getMyAssets(String token);

  org.springframework.core.io.Resource downloadAsset(Long id, String token) throws java.net.MalformedURLException;
}
