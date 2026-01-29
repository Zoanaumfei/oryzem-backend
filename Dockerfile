# Fase 1: Build
FROM amazoncorretto:17-alpine AS build

WORKDIR /app

# Cache de dependências Maven
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw

# Download dependencies primeiro (cache mais eficiente)
RUN ./mvnw dependency:go-offline -B

# Copiar código e compilar
COPY src ./src
RUN ./mvnw clean package -DskipTests -Dfile.encoding=UTF-8

# Fase 2: Runtime
FROM amazoncorretto:17-alpine

# Metadados
LABEL maintainer="seu-email@exemplo.com"
LABEL description="Backend Oryzem com Spring Boot e DynamoDB"

# Configurações de locale e timezone
ENV LANG=C.UTF-8
ENV TZ=America/Sao_Paulo

# Instalar dependências mínimas para produção
RUN apk add --no-cache \
    tzdata \
    curl \
    && rm -rf /var/cache/apk/*

# Criar usuário não-root para segurança
RUN addgroup -S spring && adduser -S spring -G spring

# Criar diretório para logs com permissões
RUN mkdir -p /app/logs && chown -R spring:spring /app

WORKDIR /app

# Copiar artefato do build stage
COPY --from=build --chown=spring:spring /app/target/backend-*.jar app.jar

# Mudar para usuário não-root
USER spring:spring

# Expor porta (Render usa variável PORT)
EXPOSE 8080

# Health check para Render (obrigatório para monitoramento)
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Otimizações JVM para produção no Render
ENV JAVA_OPTS="\
  -Xms256m \
  -Xmx512m \
  -XX:MaxRAM=512m \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:ParallelGCThreads=2 \
  -XX:ConcGCThreads=2 \
  -XX:+AlwaysPreTouch \
  -Djava.security.egd=file:/dev/./urandom \
  -Dfile.encoding=UTF-8"

# Comando de entrada otimizado para Render
ENTRYPOINT exec java $JAVA_OPTS \
  -Dserver.port=${PORT:-8080} \
  -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-prod} \
  -jar app.jar
