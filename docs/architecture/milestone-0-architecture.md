# Multi-Tenant Blog SaaS — Milestone 0 Artifacts

## 1. Problem Statement

Build a B2C, multi-tenant blogging platform where multiple customers (tenants) share a single deployed system while maintaining strict isolation of data, authentication, and behavior. The initial tenant will be the author’s personal website, but the system must be designed to support many tenants without architectural changes.

Key goals:

* One deployed frontend and backend
* Runtime tenant resolution
* Low operational cost for early-stage usage
* Clear evolution path for scale and isolation

Non-goals (for MVP):

* Billing and payments
* Comments, analytics, or social features
* Per-tenant infrastructure isolation

---

## 2. Constraints

### Technical Constraints

* Backend: Spring Boot 4 (Java 21+)
* Frontend: Angular 21 (single build)
* Database: PostgreSQL
* Hosting: Static frontend + API service
* No Kubernetes for MVP

### Business Constraints

* Experimental project
* Cost sensitivity
* Time-boxed development

---

## 3. High-Level Architecture

### Logical View

* Single Angular SPA served to all domains
* Single Spring Boot API
* Shared PostgreSQL database
* Multi-tenancy enforced at the API and data layers

```
Browser (any domain)
   ↓
Angular SPA (single build)
   ↓
Spring Boot API
   ↓
PostgreSQL (tenant_id scoped)
```

---

## 4. Tenancy Model

### Tenant Definition

A tenant represents a single customer/blog instance.

Tenant isolation includes:

* Data isolation (tenant_id on all tenant-scoped tables)
* Authentication isolation
* Domain and routing isolation

### Tenant Resolution Strategy

Tenant context is resolved **per request** using:

1. HTTP Host header (subdomain or custom domain)
2. JWT tenant claim (for authenticated requests)

The backend is the source of truth for tenant resolution.

---

## 5. Domain & DNS Strategy

### Platform Domains

* Default: `{tenant}.yourblog.com`
* Implemented via wildcard DNS

### Custom Domains

* User-provided domains via CNAME or A record
* Backend maps domain → tenant_id

Frontend and backend deployments remain unchanged regardless of domain.

---

## 6. Authentication & Authorization

* Stateless JWT authentication
* JWT includes tenant_id claim
* Every request validates:

    * Token authenticity
    * Token tenant_id matches resolved tenant

Authorization failures are explicit and tenant-safe.

---

## 7. Data Model (High-Level)

Tenant-scoped tables include:

* tenants
* tenant_domains
* users
* posts

Rule:

> No query may return or modify data without an explicit tenant scope.

---

## 8. Failure & Safety Principles

Assumptions:

* Requests may arrive with unknown or invalid domains
* Tenants may be misconfigured
* Auth tokens may be stale or incorrect

Principles:

* Fail closed (reject unknown tenants)
* Never infer tenant from user input alone
* No cross-tenant reads or writes

---

## 9. Architecture Decision Records (ADRs)

### ADR-001: Shared Multi-Tenant Architecture

**Decision:** Use a shared application and shared database with tenant-scoped data.

**Rationale:**

* Lowest operational cost
* Simplest deployment model
* Sufficient isolation for MVP

**Tradeoffs:**

* Noisy neighbor risk
* Strong need for disciplined data access

---

### ADR-002: Runtime Tenant Resolution

**Decision:** Resolve tenant at runtime using Host headers and JWT claims.

**Rationale:**

* Avoids per-tenant builds or deployments
* Enables wildcard and custom domains

**Tradeoffs:**

* Requires strict request filtering
* Slightly more complex request lifecycle

---

### ADR-003: Single Angular Build

**Decision:** Use one Angular build for all tenants and domains.

**Rationale:**

* Angular is static content
* Tenant context is a runtime concern

**Tradeoffs:**

* Frontend must avoid build-time tenant configuration

---

### ADR-004: Stateless Authentication

**Decision:** Use stateless JWT authentication.

**Rationale:**

* Horizontal scalability
* Simplified backend state

**Tradeoffs:**

* Token revocation complexity

---

### ADR-005: Deferred Infrastructure Isolation

**Decision:** Do not provide per-tenant infrastructure isolation for MVP.

**Rationale:**

* Cost and complexity outweigh benefits at this stage
* Clear future migration path

**Tradeoffs:**

* Reduced isolation guarantees

---

## 10. Future Evolution (Not Implemented)

* Dedicated tenant instances
* Tiered pricing and billing
* Search indexing
* Background jobs per tenant
* Read/write scaling

---

## 11. Request Lifecycle Walkthrough

This section documents the end-to-end lifecycle of a request, from DNS resolution to data access, and identifies where tenant context is resolved, validated, and enforced.

### 11.1 Public (Unauthenticated) Request — View Blog Post

**Example:** `GET https://alice.yourblog.com/posts/hello-world`

1. **DNS Resolution**

    * Wildcard DNS (`*.yourblog.com`) resolves to CDN / static hosting.

2. **Frontend Delivery**

    * CDN serves the same Angular `index.html` bundle used for all tenants.
    * Browser loads static assets.

3. **Angular Runtime Initialization**

    * Angular reads `window.location.hostname`.
    * Tenant slug (`alice`) is derived implicitly for UX purposes only.
    * Angular issues API request to backend.

4. **API Request**

    * Request sent to `https://api.yourblog.com/posts/hello-world`.
    * `Host` header still contains `alice.yourblog.com`.

5. **Tenant Resolution (Spring Boot Filter)**

    * Read `Host` header.
    * Attempt resolution in this order:

        1. Exact match in `tenant_domains` table
        2. Fallback to subdomain parsing
    * If no tenant found → return `404 Tenant Not Found`.

6. **Tenant Context Binding**

    * Resolved `tenant_id` stored in `TenantContext` for request scope.

7. **Authorization Check**

    * Public endpoint → no authentication required.
    * Tenant context still mandatory.

8. **Repository Layer Enforcement**

    * All queries include implicit `tenant_id` scope.
    * Only posts belonging to tenant are accessible.

9. **Response**

    * Post content returned.
    * Angular renders public blog page.

---

### 11.2 Authenticated Admin Request — Create Post

**Example:** `POST https://alice.yourblog.com/admin/posts`

1. **DNS & Frontend**

    * Same as public request.

2. **Angular Auth Context**

    * User is authenticated.
    * JWT is attached to request.

3. **API Request**

    * Request includes:

        * `Authorization: Bearer <JWT>`
        * `Host: alice.yourblog.com`

4. **Tenant Resolution**

    * Resolve tenant from `Host` header.

5. **Authentication Filter**

    * JWT signature validated.
    * Extract `tenant_id` claim.

6. **Tenant Consistency Check**

    * Compare tenant from domain vs tenant from token.
    * If mismatch → `403 Forbidden`.

7. **Tenant Context Binding**

    * `tenant_id` bound to request.

8. **Authorization Rules**

    * Verify user role (admin/editor).

9. **Repository Enforcement**

    * Insert post with enforced `tenant_id`.

10. **Response**

    * Post created successfully.

---

### 11.3 Invalid or Misconfigured Domain

**Example:** `GET https://unknown.yourblog.com`

* Tenant resolution fails.
* Backend returns `404` (not `401`).
* Frontend displays safe error page.

---

### 11.4 Custom Domain Request

**Example:** `GET https://www.aliceblog.com/posts`

* CDN routes domain to frontend.
* API receives `Host: www.aliceblog.com`.
* Tenant resolved via `tenant_domains` mapping.
* Request proceeds identically to subdomain-based flow.

---

### 11.5 Safety Guarantees

At no point in the request lifecycle can:

* A tenant be inferred solely from user input
* A repository query execute without tenant scope
* A token override domain-based tenant resolution

Tenant context is resolved once, validated early, and enforced everywhere.

---

## 12. TenantContext & Enforcement Strategy

This section defines how tenant context is stored, propagated, validated, and enforced throughout the backend to prevent accidental or malicious cross-tenant access.

---

### 12.1 TenantContext Design

**Goal:** Ensure every request executes with an explicit, immutable tenant context.

**Structure:**

* `TenantContext` is a request-scoped holder of `tenant_id`.
* Implemented using `ThreadLocal` (or equivalent request scope abstraction).

**Responsibilities:**

* Store resolved `tenant_id`
* Be writable only once per request
* Be cleared automatically after request completion

**Rules:**

* TenantContext must be set before controller logic executes
* TenantContext must never be null inside business logic
* TenantContext must be cleared in a `finally` block

---

### 12.2 Tenant Resolution Filter

**Position in Filter Chain:**

* Executed before authentication and authorization filters

**Responsibilities:**

1. Read `Host` header
2. Resolve `tenant_id` via:

    * `tenant_domains` table (exact match)
    * Subdomain parsing fallback
3. Reject request if tenant cannot be resolved
4. Populate `TenantContext`

**Failure Behavior:**

* Unknown tenant → `404 Not Found`
* Malformed Host header → `400 Bad Request`

---

### 12.3 Authentication & Tenant Consistency

**JWT Handling:**

* JWT includes `tenant_id` claim
* JWT is validated after tenant resolution

**Consistency Rule:**

* `tenant_id` from JWT must match `TenantContext.tenant_id`
* Mismatch results in `403 Forbidden`

**Rationale:**

* Prevents token reuse across tenants
* Ensures domain is the source of truth

---

### 12.4 Repository Enforcement Strategy

**Goal:** Make it impossible to execute tenant-unscoped queries accidentally.

**Approach:**

* All tenant-scoped entities extend `TenantScopedEntity`
* `tenant_id` is mandatory and non-null

**Repository Rules:**

* No repository method may accept `tenant_id` as a parameter
* Tenant scoping is applied automatically

**Implementation Options:**

* Hibernate filter enabled per request
* Base repository that injects `tenant_id`

**Preferred Strategy (MVP):**

* Hibernate filter with mandatory `tenant_id` parameter

---

### 12.5 Write Enforcement

For create operations:

* `tenant_id` is always sourced from `TenantContext`
* Any attempt to override `tenant_id` from request payload is ignored or rejected

---

### 12.6 Read Enforcement

For read operations:

* Queries automatically scoped by `tenant_id`
* Cross-tenant joins are prohibited

---

### 12.7 Background & Async Safety

**Principle:** Async work must carry tenant context explicitly.

Rules:

* Background jobs include `tenant_id` as part of payload
* TenantContext is re-established at job start

---

### 12.8 Testing Strategy

Required tests:

* Request without tenant → rejected
* Token/tenant mismatch → rejected
* Repository access without TenantContext → fails fast

---

### 12.9 Non-Goals

* Row-level security at database layer (future enhancement)
* Per-tenant database connections

---

## 13. Impossible States Checklist

This checklist defines system states that must *never* occur. Each item should be enforced via code structure, runtime checks, or automated tests.

---

### 13.1 Request & Context States

❌ A request reaches a controller without a resolved `TenantContext`

* Enforced by: TenantResolutionFilter
* Detection: Fail-fast exception if `TenantContext` is null

❌ `TenantContext` is modified after initial resolution

* Enforced by: Write-once setter
* Detection: IllegalStateException on second write

❌ `TenantContext` persists beyond request lifecycle

* Enforced by: `finally` cleanup in filter
* Detection: Integration test with multiple sequential requests

---

### 13.2 Authentication & Authorization States

❌ Authenticated request without tenant resolution

* Enforced by: Filter ordering (tenant before auth)

❌ JWT tenant does not match domain tenant

* Enforced by: Explicit equality check
* Result: `403 Forbidden`

❌ Token issued for one tenant used on another tenant domain

* Enforced by: Domain-first tenant resolution

---

### 13.3 Persistence States

❌ Repository query executes without tenant scoping

* Enforced by: Hibernate tenant filter
* Detection: Exception if filter parameter missing

❌ Repository method accepts `tenant_id` as an argument

* Enforced by: Code review + base repository abstraction

❌ Entity persisted with null or incorrect `tenant_id`

* Enforced by: Base entity population + DB constraint

❌ Cross-tenant joins or queries

* Enforced by: Repository design rules

---

### 13.4 Write & Mutation States

❌ Client supplies or overrides `tenant_id` in request payload

* Enforced by: Ignoring payload field or rejecting request

❌ Background job mutates data without tenant context

* Enforced by: Explicit tenant_id in job payload

---

### 13.5 Domain & Routing States

❌ Domain maps to multiple tenants

* Enforced by: Unique constraint on `tenant_domains.domain`

❌ Tenant has no primary domain

* Enforced by: Provisioning invariants

❌ Request with unknown domain returns tenant data

* Enforced by: 404 on unresolved tenant

---

### 13.6 Operational & Observability States

❌ Logs without tenant identifiers

* Enforced by: MDC population from TenantContext

❌ Metrics aggregated across tenants unintentionally

* Enforced by: Tenant-tagged metrics

---

### 13.7 Test Coverage Requirements

Each impossible state must be covered by at least one of:

* Unit test
* Integration test
* Startup-time assertion

---

## 14. Milestone 0 Exit Criteria

Milestone 0 is complete when:

* Architecture is documented
* ADRs are written and committed
* No code has been written yet
* Design decisions are explicit and intentional
