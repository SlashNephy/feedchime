FROM gradle:7.5.0-jdk17 AS cache
WORKDIR /app
ENV GRADLE_USER_HOME /app/gradle
COPY *.gradle.kts gradle.properties /app/
RUN gradle shadowJar --parallel --console=verbose

FROM gradle:7.5.0-jdk17 AS build
WORKDIR /app
COPY --from=cache /app/gradle /home/gradle/.gradle
COPY *.gradle.kts gradle.properties /app/
COPY src/main/ /app/src/main/
RUN gradle shadowJar --parallel --console=verbose

FROM amazoncorretto:21.0.2@sha256:e268835d2ba1559ef39f9c7d9fc69501ff45143e6fbf89ada118017ad14f52c1 as runtime
WORKDIR /app

COPY --from=build /app/build/libs/feedchime-all.jar /app/feedchime.jar

WORKDIR /app
ENTRYPOINT ["java", "-jar", "/app/feedchime.jar"]
