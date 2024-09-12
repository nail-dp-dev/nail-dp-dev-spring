FROM openjdk:17-jdk-bullseye
ARG JAR_PATH=build/libs/*.jar
COPY ${JAR_PATH} ndp.jar
WORKDIR /app
ENTRYPOINT ["java","-jar","/ndp.jar"]