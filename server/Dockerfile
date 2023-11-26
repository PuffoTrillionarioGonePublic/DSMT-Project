# build driver
FROM maven:3-openjdk-17 as builder-driver

WORKDIR /app

COPY driver/pom.xml /app/pom.xml
COPY driver/libs /app/libs
COPY driver/src /app/src

RUN --mount=type=cache,target=/root/.m2 mvn package -Dmaven.test.skip=true

# build spring app
FROM maven:3-openjdk-17 as builder-spring

WORKDIR /app

COPY spring/pom.xml /app/pom.xml
COPY --from=builder-driver /app/target/erldb-driver-*.jar /app/lib/erldb-driver.jar
COPY spring/src /app/src

RUN --mount=type=cache,target=/root/.m2 mvn install:install-file -Dfile=/app/lib/erldb-driver.jar -DgroupId=com.erldb -DartifactId=erldb-driver -Dversion=1.0 -Dpackaging=jar
RUN --mount=type=cache,target=/root/.m2 mvn package -Dmaven.test.skip=true

# run
FROM openjdk:17 as runner

WORKDIR /app

COPY --from=builder-spring /app/target/DSMTProject-*.jar app.jar 

EXPOSE 8084
ENV ERLDB_CONNECTION_STRING=jdbc:erldb://localhost:8080,localhost:8081,localhost:8082

ENTRYPOINT ["java", "-jar", "./app.jar"]
