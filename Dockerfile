# ===== Build stage =====
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml .
COPY src ./src

RUN mvn -B -ntp clean package -DskipTests

# ===== Runtime stage =====
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /workspace/target/quarkus-app/ /app/

ENV QUARKUS_HTTP_HOST=0.0.0.0
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar quarkus-run.jar"]