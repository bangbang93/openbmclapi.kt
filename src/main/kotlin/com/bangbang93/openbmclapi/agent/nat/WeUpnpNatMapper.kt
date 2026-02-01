package com.bangbang93.openbmclapi.agent.nat

import io.github.oshai.kotlinlogging.KotlinLogging
import java.net.InetAddress
import org.bitlet.weupnp.GatewayDevice
import org.bitlet.weupnp.GatewayDiscover

private val logger = KotlinLogging.logger {}

class WeUpnpNatMapper : NatMapper {
    private var device: GatewayDevice? = null

    override fun map(
        privatePort: Int,
        publicPort: Int,
        protocol: Protocol,
        ttlSeconds: Int,
        description: String,
    ): MappingHandle {
        val discover = GatewayDiscover()
        discover.discover()
        val gw = discover.validGateway ?: error("未发现可用的 UPnP IGD 网关")
        device = gw

        val ok =
            when (protocol) {
                Protocol.TCP ->
                    gw.addPortMapping(
                        publicPort,
                        privatePort,
                        gw.localAddress.hostAddress,
                        "TCP",
                        description,
                    )
                Protocol.UDP ->
                    gw.addPortMapping(
                        publicPort,
                        privatePort,
                        gw.localAddress.hostAddress,
                        "UDP",
                        description,
                    )
            }
        if (!ok) error("UPnP 端口映射失败: $protocol $publicPort->$privatePort")

        logger.info { "UPnP 映射成功: $protocol $publicPort->$privatePort" }
        return MappingHandle(protocol, privatePort, publicPort, ttlSeconds, description)
    }

    override fun refresh(handle: MappingHandle): Boolean {
        // WeUPnP 不支持 TTL 刷新，直接重做一次映射作为 best-effort
        return try {
            val dev = device ?: return false
            when (handle.protocol) {
                Protocol.TCP ->
                    dev.addPortMapping(
                        handle.publicPort,
                        handle.privatePort,
                        dev.localAddress.hostAddress,
                        "TCP",
                        handle.description,
                    )
                Protocol.UDP ->
                    dev.addPortMapping(
                        handle.publicPort,
                        handle.privatePort,
                        dev.localAddress.hostAddress,
                        "UDP",
                        handle.description,
                    )
            }
            true
        } catch (e: Exception) {
            logger.error(e) { "UPnP 续期失败" }
            false
        }
    }

    override fun unmap(handle: MappingHandle): Boolean {
        return try {
            val dev = device ?: return true
            when (handle.protocol) {
                Protocol.TCP -> dev.deletePortMapping(handle.publicPort, "TCP")
                Protocol.UDP -> dev.deletePortMapping(handle.publicPort, "UDP")
            }
            true
        } catch (e: Exception) {
            logger.warn(e) { "删除映射失败" }
            false
        }
    }

    override fun externalIp(): InetAddress {
        val dev = device ?: error("尚未建立映射")
        return InetAddress.getByName(dev.externalIPAddress)
    }
}
