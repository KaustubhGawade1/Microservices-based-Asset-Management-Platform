# Asset Management System Implementation Plan

## Goal
Refactor the current application into a robust **Asset Management System** with a unique, professional design, role-based access, and comprehensive asset lifecycle management.

## 1. Backend Verification & Expansion
### Auth Service
- [ ] **Logout**: Implement `POST /api/auth/signout` (if missing) to handle token invalidation/blacklist.
- [ ] **User Profile**: Ensure `GET /api/auth/me` returns full user details/stats.

### Asset Service
- [ ] **Public/Private Assets**: Ensure `isPublic` flag is operational and secure.
- [ ] **Endpoints**:
    - `GET /api/public/asset`: List public assets.
    - `GET /api/asset/search`: Enhanced search (scoped by rule).
    - `POST /api/asset/add/file`: Upload with visibility settings.
    - `DELETE /api/asset/{id}`: Owner/Admin deletion.
- [ ] **Admin Features**:
    - `GET /api/admin/assets`: View all system assets.

## 2. Frontend Architecture (React/Next.js)
### Core Infrastructure
- **State Management**: React Context (AuthContext, AssetContext).
- **Routing**: Role-based route guards (`PrivateRoute`, `AdminRoute`).
- **Networking**: Central Axios instance with interceptors for 401/403 and error toasting.

### UI/UX Design "Unique & Premium"
- **Theme**: Move away from "Google Drive" clone. Use a modern Dashboard aesthetic (Sidebar navigation, Stats cards, darker accents or glassmorphism).
- **Navigation**:
    - **Dashboard**: Stats (Total Assets, Storage), Quick Actions.
    - **My Assets**: Table/Grid view with filters.
    - **Public Assets**: Community/Shared view.
    - **Profile**: User info.
    - **Admin**: Moderation panel (if Admin).

### Feature Implementation
- **Auth**: Login (Toast errors), Register, Logout (Backend call).
- **Asset Operations**:
    - Upload Modal (Drag & drop, Visibility toggle).
    - Folder Management (if keeping hierarchy).
    - Context Actions (Delete, Edit, Download).

## 3. Execution Steps
1.  **Backend Fixes**: Add missing endpoints (`signout`, `public/private` logic).
2.  **Frontend Core**: Setup AuthProvider, Axios interceptors, new Layout.
3.  **Dashboard & Assets**: Implement the new "unique" design components.
4.  **Admin & Profile**: Build secondary pages.
5.  **Integration**: End-to-end testing.

