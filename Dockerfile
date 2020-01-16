FROM gcr.io/distroless/java:11
EXPOSE 9044
RUN mkdir /opt/location-rest-test

COPY ./target/location-rest-test*.jar /opt/location-rest-test
COPY ./src/main/properties/dev.yml /opt/location-rest-test/config.yml

WORKDIR /opt/location-rest-test
ENV CLASSPATH=/opt/location-rest-test:/opt/location-rest-test/.:/opt/location-rest-test/*
CMD [ "/usr/bin/java", "demo.application.TemplateApplication", "server", "/opt/location-rest-test/config.yml" ]
