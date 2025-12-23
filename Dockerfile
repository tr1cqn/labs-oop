FROM maven:3.9.9-eclipse-temurin-11 AS builder
WORKDIR /app

# Копируем только то, что нужно для сборки, чтобы кешировались зависимости
COPY pom.xml .
COPY lab1 ./lab1
COPY postman ./postman
COPY scripts ./scripts

# Собираем jar без тестов (тесты можно включить убрав -DskipTests)
RUN mvn -q -DskipTests package

FROM eclipse-temurin:11-jre
WORKDIR /app

# Кладем собранный артефакт
COPY --from=builder /app/target/lab2-functions-1.0-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]

