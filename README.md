# My Market App (Multi-module)

Проект разделен на 2 модуля:

- `main-app` - витрина магазина (WebFlux + Thymeleaf + R2DBC + Redis cache + payment client)
- `payment-service` - сервис оплаты (WebFlux, OpenAPI-generated API)

## Требования

- Java 21
- Maven 3.8+
- Docker

## Сборка

```bash
mvn clean package
```

Сборка модулей по отдельности:

```bash
mvn -pl main-app package
mvn -pl payment-service package
```

## Тесты

```bash
mvn -pl main-app,payment-service test
```

## Локальный запуск без Docker

1. Запустить Redis:

```bash
docker run --rm -p 6379:6379 redis:7-alpine
```

2. Запустить payment-service:

```bash
mvn -pl payment-service spring-boot:run
```

3. Запустить main-app:

```bash
mvn -pl main-app spring-boot:run
```

- main-app: [http://localhost:8080](http://localhost:8080)
- payment-service: [http://localhost:8081/payments/balance](http://localhost:8081/payments/balance)

## Запуск через Docker Compose

Перед запуском compose нужно собрать jar:

```bash
mvn -pl main-app,payment-service package -DskipTests
docker compose up --build
```

Keycloak поднимается в том же compose на `http://localhost:8180` (admin/admin).

## Локальная настройка Keycloak (ручная)

Для security используйте локальный Keycloak из `docker-compose.yml`.

1. Запуск только Keycloak :

```bash
docker compose up -d keycloak
```

2. Откройте [http://localhost:8180](http://localhost:8180) и войдите в admin console:
   - username: `admin`
   - password: `admin`

3. Создайте realm:
   - realm name: `shop`

4. Создайте client для `main-app` (OAuth2 client credentials).

5. Создайте client для `payment-service`.
## OpenAPI-first

Контракт оплаты хранится в:

- `payment-service/src/main/resources/openapi/payment-api.yaml`

Генерация выполняется плагином `openapi-generator-maven-plugin`:

- server API в `payment-service`
- webclient client в `main-app`

## Redis cache (main-app)

Кэш используется для:

- списка товаров (`findAll`)
- карточки товара (`findById`)

TTL в `main-app/src/main/resources/application.yml`:

- `market.cache.item-list-ttl`
- `market.cache.item-ttl`

## Оплата в оформлении заказа

При `POST /buy`:

1. Считается сумма корзины.
2. Проверяется баланс в payment-service.
3. Выполняется списание.
4. Только после успешной оплаты создается заказ и очищается корзина.

Если оплата не проходит или payment-service недоступен, заказ не создается, корзина не очищается.
