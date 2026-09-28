# Trade-offs and performance

## Product and architecture choices

| Choice | Reason and consequence |
| --- | --- |
| Current salary snapshot | The brief permits it. It gives HR a clear editable value and keeps first-release reports simple; historical trend questions remain out of scope. |
| Local currency per employee | Prevents silently treating unlike currencies as comparable. Cross-country reports will use fixed, dated USD rates and label converted figures. The rates are demonstration assumptions, not market quotes. |
| Spring Boot modular monolith | One deployable backend keeps the domain, API, and transaction boundary understandable for 10,000 records. Independent services would add coordination without a demonstrated need. |
| PostgreSQL with Flyway | A relational schema enforces unique identities and supports filtered queries. Explicit migrations avoid relying on Hibernate schema mutation at runtime. H2 is used for fast test isolation; PostgreSQL receives a separate seed smoke check. |
| Server-side paging and filters | The UI receives at most 100 directory records per request. Search predicates execute in the database, avoiding a 10,000-row browser payload. |
| Archive instead of delete | Old records remain identifiable for correction and review. Current-pay reports exclude them, while the directory can include archived records explicitly. |
| No authentication in demo | The brief allows one HR Manager, and all records are synthetic. This build must not hold real salaries; access control is required before any real use. |

## Capacity and verification

The target is 10,000 employees, not an unbounded payroll warehouse. The generated seed is deterministic and inserts in batches; rerunning it leaves the 10,000 existing records intact. The directory orders by employee number, applies filters in SQL, and caps page size at 100. Unique indexes protect employee number and email. A 10,000-row PostgreSQL seed was executed locally and the row count checked.

The initial analytics implementation filters active employees in SQL, then reads the matching rows into the backend to compute exact medians and currency-normalized breakdowns. This is bounded by the 10,000-employee assessment dataset, but response time and memory use still need measurement. Before release, measure representative directory queries and report requests against that seeded database: unfiltered first page, selective search, and a multi-filter page. Record response times and inspect query plans if any path is slow. A larger dataset would warrant projection queries or database-side aggregates; loading all employees into Angular would defeat the pagination design. If report breakdowns require more indexes, add them based on query plans rather than guessing. At this size, straightforward calculations and deterministic rates are preferable to caching or distributed processing.

The current tests establish correctness of salary rules, uniqueness, seeding, and API behavior. They do not yet prove production latency or deployment readiness. Those claims will be added only after measurement and a live smoke check.
