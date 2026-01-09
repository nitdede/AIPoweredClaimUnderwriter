# Multi-stage Dockerfile for Spring Boot application

# Stage 1: Build the application
FROM openjdk:17-jdk-slim AS builder

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn
COPY pom.xml .

# Make Maven wrapper executable
RUN chmod +x mvnw

# Download dependencies (this layer will be cached unless pom.xml changes)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build the application
RUN ./mvnw clean package -DskipTests -B

# Stage 2: Create the runtime image
FROM openjdk:17-jre-slim AS runtime

# Set working directory
WORKDIR /app

# Create non-root user for security
RUN addgroup --system spring && adduser --system spring --ingroup spring

# Copy the JAR file from the builder stage
COPY --from=builder /app/target/demo-*.jar app.jar

# Create logs directory
RUN mkdir logs && chown spring:spring logs

# Change ownership of the app directory
RUN chown -R spring:spring /app

# Switch to non-root user
USER spring

# Expose the port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

# Optional: Add JVM tuning parameters
# ENTRYPOINT ["java", "-Xms512m", "-Xmx1024m", "-XX:+UseG1GC", "-jar", "app.jar"]