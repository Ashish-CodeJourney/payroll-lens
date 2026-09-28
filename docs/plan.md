# Delivery plan

The first release is a single-HR-Manager workflow: find an employee, change their current annual base salary, and see the change reflected in a currency-aware report. The [one-page requirements](requirements.md) define the product boundary; this file records the implementation sequence and exit criteria.

| Increment | Observable outcome | Evidence and status |
| --- | --- | --- |
| 1. Define product and workflow | Goal, exclusions, contract, and trunk/TDD rules are reviewable before code. | Complete; requirements were committed first. |
| 2. Establish domain and persistence | Salary validation, unique employee identities, migration, and repeatable 10,000-row seed. | Complete; JUnit and database tests plus PostgreSQL seed smoke check. |
| 3. Manage employees through API | Search, page, create, detail, edit, archive, and consistent errors. | Complete; HTTP integration tests and selected mutation checks. |
| 4. Answer HR pay questions | Active population, median/distribution, totals and breakdowns with a stated USD rate snapshot. | Next backend slice; test calculation and filter consistency first. |
| 5. Make workflow usable in Angular | Directory, detail/edit form, report view, currency formatting, and clear loading/error/empty states. | Pending; add component and service behavior tests before UI code. |
| 6. Release and explain | CI, production build, deployed demo, short video, measured performance, and final reviewer instructions. | Pending; verify the deployed URL and video before claiming readiness. |

Each behavior is built in a RED → GREEN → mutation check → refactor loop. Tests run locally before a small conventional commit on `main`; no failing RED commit is placed on trunk. The [development log](development-log.md) captures observed failures and test strengthening. The final end-to-end check must create or edit a salary in the UI, retrieve it through the API, and confirm the report changes consistently.
