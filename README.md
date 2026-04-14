# SpendSmart - Analytics & Report Service

## Overview
The Analytics Service is the data intelligence layer of the SpendSmart platform. Unlike the transactional services (Income, Expense, Budget), this service acts as an aggregator. It relies entirely on synchronous inter-service communication via Spring Cloud OpenFeign to gather raw financial data, process complex algorithms, and deliver dashboard-ready payloads to the frontend.

## Tech Stack
* **Java:** 21 (LTS)
* **Framework:** Spring Boot 3.2.4
* **Inter-Service Communication:** Spring Cloud OpenFeign
* **Database:** MySQL 8 / Spring Data JPA
* **Server:** Tomcat (Port 8086)

## Core Capabilities
* **Financial Health Score Algorithm:** Computes a 0-100 score based on savings rate (40 weight), budget adherence (40 weight), and expense-to-income ratio (20 weight).
* **Predictive Forecasting:** Uses a 3-month trailing average combined with a calculated momentum factor to predict future spending.
* **Data Aggregation:** Consolidates daily trends, category breakdowns, and yearly summaries across isolated microservice databases.
* **Historical Snapshots:** Persists monthly financial states to allow for rapid querying of long-term trends without recalculating historical data.

## API Specification

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/analytics/user/{userId}/snapshot` | Generate and save a financial snapshot for a specific month |
| `GET` | `/analytics/user/{userId}/health` | Calculate the real-time Financial Health Score |
| `GET` | `/analytics/user/{userId}/forecast` | Calculate the predictive spending forecast |
| `GET` | `/analytics/user/{userId}/summary/monthly` | Get aggregated monthly income/expense/savings |
| `GET` | `/analytics/user/{userId}/summary/yearly` | Get aggregated yearly data and average savings rate |
| `GET` | `/analytics/user/{userId}/breakdown/category` | Get expense totals grouped by category ID |
| `GET` | `/analytics/user/{userId}/trend/daily` | Get daily spending mapped by day of the month |
| `GET` | `/analytics/user/{userId}/cashflow` | Compare total inflow vs total outflow |

## Configuration
This service requires the following microservices to be running and accessible:
* `income-service` (localhost:8083)
* `expense-service` (localhost:8082)
* `budget-service` (localhost:8085)

Configure `application.yml` with your local MySQL credentials:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/spendsmart_analytics?useSSL=false&serverTimezone=UTC&createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true
    username: root
    password: <your_password>
