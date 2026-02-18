FROM maven:3.8.5-openjdk-17 AS build
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:17.0.1-jdk-slim
COPY --from=build /target/fic_attendance_tracker-0.0.1-SNAPSHOT.jar fic_attendance_tracker.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","fic_attendance_tracker.jar"]