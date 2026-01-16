# Technical Challenges & Solutions

## 1. Login 500 Internal Server Error (Duplicate Key)
- **Issue**: Users could not log in, receiving a generic 500 error.
- **Diagnosis**: Logs revealed a `DataIntegrityViolationException` in `RefreshTokenService`. The `refreshtoken` table has a unique constraint on `user_id`, but the application attempted to insert a new token without removing the old one.
- **Solution**: Refactored `createRefreshToken` to explicitly check for an existing token using `findByUser(userId)`, delete it if found, and `flush` the repository to ensure valid state before inserting the new token.

## 2. CORS & Network Errors
- **Issue**: After login, the dashboard showed "Network Error" and failed to load assets.
- **Diagnosis**: The `asset-service` was not configured to accept requests from the frontend running on `http://localhost:3000`. Browser console confirmed Cross-Origin Resource Sharing (CORS) blocks.
- **Solution**: Updated `WebSecurityConfig.java` in `asset-service` to add `http://localhost:3000` to the allowed origins list.

## 3. Kubernetes & Eureka Connectivity
- **Issue**: Services occasionally failed to register with Eureka or communicate with each other (`UnknownHostException`).
- **Diagnosis**: In a local Minikube environment without a proper DNS loopback for service names, internal service discovery can be flaky during startup or pod restarts.
- **Workaround**: Relied on Kubernetes Service DNS and confirmed connectivity via direct `curl` tests. Ensured `auth-service` and `asset-service` pods were correctly port-forwarded for local browser access.

## 4. Frontend Hydration Mismatches
- **Issue**: React console warnings about "Text content does not match server-rendered HTML".
- **Diagnosis**: Common in Next.js when browser extensions inject code or when local state renders differently than the server.
- **Impact**: Determined to be a non-blocking warning that did not affect the core authentication or asset viewing functionality.
