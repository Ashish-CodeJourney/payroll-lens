# VPS deployment handoff

This is the self-hosting path for the assessment demo. The tested Compose stack runs PostgreSQL, Spring Boot, and Angular behind Nginx. Only the Angular/Nginx web port is reached by the host reverse proxy; PostgreSQL and the web port bind to VPS loopback, and the API stays on the private Compose network.

## Before starting

Use a VPS with Docker and the Compose plugin, enough memory to build the Java and Angular images, a domain pointing to the VPS, and an HTTPS reverse proxy such as Caddy. The application has no authentication; keep the database synthetic and do not enter real salary data. The public site should be served through HTTPS on ports 80/443.

## Start the stack

```sh
git clone https://github.com/Ashish-CodeJourney/payroll-lens.git
cd payroll-lens
cp .env.example .env
# Edit .env and replace POSTGRES_PASSWORD with a unique value.
docker compose up --build -d db api web
docker compose run --rm seed
curl -fsS http://127.0.0.1:8088/actuator/health
curl -fsS 'http://127.0.0.1:8088/api/employees?size=1'
```

The first health response should be `{"status":"UP"}`. The employee response should show `totalElements: 10000`. Open `http://127.0.0.1:8088/reports` on the VPS to verify direct Angular routing. The seed command is idempotent and does not overwrite edited records when the database is already populated.

Copy the [Caddyfile example](../deploy/Caddyfile.example), replace `payroll.example.com` with your domain, and reload Caddy. Its reverse proxy sends HTTPS traffic to `127.0.0.1:8088`; Caddy handles the certificate when DNS and ports 80/443 are ready. Keep the Compose web port and database port off the public interface. After DNS and HTTPS are working, repeat the health, directory, and report checks at the public domain and add that URL to the README before submission.

## Update and back up

```sh
git pull --ff-only
docker compose up --build -d db api web
docker compose ps
```

Compose keeps employee data in the named `payroll_data` volume. For a database backup, run this from the repository root on the VPS:

```sh
docker compose exec -T db pg_dump -U payroll_lens -Fc payroll_lens > payroll-lens.dump
```

Store the dump outside the public web directory. Do not run `docker compose down -v` unless you intend to delete the demo database. Review logs with `docker compose logs --tail=100 api web db` if the health check fails.
