# SpendSmart - Expense Service

## Overview
The Expense Service is a core microservice within the SpendSmart ecosystem. It is responsible for managing all financial transactions, including logging expenses, handling splits, and providing aggregated financial data for users and categories.

This service is fully decoupled from the Authentication and User management layers and relies entirely on JWT tokens processed by the API Gateway for request validation.

## Tech Stack
* **Java:** 21 (LTS)
* **Framework:** Spring Boot 3.2.4
* **Database:** MySQL 8 / Spring Data JPA
* **Tools:** Lombok, Maven

## Architecture & Communication
* **Port:** Runs internally on `8082`.
* **Gateway Routing:** All external traffic should be routed through the API Gateway (`http://localhost:8080/expenses/**`).
* **Database Strategy:** Uses a dedicated schema (`spendsmart_expense`) to maintain microservice data isolation. User references are maintained via loose coupling (`userId` as an integer rather than a strict Foreign Key constraint to the Auth database).

## Key Endpoints

| HTTP Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/expenses` | Log a new expense |
| `GET` | `/expenses/{expenseId}` | Fetch specific expense details |
| `GET` | `/expenses/user/{userId}` | Get all expenses for a specific user |
| `GET` | `/expenses/category/{categoryId}` | Get all expenses under a specific category |
| `GET` | `/expenses/user/{userId}/dateRange` | Filter user expenses between two dates |
| `GET` | `/expenses/user/{userId}/month` | Get user expenses for a specific Year & Month |
| `GET` | `/expenses/user/{userId}/search` | Keyword search across title and notes |
| `PUT` | `/expenses/{expenseId}` | Update an existing expense |
| `DELETE`| `/expenses/{expenseId}` | Delete an expense |
| `GET` | `/expenses/user/{userId}/total` | Get the aggregated total spent by a user |

## Database Configuration
To run this service locally, ensure your MySQL instance is running and update the `application.yml` file with your local database credentials:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/spendsmart_expense?createDatabaseIfNotExist=true
    username: root
    password: <your_password>