#https://www.baeldung.com/ops/docker-cache-maven-dependencies#multi-staged-layered-build

FROM maven:latest as build
ENV HOME=/usr/app
RUN mkdir -p $HOME
WORKDIR $HOME
ADD pom.xml $HOME
RUN mvn verify --fail-never
ADD . $HOME
RUN mvn clean compile assembly:single

FROM openjdk:17-jdk-alpine 
COPY --from=build /usr/app/target/lojza-1.0.0-jar-with-dependencies.jar /app/runner.jar
ENTRYPOINT java -jar /app/runner.jar