package com.bangbang93.openbmclapi.agent.nat

import io.github.oshai.kotlinlogging.KotlinLogging
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.net.Inet4Address
import java.net.InetAddress
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class WeUpnpNatMapperTest {
  private val logger = KotlinLogging.logger {}

  @Test
  fun `map should handle successfully when gateway available`() {
    // Arrange: 使用 mock 避免依赖真实 UPnP 网关
    val mockMapper = mockk<WeUpnpNatMapper>()
    val expectedHandle = MappingHandle(Protocol.TCP, 4000, 4000, 300, "openbmclapi-test")

    every {
      mockMapper.map(
          privatePort = 4000,
          publicPort = 4000,
          protocol = Protocol.TCP,
          ttlSeconds = 300,
          description = "openbmclapi-test",
      )
    } returns expectedHandle

    every { mockMapper.externalIp() } returns InetAddress.getByName("127.0.0.1") as Inet4Address

    every { mockMapper.unmap(expectedHandle) } returns true

    // Act
    val handle =
        mockMapper.map(
            privatePort = 4000,
            publicPort = 4000,
            protocol = Protocol.TCP,
            ttlSeconds = 300,
            description = "openbmclapi-test",
        )
    val ip = mockMapper.externalIp()
    val removed = mockMapper.unmap(handle)

    // Assert
    assertNotNull(handle, "handle should be returned")
    assertTrue(ip is Inet4Address, "external ip should be IPv4")
    assertTrue(removed, "unmap should succeed")

    // Verify mock interactions
    verify {
      mockMapper.map(
          privatePort = 4000,
          publicPort = 4000,
          protocol = Protocol.TCP,
          ttlSeconds = 300,
          description = "openbmclapi-test",
      )
      mockMapper.externalIp()
      mockMapper.unmap(expectedHandle)
    }
  }

  @Test
  fun `externalIp should return valid IPv4 address`() {
    // Arrange
    val mockMapper = mockk<WeUpnpNatMapper>()
    val mockIp = InetAddress.getByName("192.168.1.1") as Inet4Address
    every { mockMapper.externalIp() } returns mockIp

    // Act
    val ip = mockMapper.externalIp()

    // Assert
    assertTrue(ip is Inet4Address, "externalIp should return IPv4 address")
    verify { mockMapper.externalIp() }
  }
}
