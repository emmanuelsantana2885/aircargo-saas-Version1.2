FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /build
COPY pom.xml ../pom.xml
COPY aircargo-mawb-service/pom.xml aircargo-mawb-service/pom.xml
RUN mvn dependency:go-offline -B -pl aircargo-mawb-service -am
COPY aircargo-mawb-service/src aircargo-mawb-service/src
RUN mvn package -B -pl aircargo-mawb-service -am -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /build/aircargo-mawb-service/target/*.jar app.jar
EXPOSE 9095
ENTRYPOINT ["java", "-jar", "app.jar"]
