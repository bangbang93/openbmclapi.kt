package com.bangbang93.openbmclapi.agent.config

import java.util.Properties

object Version {
  // 使用中文注释
  // 返回应用的版本：
  // 1) 优先尝试从包的 Implementation-Version (manifest) 读取
  // 2) 回退到 resources/version.properties 中的 'version' 键
  // 3) 最后回退到 "unspecified"
  val current: String by lazy {
    // 1) 从 manifest 获取
    val pkgVersion = Version::class.java.`package`?.implementationVersion
    if (!pkgVersion.isNullOrBlank()) {
      return@lazy pkgVersion
    }

    // 2) 从 resources/version.properties 获取
    try {
      val props = Properties()
      val stream = Version::class.java.classLoader.getResourceAsStream("version.properties")
      stream?.use { props.load(it) }
      val v = props.getProperty("version")?.takeIf { it.isNotBlank() }
      if (!v.isNullOrBlank()) {
        return@lazy v
      }
    } catch (ignored: Exception) {
      // 忽略，继续回退
    }

    // 3) 回退值
    "0.0.0"
  }
}
