# Usar JDK 17 leve
FROM eclipse-temurin:17-jdk-alpine

# Criar diretório de app
WORKDIR /app

# Copiar arquivos do projeto
COPY . .

# DAR PERMISSÃO DE EXECUÇÃO AO MVNW (linha nova)
RUN chmod +x mvnw

# Build do projeto usando Maven Wrapper
RUN ./mvnw clean package -DskipTests

# Rodar o jar
CMD ["java", "-jar", "target/backend-0.0.1-SNAPSHOT.jar"]