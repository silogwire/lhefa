FROM library/openjdk:12
ARG DBSERVICE_VERSION=2.0

COPY target/DBService-${DBSERVICE_VERSION}-jar-with-dependencies.jar /app/DBService.jar
WORKDIR /app

EXPOSE 8080
CMD ["java", "-jar", "/app/DBService.jar"]