FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /build
COPY pom.xml ../pom.xml
COPY aircargo-uld-service/pom.xml aircargo-uld-service/pom.xml
RUN mvn dependency:go-offline -B -pl aircargo-uld-service -am
COPY aircargo-uld-service/src aircargo-uld-service/src
RUN mvn package -B -pl aircargo-uld-service -am -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /build/aircargo-uld-service/target/*.jar app.jar
EXPOSE 9097
ENTRYPOINT ["java", "-jar", "app.jar"]
