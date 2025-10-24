# OpenBMCLAPI - Kotlin 版本

**注意**: 这是原 [bangbang93/openbmclapi](https://github.com/bangbang93/openbmclapi) 项目的 Kotlin/Ktor 移植版本。

详细的 Kotlin 实现文档，请参阅 [README_KT.md](README_KT.md)

## 快速开始

本项目使用 [Ktor Project Generator](https://start.ktor.io) 创建，并增强实现了 OpenBMCLAPI 集群节点功能。

### 前置要求

- Java 11 或更高版本
- 从 bangbang93 处获取 CLUSTER_ID 和 CLUSTER_SECRET

### 配置

设置集群凭证:

```bash
export CLUSTER_ID=你的集群ID
export CLUSTER_SECRET=你的集群密钥
```

### 运行

```bash
./gradlew run
```

服务器将启动并连接到 BMCLAPI 主控服务器，开始提供 Minecraft 资源服务。

## 什么是 OpenBMCLAPI?

BMCLAPI 是由 @bangbang93 开发的 BMCL 的一部分，用于解决国内线路对 Minecraft 官方使用的 Amazon S3 速度缓慢的问题。OpenBMCLAPI 允许社区成员运行分布式节点，帮助高效提供这些文件。

更多关于项目和如何参与的信息，请访问[原始仓库](https://github.com/bangbang93/openbmclapi)。

---

## 原 Ktor 项目特性

以下是本项目包含的特性列表:

| 名称 | 描述 |
| ----- | ----------- |
| [Koin](https://start.ktor.io/p/koin) | 提供依赖注入 |
| [Content Negotiation](https://start.ktor.io/p/content-negotiation) | 根据 Content-Type 和 Accept 头自动进行内容转换 |
| [Routing](https://start.ktor.io/p/routing) | 提供结构化的路由 DSL |
| [kotlinx.serialization](https://start.ktor.io/p/kotlinx-serialization) | 使用 kotlinx.serialization 库处理 JSON 序列化 |
| [Call Logging](https://start.ktor.io/p/call-logging) | 记录客户端请求 |
| [Call ID](https://start.ktor.io/p/callid) | 允许识别请求/调用 |
| [Static Content](https://start.ktor.io/p/static-content) | 从定义的位置提供静态文件 |
| [AutoHeadResponse](https://start.ktor.io/p/auto-head-response) | 为 HEAD 请求提供自动响应 |
| [Partial Content](https://start.ktor.io/p/partial-content) | 处理带 Range 头的请求 |
| [Default Headers](https://start.ktor.io/p/default-headers) | 为 HTTP 响应添加默认头集 |
| [Compression](https://start.ktor.io/p/compression) | 使用 GZIP 等编码算法压缩响应 |
| [Caching Headers](https://start.ktor.io/p/caching-headers) | 提供使用标准缓存控制头响应的选项 |

## 构建和运行

要构建或运行项目，使用以下任务之一:

| 任务 | 描述 |
| ---- | ----------- |
| `./gradlew test` | 运行测试 |
| `./gradlew build` | 构建所有内容 |
| `./gradlew buildFatJar` | 构建包含所有依赖的可执行 JAR |
| `./gradlew buildImage` | 构建与 fat JAR 一起使用的 Docker 镜像 |
| `./gradlew publishImageToLocalRegistry` | 在本地发布 Docker 镜像 |
| `./gradlew run` | 运行服务器 |
| `./gradlew runDocker` | 使用本地 Docker 镜像运行 |

如果服务器成功启动，你将看到以下输出:

```
2024-12-04 14:32:45.584 [main] INFO  Application - Application started in 0.303 seconds.
2024-12-04 14:32:45.682 [main] INFO  Application - Responding at http://0.0.0.0:8080
```

## 有用的链接

- [Ktor 文档](https://ktor.io/docs/home.html)
- [Ktor GitHub](https://github.com/ktorio/ktor)  
- [Ktor Slack](https://app.slack.com/client/T09229ZC6/C0A974TJ9) - 需要[申请邀请](https://surveys.jetbrains.com/s3/kotlin-slack-sign-up)才能加入
