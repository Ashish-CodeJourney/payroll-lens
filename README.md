# Payroll Lens

[![CI](https://github.com/Ashish-CodeJourney/payroll-lens/actions/workflows/ci.yml/badge.svg)](https://github.com/Ashish-CodeJourney/payroll-lens/actions/workflows/ci.yml)

Payroll Lens is an HR Manager application for maintaining ACME's current annual base salaries across 10,000 employees. It replaces spreadsheet lookup and editing with a searchable employee directory and will answer pay questions with currency-aware reports. All demo records are synthetic.

> **Assessment status:** The Spring Boot API, PostgreSQL schema, deterministic 10,000-record seed, Angular workflow, local container deployment, passing [hosted CI run](https://github.com/Ashish-CodeJourney/payroll-lens/actions/runs/36419505641), and [demo video](docs/demo.mp4) are present. The public VPS deployment is left to the owner; the [VPS handoff](docs/vps-deployment.md) contains the exact steps. This status is deliberately explicit so reviewers can distinguish delivered behavior from the [product requirements](docs/requirements.md).

## Run the full app locally

With Docker and Compose installed, run from the repository root:

```sh
make run
```

Open `http://localhost:8088`. The web container serves Angular and proxies `/api` to Spring Boot. The database is mapped to localhost port `5433` and is not exposed publicly. Flyway applies the schema on startup. The seed command inserts the same 10,000 synthetic employees once and leaves existing rows intact on reruns. `WEB_PORT` can change the web host port; `POSTGRES_PASSWORD` can override the local demo default. For a public demo, follow the [VPS handoff](docs/vps-deployment.md).

Review the interactive API reference at `http://localhost:8088/swagger-ui.html` or fetch its OpenAPI JSON at `http://localhost:8088/v3/api-docs`. Both are served through the same web port as Angular. The reference documents employee management and active-pay reports; the [API contract](docs/api-contract.md) explains error and currency semantics in prose.

Run `make help` for all commands. Common follow-ups are `make health`, `make logs`, `make stop`, `make db-shell`, `make db-backup`, `make test`, and `make build`. Use `make rebuild` after source changes to refresh the container images. `make test-layout` checks the running web app at desktop, tablet, and mobile widths; it requires Chrome and the frontend dependencies. `make db-backup BACKUP_FILE=backup.dump` refuses to overwrite an existing dump. `make stop` preserves the database volume.

Check the running stack:

```sh
curl 'http://localhost:8088/api/employees?query=ACM-00001&size=10'
curl 'http://localhost:8088/actuator/health'
```

For local development without the app containers, use Java 21 or later, Maven 3.9 or later, and Node.js 24 with npm 11. Run `make db-up`, `make seed`, and `make install-frontend`, then start `make dev-backend` and `make dev-frontend` in separate terminals. The Angular development server at `http://localhost:4200` proxies `/api` to the backend at `http://localhost:8080`.

The directory supports search, filters, paging, and local-currency display. Select an employee to change salary or archive the record, or use **Add employee** to create one. After creation, the app opens the saved employee's detail page. Archived records remain visible but are read-only in both the UI and API. **Reports** answers pay questions for the active, filtered population. The same archive action is available through the deployed API:

```sh
curl -X PATCH 'http://localhost:8088/api/employees/1/archive'
```

The [short silent demo](docs/demo.mp4) shows a directory search, salary edit, and updated filtered report. To regenerate it from the running Compose stack, install Chrome and FFmpeg, then run `cd frontend && npm ci && npm run demo:record`. The script asserts the saved value and report result, then restores the seeded employee's original salary even if recording fails.

## Delivered behavior and next milestones

| Area | Current state |
| --- | --- |
| Employee records | Create, retrieve, update current salary and profile, and archive through JSON API; validation and duplicate detection are covered by tests. |
| Directory | Server-side search, country/department/level/status filters, and pages capped at 100 records. |
| Demo data | Repeatable seed of 10,000 employees across five countries and currencies. |
| Analytics | Active headcount, dated USD total and median, country/department/level breakdowns, and salary distribution through JSON API. |
| Angular UI | Responsive directory, employee create/edit/archive form, and filtered salary reports, with Angular Material navigation and cards. |
| Deployment and demo | Local Compose stack, [passing CI](https://github.com/Ashish-CodeJourney/payroll-lens/actions/runs/36419505641), and [video walkthrough](docs/demo.mp4) are verified. [VPS instructions](docs/vps-deployment.md) are ready for the owner to publish the URL. |

Money is stored as annual gross base pay in each employee's local currency. Cross-country reports use USD with a fixed `2026-01-01` rate snapshot. Supported currencies and USD-per-unit factors are USD 1.00, EUR 1.10, GBP 1.25, INR 0.012, and CAD 0.74. These are deterministic demo assumptions, not market quotes; local values are never summed as if they share a currency.

## Verify the work

```sh
make install-frontend
make test
make test-frontend-coverage
make build
make test-layout # after starting the web app
```

Backend tests currently cover salary rules, persistence and uniqueness, seeding, employee HTTP behavior, and filtered analytics. The frontend tests cover navigation, directory, employee form, and report interactions. JaCoCo writes its backend report to `backend/target/site/jacoco/` after `mvn verify`; the optional frontend V8 coverage report is generated by `make test-frontend-coverage`. [Development evidence](docs/development-log.md) records observed RED/GREEN steps, coverage, and selected mutation checks.

## Reviewer map

- [One-page requirements](docs/requirements.md): goal, scope, success criteria, and deliberate exclusions. This was the first commit.
- [Delivery plan](docs/plan.md): vertical slices and completion criteria.
- [Architecture and diagram](docs/architecture.md): component boundaries and data flow.
- [Trade-offs and performance](docs/decisions.md): why the solution is shaped this way and how it handles 10,000 records.
- [API contract](docs/api-contract.md): planned and delivered JSON interface; consult the status table above for implementation progress.
- [AI collaboration record](docs/ai-workflow.md): prompts, verification, and responsibility for AI-assisted work.
- [Development evidence](docs/development-log.md): test-first observations and mutation checks.
- [VPS deployment handoff](docs/vps-deployment.md): self-hosting, HTTPS, verification, updates, and backup.
- [Working rules](AGENTS.md): TDD, conventional commits, and trunk-based development.

The project follows short, tested commits directly on `main`. The history starts with requirements, then scaffolding, domain rules, persistence, seed, and employee API slices. Each behavioral commit contains its tests; failing RED states are documented instead of committed to trunk.

## Configuration and limits

The API accepts `DATABASE_URL`, `DATABASE_USER`, and `DATABASE_PASSWORD` environment variables, with local Compose defaults in the backend configuration. The Compose password is for synthetic local demo data only. No authentication or authorization is implemented; real employee salary data must not be entered into this assessment build. Salary history, payroll execution, import/export, and live FX rates are intentionally excluded from the first release for the reasons in the requirements and decision notes.

Licensed under the [MIT License](LICENSE).
