FROM gradle:7.6.4-jdk17@sha256:50233d3e173694d1d941523836db9051365d87bd69b53ef1c5567e5f98574c52 AS cache
WORKDIR /app
ENV GRADLE_USER_HOME /app/gradle
COPY *.gradle.kts gradle.properties /app/
RUN gradle shadowJar --parallel --console=verbose

FROM gradle:7.6.4-jdk17@sha256:50233d3e173694d1d941523836db9051365d87bd69b53ef1c5567e5f98574c52 AS build
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
