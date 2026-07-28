FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /build
COPY pom.xml .
COPY aircargo-common/pom.xml aircargo-common/
COPY aircargo-common/src aircargo-common/src
COPY aircargo-gateway/pom.xml aircargo-gateway/
RUN mvn dependency:go-offline -B -pl aircargo-gateway -am
COPY aircargo-gateway/src aircargo-gateway/src
RUN mvn package -B -pl aircargo-gateway -am -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /build/aircargo-gateway/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
