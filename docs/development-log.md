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
