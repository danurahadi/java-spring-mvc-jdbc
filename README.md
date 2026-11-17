# Spring Boot MVC + Spring JDBC + MySQL + Auth0 Boilerplate Backend

Boilerplate BE for a **non-reactive, frontend-facing Spring Boot REST API** using:

- Java 25
- Apache Maven via Maven Wrapper (baseline 3.10.0; the wrapper file is authoritative)
- Spring Boot 4.1.x / Spring MVC / embedded Tomcat
- Spring JDBC (`JdbcTemplate` / `NamedParameterJdbcTemplate`)
- MySQL + InnoDB + HikariCP + Flyway
- Auth0 as the external identity provider and JWT issuer
- Local MySQL users, identity mappings, roles, and permissions
- In-app authorization as the sole authority for business permissions
- Springdoc OpenAPI + hosted Swagger UI

## Identity and authorization decisions encoded

1. Auth0 authenticates users and issues API access tokens.
2. Spring Security validates the API access token's signature, issuer, audience, and temporal claims.
3. The application resolves the exact external identity using `(iss, sub)`.
4. A local `users` record represents the application's user; business records reference its local primary key.
5. `roles`, `permissions`, `user_roles`, and `role_permissions` live in MySQL.
6. Local roles/permissions are the authority for business authorization. Auth0 RBAC claims are not mirrored or treated as local permissions.
7. User provisioning is idempotent and race-safe; matching email addresses never trigger automatic account linking.
8. No local passwords or duplicate password-reset/MFA flows are created.

## Architectural note

This boilerplate intentionally designed to be modular-monolith and excludes WebFlux, R2DBC, Spring Data JPA, and Hibernate ORM. 
Because the application is imperative Spring MVC backed by JDBC templates.
