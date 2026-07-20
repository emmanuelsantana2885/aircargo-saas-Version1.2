FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /build
COPY pom.xml ../pom.xml
COPY aircargo-api/pom.xml aircargo-api/pom.xml
RUN mvn dependency:go-offline -B -pl aircargo-api -am
COPY aircargo-api/src aircargo-api/src
RUN mvn package -B -pl aircargo-api -am -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /build/aircargo-api/target/*.jar app.jar

EXPOSE 9091
ENTRYPOINT ["java", "-jar", "app.jar"]
