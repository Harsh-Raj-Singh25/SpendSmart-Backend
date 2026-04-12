# SpendSmart - Category Service

## Overview
The Category Service manages the financial taxonomy for the SpendSmart platform. It allows users to define custom categories for their income and expenses, assign visual identities (icons and colors), and set specific budget limits for granular financial tracking.

## Tech Stack
* **Java:** 21 (LTS)
* **Framework:** Spring Boot 3.2.4
* **Database:** MySQL 8 / Spring Data JPA
* **Server:** Tomcat (Port 8084)

## Features
* **Multi-Tenancy:** Categories are scoped to specific `userId`s.
* **Default Seeding:** Automated initialization of standard categories (Food, Salary, etc.) for new users.
* **Visual Identity:** Supports hex color codes and emojis/icons for frontend visualization.
* **Budget Tracking:** Allows setting maximum spend limits per category.

## API Specification

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/categories` | Create a custom category |
| `POST` | `/categories/user/{userId}/initDefaults` | Seed default categories for a new user |
| `GET` | `/categories/user/{userId}` | List all categories for a user |
| `GET` | `/categories/user/{userId}/type` | List categories filtered by INCOME/EXPENSE |
| `GET` | `/categories/defaults` | List system-wide default categories |
| `PUT` | `/categories/{categoryId}` | Update category metadata |
| `PATCH`| `/categories/{categoryId}/budget` | Update the budget limit for a category |
| `DELETE`| `/categories/{categoryId}` | Remove a custom category |
| `GET` | `/categories/user/{userId}/count` | Get total number of categories created |

## Setup
1. Configure `application.yml` with your local MySQL credentials.
2. Ensure the `api-gateway` is configured to route `/categories/**` to port `8084`.
3. Run `mvn clean install` to build and verify tests.