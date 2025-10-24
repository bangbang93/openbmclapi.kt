# OpenBMCLAPI - Kotlin Edition

[English](README.md) | [中文文档](README_CN.md)

**Note**: This is a Kotlin/Ktor port of the original [bangbang93/openbmclapi](https://github.com/bangbang93/openbmclapi) project.

For detailed documentation about this Kotlin implementation, see [README_KT.md](README_KT.md).

## Quick Start

This project was created using the [Ktor Project Generator](https://start.ktor.io) and has been enhanced to implement the OpenBMCLAPI cluster node functionality.

### Prerequisites

- Java 11 or higher  
- CLUSTER_ID and CLUSTER_SECRET from bangbang93

### Configuration

Set your cluster credentials:

```bash
export CLUSTER_ID=your-cluster-id
export CLUSTER_SECRET=your-cluster-secret
```

### Run

```bash
./gradlew run
```

The server will start and connect to the BMCLAPI master server to begin serving Minecraft resources.

## What is OpenBMCLAPI?

BMCLAPI is a service developed by @bangbang93 as part of BMCL to address slow download speeds from Amazon S3 for Minecraft resources in China. OpenBMCLAPI allows community members to run distributed nodes that help serve these files efficiently.

For more information about the project and how to participate, visit the [original repository](https://github.com/bangbang93/openbmclapi).

---

## Original Ktor Project Features

- [Ktor Documentation](https://ktor.io/docs/home.html)
- [Ktor GitHub page](https://github.com/ktorio/ktor)
- The [Ktor Slack chat](https://app.slack.com/client/T09229ZC6/C0A974TJ9). You'll need to [request an invite](https://surveys.jetbrains.com/s3/kotlin-slack-sign-up) to join.

## Features

Here's a list of features included in this project:

| Name                                                                   | Description                                                                        |
| ------------------------------------------------------------------------|------------------------------------------------------------------------------------ |
| [Koin](https://start.ktor.io/p/koin)                                   | Provides dependency injection                                                      |
| [Content Negotiation](https://start.ktor.io/p/content-negotiation)     | Provides automatic content conversion according to Content-Type and Accept headers |
| [Routing](https://start.ktor.io/p/routing)                             | Provides a structured routing DSL                                                  |
| [kotlinx.serialization](https://start.ktor.io/p/kotlinx-serialization) | Handles JSON serialization using kotlinx.serialization library                     |
| [Call Logging](https://start.ktor.io/p/call-logging)                   | Logs client requests                                                               |
| [Call ID](https://start.ktor.io/p/callid)                              | Allows to identify a request/call.                                                 |
| [Static Content](https://start.ktor.io/p/static-content)               | Serves static files from defined locations                                         |
| [AutoHeadResponse](https://start.ktor.io/p/auto-head-response)         | Provides automatic responses for HEAD requests                                     |
| [Partial Content](https://start.ktor.io/p/partial-content)             | Handles requests with the Range header                                             |
| [Default Headers](https://start.ktor.io/p/default-headers)             | Adds a default set of headers to HTTP responses                                    |
| [Compression](https://start.ktor.io/p/compression)                     | Compresses responses using encoding algorithms like GZIP                           |
| [Caching Headers](https://start.ktor.io/p/caching-headers)             | Provides options for responding with standard cache-control headers                |

## Building & Running

To build or run the project, use one of the following tasks:

| Task                                    | Description                                                          |
| -----------------------------------------|---------------------------------------------------------------------- |
| `./gradlew test`                        | Run the tests                                                        |
| `./gradlew build`                       | Build everything                                                     |
| `./gradlew buildFatJar`                 | Build an executable JAR of the server with all dependencies included |
| `./gradlew buildImage`                  | Build the docker image to use with the fat JAR                       |
| `./gradlew publishImageToLocalRegistry` | Publish the docker image locally                                     |
| `./gradlew run`                         | Run the server                                                       |
| `./gradlew runDocker`                   | Run using the local docker image                                     |

If the server starts successfully, you'll see the following output:

```
2024-12-04 14:32:45.584 [main] INFO  Application - Application started in 0.303 seconds.
2024-12-04 14:32:45.682 [main] INFO  Application - Responding at http://0.0.0.0:8080
```

