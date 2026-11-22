# Microservices Project - Resume Entry

Based on the actual implementation and recent architectural updates (removing Eureka in favor of Kubernetes, adding Zipkin, etc.), here is the updated project description for your resume.

## Project Title
**Microservices-based Asset Management Platform**

## Technologies & Tools
**Java 21 | Spring Boot 3 | Spring Cloud OpenFeign | React (Vite) | PostgreSQL | Docker | Kubernetes | RabbitMQ | Zipkin**

## Bullet Points (Action-Oriented)

*   **Microservices Architecture**: Architected and developed a scalable microservices system (Auth, Admin, Asset services) using **Spring Boot 3** and **Java 21**, ensuring modularity and maintainability.
*   **Security & Identity**: Implemented robust security with **Spring Security 6** and **JWT (JSON Web Tokens)**, designing custom filters for detailed Role-Based Access Control (RBAC) across the distributed system.
*   **Kubernetes-Native Deployment**: Containerized services using **Docker** and orchestrated deployment on **Kubernetes (Minikube)**, leveraging K8s-native Service Discovery and ConfigMaps to replace legacy Eureka/Config Server patterns for improved resource efficiency.
*   **Inter-Service Communication**: Established synchronous communication using **Spring Cloud OpenFeign** and configured **RabbitMQ** for asynchronous messaging, decoupling core services for better fault tolerance.
*   **Observability & Monitoring**: Integrated **Zipkin** for distributed tracing and **Micrometer** for metrics, enabling real-time latency monitoring and request tracking across the microservices mesh.
*   **Full-Stack Implementation**: Built a responsive Single Page Application (SPA) dashboard using **React and Vite**, consuming RESTful APIs to provide real-time asset management and active user monitoring capabilities.

## Key Highlights (for "Key Skills" or details section)
*   Migrated from Netflix Eureka to Kubernetes-native discovery.
*   Optimized backend performance by isolating admin operations.
*   Implemented centralized error handling and API gateway patterns via Kubernetes Services.
