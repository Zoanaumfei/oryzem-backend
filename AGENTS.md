# Oryzem Backend (Spring Boot)

## Purpose
Spring Boot backend for the Oryzem website. Provides API endpoints and server-rendered views.

## Stack
- Java 17
- Spring Boot 3.5.8
- Maven (wrapper scripts included)
- MapStruct + Lombok
- AWS SDK (DynamoDB, S3)

## Project Layout
- `src/main/java`: application code
- `src/main/resources`: config and templates
  - `application.yml`, `application.properties`, `application-dev.yml`, `application-prod.yml`
  - `templates/`, `static/`
- `src/test/java`: tests

## Common Commands
- Run (Windows): `mvnw.cmd spring-boot:run`
- Run (macOS/Linux): `./mvnw spring-boot:run`
- Test: `mvnw.cmd test` or `./mvnw test`
- Package: `mvnw.cmd -DskipTests package` or `./mvnw -DskipTests package`

## Profiles
- Use `SPRING_PROFILES_ACTIVE=dev` or `-Dspring-boot.run.profiles=dev` for dev settings.
- Use `SPRING_PROFILES_ACTIVE=prod` for production settings.

## Notes for Changes
- Keep Java version 17 aligned with the Maven compiler settings.
- Do not edit generated files under `target/`.
- If modifying mapping logic, keep MapStruct and Lombok annotation processor order intact.
