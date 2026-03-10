FROM eclipse-temurin:17-jdk

WORKDIR /app
ARG JAR_PATH=build/libs/*.jar
COPY ${JAR_PATH} ndp.jar

ENTRYPOINT ["java","-jar","ndp.jar"]