FROM maven:3-jdk-8

ADD pom.xml pom.xml
ADD src src
RUN mvn clean
RUN mvn install -DskipTests
RUN mvn package -DskipTests

EXPOSE 4567
CMD java -jar target/musicbrainz_search-1.0.0-SNAPSHOT.jar
