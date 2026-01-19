# Tahvago API - Security & Authentication Documentation

This repository contains a Spring Boot 17 backend implementation focusing on secure user lifecycles, including registration, email verification, JWT authentication, and brute-force protection.

---

## 🚀 Getting Started

### Prerequisites
* **Java 17**
* **Maven**
* **Database** (PostgreSQL/MySQL/H2), ## currently using MySQL, if you want to use other database, then update your dependencies accordingly

### Installation
1. Clone the repository.
2. Configure your database and JWT secrets in `src/main/resources/application.properties`.
3. Run the application:
   ```bash
   ./mvnw spring-boot:run
