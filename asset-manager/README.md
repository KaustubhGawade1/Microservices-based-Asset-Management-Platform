# AssetFlow - Modern Asset Management System

AssetFlow is a production-ready, microservices-compatible frontend for managing enterprise digital assets. It features a modern React UI, strict Role-Based Access Control (RBAC), and a seamless user experience.

## 🚀 Features

### 1. Authentication & Security
- **Role-Based Access:** Distinction between `USER` and `ROLE_ADMIN`.
- **Admin Approval Workflow:** New registrations are submitted as "Requests" that must be approved by an Admin before access is granted.
- **Secure Token Handling:** Logic included for JWT storage and automatic token refreshing on 401 errors.

### 2. Dashboard & Asset Management
- **My Assets:** Private storage for authenticated users.
- **Public Assets:** Network-wide shared folder accessible to all users.
- **Upload:** Drag-and-drop support with an option to set initial visibility (Public/Private).
- **Visibility Toggle:** Owners and Admins can switch an asset between Public and Private after upload.

### 3. Admin Console (Strictly Restricted)
- **File Management:** View all system files, force delete assets, and toggle visibility.
- **User Management:**
    - **Pending Approvals:** Accept or Reject new account requests.
    - **Active Users:** View directory and delete user accounts.

### 4. User Profile
- dedicated profile section displaying user details, role, account status, and membership duration.

## 🛠 Tech Stack

- **Frontend Framework:** React 18
- **Styling:** Tailwind CSS (Glassmorphism, Gradients, Responsive Grid)
- **Icons:** Lucide React
- **Language:** TypeScript
- **HTTP Client:** Native `fetch` with custom interceptor logic.

## 📦 Setup Instructions

1.  **Install Dependencies:**
    ```bash
    npm install react react-dom lucide-react tailwindcss
    ```

2.  **Run Application:**
    Ensure your backend services are running on ports `:8081` (Auth), `:8085` (Asset), and `:8089` (Admin).
    
    If no backend is available, the app defaults to **Demo Mode** (Mock Data), allowing full UI exploration without a server.

## 📂 File Structure

- `App.tsx`: The main Single Page Application containing all logic, views, and components.
