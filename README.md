# What is it
This is a pet project, a backend part of a simple messenger with the most basic messenger functionalities implemented so far:

- Authorization  
- Chat creation with another user  
- Joining of other users into the existing chat  
- Messenging in said chats


>The project is in progress.

The project has two branches:  Docker-deployment is the branch which is described here, kubernetes-deployment branch is currently behind in terms of functionality and should be overlooked.

# running
## preconditions
- **Docker**>=28.3.2  
- **OpenJDK**>=21.0.7

- ports:
  - 8080
  - 25432
  - 26379
  - 9092
  - 9093
  - 29092
  - 9094
  - 9095
  - 29094
  - 9096
  - 9097
  - 29096

  are expected to be available for all the containers to be mapped properly with default settings, otherwise the ports should be changed in compose.yaml and application.properties
- port 5173 is expected to be free for the frontend, otherwise set the `CORS_ALLOWED_ORIGINS` environment variable (see Configuration below)
## building
After preconditions are met:
1. Build the application jar: `./gradlew build -x test`
2. Build the Docker image: `docker build -t messenger:latest .`
3. Run `docker compose up -d` to start all containers including the messenger app.

The Spring app will apply the Liquibase changelog to PostgreSQL on startup. The backend will be accessible on `localhost:8080`.

Alternatively, if you prefer to run the Spring app locally outside Docker (e.g. for development), skip steps 1–2, run `docker compose up -d` to start only the infrastructure (comment out the `messenger` service in `compose.yaml`), then start the app with `./gradlew bootRun`.

## configuration
All connection settings and secrets are read from environment variables with local defaults built in. Set any of these when the defaults don't match your environment:

| Environment Variable | Default (local dev) | Description |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:25432/mydatabase` | PostgreSQL JDBC URL |
| `DB_USERNAME` | `myuser` | PostgreSQL username |
| `DB_PASSWORD` | `secret` | PostgreSQL password |
| `REDIS_HOST` | `localhost` | Redis host |
| `REDIS_PORT` | `26379` | Redis port |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:29092,localhost:29094,localhost:29096` | Kafka broker list |
| `JWT_SECRET` | *(default key — change in production)* | HMAC-SHA256 signing key for JWT tokens (must be ≥ 256 bits) |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173` | Allowed frontend origin for CORS |

**Important:** Always set `JWT_SECRET` to a strong random value in any non-local environment. The default key is insecure.

### Setting environment variables

**Linux / macOS (shell export):**
```bash
export JWT_SECRET=your-very-long-random-secret-here
export CORS_ALLOWED_ORIGINS=https://yourfrontend.example.com
./gradlew bootRun
```

**Inline for a single run:**
```bash
JWT_SECRET=your-secret CORS_ALLOWED_ORIGINS=https://yourfrontend.example.com ./gradlew bootRun
```

**Docker (`-e` flags):**
```bash
docker run -e JWT_SECRET=your-secret \
           -e DB_URL=jdbc:postgresql://db-host:5432/messenger \
           -e DB_USERNAME=prod_user \
           -e DB_PASSWORD=prod_password \
           -e REDIS_HOST=redis-host \
           -e KAFKA_BOOTSTRAP_SERVERS=kafka1:9092,kafka2:9092 \
           -e CORS_ALLOWED_ORIGINS=https://yourfrontend.example.com \
           -p 8080:8080 messenger:latest
```

**Kubernetes (env in deployment manifest):**
```yaml
env:
  - name: JWT_SECRET
    valueFrom:
      secretKeyRef:
        name: messenger-secrets
        key: jwt-secret
  - name: DB_PASSWORD
    valueFrom:
      secretKeyRef:
        name: messenger-secrets
        key: db-password
  - name: CORS_ALLOWED_ORIGINS
    value: "https://yourfrontend.example.com"
```
