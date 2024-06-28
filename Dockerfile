FROM gradle:7.5.0-jdk17 AS cache
WORKDIR /app
ENV GRADLE_USER_HOME /app/gradle
COPY *.gradle.kts gradle.properties /app/
RUN gradle shadowJar --parallel --console=verbose

FROM gradle:6.9.1-jdk17@sha256:9acc48df9c4512e8f8d71e532562038675c571fe65c97a728b52591b84c50a4b AS build
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
