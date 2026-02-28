# ── Stage 1: Build ──────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# Cache dependency layer separately (only re-runs when pom.xml changes)
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Build the fat JAR
COPY src ./src
RUN mvn package -DskipTests -q

# ── Stage 2: Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

# Non-root user — security best practice in Kubernetes
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

WORKDIR /app

# Copy the fat JAR from the build stage
COPY --from=builder /app/target/k8s-java-demo-1.0.0.jar app.jar

# Expose the Spring Boot port
EXPOSE 8080

# JVM flags:
#   -XX:+UseContainerSupport      — respect cgroup CPU/memory limits (Kubernetes resources)
#   -XX:MaxRAMPercentage=75.0     — use 75% of container memory for heap
#   -Djava.security.egd=...       — faster startup in containers
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
