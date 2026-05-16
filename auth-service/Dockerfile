# Build stage
FROM maven:3.9.7-sapmachine-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:21-jre
WORKDIR /app
# Creates working directory inside container.
COPY --from=build /app/target/*.jar app.jar 
#Copies built jar from local machine → container
EXPOSE 8081
#Documents service port.
ENTRYPOINT ["java", "-jar", "app.jar"]
#Runs application when container starts.
