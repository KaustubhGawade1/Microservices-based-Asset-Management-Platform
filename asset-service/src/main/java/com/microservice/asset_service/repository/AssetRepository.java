package com.microservice.asset_service.repository;

import com.microservice.asset_service.model.asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssetRepository extends JpaRepository<asset, Long> {

    // Full-text search by title, contentType, or uploader username
    @Query("SELECT a FROM asset a WHERE " +
    // "LOWER(a.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(a.filename) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(a.contentType) LIKE LOWER(CONCAT('%', :query, '%')) ")
    // "LOWER(a.uploadedBy.username) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<asset> searchAllFields(@Param("query") String query);

    // --- Google Drive Clone Queries ---

    // Get files in a folder (not deleted)
    List<asset> findByParentIdAndIsDeletedFalse(Long parentId);

    // Get root files (parentId is null, not deleted)
    List<asset> findByParentIdIsNullAndIsDeletedFalse();

    // Get starred files (not deleted)
    List<asset> findByIsStarredTrueAndIsDeletedFalse();

    // Get trashed files
    List<asset> findByIsDeletedTrue();

    // Get all non-deleted files (for general search/admin)
    List<asset> findByIsDeletedFalse();

    // Fetch only assets uploaded by a specific user
    @Query("SELECT a FROM asset a WHERE a.user_id = :userId")
    List<asset> findByUser_id(@Param("userId") Long userId);

    // Fetch only public assets
    List<asset> findByIsPublicTrue();

    // Fetch by ID and uploader (for access control)
    // Optional<asset> findByIdAndUploadedBy(Long id, Users user);
}
