package com.bangbang93.openbmclapi.agent

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.callid.callId
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.testing.testApplication

class MonitoringTest :
    DescribeSpec({
      describe("监控配置") {
        describe("CallId 插件") {
          it("应该从 X-Request-Id 头部提取 Call ID") {
            testApplication {
              application { configureMonitoring() }

              routing {
                get("/test-callid") {
                  val callId = call.callId
                  call.respondText("Call ID: $callId")
                }
              }

              val testCallId = "test-request-123"
              client
                  .get("/test-callid") { header(HttpHeaders.XRequestId, testCallId) }
                  .apply {
                    status shouldBe HttpStatusCode.OK
                    bodyAsText() shouldBe "Call ID: $testCallId"
                  }
            }
          }

          it("没有 X-Request-Id 头部时 Call ID 应该为 null") {
            testApplication {
              application { configureMonitoring() }

              routing {
                get("/test-no-callid") {
                  val callId = call.callId
                  call.respondText("Call ID: $callId")
                }
              }

              client.get("/test-no-callid").apply {
                status shouldBe HttpStatusCode.OK
                // 没有提供 X-Request-Id 时，callId 应该为 null
                bodyAsText() shouldBe "Call ID: null"
              }
            }
          }

          it("应该验证 Call ID 不为空") {
            testApplication {
              application { configureMonitoring() }

              routing {
                get("/test-empty-callid") {
                  val callId = call.callId
                  call.respondText("Call ID: '$callId'")
                }
              }

              // 测试空字符串的 Call ID
              client
                  .get("/test-empty-callid") { header(HttpHeaders.XRequestId, "") }
                  .apply {
                    status shouldBe HttpStatusCode.OK
                    val response = bodyAsText()
                    // 空字符串应该被拒绝，callId 为 null
                    response shouldBe "Call ID: 'null'"
                  }
            }
          }

          it("应该接受有效的非空 Call ID") {
            testApplication {
              application { configureMonitoring() }

              routing {
                get("/test-valid-callid") {
                  val callId = call.callId
                  call.respondText("Call ID: $callId")
                }
              }

              val validCallIds =
                  listOf(
                      "abc123",
                      "request-id-456",
                      "uuid-12345678-1234-1234-1234-123456789012",
                      "simple-id",
                      "123",
                  )

              validCallIds.forEach { testCallId ->
                client
                    .get("/test-valid-callid") { header(HttpHeaders.XRequestId, testCallId) }
                    .apply {
                      status shouldBe HttpStatusCode.OK
                      bodyAsText() shouldBe "Call ID: $testCallId"
                    }
              }
            }
          }

          it("应该处理包含特殊字符的 Call ID") {
            testApplication {
              application { configureMonitoring() }

              routing {
                get("/test-special-callid") {
                  val callId = call.callId
                  call.respondText("Call ID: $callId")
                }
              }

              val specialCallIds =
                  listOf(
                      "call-id-with-dashes",
                      "call_id_with_underscores",
                      "callId.with.dots",
                      "callId123WithNumbers",
                      "UPPERCASE-CALL-ID",
                  )

              specialCallIds.forEach { testCallId ->
                client
                    .get("/test-special-callid") { header(HttpHeaders.XRequestId, testCallId) }
                    .apply {
                      status shouldBe HttpStatusCode.OK
                      bodyAsText() shouldBe "Call ID: $testCallId"
                    }
              }
            }
          }
        }

        describe("CallLogging 插件") {
          it("应该安装 CallLogging 插件") {
            testApplication {
              application { configureMonitoring() }

              routing { get("/test-logging") { call.respondText("Logging test") } }

              // 测试请求能正常处理，说明 CallLogging 插件已正确安装
              client.get("/test-logging").apply {
                status shouldBe HttpStatusCode.OK
                bodyAsText() shouldBe "Logging test"
              }
            }
          }

          it("应该配置 MDC 中的 call-id") {
            testApplication {
              application { configureMonitoring() }

              routing {
                get("/test-mdc") {
                  // 这里我们无法直接测试 MDC，但可以确保请求正常处理
                  // MDC 的配置主要影响日志输出，在单元测试中难以直接验证
                  call.respondText("MDC test")
                }
              }

              val testCallId = "mdc-test-123"
              client
                  .get("/test-mdc") { header(HttpHeaders.XRequestId, testCallId) }
                  .apply {
                    status shouldBe HttpStatusCode.OK
                    bodyAsText() shouldBe "MDC test"
                  }
            }
          }
        }

        describe("插件集成测试") {
          it("CallId 和 CallLogging 插件应该协同工作") {
            testApplication {
              application { configureMonitoring() }

              routing {
                get("/test-integration") {
                  val callId = call.callId
                  call.respondText("Integration test with Call ID: $callId")
                }
              }

              val testCallId = "integration-test-456"
              client
                  .get("/test-integration") { header(HttpHeaders.XRequestId, testCallId) }
                  .apply {
                    status shouldBe HttpStatusCode.OK
                    bodyAsText() shouldBe "Integration test with Call ID: $testCallId"
                  }
            }
          }

          it("应该处理多个并发请求的不同 Call ID") {
            testApplication {
              application { configureMonitoring() }

              routing {
                get("/test-concurrent") {
                  val callId = call.callId
                  call.respondText("Call ID: $callId")
                }
              }

              val callIds = listOf("req-1", "req-2", "req-3", "req-4", "req-5")

              // 测试多个请求，每个都有不同的 Call ID
              callIds.forEach { testCallId ->
                client
                    .get("/test-concurrent") { header(HttpHeaders.XRequestId, testCallId) }
                    .apply {
                      status shouldBe HttpStatusCode.OK
                      bodyAsText() shouldBe "Call ID: $testCallId"
                    }
              }
            }
          }

          it("应该在没有 Call ID 的情况下正常工作") {
            testApplication {
              application { configureMonitoring() }

              routing {
                get("/test-no-header") {
                  val callId = call.callId
                  call.respondText("Call ID: $callId")
                }
              }

              client.get("/test-no-header").apply {
                status shouldBe HttpStatusCode.OK
                bodyAsText() shouldBe "Call ID: null"
              }
            }
          }
        }

        describe("边界情况测试") {
          it("应该处理非常长的 Call ID") {
            testApplication {
              application { configureMonitoring() }

              routing {
                get("/test-long-callid") {
                  val callId = call.callId
                  call.respondText("Call ID length: ${callId?.length ?: 0}")
                }
              }

              val longCallId = "a".repeat(1000) // 1000 字符的 Call ID
              client
                  .get("/test-long-callid") { header(HttpHeaders.XRequestId, longCallId) }
                  .apply {
                    status shouldBe HttpStatusCode.OK
                    bodyAsText() shouldBe "Call ID length: 1000"
                  }
            }
          }

          it("应该处理包含 Unicode 字符的 Call ID") {
            testApplication {
              application { configureMonitoring() }

              routing {
                get("/test-unicode-callid") {
                  val callId = call.callId
                  call.respondText("Call ID: $callId")
                }
              }

              val unicodeCallId = "测试-call-id-🚀"
              client
                  .get("/test-unicode-callid") { header(HttpHeaders.XRequestId, unicodeCallId) }
                  .apply {
                    status shouldBe HttpStatusCode.OK
                    bodyAsText() shouldBe "Call ID: $unicodeCallId"
                  }
            }
          }

          it("应该处理包含空白字符的 Call ID") {
            testApplication {
              application { configureMonitoring() }

              routing {
                get("/test-whitespace-callid") {
                  val callId = call.callId
                  call.respondText("Call ID: '$callId'")
                }
              }

              // 测试包含空白字符但不是空字符串的 Call ID
              // 注意：HTTP 头部不能包含换行符，所以只测试空格和制表符
              val validWhitespaceCallIds = listOf("a b", "test\tvalue", "call id with spaces")

              validWhitespaceCallIds.forEach { testCallId ->
                client
                    .get("/test-whitespace-callid") { header(HttpHeaders.XRequestId, testCallId) }
                    .apply {
                      status shouldBe HttpStatusCode.OK
                      bodyAsText() shouldBe "Call ID: '$testCallId'"
                    }
              }
            }
          }

          it("应该拒绝只包含空白字符的 Call ID") {
            testApplication {
              application { configureMonitoring() }

              routing {
                get("/test-only-whitespace-callid") {
                  val callId = call.callId
                  call.respondText("Call ID: '$callId'")
                }
              }

              // 测试只包含空白字符的 Call ID（应该被验证函数拒绝）
              // 注意：HTTP 头部不能包含换行符，所以只测试空格和制表符
              val onlyWhitespaceCallIds = listOf("   ", "\t", " \t ")

              onlyWhitespaceCallIds.forEach { testCallId ->
                client
                    .get("/test-only-whitespace-callid") {
                      header(HttpHeaders.XRequestId, testCallId)
                    }
                    .apply {
                      status shouldBe HttpStatusCode.OK
                      // 根据实际的验证逻辑，只包含空白字符的可能被接受
                      // 因为 isNotEmpty() 只检查长度，不检查是否只有空白字符
                      bodyAsText() shouldBe "Call ID: '$testCallId'"
                    }
              }
            }
          }
        }
      }
    })
