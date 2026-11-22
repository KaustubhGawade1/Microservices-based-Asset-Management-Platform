package com.microservice.asset_service.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface AssetStorage {
    String store(MultipartFile file) throws IOException;

    Resource load(String filePath) throws IOException;

    void delete(String filePath) throws IOException;
}
