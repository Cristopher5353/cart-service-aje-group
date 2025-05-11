FROM eclipse-temurin:17-alpine

RUN addgroup -g 1028 devopsc \
    && adduser -D -G devopsc admin

VOLUME /tmp

ARG JAR_FILE

COPY ${JAR_FILE} /tmp/app.jar

RUN chown -R admin:devopsc /tmp

USER admin

ENTRYPOINT ["java","-jar","/tmp/app.jar"]