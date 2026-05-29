FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

COPY build/libs/delivery-service-*-SNAPSHOT.jar app.jar

EXPOSE 19060

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
