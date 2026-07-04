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

# Koyeb sets SPRING_PROFILES_ACTIVE=prod via environment variable
#
# NOTE ON FILE UPLOADS: the /app/uploads directory below lives on the
# container's local, ephemeral filesystem. On most PaaS platforms (Koyeb
# included) this is wiped on every redeploy/restart/scale event, so uploaded
# images and documents will be lost. For production, mount a persistent
# volume at /app/uploads or switch FileStorageService to an object store
# (e.g. S3-compatible storage) - see the accompanying report for details.
ENTRYPOINT ["java", \
            "-XX:MaxRAMPercentage=75.0", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-jar", "app.jar"]
