# ---------- Stage 1: build the jar ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy the POM first so dependency downloads are cached and only re-run when the POM changes.
COPY pom.xml ./
RUN mvn -q dependency:go-offline -B || true

COPY src ./src
RUN mvn -q clean package -DskipTests -B

# ---------- Stage 2: runtime ----------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Local upload directory (note: this is ephemeral on Render/Cloud Run free tiers --
# attach a persistent disk, or move media to S3/Cloudinary, before relying on it).
RUN mkdir -p /app/uploads

COPY --from=build /app/target/app.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod
# Keep the JVM heap inside the container's memory limit so it isn't OOM-killed on small plans.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"

# Informational only. The app binds to whatever $PORT the platform injects
# (see server.port: ${PORT:8080} in application.yml).
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]