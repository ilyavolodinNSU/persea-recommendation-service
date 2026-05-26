# --- Этап 1: сборка ---
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

# Копируем gradle wrapper и build-файлы отдельно для кеширования зависимостей
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# Прогреваем кеш зависимостей
RUN ./gradlew dependencies --no-daemon || true

# Копируем исходный код и собираем приложение
COPY src src
RUN ./gradlew bootJar --no-daemon -x test

# --- Этап 2: runtime ---
FROM eclipse-temurin:21-jre
WORKDIR /app

# Создаём non-root пользователя
RUN groupadd -r spring && useradd -r -g spring spring

USER spring:spring

# Копируем собранный jar из builder stage
COPY --from=builder --chown=spring:spring \
  /app/build/libs/persea-recommendation-service-0.0.1-SNAPSHOT.jar app.jar

# Проверка здоровья приложения через actuator
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=5 \
  CMD wget --no-verbose --tries=1 --spider \
  http://localhost:8086/actuator/health || exit 1

# Порт recommendation-service
EXPOSE 8086

# Запуск приложения
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar", \
  "--spring.profiles.active=docker"]