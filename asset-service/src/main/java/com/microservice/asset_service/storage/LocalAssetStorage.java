package com.microservice.asset_service.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class LocalAssetStorage implements AssetStorage {

    private final Path storageRoot;

    public LocalAssetStorage(@Value("${app.storage.local-root:${upload.path:uploads}}") String storageRoot) {
        this.storageRoot = Paths.get(storageRoot).toAbsolutePath().normalize();
    }

    @Override
    public String store(MultipartFile file) throws IOException {
        Files.createDirectories(storageRoot);

        String originalName = file.getOriginalFilename();
        String safeName = originalName == null ? "asset" : originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
        String objectKey = UUID.randomUUID() + "_" + safeName;
        Path destination = storageRoot.resolve(objectKey).normalize();

        Files.copy(file.getInputStream(), destination);
        return objectKey;
    }

    @Override
    public Resource load(String filePath) throws IOException {
        Path path = resolve(filePath);
        Resource resource = new UrlResource(path.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            throw new IOException("File not found: " + filePath);
        }
        return resource;
    }

    @Override
    public void delete(String filePath) throws IOException {
        Files.deleteIfExists(resolve(filePath));
    }

    private Path resolve(String filePath) {
        Path path = Paths.get(filePath);
        return path.isAbsolute() ? path.normalize() : storageRoot.resolve(path).normalize();
    }
}
