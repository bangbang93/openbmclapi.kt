package com.bangbang93.openbmclapi.agent.storage

import com.bangbang93.openbmclapi.agent.config.ClusterConfig
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class StorageFactoryTest :
    DescribeSpec({
      describe("StorageFactory") {
        describe("FileStorage") {
          it("创建文件存储成功") {
            val config = ClusterConfig(clusterId = "test", clusterSecret = "test", storage = "file")
            val factory = StorageFactory(config)
            val storage = factory.createStorage()

            storage.shouldBeInstanceOf<FileStorage>()
          }
        }

        describe("WebDAV Storage") {
          it("创建WebDAV存储成功") {
            val config =
                ClusterConfig(
                    clusterId = "test",
                    clusterSecret = "test",
                    storage = "webdav",
                    storageOpts =
                        mapOf(
                            "url" to "https://webdav.example.com",
                            "username" to "user",
                            "password" to "pass",
                            "basePath" to "/test",
                        ),
                )
            val factory = StorageFactory(config)
            val storage = factory.createStorage()

            storage.shouldBeInstanceOf<WebdavStorage>()
          }

          it("创建Alist WebDAV存储成功") {
            val config =
                ClusterConfig(
                    clusterId = "test",
                    clusterSecret = "test",
                    storage = "alist",
                    storageOpts =
                        mapOf(
                            "url" to "https://alist.example.com",
                            "username" to "user",
                            "password" to "pass",
                            "basePath" to "/test",
                            "cacheTtl" to "3600000",
                        ),
                )
            val factory = StorageFactory(config)
            val storage = factory.createStorage()

            storage.shouldBeInstanceOf<AlistWebdavStorage>()
          }
        }

        describe("Cloud Storage") {
          it("创建MinIO存储成功") {
            val config =
                ClusterConfig(
                    clusterId = "test",
                    clusterSecret = "test",
                    storage = "minio",
                    storageOpts =
                        mapOf(
                            "url" to
                                "http://test-access-key:test-secret-key@minio.example.com:9000/bucket"
                        ),
                )
            val factory = StorageFactory(config)
            val storage = factory.createStorage()

            storage.shouldBeInstanceOf<MinioStorage>()
          }

          it("创建OSS存储成功") {
            val config =
                ClusterConfig(
                    clusterId = "test",
                    clusterSecret = "test",
                    storage = "oss",
                    storageOpts =
                        mapOf(
                            "accessKeyId" to "test-key",
                            "accessKeySecret" to "test-secret",
                            "bucket" to "test-bucket",
                        ),
                )
            val factory = StorageFactory(config)
            val storage = factory.createStorage()

            storage.shouldBeInstanceOf<OssStorage>()
          }
        }

        describe("Error Handling") {
          it("缺少必需参数应该抛出异常") {
            val config =
                ClusterConfig(
                    clusterId = "test",
                    clusterSecret = "test",
                    storage = "webdav",
                    storageOpts =
                        mapOf(
                            "url" to "https://webdav.example.com"
                            // 缺少 basePath
                        ),
                )
            val factory = StorageFactory(config)

            shouldThrow<IllegalArgumentException> { factory.createStorage() }
          }

          it("不支持的存储类型应该抛出异常") {
            val config =
                ClusterConfig(
                    clusterId = "test",
                    clusterSecret = "test",
                    storage = "unsupported",
                )
            val factory = StorageFactory(config)

            val exception = shouldThrow<IllegalStateException> { factory.createStorage() }

            exception.message shouldBe "Unsupported storage type: unsupported"
          }
        }
      }
    })
