# SpendSmart - Auth Service 🔐

The **Auth Service** is the foundational identity and security microservice for the SpendSmart Expense Tracker platform. It handles user registration, authentication, token lifecycle management, and Role-Based Access Control (RBAC). 

This service is built with enterprise best practices in mind, featuring strict data isolation, IDOR vulnerability prevention, stateless JWT authentication, and comprehensive unit testing.

## 🚀 Tech Stack

* **Java:** 25
* **Framework:** Spring Boot 4.0.5
* **Security:** Spring Security, JSON Web Tokens (JWT - `jjwt` 0.11.5), BCrypt Password Hashing
* **Persistence:** Spring Data JPA, Hibernate, MySQL 8
* **Testing:** JUnit 5, Mockito
* **Tooling:** Lombok, SLF4J (Logging), Maven

## ✨ Key Features

* **Stateless Authentication:** Uses JWT for secure, scalable authentication without server-side sessions.
* **Role-Based Access Control (RBAC):** Supports `USER` and `ADMIN` roles for granular endpoint protection.
* **Security First:** * Prevents Insecure Direct Object Reference (IDOR) vulnerabilities by validating JWT identity against path variables.
  * Raw passwords never touch the database; hashed using BCrypt (Strength 10).
* **Soft Deletion:** Account deactivation flags users as inactive (`isActive = false`) to preserve historical financial data integrity.
* **Cross-Origin Resource Sharing (CORS):** Configured to accept requests securely from the Angular frontend (`http://localhost:4200`).
* **Traceability:** Implements structured SLF4J logging for monitoring authentication flows and exception handling.

## 🛠️ Prerequisites

* Java Development Kit (JDK) 25 or higher
* MySQL Server 8.x
* Maven 3.8+
* Postman (for API testing)

## ⚙️ Setup & Installation

### 1. Database Configuration
Log into your MySQL instance and create the database schema:

```sql
CREATE DATABASE spendsmart_auth;