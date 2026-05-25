# Stage 1: Build the application safely
FROM gradle:8-jdk21-alpine AS builder
WORKDIR /build

# Copy build config files
COPY gradle/ gradle/
COPY gradlew settings.gradle build.gradle ./

# Cache dependencies
RUN ./gradlew dependencies --no-daemon

# Copy source code and build jar
COPY src/ src/
RUN ./gradlew bootJar -x test --no-daemon

# Stage 2: Create lightweight production JRE runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Download the OpenTelemetry Java Agent as root
RUN wget -O opentelemetry-javaagent.jar https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v2.12.0/opentelemetry-javaagent.jar

# Create a secure non-root system user and group
RUN addgroup -S spring && adduser -S spring -G spring
RUN chown -R spring:spring /app

USER spring:spring

# Copy built artifact from builder stage with secure owner permissions
COPY --chown=spring:spring --from=builder /build/build/libs/*.jar app.jar

# Expose standard Spring Boot port
EXPOSE 8080

# Run JVM loaded with the OpenTelemetry Agent alongside optimization flags
ENTRYPOINT ["java", "-XX:+UseG1GC", "-XX:+UseContainerSupport", "-javaagent:opentelemetry-javaagent.jar", "-jar", "app.jar"]