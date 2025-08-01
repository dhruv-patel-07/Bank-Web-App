# Bank-Web-App: API Gateway Service

## Table of Contents

1. [Introduction](#1-introduction)  
2. [Tech Stack](#2-tech-stack)  
3. [Filter Details](#3-filter-details)  
   - [Security Configuration](#311-security-config)  
   - [Keycloak Role Converter](#312-keycloak-role-converter)  
   - [JWT Decoder Configuration](#313-jwtdecoderconfig)  
   - [Token Refresh Filter](#314-tokenrefreshfilter)  

---

## 1. Introduction

### API Gateway Overview

The **API Gateway** is the single-entry point for all client requests in a microservices architecture. It is responsible for request routing, authentication, authorization, and more.

#### Key Features in Local Setup

- **Routing**: Forwards requests to local microservices based on predefined paths (e.g., `/auth/**`, `/notification/**`, `/download/**`).
- **Centralized Access Point**: Clients can interact with multiple backend services via a single host and port.
- **Local Debugging**: Facilitates end-to-end testing and logging for efficient debugging.

---

## 2. Tech Stack

| Technology    | Version  |
|---------------|----------|
| JDK           | 17       |
| Maven         | 3.6.3    |
| Spring Boot   | 3.5.3    |
| Kafka         | 7.4.3    |
| Zookeeper     | 7.4.3    |
| Zipkin        | 2.23     |

---

## 3. Filter Details

### 3.1.1 Security Config

The `SecurityConfig` class secures the API Gateway using **Spring WebFlux Security** and **OAuth2 JWT** authentication.

#### Responsibilities:

- **Token-based Authentication**: Validates incoming JWT tokens from Keycloak using a custom `KeycloakRoleConverter`.
- **Public Route Configuration**: Allows public access to specific endpoints like authentication and health checks.
- **Role-Based Access Control (RBAC)**:
  - `ROLE_service`: For service-level operations (e.g., balance retrieval).
  - `ROLE_employee`: For employee-level access (e.g., freezing accounts).
  - `ROLE_admin`: For admin features (e.g., adding branches).
- **CSRF Disabled**: As it's stateless and RESTful, CSRF protection is turned off.

---

### 3.1.2 Keycloak Role Converter

The `KeycloakRoleConverter` maps Keycloak roles from a JWT token into Spring Security roles.

#### Key Actions:

- Extracts roles from the `realm_access.roles` claim in the JWT.
- Converts roles into the format `ROLE_<role>` (e.g., `admin` → `ROLE_admin`).

#### Example:

**JWT Claim**:
```json
"realm_access": {
  "roles": ["admin", "employee"]
}
