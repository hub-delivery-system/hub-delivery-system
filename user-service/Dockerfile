FROM eclipse-temurin:17-jre-jammy

WORKDIR /app
ENV TZ=Asia/Seoul

ARG JAR_FILE=build/libs/user-service-*.jar
COPY ${JAR_FILE} app.jar

EXPOSE 19020
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
