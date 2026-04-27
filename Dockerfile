FROM eclipse-temurin:23-jdk-alpine

WORKDIR /app

COPY . /app

RUN chmod +x gradlew

RUN ./gradlew bootJar --no-daemon

CMD ["java", "-jar", "build/libs/backend-0.0.1-SNAPSHOT.jar"]