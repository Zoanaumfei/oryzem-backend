# Fase 1: Build
FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app
COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Fase 2: Runtime
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app
COPY --from=build /app/target/backend-*.jar app.jar

# Expor a porta (Render usa variável $PORT)
EXPOSE 8080

# Usar a porta da variável de ambiente ou 8080 como fallback
ENTRYPOINT ["java", "-Dserver.port=${PORT:-8080}", "-jar", "app.jar"]