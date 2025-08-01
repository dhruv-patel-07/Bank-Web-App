# Bank Web App - Schedule Service

![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.3-brightgreen)
![Kafka](https://img.shields.io/badge/Kafka-7.4.3-orange)

Automated scheduling service for loan payments, reminders, and transaction notifications in the banking ecosystem.

## Table of Contents
1. [Introduction](#1-introduction)
2. [Tech Stack](#2-tech-stack)
3. [Data Dictionary](#3-data-dictionary)
4. [Schedules](#4-schedules)
5. [Project Structure](#5-project-structure)
6. [Getting Started](#6-getting-started)

## 1. Introduction
The Schedule Service automates critical monthly financial tasks including:
- EMI deductions
- Payment reminders
- Transaction notifications

### Key Features
- **Automated EMI Processing**: Initiates loan payments via Transaction Service
- **Smart Reminders**: Sends payment reminders via Notification Service
- **Staggered Notifications**: Distributes email load across user groups
- **Scheduled Operations**: Cron-based task execution

### Service Dependencies
| Service | Communication Method | Purpose |
|---------|----------------------|---------|
| Transaction Service | FeignClient/RestTemplate | Initiate payments |
| Notification Service | Kafka | Send email/SMS reminders |

## 2. Tech Stack

| Technology | Version |
|------------|---------|
| JDK | 17 |
| Spring Boot | 3.5.3 |
| Kafka | 7.4.3 |
| MongoDB | [Version] |
| Spring Scheduler | (included in Spring Boot) |

## 3. Data Dictionary

### 3.1 Collections

#### accounts
| Field | Type | Description | Example |
|-------|------|-------------|---------|
| id | String | MongoDB unique ID | "64e41f70b1f3c2553e123abc" |
| accountNum | Long | Account number | 100002345678 |
| branchId | String | Branch identifier | "BR001" |
| type | String | Account type | "loan" |

#### loan_payment
| Field | Type | Description | Example |
|-------|------|-------------|---------|
| loanId | Long | Loan identifier | 1234567890 |
| paymentId | Long | Payment reference | 9876543210 |
| scheduleDate | String | Payment due date (YYYY-MM-DD) | "2025-08-01" |
| emi | Double | Monthly installment | 15000.00 |
| isReminderSend | Boolean | Reminder status | true |

## 4. Schedules

### 4.1 Core Schedules

#### EMI Deduction
```properties
Schedule: Daily at midnight (00:00)
Cron: 0 0 0 * * *
Action: Deducts EMI via Transaction Service
Scope: All active loans

```
## Payment Reminders
Schedule: Daily at 2 PM (14:00)
Cron: 0 0 14 * * *
Action: Sends reminders 2 days before due date
Channel: Email/SMS via Notification Service

<pre markdown="1">
```
schedule-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── bank/
│   │   │           └── web/
│   │   │               └── schedule/
│   │   │                   ├── config/
│   │   │                   ├── service/
│   │   │                   │   ├── impl/
│   │   │                   │   └── scheduler/
│   │   │                   ├── repository/
│   │   │                   ├── model/
│   │   │                   ├── dto/
│   │   │                   ├── kafka/
│   │   │                   └── ScheduleApplication.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── templates/
├── test/
│   └── integration/
│       └── com/
│           └── bank/
│               └── web/
│                   └── schedule/
```
<pre>