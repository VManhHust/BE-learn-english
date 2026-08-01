# syntax=docker/dockerfile:1.7

FROM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN --mount=type=cache,target=/root/.m2 chmod +x mvnw \
    && ./mvnw --batch-mode --no-transfer-progress dependency:go-offline

COPY src/ src/
RUN --mount=type=cache,target=/root/.m2 ./mvnw --batch-mode --no-transfer-progress -DskipTests clean package \
    && cp target/*-SNAPSHOT.jar application.jar

FROM eclipse-temurin:21-jre-jammy AS runtime

RUN apt-get update \
    && apt-get install --yes --no-install-recommends ca-certificates python3 python3-venv \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system app \
    && useradd --system --gid app --home-dir /app app

WORKDIR /app

RUN python3 -m venv /opt/venv
COPY scripts/requirements.txt /tmp/python-requirements.txt
RUN /opt/venv/bin/pip install --no-cache-dir --requirement /tmp/python-requirements.txt \
    && rm /tmp/python-requirements.txt

COPY --chown=app:app scripts/ ./scripts/
COPY --from=build --chown=app:app /workspace/application.jar ./application.jar

ENV PYTHON_COMMAND=/opt/venv/bin/python \
    PYTHON_SCRIPT_PATH=/app/scripts/download_transcript.py

USER app

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/application.jar"]
