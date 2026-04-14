# SpendSmart - Recurring Transaction Service

## Overview
The Recurring Transaction Service is the automation engine of the SpendSmart platform. It allows users to set up "set-and-forget" financial rules (subscriptions, rent, salaries). Utilizing Spring's `@Scheduled` tasks and OpenFeign, it wakes up daily to autonomously generate records in the Expense and Income services without requiring user interaction.

## Tech Stack
* **Java:** 21 (LTS)
* **Framework:** Spring Boot 3.2.4
* **Inter-Service Communication:** Spring Cloud OpenFeign
* **Database:** MySQL 8 / Spring Data JPA
* **Server:** Tomcat (Port 8087)

## Core Capabilities
* **Background Automation:** A `@Scheduled` cron job runs automatically at midnight (00:00) server time to evaluate all active recurring rules.
* **Smart Date Math:** Automatically calculates the `nextDueDate` based on the selected frequency (Daily, Weekly, Monthly, Quarterly, Yearly), gracefully handling leap years and end-of-month edge cases via Java's `LocalDate` API.
* **Auto-Deactivation:** Rules containing an optional `endDate` automatically deactivate themselves once the final billing cycle is completed.
* **Cross-Service Delegation:** Uses generic DTOs over OpenFeign to route generated transactions to either the Income or Expense microservice based on the `TransactionType` enum.

## API Specification

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/recurring` | Create a new recurring transaction rule |
| `GET` | `/recurring/{id}` | Get a specific recurring rule |
| `GET` | `/recurring/user/{userId}` | Get all rules for a user |
| `GET` | `/recurring/user/{userId}/active` | Get only currently active rules |
| `GET` | `/recurring/user/{userId}/upcoming` | Get active rules due before the end of the current month |
| `PUT` | `/recurring/{id}` | Update rule details (amount, frequency, etc.) |
| `PATCH`| `/recurring/{id}/deactivate` | Soft-delete/pause the recurring rule |
| `DELETE`| `/recurring/{id}` | Hard delete the rule from the database |

## Configuration & Deployment
Ensure `@EnableScheduling` and `@EnableFeignClients` are active on the main application class.

```yaml
server:
  port: 8087
spring:
  application:
    name: recurring-service
  datasource:
    url: jdbc:mysql://localhost:3306/spendsmart_recurring?useSSL=false&serverTimezone=UTC&createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true
    username: root
    password: <your_password>