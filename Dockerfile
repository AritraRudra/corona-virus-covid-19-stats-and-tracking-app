FROM openjdk:13-jdk
COPY ./target/covid-19-app-0.0.1-SNAPSHOT.jar .
ENTRYPOINT ["java","-jar","covid-19-app-0.0.1-SNAPSHOT.jar"]
