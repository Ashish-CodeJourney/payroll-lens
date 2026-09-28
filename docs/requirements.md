# Payroll Lens — product requirements

## Goal and user

Give ACME's HR Manager one reliable place to maintain the current annual base salary of 10,000 employees across countries and answer common questions about how the organization pays people. The application is a single-manager assessment using synthetic employee data; it must run locally and be available as a deployed demo.

## In scope

- **Employee records:** Create, view, edit, and archive employees. Each active record has a unique employee number, name, work email, country, department, job title, level, annual gross base salary, and salary currency. An archived employee remains identifiable but is excluded from current-pay reports.
- **Directory:** Search by employee number, name, or email; filter by country, department, level, and status; sort and page results on the server. The UI must remain usable with 10,000 records.
- **Salary management:** Add or change an employee's current annual base salary. Reject missing or invalid fields, duplicate employee numbers or emails, and salaries that are zero or negative. Display money with the appropriate currency and never silently mix currencies.
- **Answers for HR:** Show active headcount, annual base-pay total, median salary, and salary distribution. Break down results by country, department, and level, with the directory filters applied consistently. Cross-currency totals use a fixed, documented exchange-rate snapshot and explicitly show the reporting currency and rate date. Local-currency values remain visible on employee records.
- **Delivery:** A responsive Angular UI with accessible forms and clear loading, empty, success, and error states; a Spring Boot API backed by a relational database; repeatable migrations and a deterministic script that seeds exactly 10,000 synthetic employees. Include meaningful automated tests, CI checks, setup instructions, a deployed instance, and a short video demo.

## Success criteria

An HR Manager can find an employee, change their salary, and see the updated value in the directory and reports. Filters and pagination work across the full seeded dataset. Reports state their population and currency basis; calculations match the underlying active records. A reviewer can run the application and tests from the repository instructions and use the deployed demo.

## Deliberately out of scope

- **Salary history and effective dates:** The brief accepts a current snapshot; this keeps edits and reports unambiguous for the first release.
- **Bonuses, equity, deductions, taxes, and payroll execution:** These require separate rules and workflows; annual base salary is the comparable measure this product manages.
- **Excel import/export and natural-language Q&A:** Searchable records and focused reports solve the core questions without adding file-format or AI-service complexity.
- **Authentication, roles, and approvals:** The assessment permits a single HR Manager. The deployed demo uses synthetic data only; real salary data would require access control before adoption.
- **Live foreign-exchange feeds:** Fixed rates make the demo reproducible and its calculations auditable.
