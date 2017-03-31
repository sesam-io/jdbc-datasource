FROM java:8-jre-alpine

ADD target/jdbc-datasource-1.0-SNAPSHOT.jar /srv/
COPY target/lib /srv/lib

ENTRYPOINT ["java", "-jar", "/srv/jdbc-datasource-1.0-SNAPSHOT.jar"]


