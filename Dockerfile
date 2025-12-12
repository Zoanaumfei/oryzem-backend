# Fase 1: Build
FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app
COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B
COPY src ./src

# CORREÇÃO 1: Adiciona encoding UTF-8 ao Maven
RUN ./mvnw clean package -DskipTests -Dfile.encoding=UTF-8

# Fase 2: Runtime
FROM eclipse-temurin:17-jre-alpine

# CORREÇÃO 2: Configura locale mínimo (sem instalar pacotes)
ENV LANG=C.UTF-8

WORKDIR /app
COPY --from=build /app/target/backend-*.jar app.jar

EXPOSE 8080

# CORREÇÃO 3: Adiciona encoding UTF-8 à JVM
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8080} -Dfile.encoding=UTF-8 -jar app.jar"]