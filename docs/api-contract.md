# API contract (initial)

The Angular client uses JSON under `/api`. Employee money is an annual gross base salary in the employee's local currency. Cross-country reports explicitly identify their reporting currency and fixed rate date.

| Method | Path | Behavior |
| --- | --- | --- |
| GET | `/api/employees` | Page current or archived employees. `page` is zero-based, `size` defaults to 25 and is capped at 100. Optional `query`, `country`, `department`, `level`, and `status` filters. Default order: employee number ascending. |
| GET | `/api/employees/{id}` | Return one employee or 404. |
| POST | `/api/employees` | Create an employee; return 201 and its record. |
| PUT | `/api/employees/{id}` | Replace editable profile and salary fields; return the updated record. |
| PATCH | `/api/employees/{id}/archive` | Archive an employee; return the updated record. |
| GET | `/api/analytics` | Return filtered current-pay measures and breakdowns. |

List responses contain `items`, `page`, `size`, `totalElements`, and `totalPages`. Employee records contain `id`, `employeeNumber`, `fullName`, `email`, `countryCode`, `department`, `jobTitle`, `jobLevel`, `annualSalary`, `currencyCode`, and `archived`. All invalid inputs use a consistent JSON error with `error` and `message`; field errors may include `fieldErrors`. Unknown employees return 404, duplicate identifiers return 409, and invalid fields return 422.
