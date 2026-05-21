FROM openjdk:21-jdk-slim

WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY .mvn/ .mvn/
COPY mvnw mvnw.cmd pom.xml ./

# Make mvnw executable
RUN chmod +x mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src/ src/

# Build the application
RUN ./mvnw package -DskipTests

# Run the application
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "target/booking-system-1.0.0.jar"]