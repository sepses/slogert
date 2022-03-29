# ----------------------------------------------------
FROM maven:3.8.4-openjdk-17 AS MAVEN_BUILD_IMAGE

WORKDIR /var/build/slogert

COPY . .

RUN mvn clean install -DskipTests
RUN mv ./target/slogert-1.0.0-SNAPSHOT-jar-with-dependencies.jar ./target/slogert.jar


# ----------------------------------------------------
FROM python:3.8-slim-buster AS PYTHON_IMAGE

COPY requirements.txt requirements.txt
RUN pip3 install -r requirements.txt


# ----------------------------------------------------
FROM openjdk:17-slim
COPY --from=PYTHON_IMAGE / /

WORKDIR /usr/local/slogert

COPY . .

COPY --from=MAVEN_BUILD_IMAGE /var/build/slogert/target/slogert.jar ./target

ENTRYPOINT ["sh", "-c", "java -jar target/slogert.jar -c src/main/resources/config-io.yaml"]
