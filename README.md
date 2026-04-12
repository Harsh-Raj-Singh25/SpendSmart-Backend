# SpendSmart - Income Service

## Overview
The Income Service is a core domain microservice within the SpendSmart ecosystem. It handles all inbound cash flow operations, allowing users to log salaries, freelance payments, and investments. It features built-in recurrence tracking to power future forecasting and analytics.

This service operates completely independently of the Expense and Auth services, maintaining a decoupled architecture via the API Gateway.

## Tech Stack
* **Java:** 21 (LTS)
* **Framework:** Spring Boot 3.2.4
* **Database:** MySQL 8 / Spring Data JPA
* **Tools:** Lombok, Maven

## Architecture & Communication
* **Port:** Runs internally on `8083`.
* **Gateway Routing:** All external traffic is intercepted by the API Gateway and routed via `http://localhost:8080/incomes/**`.
* **Database Strategy:** Uses a dedicated schema (`spendsmart_income`). Uses `BigDecimal` for all monetary values to prevent IEEE 754 floating-point precision loss.

## Key Endpoints

| HTTP Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/incomes` | Log a new income entry |
| `GET` | `/incomes/{incomeId}` | Fetch specific income details |
| `GET` | `/incomes/user/{userId}` | Get all income for a specific user |
| `GET` | `/incomes/user/{userId}/source?source=SALARY` | Filter income by source Enum |
| `GET` | `/incomes/user/{userId}/dateRange` | Filter income between two dates |
| `GET` | `/incomes/user/{userId}/month` | Get user income for a specific Year & Month |
| `GET` | `/incomes/user/{userId}/recurring` | Fetch only recurring income streams |
| `PUT` | `/incomes/{incomeId}` | Update an existing income entry |
| `DELETE`| `/incomes/{incomeId}` | Delete an income entry |
| `GET` | `/incomes/user/{userId}/total` | Get total all-time income |
| `GET` | `/incomes/user/{userId}/month/total` | Get aggregated income for a specific month |

## Database Configuration
To run this service locally, configure your `application.yml` with your MySQL credentials:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/spendsmart_income?createDatabaseIfNotExist=true
    username: root
    password: <your_password>
