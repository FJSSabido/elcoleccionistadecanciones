# Etapa 1: Construcción con Maven
FROM maven:3-eclipse-temurin-17 AS build

# Copia el Maven Wrapper y pom.xml primero (para cachear dependencias)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Descarga dependencias (cacheado si pom.xml no cambia)
RUN ./mvnw dependency:go-offline -B

# Copia el código fuente
COPY src src

# Construye el JAR (skip tests para más rapidez)
RUN ./mvnw clean package -DskipTests

# Etapa 2: Imagen runtime ligera
FROM eclipse-temurin:17-jre-alpine

# Crea directorio de app
WORKDIR /app

# Copia el JAR generado
COPY --from=build /target/*.jar app.jar

# Expone el puerto (Render lo detecta automáticamente, pero es buena práctica)
EXPOSE 8080

# Comando de inicio
ENTRYPOINT ["java", "-jar", "app.jar"]