# Stage 1: build the Spring Boot application with Maven
FROM maven:3.9.10-amazoncorretto-21 AS build

WORKDIR /app

# Copy only the files needed to resolve dependencies and compile
COPY pom.xml .
COPY src ./src

RUN mvn -B clean package -DskipTests

# Stage 2: runtime image
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY --from=build /app/target/*.jar /app/app.jar

RUN mkdir -p /app/uploads /app/uploads_information && \
	useradd -ms /bin/bash appuser && \
	chown -R appuser:appuser /app

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
