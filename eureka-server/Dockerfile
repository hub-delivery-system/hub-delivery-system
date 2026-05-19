FROM eclipse-temurin:17-jdk-jammy
LABEL authors="jieunpark"

VOLUME /tmp

ARG JAR_FILE=build/libs/*.jar

COPY ${JAR_FILE} app.jar


EXPOSE 19090

ENTRYPOINT ["java","-jar","/app.jar"]