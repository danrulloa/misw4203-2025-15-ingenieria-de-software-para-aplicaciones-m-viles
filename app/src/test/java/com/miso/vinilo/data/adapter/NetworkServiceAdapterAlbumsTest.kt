package com.miso.vinilo.data.adapter

import com.miso.vinilo.data.dto.CollectorIdDto
import com.miso.vinilo.data.dto.NewCommentDto
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NetworkServiceAdapterAlbumsTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var adapter: NetworkServiceAdapterAlbums
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        val baseUrl = mockWebServer.url("/").toString()
        adapter = NetworkServiceAdapterAlbums.create(baseUrl)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `postComment returns Success when server returns 201 with valid body`() = runTest {
        // Arrange
        val albumId = 101L
        val responseBody = """
            {
              "id": 5,
              "description": "This is a fantastic album!",
              "rating": 5
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setResponseCode(201).setBody(responseBody))

        val newComment = NewCommentDto(rating = 5, description = "This is a fantastic album!", collector = CollectorIdDto(100L))

        // Act
        val result = adapter.postComment(albumId, newComment)

        // Assert
        assertTrue(result is NetworkResult.Success)
        assertEquals(5, (result as NetworkResult.Success).data.rating)
        assertEquals("This is a fantastic album!", result.data.description)
        
        val request = mockWebServer.takeRequest()
        assertEquals("/albums/$albumId/comments", request.path)
        assertEquals("POST", request.method)
    }

    @Test
    fun `postComment returns Error when server returns 404`() = runTest {
        // Arrange
        val albumId = 999L // Non-existent album
        mockWebServer.enqueue(MockResponse().setResponseCode(404).setBody("Not Found"))
        val newComment = NewCommentDto(rating = 5, description = "A comment", collector = CollectorIdDto(100L))

        // Act
        val result = adapter.postComment(albumId, newComment)

        // Assert
        assertTrue(result is NetworkResult.Error)
        
        val request = mockWebServer.takeRequest()
        assertEquals("/albums/$albumId/comments", request.path)
    }
}
