# Multi-stage build for Spring Boot application
FROM gradle:8.10-jdk21-alpine AS build
WORKDIR /app

# Update Alpine packages for security
RUN apk update && apk upgrade && rm -rf /var/cache/apk/*

# Copy Gradle files
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle

# Download dependencies (cached layer)
RUN gradle dependencies --no-daemon || true

# Copy source code
COPY src ./src

# Build the application
RUN gradle bootJar --no-daemon

# Runtime stage - Using latest Java 21 LTS patch version for security
FROM eclipse-temurin:21.0.9_10-jre-alpine
WORKDIR /app

# Update Alpine packages for security and install wget for health checks (as root)
RUN apk update && apk upgrade && apk add --no-cache wget && rm -rf /var/cache/apk/*

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring

# Copy config.yml (single config file with placeholders, no secrets)
# Secrets will be loaded from environment variables in ECS
COPY config.yml /app/config.yml
RUN chown spring:spring /app/config.yml

# Copy the JAR from build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Note: Running as root to allow binding to port 443 (Fargate doesn't support NET_BIND_SERVICE capability)
# Fargate containers are isolated, so this is acceptable for this use case

# Expose HTTP & HTTPS ports
EXPOSE 8080 443

# Health check - will use HTTP or HTTPS based on ENABLE_HTTPS env var
# Note: ECS may override this with its own health checks
HEALTHCHECK --interval=30s --timeout=3s --start-period=120s --retries=3 \
  CMD sh -c 'if [ "$ENABLE_HTTPS" = "true" ]; then wget --no-verbose --tries=1 --spider --no-check-certificate https://localhost:443/ || exit 1; else wget --no-verbose --tries=1 --spider http://localhost:8080/ || exit 1; fi'

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

