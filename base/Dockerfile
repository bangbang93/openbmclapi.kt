# 基础镜像：在 eclipse-temurin 之上安装 tini，作为 PID 1 处理信号转发与僵尸进程回收
# 该镜像仅本地构建（不推送），由 Jib 通过 docker:// 从本地 daemon 读取
FROM eclipse-temurin:17-jre
RUN apt-get update && apt-get install -y --no-install-recommends tini \
    && rm -rf /var/lib/apt/lists/*
