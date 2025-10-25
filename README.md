# OpenBMCLAPI - Kotlin 版本

Kotlin/Ktor 实现的 OpenBMCLAPI 集群节点，从 [bangbang93/openbmclapi](https://github.com/bangbang93/openbmclapi) 原始 TypeScript/Node.js 版本迁移而来。

## 什么是 OpenBMCLAPI?

OpenBMCLAPI 是一个分布式文件分发网络，旨在为中国用户加速 Minecraft 资源下载。它通过创建分布式节点网络来高效提供文件，解决从 Amazon S3 下载速度慢的问题。


## 环境要求

- Java 11 或更高版本
- Kotlin 2.1.0
- Gradle 9.1.0 (已包含 wrapper)

## 配置

配置可通过环境变量或 `application.yaml` 提供。

### 必需的环境变量

| 变量 | 说明 |
|------|------|
| `CLUSTER_ID` | 你的集群 ID (从 bangbang93 处获取) |
| `CLUSTER_SECRET` | 你的集群密钥 (从 bangbang93 处获取) |

### 可选的环境变量

| 变量 | 默认值 | 说明 |
|------|---------|------|
| `CLUSTER_IP` | 自动检测 | 用户访问使用的公网 IP 或域名 |
| `CLUSTER_PORT` | 4000 | 监听端口 |
| `CLUSTER_PUBLIC_PORT` | `CLUSTER_PORT` | 对外端口 (如果与监听端口不同) |
| `CLUSTER_BYOC` | false | 自带证书模式 |
| `DISABLE_ACCESS_LOG` | false | 禁用访问日志 |
| `CLUSTER_STORAGE` | file | 存储类型: `file`, `minio`, `oss`, `webdav` |
| `SSL_KEY` | - | SSL 私钥 (BYOC 模式) |
| `SSL_CERT` | - | SSL 证书 (BYOC 模式) |
| `CLUSTER_BMCLAPI` | https://openbmclapi.bangbang93.com | 主控服务器 URL |

### 配置示例

创建 `.env` 文件或导出环境变量:

```bash
export CLUSTER_ID=你的集群ID
export CLUSTER_SECRET=你的集群密钥
export CLUSTER_PORT=4000
export CLUSTER_STORAGE=file
```

或使用 `application.yaml`:

```yaml
ktor:
    application:
        modules:
            - com.bangbang93.openbmclapi.ApplicationKt.module
    deployment:
        port: 8080

openbmclapi:
    cluster:
        id: 你的集群ID
        secret: 你的集群密钥
        port: 4000
        storage: file
```

## 构建和运行

### 构建项目

```bash
./gradlew build
```

### 运行服务器

```bash
./gradlew run
```

或运行构建的 JAR:

```bash
java -jar build/libs/openbmclapi-agent-0.0.1-all.jar
```

### 构建 Fat JAR

```bash
./gradlew buildFatJar
```

### 运行测试

```bash
./gradlew test
```

### 代码格式化

```bash
./gradlew ktlintFormat
```

## Docker 支持

即将推出 - Docker 支持将在未来更新中添加。

## 存储后端

支持多种存储后端以适应不同的部署需求:

### 文件存储 (默认)

在本地 `cache/` 目录中存储文件，使用基于哈希的目录结构:
```
cache/
  ab/
    ab12cd34ef56...
  cd/
    cd34ef56ab12...
```

配置示例:
```bash
CLUSTER_STORAGE=file
```

### WebDAV 存储

支持任何兼容 WebDAV 的存储服务（包括 Alist）:

```bash
CLUSTER_STORAGE=webdav
CLUSTER_STORAGE_OPTIONS={"url":"https://webdav.example.com","username":"user","password":"pass","basePath":"/openbmclapi"}
```

配置选项:
- `url`: WebDAV 服务器地址 (必需)
- `username`: 认证用户名 (可选)
- `password`: 认证密码 (可选)
- `basePath`: 存储基础路径 (必需)

### MinIO 存储

支持 MinIO 和其他 S3 兼容的对象存储:

```bash
CLUSTER_STORAGE=minio
CLUSTER_STORAGE_OPTIONS={"url":"http://accesskey:secretkey@minio.example.com:9000/bucket/prefix"}
```

高级配置（使用内网地址）:
```bash
CLUSTER_STORAGE_OPTIONS={"url":"http://accesskey:secretkey@minio.example.com:9000/bucket","internalUrl":"http://accesskey:secretkey@minio-internal:9000/bucket"}
```

配置选项:
- `url`: MinIO 服务器地址，格式为 `scheme://accessKey:secretKey@host:port/bucket/prefix` (必需)
- `internalUrl`: 内网访问地址，用于文件上传下载（可选，提高性能）

### 阿里云 OSS 存储

使用阿里云对象存储服务:

```bash
CLUSTER_STORAGE=oss
CLUSTER_STORAGE_OPTIONS={"accessKeyId":"your-key","accessKeySecret":"your-secret","bucket":"your-bucket","endpoint":"oss-cn-hangzhou.aliyuncs.com","prefix":"openbmclapi","proxy":"false"}
```

配置选项:
- `accessKeyId`: 阿里云访问密钥 ID (必需)
- `accessKeySecret`: 阿里云访问密钥 (必需)
- `bucket`: OSS 存储桶名称 (必需)
- `endpoint`: OSS 地区节点，如 `oss-cn-hangzhou.aliyuncs.com` (可选，默认杭州)
- `prefix`: 文件存储前缀 (可选，默认为空)
- `proxy`: 是否通过服务器代理文件，`true` 表示流式传输，`false` 表示生成签名 URL (可选，默认 `true`)

## 从 TypeScript 版本迁移的说明

此 Kotlin 实现保持与原始 TypeScript 版本的 API 兼容性，同时提供:

- **类型安全**: Kotlin 的强类型系统防止许多运行时错误
- **协程**: 使用 Kotlin 协程高效的 async/await
- **Ktor 框架**: 现代、轻量级的 Web 框架
- **JVM 性能**: 更好的性能和更低的内存占用

### 主要区别

1. **无集群/守护进程模式**: 简化为单进程模型
2. **无 nginx 集成**: 通过 Ktor 直接提供文件服务

## 代码规范

项目使用 ktlint 进行代码格式化和检查:

```bash
# 检查代码风格
./gradlew ktlintCheck

# 自动格式化代码
./gradlew ktlintFormat
```

配置文件: `.editorconfig`

## 贡献

这是一个迁移项目。欢迎贡献! 请确保:

1. 代码遵循 Kotlin 约定
2. 测试通过: `./gradlew test`
3. 构建成功: `./gradlew build`
4. 代码已格式化: `./gradlew ktlintFormat`
5. 保持与原始版本的 API 兼容性

## 许可证

MIT License - 参见 LICENSE 文件

## 致谢

- 原始 TypeScript 实现: [bangbang93/openbmclapi](https://github.com/bangbang93/openbmclapi)
- BMCLAPI 服务: [bangbang93](https://github.com/bangbang93)
- Kotlin 迁移: OpenBMCLAPI.kt 贡献者

## 支持

相关问题:
- **Kotlin 实现**: 在此仓库开 issue
- **集群注册**: 联系 bangbang93
- **BMCLAPI 服务**: 访问[原始仓库](https://github.com/bangbang93/openbmclapi)

## 状态

本项目正在积极开发中。以下特性待实现:

- UPNP 支持

