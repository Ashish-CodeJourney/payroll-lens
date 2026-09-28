# AI collaboration record

This assessment uses an AI coding agent as a collaborator. The human-provided brief and follow-up instructions set the product and engineering constraints; the agent inspected the repository, proposed slices, wrote tests and implementation, ran commands, and reported evidence. The human retains responsibility for reviewing product decisions and final submission.

## Instructions and prompts used

The following are excerpts from the actual instructions supplied in this session, preserved to make the AI role inspectable:

> “Write a one-page requirements document before building the software, outlining the goal, scope & features, and what you are deliberately leaving out.”

> “You need to use Angular JS and for backend use springboot”

> “set rules that strictly follow $tdd-guardian $tdd and do incremental commits (conventional)”

> “use Trunk Based Developmenrt”

> “Now add swagger if required for API documentation”

> “the UI is not that good, can you make it better? Fix all the alignments and all (specially search employees field is not aligned with other elements)”

> “Once we create employee it's saving it but keeping me on same page with same values filled in form, and if we can not edit any values after archiving than why the form stays editable?”

The job description also specifies modern Angular with TypeScript, so this project uses current Angular rather than the legacy AngularJS 1.x framework. The repository's [working rules](../AGENTS.md) turn the TDD and trunk requirements into a repeatable workflow. No Spring Boot- or Angular-specific MCP integration was available during this work; the agent used local tools, tests, and code inspection. This file does not claim unrecorded AI prompts or reviews.

## How suggestions were checked

The agent ran each new behavioral test before implementation, observed the expected failure, then added the smallest passing change. The [development log](development-log.md) records those observations and selected mutations that tests caught. Backend verification uses `mvn verify`; the real PostgreSQL seed was also run and counted. Generated code was not treated as correct merely because it compiled. Documentation-only changes are checked against the actual files and commands rather than given artificial behavior tests.

AI accelerated scaffolding, test case identification, implementation, and documentation. It did not replace the need to inspect failures, review query behavior, or verify the final Angular workflow and deployed demo. Known gaps and next milestones remain visible in the [README](../README.md) and [plan](plan.md).

For API documentation, the agent checked springdoc's published Spring Boot compatibility matrix before selecting the dependency. A new HTTP test first observed 404 for the missing OpenAPI endpoint, then verified generated routes, metadata, same-origin server URL, operation summaries, and the UI asset. The Nginx path was checked through the running containers before and after its proxy change.

For the UI refinement, the agent inspected live browser screenshots, wrote a browser layout check that failed on the reported search alignment, and expanded it only when further visual defects were observed. Angular interaction tests caught a removed accessible search name. The final layout was checked in the running container at multiple viewport widths and the video workflow was replayed, including its salary-restoration check. Visual judgment remained a human-reviewable part of this work; the layout assertions cover measurable regressions rather than attempting to encode appearance entirely in tests.

For the create/archive follow-up, the agent used Angular tests for navigation and disabled controls, plus an HTTP test for the archive rule. Testing revealed that the backend still accepted edits to archived records, so the fix covers the API contract as well as the form. Selected mutations confirmed that the tests detect a missing navigation and a missing archive guard. A Chrome check against rebuilt containers verified the final routes and read-only fields without adding a test employee to the seeded database.
