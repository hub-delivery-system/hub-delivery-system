FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

ARG JAR_FILE=build/libs/company-product-service-*-SNAPSHOT.jar

COPY ${JAR_FILE} app.jar

ENV TZ=Asia/Seoul
ENV SPRING_PROFILES_ACTIVE=docker

EXPOSE 19040

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
