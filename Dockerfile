FROM library/openjdk:10-jre
ARG DBSERVICE_VERSION=1.4

COPY target/DBService-${DBSERVICE_VERSION}-jar-with-dependencies.jar /app/DBService.jar
WORKDIR /app

EXPOSE 8080
CMD ["java", "-jar", "/app/DBService.jar"]