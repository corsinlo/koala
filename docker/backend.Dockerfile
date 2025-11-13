# ---------- Build stage ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY backend/pom.xml ./backend/pom.xml
RUN --mount=type=cache,target=/root/.m2 mvn -f backend/pom.xml -q -DskipTests dependency:go-offline
COPY backend ./backend
RUN --mount=type=cache,target=/root/.m2 mvn -f backend/pom.xml -q -DskipTests package

# ---------- Runtime stage ----------
FROM eclipse-temurin:21-jre
WORKDIR /app
# Copy DB into container (can be overridden by volume mount)
COPY backend/src/main/resources/maplewood_school.sqlite /app/data/maplewood_school.sqlite
COPY --from=build /workspace/backend/target/scheduler-0.0.1.jar /app/app.jar

ENV APP_DB_PATH=/app/data/maplewood_school.sqlite
EXPOSE 8080
ENTRYPOINT [ "java", "-jar", "/app/app.jar" ]
