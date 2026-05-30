FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

COPY build/libs/slack-ai-service-*-SNAPSHOT.jar app.jar

EXPOSE 19070

ENTRYPOINT ["java", "-jar", "/app/app.jar"]