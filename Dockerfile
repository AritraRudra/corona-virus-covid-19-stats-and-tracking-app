FROM openjdk:13-jdk
VOLUME /tmp
COPY target/covid-19-app-*.jar .
ENTRYPOINT ["java","-jar","covid-19-app-0.0.1-SNAPSHOT.jar"]
