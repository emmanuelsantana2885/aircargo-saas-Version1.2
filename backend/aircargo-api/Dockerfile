# ── Build stage ──────────────────────────────────────
# Context must be the backend/ root (parent pom + aircargo-common + aircargo-api)
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /build

COPY pom.xml .
COPY aircargo-common/pom.xml aircargo-common/
COPY aircargo-api/pom.xml    aircargo-api/

RUN mvn dependency:go-offline -B -pl aircargo-api -am

COPY aircargo-common/src aircargo-common/src
COPY aircargo-api/src    aircargo-api/src

RUN mvn package -B -pl aircargo-api -am -DskipTests

# ── Runtime stage ────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /build/aircargo-api/target/*.jar app.jar

EXPOSE 9091
ENTRYPOINT ["java", "-jar", "app.jar"]
