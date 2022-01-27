FROM openjdk:11-slim-buster as build

COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .

RUN chmod +x mvnw && ./mvnw -B dependency:go-offline -ntp -q

COPY src src

RUN ./mvnw -B package -DskipTests -ntp

FROM openjdk:11-jre-slim-buster

COPY --from=build target/*.jar .

EXPOSE 3000

ARG TOKEN
ARG SECRET
RUN test -n ${TOKEN} && test -n ${SECRET}
ENV SLACK_BOT_TOKEN=${TOKEN}
ENV SLACK_SIGNING_SECRET=${SECRET}

CMD java -jar Slackbot-0.0.1-SNAPSHOT.jar