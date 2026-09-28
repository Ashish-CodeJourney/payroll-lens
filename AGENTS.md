# Working rules

This repository is an Angular + TypeScript frontend and Spring Boot + Java backend take-home assessment. Follow the `tdd` and `tdd-guardian` skills strictly for all production behavior changes. Documentation and setup-only changes do not need artificial tests.

## Test-first workflow

1. Define one observable behavior and write a focused test through a public interface. Run it and confirm it fails for the expected reason before writing production code. Record the RED result in the development evidence for the increment; do not integrate a failing build into the trunk.
2. Add only the production code needed to pass. Run the focused test and all relevant checks, then commit the tested slice with `feat:` or `fix:`. Include its behavior test in that commit.
3. Run mutation testing on changed business logic. Record killed and surviving mutants and strengthen tests for meaningful survivors. Verify coverage rather than claiming it from inspection.
4. Assess refactoring after tests are strong; preserve behavior, rerun checks, and use a separate `refactor:` commit when a change is worthwhile.

Tests must describe business outcomes, be fast and deterministic, and avoid assertions about private implementation details. Use JUnit for Java behavior, focused Spring integration tests for persistence and HTTP contracts, and Angular component/service tests for user interactions. Keep money calculations precise and test currency and boundary cases. Do not commit a broken mainline: before each GREEN or refactor commit, run the relevant tests, lint/static checks, and build. Record material trade-offs and AI-assisted decisions in the repository. Use conventional commit types (`docs`, `chore`, `test`, `feat`, `fix`, `refactor`) and keep each commit focused.

## Trunk-based development

Use `main` as the trunk. Integrate small, complete changes frequently; avoid long-lived feature branches. Keep `main` buildable and deployable after every commit. Prove the RED phase locally and preserve its evidence in a short development log rather than committing a failing trunk. Use short-lived branches only when external review requires them, and merge them back promptly. CI is the integration gate; do not hide failing or skipped checks.
