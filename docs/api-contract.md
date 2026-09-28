# API contract

The Angular client uses JSON under `/api`. Employee money is an annual gross base salary in the employee's local currency. Cross-country reports explicitly identify their reporting currency and fixed rate date.

Swagger UI is available at `/swagger-ui.html`, with generated OpenAPI JSON at `/v3/api-docs`. The generated contract is derived from the running Spring Boot application; this page adds the business rules and error semantics behind the endpoints.

| Method | Path | Behavior |
| --- | --- | --- |
| GET | `/api/employees` | Page current or archived employees. `page` is zero-based, `size` defaults to 25 and is capped at 100. Optional `query`, `country`, `department`, `level`, and `status` filters. Default order: employee number ascending. |
| GET | `/api/employees/{id}` | Return one employee or 404. |
| POST | `/api/employees` | Create an employee; return 201 and its record. |
| PUT | `/api/employees/{id}` | Replace editable profile and salary fields; return the updated record. |
| PATCH | `/api/employees/{id}/archive` | Archive an employee; return the updated record. |
| GET | `/api/analytics` | Return current-pay measures for active employees. Optional `query`, `country`, `department`, and `level` filters match the directory. Response includes `reportingCurrency`, `rateDate`, `headcount`, `annualTotalUsd`, `medianAnnualUsd`, `byCountry`, `byDepartment`, `byLevel`, and `distribution`. |

List responses contain `items`, `page`, `size`, `totalElements`, and `totalPages`. Employee records contain `id`, `employeeNumber`, `fullName`, `email`, `countryCode`, `department`, `jobTitle`, `jobLevel`, `annualSalary`, `currencyCode`, and `archived`. All invalid inputs use a consistent JSON error with `error` and `message`; field errors may include `fieldErrors`. Unknown employees return 404, duplicate identifiers return 409, and invalid fields return 422.

Reports use the fixed `2026-01-01` USD rates documented in the README. Breakdown entries contain `label`, `headcount`, `annualTotalUsd`, and `medianAnnualUsd`; distribution bands contain `label` and `headcount`. An empty population returns zero totals/median and empty breakdowns. Employee writes accept only currencies for which a reporting rate exists.
