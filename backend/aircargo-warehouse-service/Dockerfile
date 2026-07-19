FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /build
COPY pom.xml ../pom.xml
COPY aircargo-warehouse-service/pom.xml aircargo-warehouse-service/pom.xml
RUN mvn dependency:go-offline -B -pl aircargo-warehouse-service -am
COPY aircargo-warehouse-service/src aircargo-warehouse-service/src
RUN mvn package -B -pl aircargo-warehouse-service -am -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /build/aircargo-warehouse-service/target/*.jar app.jar
EXPOSE 9096
ENTRYPOINT ["java", "-jar", "app.jar"]
