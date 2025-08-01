# 🏦 Account Service

The **Account Service** is a core microservice in a banking ecosystem responsible for customer account management and financial operations like Fixed Deposits (FDs), Recurring Deposits, and Loan handling.

It exposes RESTful APIs for internal/external communication and interacts with other services via **Kafka**, **Feign Clients**, and **RestTemplate**.

---

## 📌 Key Features

- ✅ Create New Accounts (Savings, Current, Recurring)
- 👨‍💼 Add Employee
- 🚀 Activate Account
- 💰 Book Fixed Deposit (FD)
- ❌ Close Fixed Deposit (FD)
- 💴 FD Listing by Account
- 💵 Apply for Loan
- 📊 Check Account Balance
- 📈 Add Loan Interest Rate
- 📱 Calculate Recurring Account Interest
- 📃 Get Pending Activation Accounts
- 🏠 Add New Branch
- 🏛️ Fetch Branch Details
- 📋 Get Account Details

---

## 🛠️ Technology Stack

| Component       | Technology Used                    |
|----------------|-------------------------------------|
| Language        | Java (Spring Boot)                 |
| Communication   | Kafka, Feign Client, RestTemplate  |
| Database        | PostgreSQL                         |
| API Style       | RESTful (OpenAPI 3.1.0 + Swagger)  |

---

## 🔌 Communication

### Kafka
- Asynchronous event handling for account events (e.g., new account, loan initiation, FD updates).

### Feign Client
- Used for declarative HTTP communication with services like **Transaction Service**.

### RestTemplate
- Synchronous calls to external/internal APIs where Feign is not used.

---

## 🧱 Database Design (PostgreSQL)

The following entities are persisted:

- `AccountUser` – Holds user identity and account preferences
- `AccountDetails` – Core account information
- `Branch` – Bank branch details
- `Employee` – Employee metadata
- `FD` – Fixed deposit entries
- `RecurringAccount` – Recurring deposit info
- `Loan` – Loan applications
- `Interest` – Interest rates for loan

---

## 📮 REST API Endpoints

Base URL: `http://localhost:8085/api/v1/account/user`

| Method | Endpoint                                 | Description                            |
|--------|------------------------------------------|----------------------------------------|
| POST   | `/create-account`                        | Create a new account                   |
| PUT    | `/active-account/{uid}`                  | Activate user account                  |
| POST   | `/start-fd`                              | Book a new FD                          |
| PUT    | `/close-fd`                              | Close an existing FD                   |
| GET    | `/my-fds/{account}`                      | Get all FDs by account number          |
| POST   | `/create-loan-account`                   | Apply for a loan                       |
| POST   | `/recurring-account-calculator`          | Calculate recurring deposit interest   |
| GET    | `/pending-active-account`                | Get list of pending activation accounts|
| POST   | `/add-interst-rate`                      | Add new interest rate for loans/FDs    |
| GET    | `/check-balance`                         | Check balance of an account            |
| POST   | `/add-employee`                          | Add a new employee                     |
| POST   | `/add-branch`                            | Register a new branch                  |
| GET    | `/get-branch-name/{uid}`                 | Fetch branch name by user ID          |
| GET    | `/get-account-details/{account}`         | Fetch detailed account info            |

---


### Example DTOs

- `AccountUserDto`: For creating accounts
- `NewLoanDTO`: For applying for a loan
- `newFDdto`: For booking an FD
- `FdWithdraw`: For closing an FD
- `InterestRateDTO`: For interest rate management
- `EmployeeDTO`: For adding employees
- `BranchDTO`: For adding a branch
- `RecurringAccountCalculator`: For interest calculation
- `BalanceCheck`: For balance inquiry
- `ResponseDTO`: Standard response format

---

## 🔐 Security

Most endpoints require an `Authorization` header (e.g., JWT token) for access:
```http
Authorization: Bearer <token>
