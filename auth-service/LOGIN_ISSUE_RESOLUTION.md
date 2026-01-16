# Login Issue Resolution & Kubernetes Version Update

This document details the step-by-step resolution of the `401 Unauthorized` login errors for users `user1` and `admin`, and explains the critical Kubernetes deployment update from `v2` to `v3`.

## 1. The Issue
Despite having correct credentials and successful registration responses, login attempts for `user1` and `admin` were consistently failing with:
```json
{
    "error": "Unauthorized",
    "message": "Full authentication is required to access this resource",
    "status": 401
}
```

## 2. Root Cause Analysis
The investigation revealed two primary issues:
1.  **Deployment Version Mismatch**: The running Kubernetes pod for `auth-service` was using an older Docker image (`microservices/auth-service:v2`). This image did not contain the recent code fixes (CORS updates, Token handling) that were built into the `v3` image.
2.  **Database Inconsistency**: Due to the backend logic changes and previous failed attempts, the `user1` and `admin` accounts in the PostgreSQL database were in a corrupted state (mismatched password encodings or orphaned tokens).

## 3. Resolution Steps

### Step 1: Updating Kubernetes Deployment (v2 -> v3)
We discovered the specific version mismatch by inspecting the running deployment:
```bash
kubectl get deployment auth-service -o=jsonpath='{.spec.template.spec.containers[0].image}'
# Output was: microservices/auth-service:v2
```

We updated the Kubernetes manifest `k8s/auth-service.yaml` to use the correct image version:
```yaml
# Before
image: microservices/auth-service:v2

# After
image: microservices/auth-service:v3
```

Then applied the changes and forced a pod restart:
```bash
kubectl apply -f k8s/auth-service.yaml
kubectl delete pod -l app=auth-service # Forces K8s to pull new image and start fresh
```
**Why this helped**: This ensured that the code running in the cluster actually matched the latest codebase where we had fixed the 500 Internal Server Errors and CORS issues.

### Step 2: Database Cleanup
To ensure a clean slate, we removed the corrupted user records and their dependencies (refresh tokens, roles) directly from the database pod.

**Command for `user1`:**
```bash
kubectl exec -it $(kubectl get pod -l app=postgres -o jsonpath="{.items[0].metadata.name}") -- psql -U myuser -d auth_db -c "DELETE FROM refreshtoken WHERE user_id IN (SELECT user__id FROM users WHERE username='user1'); DELETE FROM user_roles_mapping WHERE user__id IN (SELECT user__id FROM users WHERE username='user1'); DELETE FROM users WHERE username='user1';"
```

**Command for `admin`:**
```bash
kubectl exec -it $(kubectl get pod -l app=postgres -o jsonpath="{.items[0].metadata.name}") -- psql -U myuser -d auth_db -c "DELETE FROM refreshtoken WHERE user_id IN (SELECT user__id FROM users WHERE username='admin'); DELETE FROM user_roles_mapping WHERE user__id IN (SELECT user__id FROM users WHERE username='admin'); DELETE FROM users WHERE username='admin';"
```

### Step 3: Re-registration
We re-registered the users via the API to let the `auth-service` handle password encoding and role assignment correctly.

**Register `user1`:**
```bash
curl -v -X POST http://localhost:8081/api/auth/signup \
-H "Content-Type: application/json" \
-d '{"username":"user1", "email":"user1@example.com", "password":"password1", "role":["user"]}'
```

**Register `admin`:**
```bash
curl -v -X POST http://localhost:8081/api/auth/signup \
-H "Content-Type: application/json" \
-d '{"username":"admin", "email":"admin@example.com", "password":"adminPass", "role":["admin"]}'
```

## 4. Verification
Finally, we verified the fix by logging in with the specific credentials.

**Login `user1`:**
```bash
curl -v -X POST http://localhost:8081/api/auth/signin \
-H "Content-Type: application/json" \
-d '{"username":"user1", "password":"password1"}'
```

**Login `admin`:**
```bash
curl -v -X POST http://localhost:8081/api/auth/signin \
-H "Content-Type: application/json" \
-d '{"username":"admin", "password":"adminPass"}'
```

Both commands returned `200 OK` with valid Access and Refresh tokens.
