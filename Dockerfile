# Многоэтапная сборка для оптимизации размера образа
FROM maven:3.9-eclipse-temurin-11 AS build

WORKDIR /app

# Копируем pom.xml и загружаем зависимости (кэширование слоя)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Копируем исходный код и собираем WAR
COPY lab1 ./lab1
WORKDIR /app/lab1
RUN mvn clean package -DskipTests -B

# Финальный образ с Tomcat
FROM tomcat:9.0-jre11-temurin

# Удаляем стандартные приложения Tomcat
RUN rm -rf /usr/local/tomcat/webapps/*

# Копируем собранный WAR файл
COPY --from=build /app/lab1/target/lab2-functions-*.war /usr/local/tomcat/webapps/ROOT.war

# Создаем директорию для логов
RUN mkdir -p /app/logs

# Настраиваем переменные окружения для базы данных
ENV DB_URL=jdbc:h2:mem:lab6db;DB_CLOSE_DELAY=-1
ENV DB_DRIVER=org.h2.Driver
ENV DB_USER=sa
ENV DB_PASSWORD=
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Открываем порт Tomcat
EXPOSE 8080

# Запускаем Tomcat
CMD ["catalina.sh", "run"]

