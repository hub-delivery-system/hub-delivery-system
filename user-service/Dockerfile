FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

COPY build/libs/user-service-*-SNAPSHOT.jar app.jar

EXPOSE 19020

ENTRYPOINT ["java", "-jar", "/app/app.jar"]