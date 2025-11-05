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

class NetworkServiceAdapterAlbumsIntegrationTest {

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
    fun `retrofit parses album list from mock server`() = runTest {
        // FIX: Added empty tracks and performers to match the DTO
        val json = """
            [
              {"id":100,"name":"Buscando América","cover":"https://example.com/cover.jpg","releaseDate":"1984-08-01T00:00:00.000Z","description":"Album description","genre":"Salsa","recordLabel":"Elektra","tracks":[],"performers":[]}
            ]
        """.trimIndent()
        server.enqueue(MockResponse().setResponseCode(200).setBody(json))

        val baseUrl = server.url("/").toString()
        val adapter = NetworkServiceAdapterAlbums.create(baseUrl)

        val result = adapter.getAlbums()

        when (result) {
            is NetworkResult.Success -> {
                val list = result.data
                assertEquals(1, list.size)
                val a = list[0]
                assertEquals(100L, a.id)
                assertEquals("Buscando América", a.name)
                assertEquals("https://example.com/cover.jpg", a.cover)
            }
            is NetworkResult.Error -> {
                fail("Expected success but got error: ${result.message}")
            }
        }
    }

    @Test
    fun `adapter returns error when network fails`() = runTest {
        // Simulate abrupt disconnect to trigger a network error/IOException
        val response = MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START)
        server.enqueue(response)

        val baseUrl = server.url("/").toString()
        val adapter = NetworkServiceAdapterAlbums.create(baseUrl)

        val result = adapter.getAlbums()

        when (result) {
            is NetworkResult.Success -> {
                fail("Expected error but got success with ${result.data.size} items")
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
