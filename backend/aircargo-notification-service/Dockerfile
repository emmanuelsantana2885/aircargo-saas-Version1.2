FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /build
COPY pom.xml ../pom.xml
COPY aircargo-notification-service/pom.xml aircargo-notification-service/pom.xml
RUN mvn dependency:go-offline -B -pl aircargo-notification-service -am
COPY aircargo-notification-service/src aircargo-notification-service/src
RUN mvn package -B -pl aircargo-notification-service -am -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /build/aircargo-notification-service/target/*.jar app.jar
EXPOSE 9100
ENTRYPOINT ["java", "-jar", "app.jar"]
