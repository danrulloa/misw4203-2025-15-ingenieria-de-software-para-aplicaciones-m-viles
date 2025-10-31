package com.miso.vinilo.data.adapter

import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import java.io.IOException

class NetworkServiceAdapterMusiciansIntegrationTest {

    private lateinit var server: MockWebServer

    @Before
    fun startServer() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun shutdownServer() {
        server.shutdown()
    }

    @Test
    fun `retrofit parses musician list from mock server`() = runTest {
        val json = """
            [
              {"id":100,"name":"Rubén Blades Bellido de Luna","image":"https://example.com/img.jpg","description":"Description","birthDate":"1948-07-16T00:00:00.000Z"}
            ]
        """.trimIndent()
        server.enqueue(MockResponse().setResponseCode(200).setBody(json))

        val baseUrl = server.url("/").toString()
        val adapter = NetworkServiceAdapterMusicians.create(baseUrl)

        val result = adapter.getMusicians()

        when (result) {
            is NetworkResult.Success -> {
                val list = result.data
                assertEquals(1, list.size)
                val m = list[0]
                assertEquals(100L, m.id)
                assertEquals("Rubén Blades Bellido de Luna", m.name)
                assertEquals("https://example.com/img.jpg", m.image)
            }
            is NetworkResult.Error -> {
                fail("Expected success but got error: ${'$'}{result.message}")
            }
        }
    }

    @Test
    fun `adapter returns error when network fails`() = runTest {
        // Simulate abrupt disconnect to trigger a network error/IOException
        val response = MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START)
        server.enqueue(response)

        val baseUrl = server.url("/").toString()
        val adapter = NetworkServiceAdapterMusicians.create(baseUrl)

        val result = adapter.getMusicians()

        when (result) {
            is NetworkResult.Success -> {
                fail("Expected error but got success with ${'$'}{result.data.size} items")
            }
            is NetworkResult.Error -> {
                // message should be non-empty and throwable should be an IOException (or subclass)
                val msg = result.message
                val thr = result.throwable
                assertTrue(msg.isNotBlank())
                assertTrue(thr is IOException)
            }
        }
    }
}
