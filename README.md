# Quarkus PetClinic

This repository is a Quarkus redesign of the original
[Spring PetClinic](https://github.com/spring-projects/spring-petclinic)
sample application.

The goal is not to prove one framework is better than the other, and it is not a
line-by-line port. Spring PetClinic is the canonical Spring version of this
sample. This fork keeps the PetClinic domain and business ideas, then asks what
the application looks like when it is built directly with Quarkus idioms.

The result is intentionally different from the Spring application:

- CDI instead of Spring-managed components.
- JAX-RS JSON resources instead of Spring MVC controllers.
- Hibernate ORM with Panache instead of Spring Data repositories.
- `application.properties` and Dev Services instead of profile-specific manual
  database setup.
- Integration-style `@QuarkusTest` tests instead of Spring test slices.

The current implementation focuses on the owner, pet, pet type, and visit flows.
It is API-first rather than a server-rendered web UI.

## Current Scope

This project currently ships the Quarkus backend/API side of PetClinic. Swagger
UI is the browser entry point for exploring and exercising the REST API; it is
not intended to replace the original PetClinic user interface.

A Quarkus-native frontend would be a separate follow-up direction. For example,
the application could add server-rendered pages with Qute, or serve bundled web
assets from Quarkus, while keeping the current REST API available for machine
clients and documentation.

## Run

Docker must be available so Quarkus Dev Services can start PostgreSQL.

```bash
./mvnw quarkus:dev
```

The app starts on <http://localhost:8080> and redirects to Swagger UI.

Use Swagger UI for the API reference and request examples:

- Swagger UI: <http://localhost:8080/q/swagger-ui>
- OpenAPI document: <http://localhost:8080/q/openapi>

## Test

```bash
./mvnw test
```

The tests use `@QuarkusTest` and the same PostgreSQL Dev Services setup as
development mode. Quarkus starts the PostgreSQL database with Testcontainers, so
the integration and end-to-end tests run against the same containerized database
style used during local development.

`PetClinicEndToEndTest` exercises the main clinic journey through the REST API:
owner registration, owner search/update/delete, pet registration, visit
registration, ownership boundaries, and seeded catalog data.

## Native Build

```bash
./mvnw package -Dnative
```

## Design

- `domain`: Panache entities with PetClinic behavior such as owner-owned pets and
  pet visits.
- `service`: CDI application service for transactional business flows.
- `resource`: JAX-RS resources exposing JSON REST endpoints.

The application intentionally ships as a Maven-only Quarkus REST service with no
Spring dependencies, server-rendered templates, or Gradle build.
