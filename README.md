# Task Tracker

Backend-сервис для управления задачами с JWT-аутентификацией.

## Стек

- Java 21, Spring Boot 3.2, Spring Security + JWT
- PostgreSQL 15, Spring Data JPA, Liquibase
- Gradle, Docker, Testcontainers

## Сборка

```bash
./gradlew build
```

## Тесты

```bash
./gradlew test
```

## Запуск локально

Требуется запущенный PostgreSQL на `localhost:5432` с базой `tasktracker` (пользователь/пароль: `postgres`).

```bash
./gradlew bootRun
```

## Запуск через Docker

```bash
docker-compose up --build
```

- Приложение: http://localhost:8080
- pgAdmin: http://localhost:5050 (admin@admin.com / admin)
- Swagger UI: http://localhost:8080/swagger-ui/index.html

## Email-уведомления (опционально)

Задайте переменные окружения:

```env
MAIL_ENABLED=true
MAIL_USERNAME=your@gmail.com
MAIL_PASSWORD=your-app-password
```

## Примеры curl-запросов

**Регистрация:**
```bash
curl -i -X POST http://localhost:8080/register \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"pass1","repeatPassword":"pass1"}'
```

**Вход:**
```bash
curl -i -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"pass1"}'
```

**Создать задачу:**
```bash
curl -X POST http://localhost:8080/tasks \
  -H "Authorization: Bearer <JWT>" \
  -H "Content-Type: application/json" \
  -d '{"title":"Моя задача","description":"Описание"}'
```

**Список задач:**
```bash
curl http://localhost:8080/tasks \
  -H "Authorization: Bearer <JWT>"
```

**Изменить статус:**
```bash
curl -X PATCH http://localhost:8080/tasks/1/status \
  -H "Authorization: Bearer <JWT>" \
  -H "Content-Type: application/json" \
  -d '{"done":true}'
```

**Удалить задачу:**
```bash
curl -X DELETE http://localhost:8080/tasks/1 \
  -H "Authorization: Bearer <JWT>"
```

## Ссылка на репозиторий

https://github.com/YOUR_USERNAME/task-tracker
