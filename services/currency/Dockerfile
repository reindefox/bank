# syntax=docker/dockerfile:1.7-labs

FROM gradle:jdk21-corretto
 AS build
WORKDIR /workspace

COPY . .

RUN gradle :services:currency:bootJar --no-daemon --stacktrace -g /workspace/.gradle

FROM eclipse-temurin:21-jre AS run
WORKDIR /app
COPY --from=build /workspace/services/currency/build/libs/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java","-jar","/app/app.jar"]

