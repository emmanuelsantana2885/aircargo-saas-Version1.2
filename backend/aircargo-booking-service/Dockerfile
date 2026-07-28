FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /build
COPY pom.xml .
COPY aircargo-common/pom.xml aircargo-common/
COPY aircargo-common/src aircargo-common/src
COPY aircargo-feign-clients/pom.xml aircargo-feign-clients/
COPY aircargo-feign-clients/src aircargo-feign-clients/src
COPY aircargo-booking-service/pom.xml aircargo-booking-service/
RUN mvn dependency:go-offline -B -pl aircargo-booking-service -am
COPY aircargo-booking-service/src aircargo-booking-service/src
RUN mvn package -B -pl aircargo-booking-service -am -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /build/aircargo-booking-service/target/*.jar app.jar
EXPOSE 9094
ENTRYPOINT ["java", "-jar", "app.jar"]
