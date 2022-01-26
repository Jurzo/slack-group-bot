FROM openjdk:11-slim-buster as build

COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .

RUN chmod +x mvnw && ./mvnw -B dependency:go-offline

COPY src src

ARG TOKEN
ARG SECRET
ENV SLACK_BOT_TOKEN=${TOKEN}
ENV SLACK_SIGNING_SECRET=${SECRET}

RUN ./mvnw -B package

FROM openjdk:11-jre-slim-buster

COPY --from=build target/*.jar .

EXPOSE 3000

ARG TOKEN
ARG SECRET
ENV SLACK_BOT_TOKEN=${TOKEN}
ENV SLACK_SIGNING_SECRET=${SECRET}

CMD SLACK_BOT_TOKEN=$(cat .env/token) SLACK_SIGNING_SECRET=$(cat .env/secret) java -jar Slackbot-0.0.1-SNAPSHOT.jar