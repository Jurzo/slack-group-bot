FROM openjdk:11-slim-buster as build

COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .

RUN chmod +x mvnw && ./mvnw -B dependency:go-offline -ntp -q

COPY src src

RUN ./mvnw -B package -DskipTests -ntp

FROM openjdk:11-jre-slim-buster

COPY --from=build target/*.jar .

RUN useradd -m botuser
USER botuser

CMD java -jar -Dserver.port=$PORT Slackbot-0.0.1-SNAPSHOT.jar