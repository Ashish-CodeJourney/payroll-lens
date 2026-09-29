# Render assessment deployment

The assessment demo is at [payroll-lens.onrender.com](https://payroll-lens.onrender.com). The web service and database are both on Render's **free** plan in Singapore. The web service serves the compiled Angular application and Spring Boot API from one Docker image; it connects to PostgreSQL over Render's internal network. Flyway applies schema changes at startup, and the idempotent seed inserts 10,000 synthetic employees only into an empty employee table.

| Resource | Render ID | Dashboard |
| --- | --- | --- |
| Web service `payroll-lens` | `srv-datjku6gekts73b3kii0` | [Service](https://dashboard.render.com/web/srv-datjku6gekts73b3kii0) |
| PostgreSQL `payroll-lens-db` | `dpg-datjkivlot8c73fr71k0-a` | [Database](https://dashboard.render.com/d/dpg-datjkivlot8c73fr71k0-a) |

The repository's [Render Blueprint](../render.yaml) validates and describes the intended free-plan topology, including dynamic database environment variables and a `checksPass` auto-deploy trigger. The live resources were created with Render CLI after connecting the official Render MCP server because this running agent session did not expose the new MCP tools. **The live CLI-created resources are not Blueprint-managed:** their web service currently auto-deploys on commits to `main`, while GitHub Actions independently tests that branch. Render stores the live database credentials as service environment variables; no credentials are committed. The database has an empty external IP allow list.

## Free-tier limits and use

The web service sleeps after 15 minutes without incoming traffic and may take several minutes to wake because it starts a Java application. Render reports the free database expiry as **October 29, 2026 at 04:10 UTC**, 30 days after creation. It has no backups. Do not put actual employee salary data into this unauthenticated demo. The local Compose setup or [VPS deployment](vps-deployment.md) is the path for a longer-lived demonstration, with authentication and backup policy required before real-world use.

## Verification and maintenance

The deployment built commit `f1cc55d` and Render reported it `live` on September 29, 2026. That commit's [GitHub CI run](https://github.com/Ashish-CodeJourney/payroll-lens/actions/runs/36520346255) passed. A public check on September 29 received HTTP 200 from `/actuator/health` after a roughly two-minute cold start. The root page, all four Angular deep-link patterns, Swagger UI, OpenAPI JSON, the employee API, and the analytics API then returned HTTP 200. The employee API reported exactly 10,000 records and analytics reported 10,000 active employees. To repeat the public check, allow for cold start:

```sh
curl --fail --max-time 240 https://payroll-lens.onrender.com/actuator/health
curl --fail --max-time 240 'https://payroll-lens.onrender.com/api/employees?status=ALL&size=1'
```

Visit [the directory](https://payroll-lens.onrender.com/employees), [reports](https://payroll-lens.onrender.com/reports), and [Swagger UI](https://payroll-lens.onrender.com/swagger-ui.html) in a browser. The employee API's `totalElements` should be at least 10,000; it can rise as the demo is used. The seed runs on startup but does not replace saved edits or add duplicate employees. Use the Render service dashboard for deploy and application logs. CI and local tests remain the source-code gate; verify the public URL after each deployment.

This deployment is intentionally free and temporary. Render's [free-tier documentation](https://render.com/docs/free) explains service spin-down and database expiry; the [MCP setup guide](https://render.com/docs/mcp-server) documents the connection used here.
