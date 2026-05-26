FROM eclipse-temurin:17-jdk-jammy

LABEL authors="jieunpark"

WORKDIR /app

# ⭐ 특정 JAR만 복사 (plain.jar 제외)
COPY build/libs/hub-service-*-SNAPSHOT.jar app.jar

EXPOSE 19030

ENTRYPOINT ["java", "-jar", "/app/app.jar"]