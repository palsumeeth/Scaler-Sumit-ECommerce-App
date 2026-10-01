# Use an official openJDK runtime as a parent image
FROM eclipse-temurin:17-jdk

WORKDIR /app

# JAR to the container
COPY target/email-service-1.0.0.jar app.jar

EXPOSE 8090

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]