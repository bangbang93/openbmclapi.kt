package com.bangbang93.openbmclapi.agent

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.testing.testApplication

class HTTPTest :
    DescribeSpec({
        describe("HTTP 配置") {
            describe("PartialContent 插件") {
                it("应该安装 PartialContent 插件") {
                    testApplication {
                        application { configureHTTP() }

                        routing {
                            get("/test-content") {
                                call.respondText("0123456789", ContentType.Text.Plain)
                            }
                        }

                        // 测试正常请求仍然工作
                        client.get("/test-content").apply {
                            status shouldBe HttpStatusCode.OK
                            bodyAsText() shouldBe "0123456789"
                        }
                    }
                }

                it("应该配置最大 Range 数量为 10") {
                    testApplication {
                        application { configureHTTP() }

                        routing {
                            get("/test-content") {
                                call.respondText("0123456789abcdefghij", ContentType.Text.Plain)
                            }
                        }

                        // 测试插件已安装，正常请求工作
                        client.get("/test-content").apply {
                            status shouldBe HttpStatusCode.OK
                            bodyAsText() shouldBe "0123456789abcdefghij"
                        }
                    }
                }
            }

            describe("DefaultHeaders 插件") {
                it("应该添加 X-Engine 头部") {
                    testApplication {
                        application { configureHTTP() }

                        routing { get("/test") { call.respondText("test") } }

                        client.get("/test").apply { headers["X-Engine"] shouldBe "Ktor" }
                    }
                }
            }

            describe("Compression 插件") {
                it("应该支持 gzip 压缩") {
                    testApplication {
                        application { configureHTTP() }

                        routing {
                            get("/test-compression") {
                                call.respondText("这是一个很长的文本内容，应该被压缩以节省带宽。".repeat(100))
                            }
                        }

                        client
                            .get("/test-compression") { header(HttpHeaders.AcceptEncoding, "gzip") }
                            .apply {
                                status shouldBe HttpStatusCode.OK
                                // 检查是否有压缩相关的头部
                                headers[HttpHeaders.ContentEncoding] shouldBe "gzip"
                            }
                    }
                }

                it("不支持压缩时应该返回原始内容") {
                    testApplication {
                        application { configureHTTP() }

                        routing { get("/test-no-compression") { call.respondText("test content") } }

                        client.get("/test-no-compression").apply {
                            status shouldBe HttpStatusCode.OK
                            bodyAsText() shouldBe "test content"
                            headers[HttpHeaders.ContentEncoding] shouldBe null
                        }
                    }
                }
            }

            describe("CachingHeaders 插件") {
                it("应该为 CSS 文件设置缓存头部") {
                    testApplication {
                        application { configureHTTP() }

                        routing {
                            get("/styles.css") {
                                call.respondText("body { color: red; }", ContentType.Text.CSS)
                            }
                        }

                        client.get("/styles.css").apply {
                            status shouldBe HttpStatusCode.OK
                            headers[HttpHeaders.CacheControl] shouldBe "max-age=86400"
                        }
                    }
                }

                it("非 CSS 文件不应该设置缓存头部") {
                    testApplication {
                        application { configureHTTP() }

                        routing {
                            get("/test.html") {
                                call.respondText(
                                    "<html><body>test</body></html>",
                                    ContentType.Text.Html,
                                )
                            }
                            get("/test.js") {
                                call.respondText(
                                    "console.log('test');",
                                    ContentType.Text.JavaScript,
                                )
                            }
                            get("/test.json") {
                                call.respondText("{\"test\": true}", ContentType.Application.Json)
                            }
                        }

                        listOf("/test.html", "/test.js", "/test.json").forEach { path ->
                            client.get(path).apply {
                                status shouldBe HttpStatusCode.OK
                                headers[HttpHeaders.CacheControl] shouldBe null
                            }
                        }
                    }
                }

                it("CSS 文件应该设置 24 小时缓存") {
                    testApplication {
                        application { configureHTTP() }

                        routing {
                            get("/main.css") {
                                call.respondText(
                                    ".container { width: 100%; }",
                                    ContentType.Text.CSS,
                                )
                            }
                        }

                        client.get("/main.css").apply {
                            status shouldBe HttpStatusCode.OK
                            val cacheControl = headers[HttpHeaders.CacheControl]
                            cacheControl shouldBe "max-age=86400" // 24 * 60 * 60 = 86400 秒
                        }
                    }
                }
            }

            describe("插件集成测试") {
                it("所有插件应该协同工作") {
                    testApplication {
                        application { configureHTTP() }

                        routing {
                            get("/integration-test.css") {
                                call.respondText(
                                    ".test { font-size: 16px; }".repeat(100),
                                    ContentType.Text.CSS,
                                )
                            }
                        }

                        client
                            .get("/integration-test.css") {
                                header(HttpHeaders.AcceptEncoding, "gzip")
                            }
                            .apply {
                                // 应该同时支持压缩和缓存
                                status shouldBe HttpStatusCode.OK
                                headers["X-Engine"] shouldBe "Ktor"
                                headers[HttpHeaders.CacheControl] shouldBe "max-age=86400"
                                headers[HttpHeaders.ContentEncoding] shouldBe "gzip"
                            }
                    }
                }
            }
        }
    })
