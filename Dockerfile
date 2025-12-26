# Etapa de build con Maven preinstalado y Java 17
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copia pom.xml para cachear dependencias
COPY pom.xml .

# Descarga dependencias (se cachea si pom.xml no cambia)
RUN mvn dependency:go-offline -B

# Copia el código fuente
COPY src ./src

# Construye el JAR saltando tests
RUN mvn clean package -DskipTests

# Etapa final: runtime ligero con solo JRE 17
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copia el JAR generado en la etapa anterior
COPY --from=build /app/target/*.jar app.jar

# Expone el puerto (Render lo detecta automáticamente, pero es buena práctica)
EXPOSE 8080

# Comando de ejecución
ENTRYPOINT ["java", "-jar", "app.jar"]