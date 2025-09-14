FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY build/libs/messenger-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

#first build app (./gradlew build -x test)  thenrun minikube docker-env to get full invocation, then docker build -t messenger:latest . and apply all kubernetes