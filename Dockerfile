FROM library/openjdk:10-jre

COPY target/DBService-1.0-SNAPSHOT-jar-with-dependencies.jar /app/DBService.jar
COPY config.json /app/config.json
WORKDIR /app

CMD ["java", "-jar", "/app/DBService.jar"]