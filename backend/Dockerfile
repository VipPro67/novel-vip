# Build stage
FROM maven:3.8.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy only necessary files to leverage caching
COPY pom.xml .
COPY .env /app
RUN mvn dependency:go-offline -B

# Copy source code and build the application
COPY src ./src
RUN mvn clean package -DskipTests -B

# Run stage (smaller image)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# Install curl for healthcheck
RUN apk add --no-cache curl
# Copy the specific JAR file
COPY --from=build /app/target/novel-vippro-0.0.1-SNAPSHOT.jar app.jar

# Expose application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]