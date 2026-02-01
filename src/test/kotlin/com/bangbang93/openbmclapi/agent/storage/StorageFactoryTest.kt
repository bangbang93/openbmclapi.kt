package com.bangbang93.openbmclapi.agent.storage

import com.bangbang93.openbmclapi.agent.config.ClusterConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class StorageFactoryTest {
  @Test
  fun `创建文件存储成功`() {
    val config =
        ClusterConfig(
            clusterId = "test",
            clusterSecret = "test",
            storage = "file",
        )
    val factory = StorageFactory(config)
    val storage = factory.createStorage()

    assertTrue(storage is FileStorage, "应该创建 FileStorage 实例")
  }

  @Test
  fun `创建WebDAV存储成功`() {
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

    assertTrue(storage is WebdavStorage, "应该创建 WebdavStorage 实例")
  }

  @Test
  fun `创建Alist WebDAV存储成功`() {
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

    assertTrue(storage is AlistWebdavStorage, "应该创建 AlistWebdavStorage 实例")
  }

  @Test
  fun `创建MinIO存储成功`() {
    val config =
        ClusterConfig(
            clusterId = "test",
            clusterSecret = "test",
            storage = "minio",
            storageOpts =
                mapOf(
                    "url" to "http://test-access-key:test-secret-key@minio.example.com:9000/bucket",
                ),
        )
    val factory = StorageFactory(config)
    val storage = factory.createStorage()

    assertTrue(storage is MinioStorage, "应该创建 MinioStorage 实例")
  }

  @Test
  fun `创建OSS存储成功`() {
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

    assertTrue(storage is OssStorage, "应该创建 OssStorage 实例")
  }

  @Test
  fun `缺少必需参数应该抛出异常`() {
    val config =
        ClusterConfig(
            clusterId = "test",
            clusterSecret = "test",
            storage = "webdav",
            storageOpts =
                mapOf(
                    "url" to "https://webdav.example.com",
                    // 缺少 basePath
                ),
        )
    val factory = StorageFactory(config)

    assertFailsWith<IllegalArgumentException> { factory.createStorage() }
  }

  @Test
  fun `不支持的存储类型应该抛出异常`() {
    val config =
        ClusterConfig(
            clusterId = "test",
            clusterSecret = "test",
            storage = "unsupported",
        )
    val factory = StorageFactory(config)

    val exception = assertFailsWith<IllegalStateException> { factory.createStorage() }

    assertEquals("Unsupported storage type: unsupported", exception.message)
  }
}
