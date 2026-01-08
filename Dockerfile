FROM eclipse-temurin:23-jdk-alpine

WORKDIR /app

COPY . /app

RUN chmod +x gradlew

RUN ./gradlew bootJar --no-daemon

ENV SPRING_DATASOURCE_URL=jdbc:postgresql://dpg-d0pkp5mmcj7s73e916qg-a.oregon-postgres.render.com:5432/backend_scsh
ENV SPRING_DATASOURCE_USERNAME=admin
ENV SPRING_DATASOURCE_PASSWORD=TMR7CbKg6kxdIaQanxBXkDYGqbW8YpO1
ENV CLIENT_URL=https://assistant_web_editor.vercel.app

CMD ["java", "-jar", "build/libs/backend-0.0.1-SNAPSHOT.jar"]