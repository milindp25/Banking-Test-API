FROM amazoncorretto:17
# Use a wildcard that excludes "plain"
ARG JAR_FILE=build/libs/*-SNAPSHOT.jar

# Copy the JAR file into the container as app.jar
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]
