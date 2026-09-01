# 构建阶段
FROM eclipse-temurin:17-jdk AS build
WORKDIR /src

# 先复制构建配置，利用 Docker 层缓存
COPY gradle gradle
COPY gradlew settings.gradle.kts build.gradle.kts gradle.properties ./
RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon > /dev/null 2>&1 || true

COPY src ./src
RUN ./gradlew shadowJar --no-daemon

# 运行阶段
FROM eclipse-temurin:17-jre
WORKDIR /app

RUN useradd --create-home --uid 10001 openbmclapi

COPY --from=build /src/build/libs/*-all.jar /app/openbmclapi-agent.jar
RUN chown -R openbmclapi /app

USER openbmclapi

ENV CLUSTER_PORT=4000
EXPOSE 4000
VOLUME /app/cache

ENTRYPOINT ["java", "-jar", "/app/openbmclapi-agent.jar"]
