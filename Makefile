COMPOSE := docker compose
MAVEN := mvn -f backend/pom.xml
NPM := npm --prefix frontend
BACKUP_FILE ?= payroll-lens.dump
WEB_PORT ?= 8088

.DEFAULT_GOAL := help
.PHONY: help run up rebuild stop logs ps health seed db-up db-status db-shell db-backup \
	dev-backend dev-frontend install-frontend test test-backend test-frontend \
	build build-backend build-frontend

help: ## Show available commands
	@awk 'BEGIN { FS = ":.*## " } /^[a-zA-Z0-9_-]+:.*## / { printf "%-19s %s\n", $$1, $$2 }' $(MAKEFILE_LIST)

run: ## Start and seed the complete app (builds missing images)
	$(MAKE) up
	$(MAKE) seed

up: ## Start database, API, and web containers
	$(COMPOSE) up -d db api web

rebuild: ## Rebuild images after source changes, then restart the app
	$(COMPOSE) up --build -d db api web

stop: ## Stop containers without deleting database data
	$(COMPOSE) down

logs: ## Follow application and database logs
	$(COMPOSE) logs -f --tail=100 api web db

ps: ## Show container status
	$(COMPOSE) ps

health: ## Check the API through the web proxy
	curl -fsS "http://127.0.0.1:$(WEB_PORT)/actuator/health"

seed: db-up ## Idempotently seed 10,000 employees (run up first)
	$(COMPOSE) run --rm seed

db-up: ## Start only PostgreSQL for local development
	$(COMPOSE) up -d db

db-status: ## Check PostgreSQL readiness
	$(COMPOSE) exec -T db pg_isready -U payroll_lens -d payroll_lens

db-shell: ## Open psql in the database container
	$(COMPOSE) exec db psql -U payroll_lens -d payroll_lens

db-backup: ## Save a custom-format dump (BACKUP_FILE=payroll-lens.dump)
	@set -eu; \
		test ! -e "$(BACKUP_FILE)" || { echo "Backup already exists: $(BACKUP_FILE)" >&2; exit 1; }; \
		tmp=$$(mktemp "$(BACKUP_FILE).tmp.XXXXXX"); \
		trap 'rm -f "$$tmp"' EXIT; \
		$(COMPOSE) exec -T db pg_dump -U payroll_lens -Fc payroll_lens > "$$tmp"; \
		mv "$$tmp" "$(BACKUP_FILE)"

dev-backend: ## Run Spring Boot locally (start db-up first)
	$(MAVEN) spring-boot:run

dev-frontend: ## Run Angular locally at http://localhost:4200
	$(NPM) start

install-frontend: ## Install locked frontend dependencies
	$(NPM) ci

test: test-backend test-frontend ## Run backend and frontend tests

test-backend: ## Run JUnit and integration tests with JaCoCo
	$(MAVEN) verify

test-frontend: ## Run Angular tests once
	$(NPM) test -- --watch=false

build: build-backend build-frontend ## Build backend and frontend

build-backend: ## Package Spring Boot (includes backend tests)
	$(MAVEN) package

build-frontend: ## Build production Angular assets
	$(NPM) run build
