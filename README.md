# Payroll Lens

Payroll Lens is an HR Manager application for maintaining ACME's current annual base salaries across 10,000 employees. It replaces spreadsheet lookup and editing with a searchable employee directory and will answer pay questions with currency-aware reports. All demo records are synthetic.

> **Assessment status:** The Spring Boot employee API, filtered salary analytics, PostgreSQL schema, deterministic 10,000-record seed, and backend tests are implemented. The Angular directory and employee form are usable; report screens, CI, public deployment, and video demo are still in progress. This status is deliberately explicit so reviewers can distinguish delivered behavior from the [product requirements](docs/requirements.md).

## Try the current backend

Prerequisites: Java 21 or later, Maven 3.9 or later, Node.js 22 or later, npm, and Docker with Compose. From the repository root:

```sh
docker compose up -d db
sh scripts/seed.sh
cd backend && mvn spring-boot:run
```

The database is mapped to local port `5433`; the API starts at `http://localhost:8080`. The seed command is idempotent: it inserts the same 10,000 synthetic employees once and leaves existing rows intact on reruns. Flyway applies the schema on startup.

```sh
curl 'http://localhost:8080/api/employees?query=ACM-00001&size=10'
curl 'http://localhost:8080/actuator/health'
```

The Angular app can be started separately with `cd frontend && npm ci && npm start`, then opened at `http://localhost:4200`. The development server proxies `/api` to the backend. The directory supports search, filters, paging, and local-currency display. Select an employee to change salary or archive the record, or use **Add employee** to create one. The report screen is still in progress. The same archive action is available through the API:

```sh
curl -X PATCH 'http://localhost:8080/api/employees/1/archive'
```

## Delivered behavior and next milestones

| Area | Current state |
| --- | --- |
| Employee records | Create, retrieve, update current salary and profile, and archive through JSON API; validation and duplicate detection are covered by tests. |
| Directory | Server-side search, country/department/level/status filters, and pages capped at 100 records. |
| Demo data | Repeatable seed of 10,000 employees across five countries and currencies. |
| Analytics | Active headcount, dated USD total and median, country/department/level breakdowns, and salary distribution through JSON API. |
| Angular UI | Material directory and employee create/edit/archive form; report screen is next. |
| Deployment and demo | Planned after the end-to-end workflow is complete. |

Money is stored as annual gross base pay in each employee's local currency. Cross-country reports use USD with a fixed `2026-01-01` rate snapshot. Supported currencies and USD-per-unit factors are USD 1.00, EUR 1.10, GBP 1.25, INR 0.012, and CAD 0.74. These are deterministic demo assumptions, not market quotes; local values are never summed as if they share a currency.

## Verify the work

```sh
cd backend && mvn verify
cd frontend && npm ci && npm test -- --watch=false
cd frontend && npm run build
```

Backend tests currently cover salary rules, persistence and uniqueness, seeding, employee HTTP behavior, and filtered analytics. The frontend tests cover navigation, directory, and employee form interactions. JaCoCo writes its backend report to `backend/target/site/jacoco/` after `mvn verify`. [Development evidence](docs/development-log.md) records observed RED/GREEN steps and selected mutation checks.

## Reviewer map

- [One-page requirements](docs/requirements.md): goal, scope, success criteria, and deliberate exclusions. This was the first commit.
- [Delivery plan](docs/plan.md): vertical slices and completion criteria.
- [Architecture and diagram](docs/architecture.md): component boundaries and data flow.
- [Trade-offs and performance](docs/decisions.md): why the solution is shaped this way and how it handles 10,000 records.
- [API contract](docs/api-contract.md): planned and delivered JSON interface; consult the status table above for implementation progress.
- [AI collaboration record](docs/ai-workflow.md): prompts, verification, and responsibility for AI-assisted work.
- [Development evidence](docs/development-log.md): test-first observations and mutation checks.
- [Working rules](AGENTS.md): TDD, conventional commits, and trunk-based development.

The project follows short, tested commits directly on `main`. The history starts with requirements, then scaffolding, domain rules, persistence, seed, and employee API slices. Each behavioral commit contains its tests; failing RED states are documented instead of committed to trunk.

## Configuration and limits

The API accepts `DATABASE_URL`, `DATABASE_USER`, and `DATABASE_PASSWORD` environment variables, with local Compose defaults in the backend configuration. The Compose password is for synthetic local demo data only. No authentication or authorization is implemented; real employee salary data must not be entered into this assessment build. Salary history, payroll execution, import/export, and live FX rates are intentionally excluded from the first release for the reasons in the requirements and decision notes.

Licensed under the [MIT License](LICENSE).
