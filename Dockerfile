FROM java:8

# Install maven
RUN apt-get update
RUN apt-get install -y maven

WORKDIR /code
CMD dockerize -wait tcp://db:5432 -timeout 60s

# Prepare by downloading dependencies
ADD pom.xml /code/pom.xml
RUN ["mvn", "dependency:resolve"]
RUN ["mvn", "verify"]

# Adding source, compile and package into a fat jar
ADD src /code/src
RUN ["mvn", "package"]

EXPOSE 4567
CMD ["/usr/lib/jvm/java-8-openjdk-amd64/bin/java", "-jar", "target/musicbrainz_search-0.0.1.jar"]