FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /build
COPY pom.xml ../pom.xml
COPY aircargo-load-planning-service/pom.xml aircargo-load-planning-service/pom.xml
RUN mvn dependency:go-offline -B -pl aircargo-load-planning-service -am
COPY aircargo-load-planning-service/src aircargo-load-planning-service/src
RUN mvn package -B -pl aircargo-load-planning-service -am -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /build/aircargo-load-planning-service/target/*.jar app.jar
EXPOSE 9098
ENTRYPOINT ["java", "-jar", "app.jar"]
