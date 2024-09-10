FROM openjdk:17-jdk
ARG JAR_PATH=build/libs/*.jar
COPY ${JAR_PATH} ndp.jar
ENTRYPOINT ["java","-jar","/ndp.jar"]