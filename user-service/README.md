# User Service

First microservice in the bank application. Owns user identity, authentication,
and KYC status; publishes domain events to `user.events` on Kafka for the
Notification Service (and any future consumer) to react to.

## Stack

- Java 21, Spring Boot 3.3
- Spring Web, Spring Data JPA, Spring Security, Spring Kafka
- PostgreSQL + Flyway migrations
- JWT (jjwt) for stateless auth, BCrypt for password hashing
- Lombok

## Run locally

1. Start Postgres + Kafka + Zookeeper:
   ```
   docker compose up -d
   ```
2. Run the service:
   ```
   ./mvnw spring-boot:run
   ```
   It starts on `http://localhost:8081`.

## Endpoints

| Method | Path                    | Auth        | Description                                    |
|--------|-------------------------|-------------|-------------------------------------------------|
| POST   | `/users/register`       | none        | Create a user, publishes `UserRegistered`        |
| POST   | `/users/login`          | none        | Verifies credentials, returns a JWT              |
| GET    | `/users/{id}`           | Bearer JWT  | Fetch a user by id                               |
| PUT    | `/users/{id}/kyc-status`| Bearer JWT  | Update KYC status, publishes `KycStatusUpdated`  |

## Config (env vars)

- `DB_USERNAME`, `DB_PASSWORD` — Postgres credentials (default `postgres`/`postgres`)
- `KAFKA_BOOTSTRAP_SERVERS` — default `localhost:9092`
- `JWT_SECRET` — HMAC signing key; **override this in every environment**
- `JWT_EXPIRATION_MS` — default 1 hour

## Design notes

- Layering: `UserController` → `UserService` → `UserRepository`.
- `UserEventPublisher` publishes `UserRegistered` and `KycStatusUpdated`, keyed
  by `userId` so all of one user's events stay in order on the same partition
  — the same pattern the topic design uses for `accountId`/`transactionId` on
  `account.events` and `transaction.events`.
- The event DTOs (`UserRegisteredEvent`, `KycStatusUpdatedEvent`) are built
  with the **Builder pattern** via Lombok's `@Builder`.
- `JwtAuthenticationFilter` validates the bearer token and sets the user id as
  the authenticated principal; `SecurityConfig` wires it in before Spring
  Security's default filter and leaves only `/register` and `/login` public.
- Passwords are BCrypt-hashed; the plaintext password never reaches the DB or
  a log line.
- `spring.jpa.hibernate.ddl-auto: validate` — schema changes go through the
  Flyway migration in `src/main/resources/db/migration`, not Hibernate
  auto-DDL.

## Next up

Per the architecture diagram: Account Service, Transaction Service, Fraud
Detection Service, Notification Service — each owns its own DB and
consumes/produces the topics from the Kafka topic design (`account.events`,
`transaction.events`, `fraud.verdicts`, `notification.dlt`).
