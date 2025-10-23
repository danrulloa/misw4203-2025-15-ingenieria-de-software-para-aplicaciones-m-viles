package com.miso.vinilo.data.network

import com.miso.vinilo.data.model.Genre
import com.miso.vinilo.data.model.RecordLabel
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
        val json = """
            [
              {
                "id": 100,
                "name": "Buscando América",
                "cover": "https://i.pinimg.com/564x/aa/5f/ed/aa5fed7fac61cc8f41d1e79db917a7cd.jpg",
                "releaseDate": "1984-08-01T00:00:00.000Z",
                "description": "Buscando América es el primer álbum de la banda de Rubén Blades y Seis del Solar lanzado en 1984",
                "genre": "Salsa",
                "recordLabel": "Elektra"
              }
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
                val album = list[0]
                assertEquals(100L, album.id)
                assertEquals("Buscando América", album.name)
                assertEquals("https://i.pinimg.com/564x/aa/5f/ed/aa5fed7fac61cc8f41d1e79db917a7cd.jpg", album.cover)
                assertEquals("1984-08-01T00:00:00.000Z", album.releaseDate)
                assertEquals("Buscando América es el primer álbum de la banda de Rubén Blades y Seis del Solar lanzado en 1984", album.description)
                assertEquals(Genre.SALSA, album.genre)
                assertEquals(RecordLabel.ELEKTRA, album.recordLabel)
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