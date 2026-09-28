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
