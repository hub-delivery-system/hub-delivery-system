FROM eclipse-temurin:17-jre-jammy

VOLUME /tmp

ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

ENV TZ=Asia/Seoul

EXPOSE 19010

ENTRYPOINT ["java", "-jar", "/app.jar"]
