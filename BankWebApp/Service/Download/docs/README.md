# Bank Web App - Download Service

![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.3-brightgreen)
![Redis](https://img.shields.io/badge/Redis-8.0.3-red)

A secure service for downloading and emailing bank statements with rate limiting functionality.

## Table of Contents
1. [Introduction](#1-introduction)
2. [Tech Stack](#2-tech-stack)
3. [Redis Implementation](#3-redis-implementation)
4. [REST API Endpoints](#4-rest-api-endpoints)
5. [Project Structure](#5-project-structure)

## 1. Introduction
This service allows users to securely download their bank statements or have them emailed. The service generates downloadable PDF files or sends statements directly to the user's email address.

### Features
- Download bank statements within a specified time range
- Email bank statements within a specified time range
- Rate limiting to prevent abuse (4 actions per day per user)

## 2. Tech Stack

| Technology       | Version   |
|------------------|-----------|
| JDK              | 17        |
| Maven            | 3.6.3     |
| Spring Boot      | 3.5.3     |
| Kafka            | 7.4.3     |
| Zookeeper        | 7.4.3     |
| OpenPDF          | 2.0.4     |
| Redis            | 8.0.3     |
| Zipkin           | 2.23      |

## 3. Redis Implementation
Redis is used for rate limiting functionality:

- **Storage Format**: `<String, Integer>`
- **Key Format**: `rate_limit:<UID>`  
  Example: `rate_limit:aefcfdtrev-rebvsddasdsdf-sdfsf`
- **Value**: Integer representing the number of actions performed today
- **Expiration**: Keys expire at midnight UTC to reset daily usage

## 4. REST API Endpoints

### Base URL
`http://127.0.0.1:8222/api/v1/download/`

### 4.1 Download Transaction Report
**Endpoint**: `GET /api/v1/download/download-transaction-report`

#### Parameters:
| Name        | Type     | Description                              | Example        |
|-------------|----------|------------------------------------------|----------------|
| account     | String   | Account number                           | 1234567890     |
| startDate   | String   | Start date (YYYY-MM-DD, optional)        | 2025-01-01    |
| endDate     | String   | End date (YYYY-MM-DD, optional)          | 2025-01-31    |
| month       | String   | Specific month (YYYY-MM, optional)       | 2025-01       |
| download    | String   | "yes" to download, "no" to email         | yes/no         |

#### Headers:
- `Authorization`: Bearer token (required)

#### Usage Limits:
- 4 combined actions per day (downloads + emails)

## 5. Project Structure

download/
├── src/
│ ├── main/
│ │ ├── java/
│ │ │ └── com/
│ │ │ └── bank/
│ │ │ └── web/
│ │ │ └── app/
│ │ │ └── download/
│ │ │ ├── config/
│ │ │ ├── controller/
│ │ │ ├── redis/
│ │ │ ├── service/
│ │ │ ├── dto/
│ │ │ ├── kafka/
│ │ │ ├── exception/
│ │ │ └── DownloadApplication.java
│ │ └── resources/
│ │ ├── application.yml
│ │ ├── static/
│ │ └── templates/
├── test/
│ └── java/
│ └── com/
│ └── bank/
│ └── web/
│ └── app/
│ └── download/


## Getting Started

1. Clone the repository
2. Configure Redis and Kafka
3. Update application.yml with your configurations
4. Build with Maven: `mvn clean install`
5. Run the application: `java -jar target/download-service.jar`