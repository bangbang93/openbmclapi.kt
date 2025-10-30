package com.bangbang93.openbmclapi.agent.nat

import io.github.oshai.kotlinlogging.KotlinLogging
import org.bitlet.weupnp.GatewayDiscover
import java.net.Inet4Address
import kotlin.test.Test
import kotlin.test.assertTrue

class WeUpnpNatMapperTest {
    private val logger = KotlinLogging.logger {}

    @Test
    fun `map and unmap via WeUPnP if gateway available`() {
        val discover = GatewayDiscover()
        logger.info { "Discovering UPnP gateways..." }
        val found = discover.discover()
        logger.info { "Discovery result: found=$found, gw=${discover.validGateway}" }
        val gw =
            discover.validGateway ?: run {
                logger.info { "No UPnP IGD gateway found - skipping test" }
                return
            }

        val mapper = WeUpnpNatMapper()
        val privatePort = 4000
        val publicPort = 4000

        val handle =
            mapper.map(
                privatePort = privatePort,
                publicPort = publicPort,
                protocol = Protocol.TCP,
                ttlSeconds = 300,
                description = "openbmclapi-test",
            )

        val ip = mapper.externalIp()
        logger.info { "External IP: ${ip.hostAddress}" }
        assertTrue(ip is Inet4Address, "external ip should be IPv4")

        val removed = mapper.unmap(handle)
        logger.info { "Unmapped result: $removed" }
    }
}
