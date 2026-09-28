#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")/../backend"
exec mvn -q spring-boot:run -Dspring-boot.run.arguments="--spring.main.web-application-type=none --app.seed=true"
