# Helpdesk Platform

Multi-tenant support ticket / helpdesk backend, built as a portfolio project to
demonstrate production-grade backend practices: multi-tenancy, security,
testing, async/event-driven design, performance, and CI/CD.

## Domain

- **Tenant** — a company/organization using the helpdesk
- **User** — belongs to a tenant; role is Admin, Agent, or Customer
- **Ticket** — created by a customer, assigned to an agent; has status and priority
- **Comment** — thread of messages on a ticket (internal notes vs customer-visible)
- **SLA Policy** — response/resolution time targets based on priority

Tenant isolation: shared database, `tenant_id` column on every tenant-scoped table.

## Tech stack

- Java 21, Spring Boot 3.4
- Spring Data JPA, PostgreSQL
- Spring Security (JWT-based, no external identity provider)
- Testcontainers for integration tests
- Docker & Docker Compose
- GitHub Actions CI

## MVP scope (Phase 1)

- Tenant registration
- User management within a tenant
- Ticket CRUD scoped to tenant
- Ticket assignment to agents
- Comments on tickets
- Ticket status workflow: Open → In Progress → Resolved → Closed

## Running locally

```bash
docker compose up --build
```

App will be available at `http://localhost:8080`.

## Roadmap

- [ ] Phase 1 — Core domain & CRUD
- [ ] Phase 2 — Spring Security (JWT, role-based access)
- [ ] Phase 3 — Testing (unit + integration with Testcontainers)
- [ ] Phase 4 — Async/events (ticket assignment, SLA breach notifications)
- [ ] Phase 5 — Performance (caching, indexing)
- [ ] Phase 6 — Full CI/CD (already scaffolded in Phase 1)
