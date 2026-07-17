# =========================================================
# Multi-stage Dockerfile — KV Construction App
# Stage 1: build the fat JAR with Maven
# Stage 2: run the JAR on a slim JRE image
# =========================================================

# ---------- Stage 1: Build ----------
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Copy pom first — lets Docker cache the dependency layer
COPY pom.xml .
RUN mvn dependency:go-offline -B -q

# Copy source and build the fat JAR (skip tests during Docker build)
COPY src/ src/
RUN mvn clean package -DskipTests -B -q

# ---------- Stage 2: Run ----------
FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

# Non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

# Create uploads directory and set ownership
RUN mkdir -p /app/uploads && chown -R spring:spring /app

USER spring:spring

# Copy the fat JAR from build stage
COPY --from=build --chown=spring:spring /app/target/*.jar app.jar

EXPOSE 8080

# Container-level readiness check (uses busybox wget, already present on
# the alpine base image, against the actuator health endpoint).
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# The platform sets SPRING_PROFILES_ACTIVE=prod via environment variable.
#
# NOTE ON FILE UPLOADS: uploaded images go to Cloudflare R2 (S3-compatible
# object storage), not local disk - see FileStorageService. This is required
# because the container's local filesystem is ephemeral on most PaaS
# platforms (Render, Koyeb, etc.) and gets wiped on every redeploy/restart.
# The /app/uploads directory created above is now unused by the app itself;
# it's left in place only because WebConfig still maps /uploads/** to it for
# backward compatibility with any pre-R2 local files that may still exist.
ENTRYPOINT ["java", \
            "-XX:MaxRAMPercentage=75.0", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-jar", "app.jar"]
