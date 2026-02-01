package com.bangbang93.openbmclapi.agent.nat

import java.net.InetAddress

/** NAT 端口映射抽象：封装 UPnP / NAT-PMP / PCP 的最小能力集合。 */
interface NatMapper {
  /** 建立端口映射，返回可用于续期与清理的句柄。 */
  fun map(
      privatePort: Int,
      publicPort: Int = privatePort,
      protocol: Protocol = Protocol.TCP,
      ttlSeconds: Int = 3600,
      description: String = "openbmclapi",
  ): MappingHandle

  /** 刷新现有映射（续期）。返回是否成功。 */
  fun refresh(handle: MappingHandle): Boolean

  /** 删除映射。返回是否成功。 */
  fun unmap(handle: MappingHandle): Boolean

  /** 获取外网 IP（若可用）。 */
  fun externalIp(): InetAddress
}

enum class Protocol {
  TCP,
  UDP
}

data class MappingHandle(
    val protocol: Protocol,
    val privatePort: Int,
    val publicPort: Int,
    val ttlSeconds: Int,
    val description: String,
)
