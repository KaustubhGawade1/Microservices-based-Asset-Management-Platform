package com.microservice.asset_service.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
public class asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long asset_id;

    private Long parentId; // null means root directory

    @Column(columnDefinition = "boolean default false")
    private boolean isFolder = false;

    @Column(columnDefinition = "boolean default false")
    private boolean isStarred = false;

    @Column(columnDefinition = "boolean default false")
    private boolean isDeleted = false; // Soft delete for trash

    public asset() {

    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    @Getter
    @Setter
    private String filename;

    // video, doc, etc.

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    private LocalDateTime uploadedAt;

    public asset(String filename) {

        this.filename = filename;

        // this.asset_id = asset_id;
    }

    public Long getAsset_id() {
        return asset_id;
    }

    public void setAsset_id(Long asset_id) {
        this.asset_id = asset_id;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public asset(String filename, String contentType, Long size, String filePath) {
        this.filename = filename;

        this.contentType = contentType;
        this.size = size;
        this.filePath = filePath;
    }

    private String contentType;
    private Long size;
    private String filePath;

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    private boolean isPublic;

    private Long user_id;

    public void setUploadedById(Long userId) {
        user_id = userId;
    }

    public Long getUploadedById() {
        return user_id;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public boolean isFolder() {
        return isFolder;
    }

    public void setFolder(boolean folder) {
        isFolder = folder;
    }

    public boolean isStarred() {
        return isStarred;
    }

    public void setStarred(boolean starred) {
        isStarred = starred;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }
}
