FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

COPY build/libs/order-service-*-SNAPSHOT.jar app.jar

EXPOSE 19050

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
