package com.bangbang93.openbmclapi

import com.bangbang93.openbmclapi.agent.module
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApplicationTest {
    @Test
    fun `主页返回OK`() =
        testApplication {
            // Arrange
            application {
                module()
            }

            // Act
            client.get("/").apply {
                // Assert
                assertEquals(HttpStatusCode.OK, status)
                val responseText = bodyAsText()
                assertTrue(responseText.contains("OpenBMCLAPI"))
            }
        }
}
