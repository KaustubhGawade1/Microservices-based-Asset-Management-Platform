# Backend Integration Changes & Challenges

This document details the changes made to the `auth-service`, `admin-service`, and `asset-service` to integrated the `asset-manager` frontend, along with the technical challenges encountered and their resolutions.

## 1. Auth Service Changes [`auth-service`]
**Goal**: Implement a "User Approval Workflow" where new registrations are pending until approved by an admin.

### Key Modifications:
- **New Entity**: `UserRequest`
    - Stores pending registration data (`username`, `email`, `password`, `requestedAt`).
- **New Repository**: `UserRequestRepository`
    - Standard JPA repository for `UserRequest`.
- **Modified Controller**: `AuthController`
    - Added `POST /signup-request`: Accepts registration details and saves them to `UserRequest` table instead of creating an active user immediately.
- **New Internal Controller**: `AdminUserRequestController`
    - Designed for `admin-service` to consume.
    - `GET /api/internal/requests`: Lists all pending requests.
    - `POST /api/internal/requests/{id}/approve`: Moves data from `UserRequest` to `Users` table (granting `ROLE_USER`) and deletes the request.
    - `DELETE /api/internal/requests/{id}`: Rejects/Deletes the request.
    - `DELETE /api/internal/users/{username}`: Deletes an active user.

## 2. Admin Service Changes [`admin-service`]
**Goal**: Expose endpoints for the Admin Console to manage user requests and assets.

### Key Modifications:
- **Modified Feign Client**: `AuthServiceClient`
    - Added mappings to consume the new `auth-service` internal endpoints (`getRequests`, `approveRequest`, `deleteRequest`, `deleteUser`).
- **Modified Service**: `AdminUserService`
    - Added logic to fetch requests and forward approval/rejection actions to `auth-service`.
    - Implemented a workaround to map `username` (from frontend) to `id` (required by backend) for approvals.
- **Modified Controller**: `AdminUserController`
    - `GET /api/admin/users/requests`: Proxy to fetch pending requests.
    - `POST /api/admin/users/{username}/approve`: Triggers user approval.
    - `DELETE /api/admin/users/requests/{id}`: Triggers request rejection.
    - `DELETE /api/admin/users/{username}`: Triggers active user deletion.

## 3. Asset Service Changes [`asset-service`]
**Goal**: Allow users to toggle asset visibility (Private/Public) and ensure data compatibility.

### Key Modifications:
- **Modified Interface & Implementation**: `assetservice` / `assetserviceimp`
    - Added `toggleVisibility(Long id, boolean makePublic, String token)` method.
    - **Logic**: Verifies the JWT token and checks if the requester is the **owner** of the file or an **admin** before allowing the change.
- **Modified Controller**: `AssetController`
    - Added `POST /api/asset/{id}/visibility`: Endpoint receiving `makePublic` boolean. Extracts token from header/cookie and calls the service.
- **Data Model Updates**:
    - **`asset.java`**: Added `getUploadedById()` method (was missing) to support ownership checks.
    - **`UsersDto.java`**: Refactored `role` (String) to `roles` (Set of RoleDto) to match the JSON structure returned by `auth-service` (`/me` endpoint), preventing deserialization errors.

---

## Technical Challenges & Resolutions

During the implementation and verification process, several issues were encountered and resolved:

### 1. `auth-service` Compilation Errors
- **Issue**: `AdminUserRequestController` failed to compile because `userRepository.findByUsername` was undefined.
    - **Cause**: Case sensitivity. The generated method in JPA repository was `findByusername` (lowercase 'u').
    - **Resolution**: Corrected the method call to `findByusername`.
- **Issue**: `Optional` handling error. passing `.orElseThrow` on a method returning the entity directly.
    - **Cause**: `findByusername` was defined to return `Users` directly, not `Optional<Users>`.
    - **Resolution**: Removed `.orElseThrow` usage.

### 2. `admin-service` Build Failures
- **Issue**: Syntax error "reached end of file while parsing".
    - **Cause**: Missing closing brace `}` in `AdminUserController`.
    - **Resolution**: Added the missing brace.
- **Issue**: `AuthServiceClient` compilation error "cannot find symbol".
    - **Cause**: Missing imports for `PostMapping` and `DeleteMapping` annotations used in the Feign client.
    - **Resolution**: Added `org.springframework.web.bind.annotation.*` imports.
- **Issue**: `AdminUserService` compilation error.
    - **Cause**: Missing `java.util.List` import.
    - **Resolution**: Added the import.

### 3. `asset-service` Inter-Service Communication Mismatch
- **Issue**: `UsersDto` deserialization failure.
    - **Cause**: `auth-service` returns roles as a set of objects (`Set<user_role>`), but `asset-service`'s `UsersDto` expected a single `String role`.
    - **Resolution**: Updated `UsersDto` in `asset-service` to use `Set<RoleDto>` and created a `RoleDto` class to match the JSON structure.
- **Issue**: `assetserviceimp` compilation error.
    - **Cause**: `asset.getUploadedById()` was not defined in the model.
    - **Resolution**: Added the getter method to `asset.java`.

## Conclusion
All backend services are now aligned with the frontend requirements. The system supports a secure implementation of user approvals and asset visibility controls, with proper cross-service communication and error handling.
