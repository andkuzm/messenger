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
After preconditions are met, running `docker compose up -d` should build and run all required containers in Docker.
When containers are running, starting MessengerApplication at `java/com/react_spring/messenger/MessengerApplication.java` will launch the spring app, and apply the changelog to the PostgreSQL database, completing the setup of the backend part, which should now be accessible on localhost:8080.

## configuration
All connection settings and secrets are read from environment variables with sensible local defaults. Override any of these for non-default or production deployments:

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
