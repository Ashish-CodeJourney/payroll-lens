FROM node:24-alpine AS frontend-build
WORKDIR /frontend
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

FROM maven:3.9-eclipse-temurin-21 AS backend-build
WORKDIR /backend
COPY backend/pom.xml ./
COPY backend/src ./src
COPY --from=frontend-build /frontend/dist/frontend/browser ./src/main/resources/static
RUN mvn -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=backend-build /backend/target/payroll-lens-0.1.0.jar ./app.jar
EXPOSE 10000
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
