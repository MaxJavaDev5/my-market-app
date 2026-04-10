# My Market App

Веб-приложение «Витрина интернет-магазина» с использованием Spring Boot на блокирующем стеке технологий.
Функциональные возможности: просмотр товаров, корзина, оформление заказов.

## Требования

- Java 21
- Maven 3.8+

## Сборка

```bash
mvn clean package
```

## Тесты

```bash
mvn test
```

## Запуск локально

```bash
java -jar target/my-market-app-0.0.1-SNAPSHOT.jar
```

Приложение слушает порт **8080** : http://localhost:8080/

## Docker

Сборка образа (после `mvn clean package`):

```bash
docker build -t my-market-app .
docker run -p 8080:8080 my-market-app
```

## База данных

В режиме разработки используется H2 в памяти.
