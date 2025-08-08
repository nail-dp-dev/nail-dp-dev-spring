FROM openjdk:17-jdk-slim

WORKDIR /app
ARG JAR_PATH=build/libs/*.jar
COPY ${JAR_PATH} ndp.jar

ENTRYPOINT ["java","-jar","ndp.jar"]