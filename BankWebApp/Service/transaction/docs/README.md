# Bank Web App - Transaction Service

![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.3-brightgreen)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17.5-blue)
![Kafka](https://img.shields.io/badge/Kafka-7.4.3-orange)

Core banking service handling all financial transactions including deposits, transfers, EMI processing, and reporting.

## Table of Contents
1. [Introduction](#1-introduction)
2. [Tech Stack](#2-tech-stack)
3. [Data Dictionary](#3-data-dictionary)
4. [API Endpoints](#4-api-endpoints)
5. [Resilience Configuration](#5-resilience-configuration)
6. [Project Structure](#6-project-structure)
7. [Getting Started](#7-getting-started)

## 1. Introduction
The Transaction Service is the financial backbone of the banking application, processing:

- **Core Transactions**: Deposits, transfers, balance checks
- **Automated Processes**: EMI deductions, recurring payments
- **Financial Products**: Fixed deposit booking
- **Administration**: Account freezing, branch reporting
- **Customer Tools**: Statement generation, transaction history

### Key Features
- Real-time transaction processing
- Integration with Elasticsearch for admin search
- Scheduled EMI deductions
- PDF statement generation
- Circuit breaking and rate limiting
- Comprehensive branch reporting

## 2. Tech Stack

| Technology | Version |
|------------|---------|
| JDK | 17 |
| Spring Boot | 3.5.3 |
| PostgreSQL | 17.5 |
| Kafka | 7.4.3 |
| Elasticsearch | 8.13.4 |
| Kibana | 8.13.4 |
| Resilience4j | 2.1.0 |
| OpenPDF | 2.0.4 |

## 3. Data Dictionary

### 3.1 Core Tables

#### Account
| Field | Type | Description |
|-------|------|-------------|
| accountNum | Long | Primary account identifier |
| uid | String | Linked user ID |
| balance | Double | Current available balance |
| isFreeze | Boolean | Freeze status flag |

#### Transaction
| Field | Type | Description |
|-------|------|-------------|
| tId | Long | Transaction ID |
| transactionType | String | Debit/Credit/EMI |
| amount | Double | Transaction amount |
| affected_balance | Double | Post-transaction balance |

#### LoanPayment (EMI Tracking)
| Field | Type | Description |
|-------|------|-------------|
| emiAmount | Double | Fixed monthly installment |
| dueAmount | Double | Outstanding payment |

## 4. API Endpoints

### Base URL: `http://localhost:8222/api/v1/transaction`

### 4.1 Core Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/deposit` | POST | Fund deposit to account |
| `/transfer` | POST | Inter-account transfers |
| `/check-balance` | POST | Account balance check |
| `/emi-deduct` | POST | Automated EMI processing |

### 4.2 Administrative Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/admin/freeze` | PUT | Freeze/unfreeze accounts |
| `/admin/search/{keyword}` | GET | Elasticsearch-powered search |
| `/admin/branch-report/{branch}` | GET | Branch-level transaction analytics |

### 4.3 Sample Requests

**Deposit Example:**
```bash
curl -X POST \
  http://localhost:8222/api/v1/transaction/deposit \
  -H 'Authorization: Bearer {token}' \
  -d '{
    "accountNumber": 234197540589,
    "amount": 400.00,
    "method": "neft"
  }'
```

## Rate Limiter
<pre markdown="1">

```
resilience4j.ratelimiter:
  instances:
    transactionApi:
      limitForPeriod: 10
      limitRefreshPeriod: 5s
      timeoutDuration: 1s
```

</pre>


## 6. Project Structure
<pre markdown="1">

transaction-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── bank/
│   │   │           └── web/
│   │   │               └── transaction/
│   │   │                   ├── config/
│   │   │                   ├── controller/
│   │   │                   ├── service/
│   │   │                   ├── repository/
│   │   │                   ├── model/
│   │   │                   ├── dto/
│   │   │                   ├── kafka/
│   │   │                   ├── pdf/
│   │   │                   └── TransactionApplication.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── templates/
├── test/
│   └── integration/
│       └── com/
│           └── bank/
│               └── web/
│                   └── transaction/


</pre>