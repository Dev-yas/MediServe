# Stage 1: Build the app
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run the app
FROM openjdk:21-jdk AS runner

WORKDIR /app

COPY --from=builder /app/target/auth-service-0.0.1-SNAPSHOT.jar .
CMD ["java", "-jar", "auth-service-0.0.1-SNAPSHOT.jar"]
