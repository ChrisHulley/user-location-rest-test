FROM gcr.io/distroless/java:11
EXPOSE 9044
EXPOSE 9045

COPY ./target/user-location-rest-test*.jar /user-location-rest-test.jar
COPY ./src/main/properties/dev.yml /config.yml

COPY ./cacerts /etc/ssl/certs/java/cacerts

ENTRYPOINT ["java", "-jar", "/user-location-rest-test.jar", "server", "/config.yml" ]
