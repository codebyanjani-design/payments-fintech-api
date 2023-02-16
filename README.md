# 💳 Payments & FinTech API (2023)

> **Career Context:** Built in 2023 as a Senior Software Engineer. This project tackles some of the hardest computer science problems encountered in the industry: high concurrency, transactional integrity, data races, and distributed systems architecture within a highly regulated financial domain.

## 📖 Executive Summary
The Payments & FinTech API is a mission-critical, high-performance Java Spring Boot backend designed for secure digital wallet management and payment processing. In the financial sector, a single data race can result in double-spending, leading to catastrophic monetary loss. This system is engineered specifically to prevent these anomalies using advanced database locking mechanisms, robust Spring `@Transactional` management, and idempotent design patterns.

## 🏗️ System Architecture

```mermaid
graph LR
    User[Mobile App / Web Client] -->|REST / JSON| Controller[Spring REST Controllers]
    Controller -->|DTO Validation| Service[Transaction Business Service]
    Service -->|@Transactional Boundaries| JPA[Spring Data JPA Layer]
    
    subgraph Database Concurrency Control
        JPA -->|PESSIMISTIC_WRITE Lock| DB[(PostgreSQL Database)]
        DB -.->|Row Locked| DB
    end
    
    Service -.->|Simulated HTTP Callback| PaymentGateway[External Payment Gateway Integration]
```

## ✨ Core Features & Functionality
* **JPA Pessimistic Locking:** Implements strict database-level row locks (`@Lock(LockModeType.PESSIMISTIC_WRITE)`) to serialize access to digital wallets during concurrent transfer requests, absolutely eliminating the risk of double-spending.
* **Deadlock Prevention:** Complex transfer workflows mathematically sort wallet IDs before acquiring locks to ensure consistent lock acquisition ordering, preventing the system from freezing due to circular database deadlocks.
* **Idempotent Transactions:** Every financial transaction requires a unique `referenceId`. The system checks this ID to ensure that if a client accidentally sends the same payment request twice (e.g., due to a network timeout retry), the system will safely ignore the duplicate.
* **Cursor-Based Pagination:** Financial histories grow infinitely. Standard offset pagination (`LIMIT X OFFSET Y`) becomes dangerously slow on massive tables. This API implements cursor-based pagination utilizing indexed `createdAt` timestamps to maintain lightning-fast query times regardless of table size.
* **Global Exception Management:** A robust `@ControllerAdvice` handles edge cases like `InsufficientFundsException` and database lock timeouts, translating them into elegant, standardized API error responses.

## 🧠 Design Decisions & Trade-offs
### Java & Spring Boot
Java Spring Boot remains the undisputed industry standard for financial systems. It was chosen for its unparalleled maturity, extreme type safety, robust multi-threading capabilities, and its declarative `@Transactional` management, which allows developers to easily rollback complex operations if a failure occurs halfway through.

### Pessimistic Locking vs. Optimistic Locking
**The Dilemma:** When two users try to withdraw funds from the exact same wallet at the exact same millisecond, a race condition occurs. 
- **Optimistic Locking** (using a `@Version` column) allows both to read the balance, but throws an exception on the second save. This requires the application to implement complex retry logic. 
- **Pessimistic Locking** tells the database to physically lock the row until the first transaction is entirely complete. 
**The Decision:** Given the high stakes of financial data, Pessimistic Locking was chosen. While it introduces slight latency bottlenecks on highly active individual wallets, it provides absolute, unshakeable guarantees against data corruption and double-spending without needing application-level retry loops.

## 📂 Project Structure
```text
payments-fintech-api/
├── src/main/java/com/fintech/
│   ├── Application.java          # Spring Boot Main Entry Point
│   ├── config/                   # Global Configurations
│   ├── controller/               # REST API endpoints
│   ├── domain/                   # JPA Entity Models (Wallet, Transaction)
│   ├── dto/                      # Data Transfer Objects & Validation
│   ├── exception/                # @ControllerAdvice and custom Exception classes
│   ├── repository/               # Spring Data JPA Interfaces
│   └── service/                  # Transactional Business Logic
├── src/main/resources/
│   └── application.yml           # Database and application properties
└── pom.xml                       # Maven dependency management
```

## 🚀 Setup & Installation

### Prerequisites
- **Java Development Kit (JDK)** 17 or higher
- **Maven** (3.6+ or higher)

### Local Development
1. **Clone the repository:**
   ```bash
   git clone https://github.com/codebyanjani-design/payments-fintech-api.git
   cd payments-fintech-api
   ```
2. **Build the project:**
   ```bash
   mvn clean install
   # This will download dependencies and compile the Java source files
   ```
3. **Run the Spring Boot application:**
   ```bash
   mvn spring-boot:run
   ```
4. The embedded Tomcat server will start up on `http://localhost:8080`. The application utilizes an embedded H2 database by default for frictionless local testing.

## 🧪 Testing Strategy
- **Concurrency Testing:** Custom integration tests utilizing `ExecutorService` and `CountDownLatch` are designed to hammer the transfer endpoint with dozens of simultaneous threads to mathematically prove that the pessimistic locks successfully serialize the requests and prevent balance anomalies.
- **Unit Testing:** Business services are heavily tested using JUnit 5 and Mockito to simulate database interactions and external gateway failures.

## ⚙️ CI/CD Deployment Pipeline
The repository features a Maven-based GitHub Actions CI pipeline (`.github/workflows/ci.yml`). On every pull request, the pipeline automatically spins up a Linux runner, provisions JDK 17, downloads Maven dependencies, compiles the source, runs the rigorous concurrency test suite, and packages the application into a deployable `.jar` artifact.

