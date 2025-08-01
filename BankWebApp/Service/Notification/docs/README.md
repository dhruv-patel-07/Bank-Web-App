# Bank Web App - Notification Service

![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.3-brightgreen)
![Kafka](https://img.shields.io/badge/Kafka-7.4.3-orange)

A centralized email notification service for the banking application ecosystem, designed for asynchronous and decoupled communication.

## Table of Contents
1. [Introduction](#1-introduction)
2. [Tech Stack](#2-tech-stack)
3. [Features](#3-features)
4. [Integration](#4-integration)
5. [Project Structure](#5-project-structure)

## 1. Introduction
The Notification Microservice handles all email delivery across the application ecosystem. Designed as an independent service, it can be integrated with various microservices to send:

- Transactional emails
- Informational notifications
- Alert-based communications

### Key Benefits
- **Consistent email formatting** across all services
- **Scalable architecture** with asynchronous processing
- **Decoupled design** - services trigger notifications without email logic
- **Centralized management** of all email templates and delivery

### Common Use Cases
- Bank statement delivery
- Account activity alerts
- Password reset emails
- Verification emails
- Security notifications

## 2. Tech Stack

| Technology            | Version   |
|-----------------------|-----------|
| JDK                   | 17        |
| Maven                 | 3.6.3     |
| Spring Boot           | 3.5.3     |
| Kafka                 | 7.4.3     |
| Zookeeper             | 7.4.3     |
| Zipkin                | 2.23      |
| Spring Mail Starter   | (included in Spring Boot) |
| Thymeleaf Template Engine | (included in Spring Boot) |

## 3. Features

### Core Functionality
- Asynchronous email processing via Kafka
- Template-based email generation
- Retry mechanism for failed deliveries
- Email queue management

### Email Types Supported
- HTML-formatted emails
- Text-based emails
- Template-driven emails with dynamic content
- Attachments support

## 4. Integration

### Integration Methods
1. **Kafka Message Queue** (Recommended)
   - Services publish notification events to Kafka topics
   - Notification service consumes and processes these events

2. **REST API Endpoints**
   - Direct HTTP calls to notification endpoints
   - Suitable for synchronous notifications

### Sample Kafka Event Structure
```json
{
  "eventType": "STATEMENT_READY",
  "recipientEmail": "customer@example.com",
  "subject": "Your Monthly Statement",
  "templateName": "statement-email",
  "templateVariables": {
    "customerName": "John Doe",
    "accountNumber": "XXXXXX7890",
    "statementPeriod": "January 2025"
  }
}

```
<pre markdown="1">
```
notification-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── bank/
│   │   │           └── web/
│   │   │               └── app/
│   │   │                   └── notification/
│   │   │                       ├── config/
│   │   │                       ├── controller/
│   │   │                       ├── service/
│   │   │                       ├── dto/
│   │   │                       ├── kafka/
│   │   │                       ├── exception/
│   │   │                       ├── model/
│   │   │                       └── NotificationApplication.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── templates/        # Email templates
│   │       └── static/
├── test/
│   └── java/
│       └── com/
│           └── bank/
│               └── web/
│                   └── app/
│                       └── notification/

```
</pre>