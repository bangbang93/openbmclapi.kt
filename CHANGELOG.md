# Changelog

本项目的重要变更记录于此文件。
格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/)，版本号遵循 [语义化版本](https://semver.org/lang/zh-CN/)。

## [1.0.1] - 2026-11-07

### 修复

- 修正 syncFiles 的并发限制与等待逻辑，确保全部文件下载完成
- syncFiles 改用 Channel worker 池调度下载，避免海量文件时协程数量随文件数膨胀

### 变更

- syncFiles 并发控制重构为 coroutineScope + launch，移除类级 CoroutineScope
- 升级 OpenBMCLAPI 协议版本
- 构建版本号与发布 tag 对齐，运行时 `Version.current` 上报真实版本

## [1.0.0] - 2026-09-01

首个正式发布，从 [TypeScript/Node.js 版](https://github.com/bangbang93/openbmclapi)迁移而来的完整集群节点实现。

### 新增

- 集群节点核心流程：启动引导（bootstrap）、注册上线、keepalive 查活、文件同步与校验
- 文件下载服务：`/download/{hash}` 分片下载、`/measure/{size}` 测速接口
- 多存储后端：本地文件、WebDAV、Alist、MinIO、阿里云 OSS
- HTTPS 支持：自动申请证书，兼容 BYOC 自定义证书
- UPnP 端口映射：启用开关、IPv4 校验、关停时自动清理
- 文件下载并发限制与失败重试
- 优雅停止
- Docker 镜像发布：Jib 分层构建，amd64/arm64 多架构 manifest，tini 作为 PID 1
- CI/CD：GitHub Actions 测试与 SonarQube 扫描，推送 `v*` tag 自动发布 GitHub Release（可执行 JAR）与 Docker Hub 镜像

### 变更

- 移除默认的静态文件服务支持

[1.0.1]: https://github.com/bangbang93/openbmclapi.kt/compare/v1.0.0...v1.0.1
[1.0.0]: https://github.com/bangbang93/openbmclapi.kt/releases/tag/v1.0.0
