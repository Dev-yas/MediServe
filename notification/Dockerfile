# ===== Stage 1: Build with Maven =====
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy POM first and download dependencies (cache boost)
COPY pom.xml .

RUN mvn dependency:go-offline -B

# Copy the full source after dependencies
COPY src ./src

RUN mvn clean package -DskipTests

# ===== Stage 2: Run the app =====
FROM openjdk:21-jdk AS runner

WORKDIR /app

# Copy the generated JAR from builder stage
COPY --from=builder /app/target/notification-0.0.1-SNAPSHOT.jar ./app.jar

# Expose the gRPC port and any other
EXPOSE 4002
EXPOSE 9002

# Set environment variables at runtime externally (optional in Dockerfile)
# ENV MAIL_HOST=smtp.gmail.com \
#     MAIL_PORT=587 \
#     MAIL_USERNAME=example@gmail.com \
#     MAIL_PASSWORD=yourpassword

ENTRYPOINT ["java", "-jar", "app.jar"]
