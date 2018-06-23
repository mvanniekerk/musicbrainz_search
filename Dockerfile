FROM openjdk:8-jre-alpine

ADD target/musicbrainz_search-*.jar musicbrainz_search.jar

EXPOSE 4567
CMD java -jar musicbrainz_search.jar
