# What is it
This is a pet project, a backend part of a simple messenger with the most basic messenger functionalities implemented so far:

- Authorization  
- Chat creation with another user  
- Joining of other users into the existing chat  
- Messenging in said chats


>The project is currently incomplete.

The project has two branches:  Docker-deployment is the branch which is described here, kubernetes-deployment branch is currently behind in terms of functionality and should be overlooked.

# running
## preconditions
- **Docker** ≥ 28.3.2  
- **OpenJDK** 21.0.7

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
- port 5173 is expected to be free for the frontend, otherwise `configuration.setAllowedOrigins(List.of("http://localhost:5173")); // frontend` should be changed in the `java/com/react_spring/messenger/system/config/SecurityConfig.java`
## building
After preconditions are met, running `docker compose up -d` should build and run all required containers in Docker.  
When containers are running, starting MessengerApplication at `java/com/react_spring/messenger/MessengerApplication.java` will launch the spring app, and apply the changelog to the PostgreSQL database, completing the setup of the backend part, which should now be accessible on localhost:8080.
