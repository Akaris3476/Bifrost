# Stage 1: Build stage
FROM maven:3.9.14-amazoncorretto-25 AS build

# Copy Maven files for dependency resolution
COPY pom.xml ./
COPY .mvn .mvn

# Copy application source code
COPY src src

# Build the project and create the executable JAR
RUN mvn clean install -DskipTests

# Stage 2: Run stage
FROM amazoncorretto:25-al2023-headless

# Set working directory
WORKDIR /app

# Copy the JAR file from the build stage
COPY --from=build target/*.jar /app/bifrost-be.jar

# Install shadow-utils for useradd, then create non-root user for security
RUN dnf install -y shadow-utils && \
    useradd --system --no-create-home --shell /sbin/nologin appuser && \
    dnf remove -y shadow-utils && \
    dnf clean all
USER appuser

# Expose port 9790
EXPOSE 9790

# Set the entrypoint command for running the application
ENTRYPOINT ["java", "-jar", "/app/bifrost-be.jar"]