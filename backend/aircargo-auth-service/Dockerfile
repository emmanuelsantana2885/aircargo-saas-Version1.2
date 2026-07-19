FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /build
COPY pom.xml ../pom.xml
COPY aircargo-auth-service/pom.xml aircargo-auth-service/pom.xml
RUN mvn dependency:go-offline -B -pl aircargo-auth-service -am
COPY aircargo-auth-service/src aircargo-auth-service/src
RUN mvn package -B -pl aircargo-auth-service -am -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /build/aircargo-auth-service/target/*.jar app.jar
EXPOSE 9092
ENTRYPOINT ["java", "-jar", "app.jar"]
