# CLAUDE.md — Messenger Backend

This file provides guidance for AI assistants (Claude Code and similar tools) working in this codebase.

---

## Project Overview

A Java/Spring Boot backend for a real-time messenger application. Provides REST APIs for user authentication, chat management, and messaging, with event-driven architecture using Apache Kafka and Redis for unread message tracking.

**Status:** In progress (see README for branch notes)

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5.5 |
| Security | Spring Security 6 + JWT (JJWT 0.11.5) |
| Persistence | Spring Data JPA (Hibernate) + PostgreSQL |
| Migrations | Liquibase |
| Messaging | Apache Kafka 3.9.1 (KRaft, 3-broker cluster) |
| Caching | Redis |
| Build | Gradle (Kotlin DSL) |
| Containers | Docker Compose (dev) / Kubernetes (prod) |
| Testing | JUnit 5, Mockito, Spring MockMvc, TestContainers |

---

## Repository Structure

```
messenger/
├── src/
│   ├── main/
│   │   ├── java/com/react_spring/messenger/
│   │   │   ├── MessengerApplication.java          # Entry point
│   │   │   ├── controller/
│   │   │   │   ├── ChatController.java            # /chat endpoints
│   │   │   │   └── MessageController.java         # /message endpoints
│   │   │   ├── service/
│   │   │   │   ├── ChatService.java
│   │   │   │   └── MessageService.java
│   │   │   ├── model/                             # JPA entities + DTOs
│   │   │   │   ├── Chat.java
│   │   │   │   ├── Message.java
│   │   │   │   ├── MessageDto.java
│   │   │   │   ├── ChatCreationRequest.java
│   │   │   │   └── ...
│   │   │   ├── repository/
│   │   │   │   ├── ChatRepository.java
│   │   │   │   ├── MessageRepository.java
│   │   │   │   └── UserRepository.java
│   │   │   ├── kafka/
│   │   │   │   ├── producer/                      # ChatMessageProducer, ChatReadProducer
│   │   │   │   ├── consumer/                      # ChatMessageConsumer, ChatReadConsumer
│   │   │   │   └── model/                         # ChatMessage, ChatRead events
│   │   │   └── system/                            # Infrastructure config
│   │   │       ├── config/                        # SecurityConfig, RedisConfig, KafkaConfig
│   │   │       ├── user/                          # User subsystem (model, repo, service, controller)
│   │   │       ├── jwt/service/JwtService.java    # Token generation/validation
│   │   │       └── filter/JwtAuthenticationFilter.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── db/changelog/db.changelog-master.yaml  # Liquibase schema + seed data
│   └── test/
│       └── java/com/react_spring/messenger/
│           ├── MessengerApplicationTests.java
│           ├── UserControllerIT.java
│           └── MessageControllerIT.java
├── k8s/                                           # Kubernetes manifests
│   ├── deployment.yaml
│   ├── service.yaml
│   ├── postgres/
│   ├── redis/
│   └── kafka/{1,2,3}/
├── compose.yaml                                   # Docker Compose for local dev
├── Dockerfile
├── build.gradle
└── settings.gradle
```

---

## Development Setup

### Prerequisites

- Docker >= 28.3.2
- OpenJDK >= 21.0.7

### Starting Local Infrastructure

```bash
docker compose up -d
```

This brings up:
- **PostgreSQL** on `localhost:25432` (user: `myuser`, pass: `secret`, db: `mydatabase`)
- **Redis** on `localhost:26379`
- **Kafka cluster** (3 brokers in KRaft mode): `localhost:29092`, `localhost:29094`, `localhost:29096`

### Running the Application

```bash
./gradlew bootRun
```

App starts on `http://localhost:8080`. Liquibase automatically applies schema migrations and seeds test data (users alice, bob, carol).

### Building

```bash
./gradlew build          # Full build including tests
./gradlew build -x test  # Skip tests
```

### Running Tests

```bash
./gradlew test
```

Tests use `@SpringBootTest` with `@Transactional` for automatic rollback. Kafka producers are mocked with `@MockitoBean`.

---

## Database Schema

Managed via Liquibase (`db/changelog/db.changelog-master.yaml`). Never modify the schema by editing JPA entity annotations alone — create a new Liquibase changeset.

| Table | Key Columns |
|---|---|
| `users` | `id`, `username` (unique), `password`, `timestamp` |
| `chats` | `id`, `title` |
| `chat_users` | `chat_id`, `user_id` (many-to-many join) |
| `messages` | `id`, `sender_id`, `receiver_id`, `chat_id`, `timestamp`, `message` |

---

## API Endpoints

### Authentication (`/user/auth/**` — public)

| Method | Path | Description |
|---|---|---|
| POST | `/user/auth/register` | Register new user |
| POST | `/user/auth/login` | Login, returns JWT |

### Users (authenticated)

| Method | Path | Description |
|---|---|---|
| GET | `/user/profile` | Get current user profile |
| GET | `/user/search?username=...` | Find users by username |

### Chats (authenticated)

| Method | Path | Description |
|---|---|---|
| POST | `/chat/create` | Create a new chat |
| POST | `/chat/join/{chatId}` | Join an existing chat |
| GET | `/chat/all` | Get all chats for current user |

### Messages (authenticated)

| Method | Path | Description |
|---|---|---|
| POST | `/message/send` | Send a message |
| GET | `/message/{chatId}` | Get messages (paginated) |
| PATCH | `/message/{messageId}` | Edit a message |

---

## Authentication Flow

1. Client registers or logs in via `/user/auth/**` — receives a JWT.
2. All subsequent requests include `Authorization: Bearer <token>`.
3. `JwtAuthenticationFilter` extracts and validates the token.
4. The current user's `userId` is stored in `Authentication.getDetails()` as a `Long`.
5. Controllers retrieve it via `authentication.getDetails()`.

JWT tokens expire after **2 hours**. The secret key is currently hardcoded (see Known TODOs).

---

## Event-Driven Messaging (Kafka)

Kafka topics are auto-created with 3 partitions and replication factor 3:

| Topic | Purpose |
|---|---|
| `chat-messages` | New message events |
| `chat-read` | Read receipt events |

**Message send flow:**
1. `MessageController` saves message to DB.
2. `ChatMessageProducer` publishes event to `chat-messages`.
3. `ChatMessageConsumer` increments Redis counter: `unread:{chatId}:{receiverId}`.

**Read flow:**
1. `ChatReadProducer` publishes event to `chat-read`.
2. `ChatReadConsumer` decrements the Redis unread counter.

---

## Key Conventions

### Package Organization

Follow the existing pattern:
- `controller/` — thin HTTP layer only, delegate to services
- `service/` — all business logic
- `model/` — JPA entities and request/response DTOs
- `repository/` — Spring Data JPA interfaces
- `kafka/` — producers and consumers
- `system/` — cross-cutting concerns (security, config, JWT)

### Entity Conventions

- Use **Lombok** (`@Data`, `@NoArgsConstructor`, etc.) to reduce boilerplate.
- Annotate sensitive/internal fields with `@JsonIgnore` (e.g., `password`, `timestamp`).
- Use `@UpdateTimestamp` for automatic timestamp management.
- For many-to-many joins, use a separate join table (`chat_users`) rather than embedding.

### Authentication in Controllers

```java
// Get current user ID from authentication object
Long userId = (Long) authentication.getDetails();
```

### Pagination

`MessageService` supports cursor-based pagination using message ID (`before`/`after`). Use Spring Data `Page<T>` and `Pageable`.

### CORS

The allowed frontend origin is configured via the `CORS_ALLOWED_ORIGINS` environment variable (default: `http://localhost:5173`). Do not hardcode a URL in `SecurityConfig` — update the env var instead.

### Environment Variables

All secrets and connection strings are externalized. See README for the full table. When adding new configurable values, follow the same pattern: `${ENV_VAR:default}` in `application.properties` and read via `@Value` in the relevant class.

---

## Remaining TODOs

- **Message/Chat services**: Input validation is missing — add `@NotBlank`, `@NotNull` etc. and a global `@ControllerAdvice` for validation errors.
- **`@PreAuthorize`**: Method-level security annotations are commented out in service/controller layer — consider enabling for finer-grained authorization.

---

## Kubernetes Deployment

Manifests are in `k8s/`. The app runs as a single-replica deployment. Infrastructure (PostgreSQL, Redis, Kafka) is each deployed as a StatefulSet.

```bash
# Build Docker image
./gradlew build -x test
docker build -t messenger:latest .

# Deploy to Kubernetes
kubectl apply -f k8s/postgres/full-config.yaml
kubectl apply -f k8s/redis/full-config.yaml
kubectl apply -f k8s/kafka/1/full-config.yaml
kubectl apply -f k8s/kafka/2/full-config.yaml
kubectl apply -f k8s/kafka/3/full-config.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
```

---

## Testing Guidelines

- Prefer integration tests (`@SpringBootTest` + MockMvc) over isolated unit tests — this project favors Spring context testing.
- Use `@Transactional` on test classes so each test gets a clean database state via rollback.
- Mock external dependencies (Kafka producers) with `@MockitoBean`.
- Test setup data goes in `@BeforeEach` methods.
- Test classes follow the `*IT` suffix convention for integration tests (e.g., `UserControllerIT`).
