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
