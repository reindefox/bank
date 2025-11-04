# syntax=docker/dockerfile:1.7-labs

FROM gradle:jdk21-corretto AS build
WORKDIR /workspace

# Copy the entire monorepo to allow Gradle to resolve settings and dependencies
COPY . .

# Build only the report service fat jar
RUN gradle :services:report:bootJar --no-daemon --stacktrace -g /workspace/.gradle

FROM eclipse-temurin:21-jre AS run
WORKDIR /app
COPY --from=build /workspace/services/report/build/libs/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java","-jar","/app/app.jar"]

