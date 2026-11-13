# ================================
# Etapa 1: Build da aplicação
# ================================
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copia os arquivos de dependência primeiro para aproveitar o cache do Docker
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia o código-fonte e realiza o build
COPY src ./src
RUN mvn package -DskipTests

# ================================
# Etapa 2: Imagem final de execução
# ================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Cria um usuário não-root por questões de segurança.
# A pasta /app/uploads precisa pertencer a ele: sem isso a aplicação (que roda como "spring")
# não consegue criar a pasta de anexos e todo upload de foto falha com erro 500.
RUN addgroup -S spring && adduser -S spring -G spring \
    && mkdir -p /app/uploads \
    && chown -R spring:spring /app/uploads

# Copia apenas o JAR gerado na etapa de build
COPY --from=builder /app/target/*.jar app.jar

USER spring:spring

# Os anexos ficam aqui; monte um volume para não perdê-los quando o container for recriado
VOLUME /app/uploads

# Expõe a porta padrão do Spring Boot
EXPOSE 8080

# Comando para rodar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]
