# Multi-Tenant CMS / Blog Platform

A multi-tenant, domain-driven SaaS CMS and blogging platform built with **Spring Boot 4** and **Angular 21**, designed from day one for strict tenant isolation, scalability, and system-level correctness.

This project serves both as a production-ready foundation for a B2C SaaS and as a portfolio project demonstrating **software architecture**, **multi-tenancy**, and **platform engineering practices**.

---

## Why This Project Exists

Most CMS platforms are built as single-tenant systems first and retrofitted for SaaS later — often leading to:
- Data isolation bugs
- Security risks
- Architectural rewrites

This project takes the opposite approach:

> **Multi-tenancy is a first-class concern from day one.**

Even while operating with a single tenant during MVP, the system enforces:
- Domain-based tenant resolution
- Request-scoped tenant context
- Repository-level tenant guarantees
- Explicit cross-tenant safety constraints

---

## Core Architectural Goals

- **Strict Tenant Isolation**  
  No request, query, or background job can execute without an explicit tenant context.

- **Domain-Driven Tenant Resolution**  
  Tenants are resolved via subdomains or custom domains — not via request parameters.

- **Defense-in-Depth Enforcement**  
  Isolation is enforced at:
    - HTTP filter layer
    - Authentication layer
    - Persistence layer

- **Fail Fast, Fail Loud**  
  Invalid tenant states are rejected immediately rather than silently recovered.

- **Designed for SaaS Scale**  
  Async jobs, custom domains, provisioning workflows, and future sharding are built into the design.

---

## Architecture Documentation

The architectural foundation is defined in:

📄 **[Milestone 0 — Architecture & Multi-Tenancy Strategy](docs/architecture/milestone-0-architecture.md)**

This document is considered authoritative for:
- Tenant resolution
- Request lifecycle
- Enforcement strategies
- “Impossible states” that must never occur

Any deviation from this design is intentional and documented.

---

## Technology Stack

### Backend
- Spring Boot 4
- Spring Security
- Hibernate / JPA
- PostgreSQL (planned)
- JWT-based authentication

### Frontend
- Angular 21
- Domain-aware routing
- SaaS-ready deployment model
- Per-tenant theming and branding (planned)

### Infrastructure (Planned)
- Docker
- Reverse proxy with wildcard domain support
- CI/CD
- Per-tenant custom domain support

---

## Current Status

### ✅ Milestone 0 — Architecture & Design
- System architecture defined
- Multi-tenancy strategy finalized
- TenantContext and enforcement model designed
- Impossible states checklist established

### 🚧 Milestone 1 — Single-Tenant MVP (In Progress)
Initial implementation using a single tenant while enforcing all multi-tenant constraints:
- TenantContext enforced
- Domain-based routing active
- Repository scoping applied
- Personal site deployed as first tenant

#### Developer quickstart (API)

- Swagger UI (auto-generated via springdoc): `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Health check: `GET http://localhost:8080/health`

Local database with Docker
- A Postgres service is provided via docker-compose.
- From the repository root, start the database:
  - `docker compose up -d db`
- Default connection (matches application.properties):
  - URL: `jdbc:postgresql://localhost:5432/platform`
  - User: `platform`
  - Password: `platform`

Run the API locally
1) Ensure Docker DB is running (see above)
2) Start the API (optionally with a local/dev profile to enable the admin seeder):
   - Linux/macOS: `SPRING_PROFILES_ACTIVE=local ./mvnw -pl platform-api spring-boot:run`
   - Windows (PowerShell): `$env:SPRING_PROFILES_ACTIVE="local"; ./mvnw -pl platform-api spring-boot:run`
3) Visit Swagger UI: `http://localhost:8080/swagger-ui/index.html`
4) Use a Host header in requests for tenant resolution (e.g., `default.yourblog.com`). Swagger allows setting headers per request under "Try it out".

Authentication and local admin user
- The API uses JWT (Bearer) auth. For local/dev profiles, an admin user can be auto-seeded for the `default` tenant.
- Seeder is active only when `SPRING_PROFILES_ACTIVE` includes `local` or `dev`.
- Default seed values (override via env):
  - `seed.admin.username=admin`
  - `seed.admin.password=password`
  - `seed.admin.roles=ROLE_ADMIN`

Login flow (local)
1. Start the API with a local/dev profile (e.g., `SPRING_PROFILES_ACTIVE=local`). Ensure DB is reachable.
2. In Swagger, call `POST /auth/login` with the seeded credentials.
3. Copy the returned token, click the “Authorize” button, and paste `Bearer <token>`.
4. Call secured admin endpoints (when available) from Swagger with the token.

Example curls
- Public (no auth):
  - `curl -s 'http://localhost:8080/api/v1/screens?path=/' -H 'Host: default.yourblog.com' | jq`
- Login:
  - `curl -s -X POST 'http://localhost:8080/auth/login' -H 'Host: default.yourblog.com' -H 'Content-Type: application/json' -d '{"username":"admin","password":"password"}' | jq`
  - Create screen (requires Bearer token and Host):
  - `curl -s -X POST 'http://localhost:8080/api/v1/admin/screens' -H 'Authorization: Bearer <token>' -H 'Host: default.yourblog.com' -H 'Content-Type: application/json' -d '{"path":"/about","type":"MARKDOWN","status":"DRAFT","content":"{}"}' | jq`

Local profile and environment variables
- A local profile file is provided at `platform-api/src/main/resources/application-local.properties` with sensible developer defaults (DB, Flyway, JWT, logging). Activate it with:
  - Linux/macOS: `SPRING_PROFILES_ACTIVE=local ./mvnw -pl platform-api spring-boot:run`
  - Windows (PowerShell): `$env:SPRING_PROFILES_ACTIVE="local"; ./mvnw -pl platform-api spring-boot:run`
- You can override any property via environment variables without changing tracked files, for example:
  - `export APP_JWT_SECRET=your-strong-secret` (maps to `app.jwt.secret`)
  - `export SEED_ADMIN_USERNAME=admin` `export SEED_ADMIN_PASSWORD=password` `export SEED_ADMIN_ROLES=ROLE_ADMIN,ROLE_EDITOR`
- Tip: You may also create a personal `.env` or shell profile export file locally (not committed) and source it before starting the app.

---

## Roadmap

- Milestone 1: Single-Tenant MVP with Multi-Tenant Constraints
- Milestone 2: Tenant Provisioning & Custom Domains
- Milestone 3: Async Jobs & Background Processing
- Milestone 4: Billing & Plan Enforcement
- Milestone 5: Observability & Production Hardening

---

## What This Project Demonstrates

This repository intentionally emphasizes:
- System design and architecture
- Defensive engineering
- SaaS platform thinking
- Real-world multi-tenant patterns

Over:
- UI polish
- Feature breadth
- Demo-only functionality

---

## About This Project

This project is built as both:
- A long-term SaaS foundation
- A portfolio artifact to demonstrate senior/staff-level engineering practices

---

## License

MIT
