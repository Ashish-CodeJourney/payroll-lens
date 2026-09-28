# Development evidence

## Annual base salary validation

The first business behavior was a positive annual base salary with a required currency. For each RED step, `cd backend && mvn -q -Dtest=AnnualSalaryTest test` was run before the corresponding production change:

| New test | Observed RED result |
| --- | --- |
| Reject zero | Test compilation failed: `AnnualSalary` did not exist |
| Reject negative | Assertion failed: no exception was thrown |
| Reject missing amount | Assertion failed: `NullPointerException` instead of `IllegalArgumentException` |
| Reject missing currency | Assertion failed: no exception was thrown |

The valid amount/currency case passed against the minimum implementation. The targeted test and full backend test suite passed after the guards were completed.

Manual mutation check against the changed value object:

| Mutation | Test result |
| --- | --- |
| `signum() <= 0` → `< 0` | Killed by zero-salary test |
| `amount == null || ...` → `&&` | Killed by missing and nonpositive salary tests |
| `currency == null` → `!= null` | Killed by valid and missing-currency tests |

**Result:** 3 applied, 3 killed, 0 survived (100% of the selected mutations). Each mutation was reverted before the next run. This is a targeted mutation check, not a claim about every possible mutation.

JaCoCo verification: run `cd backend && mvn -q verify`, then inspect `target/site/jacoco/jacoco.csv`. The `AnnualSalary` row has 26/26 instructions, 6/6 branches, 6/6 lines, and 1/1 method covered. The application bootstrap class is not exercised by these unit tests; a later startup/integration check must cover deployment wiring.

AI assisted with initial planning, workflow rules, and test review. The tests were executed against the real Java implementation; failing and passing results were verified rather than accepted from generated suggestions.

## Employee persistence and uniqueness

`cd backend && mvn -q -Dtest=EmployeeRepositoryTest test` first failed to compile because the employee model and repository were absent. After the persistence test passed, it was strengthened to clear the JPA context before reloading the row. With the unique constraints absent, the duplicate employee-number and duplicate-email tests each failed because no exception was thrown. Each constraint was added only after its failing test.

Selected mutation check: replacing the stored currency with a fixed `USD` in `Employee.getSalary()` caused the reload test to fail. The absent unique constraints were also observed as failing tests before implementation. **Result:** 3 selected changes, 3 detected, 0 survived. The fixed-currency mutation was reverted. `mvn -q verify` passed with the correct mapping and constraints.

## Deterministic 10,000-employee seed

`cd backend && mvn -q -Dtest=SeedEmployeesTest test` initially failed to compile because the generator did not exist. The generator test then passed with exactly 10,000 distinct employee numbers and emails, five countries with matching currencies, five departments, positive salaries, and identical output on repeat calls. `SeedServiceTest` initially failed to compile because the database seeder did not exist; after implementation, it verified 10,000 inserted rows and zero new rows on a second call.

Selected mutations: generating 9,999 instead of 10,000 records failed the count assertion; setting every currency to `USD` failed the country/currency assertion. **Result:** 2 applied, 2 killed, 0 survived. Both mutations were reverted. The seed script was also run against local PostgreSQL, and `SELECT COUNT(*) FROM employees` returned `10000`.

`mvn -q verify` passes. Its JaCoCo report covers every line of the generator and batch insertion service, with one branch in the service still uncovered. The command-line runner is exercised by the PostgreSQL smoke check, though that separate process is not reflected in the unit-test coverage report. Coverage is reported per class rather than claimed to be 100% overall.

## Employee directory API

The HTTP test `cd backend && mvn -q -Dtest=EmployeeApiTest test` first returned 404 for `/api/employees`. With paging implemented, a new combined-filter test failed with `totalElements` 4 instead of 1; adding database predicates made it pass. Search by number and email was then observed failing against name-only search. A page-size test observed `size` 1000 before the 100 cap was added, and invalid page/size requests failed the expected 422 contract before validation was added.

Selected mutations: removing the page-size cap failed its API test, and removing the email search predicate failed the identity-search test. **Result:** 2 applied, 2 killed, 0 survived. Both were reverted before the final run.

The first full-suite run exposed shared H2 state between the seed integration test and API tests: duplicate employee-number inserts failed even though each test class passed alone. A unique in-memory database name per Spring context removed that coupling; `mvn -q verify` then passed with all test classes together.

## Employee creation API

The new POST test first received 405 because no creation handler existed. A valid request then returned 201 and persisted its salary. The invalid-fields test failed its `fieldErrors` assertions until request validation and the 422 handler were added. A duplicate employee-number request failed the expected 409 contract before the conflict handler was added.

Selected mutation: removing `@Valid` from the POST request caused the invalid-fields test to fail. **Result:** 1 applied, 1 killed, 0 survived. The annotation was restored.

## Employee detail API

`EmployeeApiTest#returnsOneEmployeeById` first received 404 while no detail route existed, then passed after the route returned the saved employee. A missing-employee test then failed because the 404 response lacked the standard JSON error; a typed not-found exception and handler supplied it. Changing that handler's HTTP status from 404 to 400 caused the missing-employee test to fail. **Result:** 1 selected mutation applied, 1 killed, 0 survived; the correct status was restored.

## Employee editing API

The PUT behavior test first received 405, then passed after the update route changed both profile and current salary. The test flushes and clears the persistence context before reading the record again. Removing the salary assignment from the update method made the test fail; it was restored. **Result:** 1 selected mutation applied, 1 killed, 0 survived.

## Employee archiving and status filtering

`EmployeeApiTest#archivesAnEmployeeWithoutRemovingTheirRecord` first received 405 before the PATCH route existed, then passed after archiving persisted the status while retaining the record. `EmployeeApiTest#filtersActiveAndArchivedEmployees` initially returned both records for the active filter; the database predicate made ACTIVE, ARCHIVED, and ALL filters behave as requested.

Selected mutation: changing the archive assignment from `true` to `false` failed the API assertion (`$.archived` expected true but was false). **Result:** 1 applied, 1 killed, 0 survived. The assignment was restored before the final suite.

## Filtered salary analytics

`AnalyticsApiTest#reportsActiveFilteredPayInDatedUsdAndCountryBreakdowns` first received 404 before the report route existed. The route now reports the filtered, active-only population with dated USD conversion, median, totals, breakdowns, and distribution bands. Focused calculator tests cover empty results, an even-size median, exact band boundaries, and an unsupported currency.

Selected mutation: changing the EUR-to-USD factor from `1.10` to `1.00` failed the API test's expected total. **Result:** 1 applied, 1 killed, 0 survived. The rate was restored before full verification.

A follow-up employee API test first accepted `JPY` (201), even though analytics had no fixed USD rate for it. Request validation now rejects unsupported currencies with a field-level 422 response, keeping every saved salary reportable.

## Angular application shell

The shell test first rendered the Angular generator's `Hello, frontend` title instead of Payroll Lens navigation. A small Angular Material toolbar and router links made the test pass. `npm test -- --watch=false` and `npm run build` passed for the shell slice.

## Angular employee directory

The first directory test failed to compile because the component did not exist. With the component and HTTP client in place, it verified a 25-record page request, a visible local-currency salary, a new search request, and an empty-result message. The initial run also exposed a missing router provider in the test fixture, which was added. Selected mutation: removing the search query from the API call made the test fail because no matching filtered request was sent. **Result:** 1 applied, 1 killed, 0 survived. The query was restored before final verification.
