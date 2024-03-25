FROM gradle:8.7.0-jdk17@sha256:d486c0c6495e938a69b8a3fdbf7519d38ed817a61002d6b84d74ca25bf7bdd7f AS cache
WORKDIR /app
ENV GRADLE_USER_HOME /app/gradle
COPY *.gradle.kts gradle.properties /app/
RUN gradle shadowJar --parallel --console=verbose

FROM gradle:8.7.0-jdk17@sha256:d486c0c6495e938a69b8a3fdbf7519d38ed817a61002d6b84d74ca25bf7bdd7f AS build
WORKDIR /app
COPY --from=cache /app/gradle /home/gradle/.gradle
COPY *.gradle.kts gradle.properties /app/
COPY src/main/ /app/src/main/
RUN gradle shadowJar --parallel --console=verbose

FROM amazoncorretto:18.0.1 as runtime
WORKDIR /app

COPY --from=build /app/build/libs/feedchime-all.jar /app/feedchime.jar

WORKDIR /app
ENTRYPOINT ["java", "-jar", "/app/feedchime.jar"]
