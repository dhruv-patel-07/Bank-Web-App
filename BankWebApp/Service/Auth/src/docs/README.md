# 🏦 Bank-Web-App: Auth-Service 🔐

---

## 📋 Table of Contents

1. [Introduction](#introduction)  
2. [Tech Stack](#tech-stack)  
3. [Data Dictionary](#data-dictionary)  
4. [REST API Endpoints](#rest-api-endpoints)  
5. [Resilience4j Configuration](#resilience4j-configuration)  
6. [Project Structure](#project-structure)  

---

## Introduction 🚀✨

The **Auth Service** is a core microservice responsible for user registration, login, and verification within the banking ecosystem. It provides secure and reliable authentication APIs and interacts with other services via Kafka and RestTemplate.

**Key Features:**  
- 📝 User Registration  
- 📧 Email Verification  
- 🔑 Secure Login  

---

## Tech Stack 🛠️💻

- ☕ JDK 17  
- 📦 Maven 3.6.3  
- 🌱 Spring Boot 3.5.3  
- 🐘 PostgreSQL 17.5  
- 🦜 Kafka 7.4.3  
- 🐝 Zookeeper 7.4.3  
- 📊 Zipkin 2.23  
- 🔐 Keycloak 24.0.3  

---

## Data Dictionary 📚

- **Database:** `users`  
- **UserVerification Table:** Stores email verification data including user ID, email, token, verification status, and timestamps.

---

## REST API Endpoints 🔗

Base URL: `http://localhost:8222/api/v1/auth`

| Endpoint                    | Method | Description                          | URL Example                                         |
|-----------------------------|--------|------------------------------------|----------------------------------------------------|
| 📝 Register User             | POST   | Register a new user with validation| `http://localhost:8222/api/v1/auth/register`       |
| 🔑 Login User                | POST   | Authenticate user and return token | `http://localhost:8222/api/v1/auth/login`          |
| ✅ Verify User Email         | GET    | Verify user account by ID and token| `http://localhost:8222/api/v1/auth/verify/{id}/{token}` |
| 💓 Health Check              | GET    | Check service status                | `http://localhost:8222/api/v1/auth/heartbeat`      |

---

## Resilience4j Configuration ⚙️

- **Circuit Breaker:** Protects the system from cascading failures with configurable thresholds.  
- **Rate Limiter:** Limits the number of requests over time to prevent overload.

---

## Project Structure 📂

