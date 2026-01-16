# Asset Management System - Backend Documentation

This project is a microservices-based Asset Management System featuring authentication, asset storage (public/private), and administrative controls.

## Services Overview

1.  **Auth Service** (`:8081`): Manages user registration, login (JWT), logout, and token refreshing.
2.  **Asset Service** (`:8085`): Handles file uploads, folder creation, and public/private asset retrieval.
3.  **Admin Service** (`:8089`): provides privileged access to manage all assets and user roles.

---

## 1. Auth Service Features
**Base URL:** `http://localhost:8081/api/auth`

### User Management
*   **Sign Up**: Register a new user.
    *   **Endpoint:** `POST /signup`
    *   **Body:** `{"username": "user1", "email": "user1@example.com", "password": "password1", "role": ["user"]}`
*   **Sign In**: Log in to receive Access and Refresh tokens.
    *   **Endpoint:** `POST /signin`
    *   **Body:** `{"username": "user1", "password": "password1"}`
*   **Sign Out**: Invalidate the current session (clears cookie).
    *   **Endpoint:** `POST /signout`
*   **Get Current User**: Retrieve details of the authenticated user.
    *   **Endpoint:** `GET /me` (or `/user`)
    *   **Header:** `Authorization: Bearer <token>`
*   **Refresh Token**: Get a new access token using a refresh token.
    *   **Endpoint:** `POST /refreshtoken`
    *   **Body:** `{"refreshToken": "..."}`

---

## 2. Asset Service Features
**Base URL:** `http://localhost:8085/api`

### Asset Operations
*   **Upload File**: Upload a file with visibility settings.
    *   **Endpoint:** `POST /upload`
    *   **Params:**
        *   `file`: (MultipartFile)
        *   `isPublic`: `true` or `false` (default: false)
        *   `parentId`: (Optional) ID of the folder to upload into.
    *   **Curl Example:**
        ```bash
        curl -X POST http://localhost:8085/api/upload \
          -H "Authorization: Bearer <token>" \
          -F "file=@/path/to/image.png" \
          -F "isPublic=true"
        ```
*   **Create Folder**: Create a logical folder to organize assets.
    *   **Endpoint:** `POST /folder`
    *   **Params:** `name`, `parentId` (optional)
*   **Get Public Assets**: Retrieve a list of all assets marked as public.
    *   **Endpoint:** `GET /public/asset`
*   **Search Assets**: Search for assets by name.
    *   **Endpoint:** `GET /search?query=filename`
*   **Delete Asset**: Delete a specific asset (Owner/Admin only).
    *   **Endpoint:** `DELETE /asset/{id}`
*   **Get Asset by ID**: Retrieve metadata for a specific asset.
    *   **Endpoint:** `GET /asset/{id}`

---

## 3. Admin Service Features
**Base URL:** `http://localhost:8089/api/admin`
**Requirement:** Authenticated user must have `ROLE_ADMIN`.

### Asset Management
*   **View All Assets**: List every asset in the system regardless of ownership.
    *   **Endpoint:** `GET /assets`
*   **Delete Asset**: Force delete any asset.
    *   **Endpoint:** `DELETE /assets/{id}`
*   **Toggle Visibility**: Change an asset's visibility status.
    *   **Endpoint:** `POST /assets/{id}/visibility`
    *   **Body:** `{"makePublic": true}`
*   **Search All**: Global search across all users' assets.
    *   **Endpoint:** `GET /assets/search?q=...`
*   **Filter by User**: View all assets belonging to a specific user.
    *   **Endpoint:** `GET /assets/user/{username}`

### User Management
*   **View User Roles**: See what roles a user has (USER, EDITOR, ADMIN).
    *   **Endpoint:** `GET /users/{username}/roles`
*   **Update User Roles**: Grant or revoke roles.
    *   **Endpoint:** `PUT /users/{username}/roles`
    *   **Body:** `{"roles": ["USER", "ADMIN"]}`

---

## Authentication Flow
1.  **Login**: User sends `username` + `password` to `/api/auth/signin`.
2.  **Token**: Server returns a JWT `accessToken` and a UUID `refreshToken`.
3.  **Access**: Client sends `Authorization: Bearer <accessToken>` in headers for requests to Asset and Admin services.
4.  **Backend Check**: Services validate the JWT via a shared secret or by verifying signature (depending on config).
5.  **Refresh**: When `accessToken` expires, client sends `refreshToken` to `/api/auth/refreshtoken` to get a new one without logging in again.
