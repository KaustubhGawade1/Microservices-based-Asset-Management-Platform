# Microservices Asset Management Platform - Interview Explanation

## 1. What this project is
This project is a microservices-based asset management system where users can:
- sign up and log in
- upload files/assets
- mark them public or private
- manage their own assets
- admin can view, delete, and change visibility of any asset
- system can run in Docker and Kubernetes

In simple words: it is like a small cloud-based file management platform, but built as multiple independent services instead of one big monolith.

---

## 2. High-level architecture
The system is split into small services, each responsible for one job.

Simple flow:

```text
Client / Frontend
      |
      v
Gateway / API layer
      |
      +--> Auth Service
      +--> Asset Service
      +--> Admin Service
      |
      +--> Service Registry (Eureka)
      +--> Config Server
      |
      +--> PostgreSQL databases
      +--> RabbitMQ
      +--> Zipkin
```

The important idea is:
- each service is independent
- services talk to each other through HTTP or messaging
- Docker runs all containers together
- Kubernetes can run the same setup in a cluster

---

## 3. What each section does

### 3.1 Auth Service
Role: user identity and security

Files involved:
- `auth-service/`
- `auth-service/src/main/java/.../controller/AuthController.java`
- `auth-service/src/main/resources/application.properties`

What it does:
- register new users
- login users
- create JWT access token
- create refresh token
- validate tokens
- manage roles like USER, EDITOR, ADMIN
- logout / clear cookie

How it works:
- User sends username + password
- Auth service checks username/password
- If valid, it creates a JWT token
- Token is returned to client and also stored in cookie/header
- Later requests include this token for secure access

Why it matters:
- this is the security gate for the entire system
- other services trust JWT and do not re-check passwords

---

### 3.2 Asset Service
Role: upload, store, retrieve, and manage files

Files involved:
- `asset-service/`
- `asset-service/src/main/java/.../controller/AssetController.java`
- `asset-service/src/main/java/.../service/assetserviceimp.java`

What it does:
- upload file to server folder called `uploads/`
- save metadata to database (file name, path, owner, visibility, folder relation)
- create folders
- show public assets
- search by name
- download asset
- update asset visibility
- show user’s own assets
- delete asset if owner or admin

How it works:
- user sends file with token
- service validates token
- file is stored in local folder or mounted volume
- DB stores metadata and ownership relationship
- later client can fetch or download assets

Important point:
- asset service is the real file manager of the system
- auth service only authenticates, asset service handles business logic for files

---

### 3.3 Admin Service
Role: system control and supervision

Files involved:
- `admin-service/`
- `admin-service/src/main/java/.../service/AdminAssetService.java`
- `admin-service/src/main/java/.../feign/AssetServiceClient.java`
- `admin-service/src/main/java/.../feign/AuthServiceClient.java`

What it does:
- see all assets in the platform
- delete any asset
- change visibility of any asset
- search assets globally
- view user roles
- approve or reject user requests
- audit admin actions

How it works:
- admin calls admin API
- admin service uses Feign clients to call other services internally
- it asks asset-service for asset data
- it asks auth-service for user details and roles

This is a classic microservice pattern:
- admin service is a coordinator service
- it does not own all data directly
- it calls other services to gather required information

---

### 3.4 Config Server
Role: shared configuration center

Files:
- `config-server/`
- `config-server/src/main/resources/application.properties`

What it does:
- stores or provides common configuration for services
- helps centralize environment settings
- can integrate with Spring Cloud Config for config externalization

In this project, it is present and configured, but currently many services are also using local `application.properties` directly. So config server is there as a framework feature, but not fully used as the only source of truth.

---

### 3.5 Service Registry (Eureka)
Role: service discovery

Files:
- `service-reg/`
- `service-reg/src/main/resources/application.properties`

What it does:
- every microservice can register itself here
- services can discover each other using service name instead of hardcoding IPs

Example:
- admin-service knows `auth-service` and `asset-service` by name
- Eureka keeps service metadata and health information

Why important:
- this removes manual URL hardcoding
- if a service restarts or moves, it can still be discovered by name

---

### 3.6 Gateway Service
Role: entry point for requests

Files:
- `gateway-service/`

What it does:
- routes external client requests to appropriate microservice
- centralizes API access
- reduces direct connection complexity for frontend

Even though it is part of the project, the current configuration also uses direct service URLs in many places, so the system is a bit hybrid: some parts use gateway routing and some parts call service endpoints directly.

---

## 4. How everything is connected
The best way to understand this is by following one request.

### Example 1: User login
1. Frontend sends login request to Auth Service
2. Auth service checks credentials in PostgreSQL (`auth_db`)
3. If correct, it creates JWT and refresh token
4. Response includes access token and refresh token
5. Frontend stores token and sends it on future requests

### Example 2: Upload asset
1. User sends upload request with JWT token
2. Asset service validates the token
3. Asset service saves file in `uploads/`
4. Asset service saves file metadata to `asset_db`
5. Ownership and visibility are recorded
6. User can later view only their assets or public assets

### Example 3: Admin views all assets
1. Admin calls Admin Service
2. Admin Service uses Feign client to hit Asset Service API
3. Asset Service returns asset list
4. Admin Service can also query Auth service for user roles
5. Admin can now manage the system centrally

This shows the microservice design clearly:
- each service has its own responsibility
- communication happens through HTTP APIs between services

---

## 5. Synchronous vs asynchronous communication

### 5.1 Synchronous communication
This is the main communication model in this project.

Examples:
- Auth service validates login
- Asset service validates JWT
- Admin service calls Asset Service using Feign/RestTemplate
- Asset Service calls Auth Service to get user info

Why it is synchronous:
- request waits for response immediately
- client gets result directly
- this is good for login, upload, search, fetch, delete, etc.

Technologies used:
- Spring Boot REST APIs
- `RestTemplate`
- OpenFeign (`@FeignClient`)

This is the easiest way to explain to an interviewer:
> “Most of the business operations happen synchronously using HTTP requests between microservices.”

---

### 5.2 Asynchronous communication
This project includes RabbitMQ, so it is ready for async communication, even though the main business flow is still mostly synchronous.

Concept:
- one service can send a message to a queue
- another service can process it later
- this is useful for background jobs, notifications, events, audit logs, emails, etc.

In this project:
- RabbitMQ is configured in `application.properties`
- each service points to `rabbitmq` host
- the system is prepared for event-driven style communication

But in the current implementation, the main functional flow still uses direct REST calls, not heavy message-driven workflows.

So in plain English:
> “The platform is designed with microservice principles, and RabbitMQ provides asynchronous capability, but the actual app logic mostly uses standard synchronous HTTP calls.”

---

## 6. Docker and how it helps
Files involved:
- `docker-compose.yml`
- each service has a `Dockerfile`

Docker creates containers for each part of the system:
- Postgres database
- Auth service
- Asset service
- Admin service
- Config server
- Service registry
- RabbitMQ
- Zipkin
- Gateway service

Why Docker is useful:
- same environment for all developers
- no need to install everything manually on each machine
- each service runs isolated in its own container
- easier service startup and dependency management

In `docker-compose.yml`, services are linked using a common network called `springnet`.

Example:
- auth-service connects to Postgres and Eureka
- asset-service connects to DB and RabbitMQ
- zipkin listens on port 9411
- rabbitmq exposes 5672 and 15672

This is a complete local environment setup for development and testing.

---

## 7. Kubernetes and how it helps
Files involved:
- `k8s/*.yaml`

Kubernetes is used to deploy the same app in a cluster environment.

For example:
- `admin-service.yaml`
- `asset-service.yaml`
- `auth-service.yaml`
- `config-server.yaml`
- `postgres.yaml`
- `rabbitmq.yaml`
- `zipkin.yaml`

What Kubernetes gives us:
- deployment management
- scaling pods
- service discovery within cluster
- restart policies
- resource limits
- environment variable injection
- easier production deployment

Example from K8s config:
- deploys a pod for a service
- exposes it through a `Service`
- sets env vars like `SPRING_DATASOURCE_URL`, `SPRING_RABBITMQ_HOST`, `MANAGEMENT_ZIPKIN_TRACING_ENDPOINT`
- limits CPU and memory usage

Simple interview answer:
> “Docker is for running the app locally in containers, while Kubernetes is for managing those containers in a cluster with scaling, monitoring, and deployment automation.”

---

## 8. RabbitMQ in this project
RabbitMQ is used as a message broker.

Why it matters:
- decouples services
- allows background processing
- supports event-driven architecture

How it is configured:
- `spring.rabbitmq.host=rabbitmq`
- `spring.rabbitmq.port=5672`
- `spring.rabbitmq.username=guest`
- `spring.rabbitmq.password=guest`

In the project configuration, RabbitMQ is set up to run in Docker and Kubernetes, and services are configured to talk to it.

Understanding for interview:
- this project is not purely message-driven, but it has the right infrastructure for async events
- if needed later, this can support notifications, queueing, background processing, or event fan-out among services

---

## 9. Zipkin in this project
Zipkin is for distributed tracing.

Why it matters:
- when a request flows through multiple services, it is difficult to know where delays happen
- Zipkin collects traces and shows the full path

How it is configured:
- each service includes:
  - `management.tracing.sampling.probability=1.0`
  - `management.zipkin.tracing.endpoint=http://zipkin:9411/api/v2/spans`

This means:
- each service sends tracing spans to Zipkin
- Zipkin stores them and visualizes the complete request path
- developers can see which service took time and where a problem occurred

For interview answer:
> “Zipkin helps us trace one user request across multiple Spring Boot services, so we can debug performance and dependency issues easily.”

---

## 10. Database design and storage
There are separate databases for different services:
- `auth_db`
- `asset_db`
- `admin_db`

This is a very important microservice principle:
- each service owns its own database
- no service should directly depend on another service's database

This gives:
- better independence
- easier scaling
- less coupling
- easier maintenance

The `init.sql` script creates database schemas and initialization data.

---

## 11. Security model
The system uses JWT-based security.

Flow:
1. User logs in
2. Auth service returns JWT
3. JWT is sent in Authorization header or cookie
4. Asset service and Admin service validate the token before processing request
5. Roles like ADMIN, EDITOR, USER decide what the user can access

Security configuration is defined in Spring Security classes and application properties.

This keeps the system secure without directly sharing user credentials across services.

---

## 12. How configuration is managed
There are multiple configuration layers in this project.

### Local Spring config
Each service has `application.properties` with settings such as:
- service port
- JWT secret
- database URL
- RabbitMQ host
- Zipkin endpoint
- Eureka config
- Resilience4J config

### Docker config
`docker-compose.yml` provides environment values for service-to-service communication.

Examples:
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE`
- `MANAGEMENT_ZIPKIN_TRACING_ENDPOINT`

### Kubernetes config
The `k8s/*.yaml` files inject env variables into pods and expose services.

This makes the system portable:
- same app can run in local Docker
- same app can run in cluster-managed environment

In interviewer terms:
> “Configuration is spread across application.properties, Docker Compose, and Kubernetes manifests so the services can run in different environments without changing the code logic.”

---

## 13. Why this is a microservices architecture
This project qualifies as microservices because:
- different functions are separated by service
- each service has its own app and database
- communication is network-based
- services can be developed and deployed independently
- it uses service discovery, config management, tracing, and messaging components

It is not a full-blown enterprise microservice system, but it is a good practical example of the pattern.

---

## 14. Simple interview answer (very short version)
> “This project is an asset management platform built using Spring Boot microservices. The auth service handles login and JWT, the asset service manages uploads and file metadata, and the admin service controls platform-wide management. Services communicate mostly through REST APIs using Feign and RestTemplate, while RabbitMQ is configured for asynchronous messaging and Zipkin is used for tracing distributed requests. Docker runs each service in isolated containers, and Kubernetes manages deployment and scaling in a cluster. PostgreSQL is used per service for data isolation, and the whole system is connected through a common network and service discovery setup.”

---

## 15. Final takeaway
This project demonstrates real-world backend engineering concepts:
- microservices
- JWT security
- REST communication
- service discovery
- Docker deployment
- Kubernetes orchestration
- message broker integration
- distributed tracing
- database separation
- environment-based configuration

That is exactly why it is a strong project for technical interviews.
