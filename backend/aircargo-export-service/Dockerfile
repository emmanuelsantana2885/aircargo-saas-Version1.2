FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /build
COPY pom.xml ../pom.xml
COPY aircargo-export-service/pom.xml aircargo-export-service/pom.xml
RUN mvn dependency:go-offline -B -pl aircargo-export-service -am
COPY aircargo-export-service/src aircargo-export-service/src
RUN mvn package -B -pl aircargo-export-service -am -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /build/aircargo-export-service/target/*.jar app.jar
EXPOSE 9099
ENTRYPOINT ["java", "-jar", "app.jar"]
