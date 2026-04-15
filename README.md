# SpendSmart - Notification & Alert Service

## Overview
The Notification Service is the communication hub of the SpendSmart architecture. Acting as a passive event listener, it processes internal webhooks from other microservices (like Budget or Recurring) and translates them into user-facing alerts. It supports both persistent In-App database badging and real-time live SMTP email dispatch for critical financial warnings.

## Tech Stack
* **Java:** 21 (LTS)
* **Framework:** Spring Boot 3.2.4
* **Database:** MySQL 8 / Spring Data JPA
* **Email Client:** Spring Boot Starter Mail (JavaMailSender)
* **Server:** Tomcat (Port 8088)

## Core Capabilities
* **Smart Routing:** Evaluates the `severity` property of incoming payloads. `INFO` and `WARNING` alerts are persisted to the database for UI rendering, while `CRITICAL` alerts automatically trigger an SMTP email dispatch.
* **In-App Badging:** Tracks `isRead` and `isAcknowledged` states to power frontend notification bell icons and unread counters.
* **Bulk Dispatch:** Optimized endpoints for broadcasting system-wide alerts to arrays of recipient IDs.
* **Fault Tolerance:** Email dispatch logic is wrapped in resilient try-catch blocks to ensure database persistence succeeds even if the external SMTP server times out.

## API Specification

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/notifications/budget-alert` | Internal trigger for critical budget breaches |
| `POST` | `/notifications/bulk` | Internal trigger for mass system broadcasts |
| `GET` | `/notifications/recipient/{id}` | Fetch full notification inbox for a user |
| `GET` | `/notifications/recipient/{id}/unread-count` | Fetch the unread badge number |
| `PATCH`| `/notifications/{id}/read` | Mark a specific notification as read |
| `PATCH`| `/notifications/recipient/{id}/read-all` | Mark entire inbox as read |
| `PATCH`| `/notifications/{id}/acknowledge` | Require explicit user acknowledgement |
| `DELETE`| `/notifications/{id}` | Remove alert from inbox history |

## SMTP Configuration
To enable live email dispatch, ensure you have configured your App Password in `application.yml`. **Never commit your actual app password to version control.**

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your.email@gmail.com
    password: ${SMTP_APP_PASSWORD} # Inject via environment variables
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true