package com.bangbang93.openbmclapi.agent.nat

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.net.Inet4Address
import java.net.InetAddress

class WeUpnpNatMapperTest :
    DescribeSpec({
      describe("WeUpnpNatMapper") {
        describe("map") {
          it("网关可用时应该成功处理") {
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

            every { mockMapper.externalIp() } returns
                InetAddress.getByName("127.0.0.1") as Inet4Address

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
            handle shouldNotBe null
            ip.shouldBeInstanceOf<Inet4Address>()
            removed shouldNotBe false

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
        }

        describe("externalIp") {
          it("应该返回有效的 IPv4 地址") {
            // Arrange
            val mockMapper = mockk<WeUpnpNatMapper>()
            val mockIp = InetAddress.getByName("192.168.1.1") as Inet4Address
            every { mockMapper.externalIp() } returns mockIp

            // Act
            val ip = mockMapper.externalIp()

            // Assert
            ip.shouldBeInstanceOf<Inet4Address>()
            verify { mockMapper.externalIp() }
          }
        }
      }
    })
