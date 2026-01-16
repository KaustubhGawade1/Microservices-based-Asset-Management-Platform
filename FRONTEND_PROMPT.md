# Gemini Frontend Generation Prompt

**Role:** Expert Frontend Developer / UX Designer
**Task:** Build/Refactor a production-ready Frontend for a Microservices Asset Management System.
**Tech Stack:** Next.js 14+ (App Router), TypeScript, Tailwind CSS, Axios, Lucide React (Icons).

---

## 1. Project Overview
You are building the frontend for a robust "Asset Management System" (a Google Drive alternative). The backend consists of three microservices (`Auth`, `Asset`, `Admin`) running locally. Your goal is to create a modern, premium, and responsive UI that integrates seamlessly with these services.

## 2. API Integration Strategy
**Base URLs:**
- **Auth Service:** `http://localhost:8081/api/auth`
- **Asset Service:** `http://localhost:8085/api`
- **Admin Service:** `http://localhost:8089/api/admin`

**Authentication Flow:**
- Login using `POST /signin` to get an `accessToken` (JWT) and `refreshToken`.
- Store `accessToken` (in memory or cookie) and use it in the `Authorization: Bearer <token>` header for ALL requests to Asset and Admin services.
- Implement an Axios interceptor:
  - If a 401 response is received, call `POST /refreshtoken` to get a new token and retry the request.
  - If refresh fails, redirect to `/login`.

## 3. Core Features to Implement

### A. Authentication Module
- **Login Page (`/login`)**:
  - Clean, centered card design.
  - Fields: Username, Password.
  - "Contact Admin" mailto link (`mailto:admin@example.com`).
  - Handle errors (show "Invalid Credentials" toast/alert).
- **Registration Page (`/register`)**:
  - Fields: Username, Email, Password.
  - Default role: `USER`.
- **Logout**: Call `POST /signout` and clear local state.

### B. Dashboard / My Assets (`/drive` or `/dashboard`)
- **Layout**: Sidebar navigation (Dashboard, My Assets, Public Assets, Settings).
- **Asset Grid/List**:
  - Fetch assets via `GET /api/asset/search` or specific user endpoints.
  - Display thumbnails for images, icons for other files.
- **Upload Functionality**:
  - "Upload New" button triggering a Modal.
  - Drag & Drop zone.
  - Toggle switch: "Make Public" (True/False).
  - API: `POST /api/upload` (Multipart form-data).

### C. Public Assets (`/public`)
- Fetch public assets via `GET /api/public/asset`.
- Accessible even to unauthenticated users (if backend allows), or restricted to logged-in users.
- Read-only view.

### D. Admin Console (`/admin`)
- **Guard**: Only accessible if user has `ROLE_ADMIN`.
- **All Assets View**:
  - List ALL system assets (`GET /api/admin/assets`).
  - Search functionality (`GET /api/admin/assets/search?q=...`).
  - **Actions**:
    - **Delete**: Force delete `DELETE /api/admin/assets/{id}`.
    - **Toggle Visibility**: Button to force Public/Private (`POST /api/admin/assets/{id}/visibility`).
- **User Management** (Optional but recommended):
  - View users and manage roles.

## 4. Design Guidelines (The "Wow" Factor)
- **Aesthetic**: Premium, Clean, Tech-focused. Avoid generic Bootstrap looks.
- **Color Palette**:
  - Primary: Deep Blue / Indigo (Trust, Professional).
  - Surface: White / Light Gray for cards.
  - Background: Very light gray or subtle gradient.
  - Accents: Vibrant colors for status (Green = Public, Amber = Private).
- **Components**:
  - Use Glassmorphism for modals or overlays.
  - Skelton loaders while fetching data.
  - Toast notifications for success/error (e.g., "Upload Successful").

## 5. Implementation Rules
- Use **Next.js App Router** structure (`app/page.tsx`, `app/login/page.tsx`, etc.).
- Create a reusable `axiosInstance` configuration.
- Use a React Context (`AuthProvider`) to manage user session globally.
- Ensure strict TypeScript typing for all API responses (User, Asset, etc.).
