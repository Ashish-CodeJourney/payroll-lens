# AI collaboration record

This assessment uses an AI coding agent as a collaborator. The human-provided brief and follow-up instructions set the product and engineering constraints; the agent inspected the repository, proposed slices, wrote tests and implementation, ran commands, and reported evidence. The human retains responsibility for reviewing product decisions and final submission.

## Instructions and prompts used

The following are excerpts from the actual instructions supplied in this session, preserved to make the AI role inspectable:

> “Write a one-page requirements document before building the software, outlining the goal, scope & features, and what you are deliberately leaving out.”

> “You need to use Angular JS and for backend use springboot”

> “set rules that strictly follow $tdd-guardian $tdd and do incremental commits (conventional)”

> “use Trunk Based Developmenrt”

The job description also specifies modern Angular with TypeScript, so this project uses current Angular rather than the legacy AngularJS 1.x framework. The repository's [working rules](../AGENTS.md) turn the TDD and trunk requirements into a repeatable workflow. No Spring Boot- or Angular-specific MCP integration was available during this work; the agent used local tools, tests, and code inspection. This file does not claim unrecorded AI prompts or reviews.

## How suggestions were checked

The agent ran each new behavioral test before implementation, observed the expected failure, then added the smallest passing change. The [development log](development-log.md) records those observations and selected mutations that tests caught. Backend verification uses `mvn verify`; the real PostgreSQL seed was also run and counted. Generated code was not treated as correct merely because it compiled. Documentation-only changes are checked against the actual files and commands rather than given artificial behavior tests.

AI accelerated scaffolding, test case identification, implementation, and documentation. It did not replace the need to inspect failures, review query behavior, or verify the final Angular workflow and deployed demo. Known gaps and next milestones remain visible in the [README](../README.md) and [plan](plan.md).
