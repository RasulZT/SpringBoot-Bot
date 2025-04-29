FROM openjdk:17-slim

ADD ./target/telegramBot-0.0.1-SNAPSHOT.jar /app/
CMD ["java", "-Xmx2048m", "-jar", "/app/telegramBot-0.0.1-SNAPSHOT.jar", "--spring.config.location=/app/"]

EXPOSE 8080