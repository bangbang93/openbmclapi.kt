# OpenBMCLAPI - Kotlin Edition

[English](README_KT.md) | [中文文档](README_KT_CN.md)

A Kotlin/Ktor implementation of the OpenBMCLAPI cluster node, migrated from the original TypeScript/Node.js version at [bangbang93/openbmclapi](https://github.com/bangbang93/openbmclapi).

## What is OpenBMCLAPI?

OpenBMCLAPI is a distributed file distribution network designed to accelerate Minecraft resource downloads for users in China. It addresses slow download speeds from Amazon S3 by creating a network of distributed nodes that serve Minecraft files efficiently.

## Features

- ✅ File synchronization from BMCLAPI master server
- ✅ Hash-based file verification (MD5/SHA-1)
- ✅ Signature-based authentication for file downloads
- ✅ Token-based API authentication with automatic refresh
- ✅ Local file storage backend
- ✅ Bandwidth measurement endpoint
- ✅ Configurable via environment variables or application.yaml
- 🚧 WebSocket-based cluster communication
- 🚧 Keepalive mechanism
- 🚧 Multiple storage backends (File, MinIO, OSS, WebDAV)
- 🚧 UPNP port mapping support

## Requirements

- Java 11 or higher
- Kotlin 2.2.20
- Gradle 9.1.0 (wrapper included)

## Configuration

Configuration can be provided via environment variables or `application.yaml`.

### Required Environment Variables

| Variable | Description |
|----------|-------------|
| `CLUSTER_ID` | Your cluster ID (obtain from bangbang93) |
| `CLUSTER_SECRET` | Your cluster secret (obtain from bangbang93) |

### Optional Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `CLUSTER_IP` | Auto-detect | Public IP or domain for user access |
| `CLUSTER_PORT` | 4000 | Port to listen on |
| `CLUSTER_PUBLIC_PORT` | `CLUSTER_PORT` | Public port (if different from listen port) |
| `CLUSTER_BYOC` | false | Bring Your Own Certificate mode |
| `DISABLE_ACCESS_LOG` | false | Disable access logging |
| `ENABLE_NGINX` | false | Use nginx for file serving |
| `ENABLE_UPNP` | false | Enable UPNP port mapping |
| `CLUSTER_STORAGE` | file | Storage type: `file`, `minio`, `oss`, `webdav` |
| `SSL_KEY` | - | SSL private key (BYOC mode) |
| `SSL_CERT` | - | SSL certificate (BYOC mode) |
| `CLUSTER_BMCLAPI` | https://openbmclapi.bangbang93.com | Master server URL |

### Example Configuration

Create a `.env` file or export environment variables:

```bash
export CLUSTER_ID=your-cluster-id
export CLUSTER_SECRET=your-cluster-secret
export CLUSTER_PORT=4000
export CLUSTER_STORAGE=file
```

Or use `application.yaml`:

```yaml
ktor:
    application:
        modules:
            - com.bangbang93.openbmclapi.ApplicationKt.module
    deployment:
        port: 8080

openbmclapi:
    cluster:
        id: your-cluster-id
        secret: your-cluster-secret
        port: 4000
        storage: file
```

## Building & Running

### Build the Project

```bash
./gradlew build
```

### Run the Server

```bash
./gradlew run
```

Or run the built JAR:

```bash
java -jar build/libs/openbmclapi-0.0.1-all.jar
```

### Build Fat JAR

```bash
./gradlew buildFatJar
```

### Run Tests

```bash
./gradlew test
```

## Docker Support

Coming soon - Docker support will be added in future updates.

## API Endpoints

### File Download
```
GET /download/{hash}?s={signature}&e={expiry}
```

Downloads a file by its hash. Requires valid signature and expiry time.

### Bandwidth Measurement
```
GET /measure/{size}?s={signature}&e={expiry}
```

Returns `size` MB of test data for bandwidth measurement (max 200 MB).

### Health Check
```
GET /
```

Returns cluster status.

## Architecture

The Kotlin implementation follows a modular architecture:

- **Configuration** (`config/`): Application configuration loading
- **Models** (`model/`): Data classes for API communication
- **Storage** (`storage/`): Storage abstraction layer with file system implementation
- **Service** (`service/`): Core services (TokenManager, ClusterService)
- **Routes** (`routes/`): HTTP endpoint handlers
- **Utilities** (`util/`): Hash verification and signature validation

## Storage Backends

### File Storage (Default)

Stores files in the local `cache/` directory with a hash-based directory structure:
```
cache/
  ab/
    ab12cd34ef56...
  cd/
    cd34ef56ab12...
```

### Future Storage Backends

- **MinIO**: S3-compatible object storage
- **Aliyun OSS**: Alibaba Cloud Object Storage Service
- **WebDAV**: WebDAV-based storage (supports Alist)

## Migration Notes from TypeScript Version

This Kotlin implementation maintains API compatibility with the original TypeScript version while offering:

- **Type Safety**: Kotlin's strong type system prevents many runtime errors
- **Coroutines**: Efficient async/await using Kotlin coroutines
- **Ktor Framework**: Modern, lightweight web framework
- **JVM Performance**: Better performance and lower memory footprint

### Key Differences

1. **No cluster/daemon mode**: Simplified to single-process model
2. **Simplified WebSocket**: Basic WebSocket support (full Socket.IO in progress)
3. **File storage only**: Additional storage backends to be implemented
4. **No nginx integration yet**: Direct file serving via Ktor

## Contributing

This is a migration project. Contributions are welcome! Please ensure:

1. Code follows Kotlin conventions
2. Tests pass: `./gradlew test`
3. Build succeeds: `./gradlew build`
4. API compatibility is maintained with original version

## License

MIT License - see LICENSE file

## Credits

- Original TypeScript implementation: [bangbang93/openbmclapi](https://github.com/bangbang93/openbmclapi)
- BMCLAPI service: [bangbang93](https://github.com/bangbang93)
- Kotlin migration: OpenBMCLAPI.kt contributors

## Support

For issues related to:
- **Kotlin implementation**: Open an issue in this repository
- **Cluster registration**: Contact bangbang93
- **BMCLAPI service**: Visit the [original repository](https://github.com/bangbang93/openbmclapi)

## Status

This project is in active development. The following features are implemented:

- ✅ Core configuration system
- ✅ Token-based authentication
- ✅ File storage backend
- ✅ Download endpoints with signature verification
- ✅ Hash validation
- 🚧 WebSocket cluster communication
- 🚧 File synchronization
- 🚧 Keepalive mechanism
- ⏳ Additional storage backends
- ⏳ UPNP support
- ⏳ Nginx integration

Legend: ✅ Complete | 🚧 In Progress | ⏳ Planned
