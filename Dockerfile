# Multi-stage Dockerfile for building and running Spring Boot (Java 21)

# Build stage - uses project's Maven wrapper to build the fat JAR
FROM maven:3.9.4-eclipse-temurin-21 AS build
WORKDIR /workspace

# Copy only necessary files first to leverage Docker layer cache
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

# Ensure wrapper is executable
RUN chmod +x ./mvnw || true

# Build the application (skip tests for faster image build; remove -DskipTests to run tests)
RUN ./mvnw -B -DskipTests package

# Runtime stage
FROM eclipse-temurin:21-jre-jammy
ARG JAR_FILE=target/*.jar
WORKDIR /app

# Copy artifact from build stage
COPY --from=build /workspace/target/*.jar app.jar

# Expose port used by Spring Boot
EXPOSE 8080

# Use a non-root user (optional) — create and use 'app' user
RUN useradd -ms /bin/bash app || true
USER app

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app/app.jar"]
