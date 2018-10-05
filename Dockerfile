FROM library/openjdk:10-jre
# FROM arjones/graalvm:1.0.0-rc1
ARG DBSERVICE_VERSION=1.0

COPY target/DBService-${DBSERVICE_VERSION}-jar-with-dependencies.jar /app/DBService.jar
WORKDIR /app

EXPOSE 8080
CMD ["java", "-jar", "/app/DBService.jar"]