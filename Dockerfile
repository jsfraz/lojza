# Use the Maven image with Eclipse Temurin 17 on Alpine Linux as the build stage.
FROM maven:3-eclipse-temurin-17-alpine as build

# Set an environment variable for the application's home directory.
ENV HOME=/usr/app

# Create the application's home directory.
RUN mkdir -p $HOME

# Set the working directory to the application's home directory.
WORKDIR $HOME

# Add the project's POM (Project Object Model) file to the image.
ADD pom.xml $HOME

# Run Maven to perform a verification (checking dependencies) of the project.
# The '--fail-never' flag ensures that the process continues even if verification fails.
RUN mvn verify --fail-never

# Add the entire project directory to the image.
ADD . $HOME

# Run Maven to clean the project, build the project, and create an executable JAR.
# The 'assembly:single' goal creates a JAR with dependencies.
# The '-P production' flag activates the 'production' Maven profile.
RUN mvn clean install assembly:single -P production

# Create a new stage using the Alpine Linux image with OpenJDK 17.
FROM openjdk:17-jdk-alpine

# Copy the compiled JAR with dependencies from the 'build' stage to the new stage.
COPY --from=build /usr/app/target/lojza-1.0.0-jar-with-dependencies.jar /app/runner.jar

# Define the entry point command for the Docker container.
ENTRYPOINT java -jar /app/runner.jar
