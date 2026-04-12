# SpendSmart - Budget Service

## Overview
The Budget Service acts as the financial watchdog for the SpendSmart platform. It provides real-time tracking of user expenditures against predefined limits, automatically calculating percentage usage and triggering alerts when users approach or exceed their spending thresholds.

## Tech Stack
* **Java:** 21 (LTS)
* **Framework:** Spring Boot 3.2.4
* **Database:** MySQL 8 / Spring Data JPA
* **Server:** Tomcat (Port 8085)

## Core Features
* **Real-time Math Engine:** Uses `BigDecimal` for precision calculations to prevent floating-point anomalies.
* **Dynamic Alerting:** Users can configure custom percentage thresholds (e.g., 80%, 90%) to receive warnings.
* **Period Management:** Supports Monthly, Weekly, and Custom date ranges with reset capabilities.
* **Progress Tracking:** Generates comprehensive DTOs detailing limit, spent, remaining, and percentage metrics.

## API Specification

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/budgets` | Create a new budget |
| `GET` | `/budgets/{budgetId}` | Fetch specific budget details |
| `GET` | `/budgets/user/{userId}` | Get all budgets for a user |
| `GET` | `/budgets/user/{userId}/active` | Get currently active budgets |
| `PATCH`| `/budgets/{budgetId}/spent` | Atomically increment the spent amount |
| `GET` | `/budgets/{budgetId}/progress` | Get calculated percentage and status DTO |
| `GET` | `/budgets/user/{userId}/alerts` | Retrieve active warning/exceeded notifications |
| `POST` | `/budgets/user/{userId}/reset` | Reset spent amounts for a new period |