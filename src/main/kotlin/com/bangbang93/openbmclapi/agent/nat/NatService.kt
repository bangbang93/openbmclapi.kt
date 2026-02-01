package com.bangbang93.openbmclapi.agent.nat

import com.bangbang93.openbmclapi.agent.config.ClusterConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import java.net.Inet4Address
import java.net.InetAddress
import org.koin.core.annotation.Single

private val logger = KotlinLogging.logger {}

@Single
class NatService(
    private val config: ClusterConfig,
) {
  private val mapper: NatMapper = WeUpnpNatMapper()

  @Volatile private var handle: MappingHandle? = null

  @Volatile private var external: InetAddress? = null

  fun startIfEnabled(): InetAddress? {
    if (!config.enableUpnp) return null

    val mapped =
        mapper.map(
            privatePort = config.port,
            publicPort = config.clusterPublicPort,
            protocol = Protocol.TCP,
            ttlSeconds = 3600,
            description = "openbmclapi",
        )
    handle = mapped
    external = mapper.externalIp()

    val ip = external ?: error("无法获取外网 IP")
    validateExternalIp(ip)
    logger.info { "UPnP/NAT 端口映射成功，外网 IP: ${ip.hostAddress}" }

    // 关闭时清理端口映射
    Runtime.getRuntime()
        .addShutdownHook(
            Thread(
                {
                  try {
                    handle?.let { mapper.unmap(it) }
                  } catch (_: Exception) {}
                },
                "nat-unmap",
            ),
        )

    return ip
  }

  /** 返回最近一次建立映射时获取到的外网 IP（可能为空）。 */
  fun getExternalIpOrNull(): InetAddress? = external

  /** 校验为 IPv4 且不属于私网/回环/组播等地址段。 */
  private fun validateExternalIp(ip: InetAddress) {
    if (ip !is Inet4Address) {
      throw IllegalStateException("不支持ipv6")
    }
    if (ip.isAnyLocalAddress ||
        ip.isLoopbackAddress ||
        ip.isMulticastAddress ||
        ip.isSiteLocalAddress) {
      throw IllegalStateException("无法获取公网IP, UPNP返回的IP位于私有地址段, IP: ${ip.hostAddress}")
    }
  }
}
