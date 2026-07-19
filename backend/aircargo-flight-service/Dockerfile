FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /build
COPY pom.xml ../pom.xml
COPY aircargo-flight-service/pom.xml aircargo-flight-service/pom.xml
RUN mvn dependency:go-offline -B -pl aircargo-flight-service -am
COPY aircargo-flight-service/src aircargo-flight-service/src
RUN mvn package -B -pl aircargo-flight-service -am -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /build/aircargo-flight-service/target/*.jar app.jar
EXPOSE 9093
ENTRYPOINT ["java", "-jar", "app.jar"]
