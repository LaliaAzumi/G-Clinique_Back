# AI Agent Guide for G-Clinique_Back

## Purpose
This project is a Spring Boot backend for the G-Clinique ERP system.

## Build and run
- `mvn spring-boot:run`
- `mvn clean package`

## Key files and packages
- `src/main/java/com/erp/clinique/service/OrdonnanceService.java`
- `src/main/java/com/erp/clinique/model` for domain models
- `src/main/java/com/erp/clinique/controller` and service classes for appointment/consultation workflows

## Relevant capabilities
- PDF generation using `openpdf`
- Email sending using `spring-boot-starter-mail`
- JPA data access via `spring-boot-starter-data-jpa`
- Thymeleaf templates may be used for email/PDF bodies

## Debugging focus for ordonnance and invoice logic
- Validate that `OrdonnanceService` excludes `NA` values and includes medical act pricing when building invoice totals.
- Check consultation/rendezvous service flows for whether ordonnance creation and patient email sending are triggered after save.
- If the front-end sees only acte results, inspect the backend response DTOs and the controller/service request handling.

## Python gateway note
- `python-api/` is a separate FastAPI adapter and should only be used when debugging integration with the Python gateway.
- Core business logic for prescriptions, ordonnances, and email is in the Spring Boot app.
