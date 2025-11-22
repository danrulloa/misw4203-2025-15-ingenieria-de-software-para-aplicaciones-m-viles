package com.miso.vinilo.ui.views.albums

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.click
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.miso.vinilo.MainActivity
import com.miso.vinilo.data.adapter.NetworkConfig
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AlbumDetailE2ETest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @get:Rule
    val composeTestRule = createEmptyComposeRule()

    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        // Start a new server for each test and set the base URL
        server = MockWebServer()
        server.start()
        NetworkConfig.baseUrl = server.url("/").toString()
    }

    @After
    fun tearDown() {
        // Shut down the server after each test
        server.shutdown()
    }

    @Test
    fun e2e_userNavigatesToDetail_and_seesAlbumDetails() {
        // Arrange: Enqueue responses for this specific test
        val albumListJson = """
        [
          {
            "id": 100,
            "name": "Buscando América",
            "cover": "https://i.pinimg.com/564x/aa/5f/ed/aa5fed7fac61cc8f41d1e79db917a7cd.jpg",
            "releaseDate": "1984-08-01T00:00:00.000Z",
            "description": "...",
            "genre": "Salsa",
            "recordLabel": "Elektra"
          }
        ]
        """
        server.enqueue(MockResponse().setResponseCode(200).setBody(albumListJson))

        val albumDetailJson = """
        {
            "id": 100,
            "name": "Buscando América",
            "cover": "https://i.pinimg.com/564x/aa/5f/ed/aa5fed7fac61cc8f41d1e79db917a7cd.jpg",
            "releaseDate": "1984-08-01T00:00:00.000Z",
            "description": "...",
            "genre": "Salsa",
            "recordLabel": "Elektra",
            "tracks": [{"id":1, "name":"Decisiones", "duration":"5:05"}],
            "performers": [{"id":1, "name":"Rubén Blades", "image":"...", "description":"..."}]
        }
        """
        server.enqueue(MockResponse().setResponseCode(200).setBody(albumDetailJson))

        // Act & Assert
        // 1. Navigate to the Albums tab
        waitAndClickNavItem("Albumes")

        // 2. Wait for the list item to appear and click it
        waitForTextFlexible("Buscando América", timeoutMs = 15_000L)
        composeTestRule.onNodeWithText("Buscando América", substring = true).performClick()

        // 3. Verify that the detail screen is shown by looking for a unique element
        waitForTextFlexible("Canciones", timeoutMs = 5_000L)
        composeTestRule.onNodeWithText("Canciones").assertIsDisplayed()
        composeTestRule.onNodeWithText("Decisiones").assertIsDisplayed() // Also check for a track name
    }

    @Test
    fun e2e_collectorAddsCommentToAlbum_seesComment() {
        // Arrange: Enqueue all responses for this specific test flow

        val albumListJson = """
        [
          {
            "id": 100,
            "name": "Buscando América",
            "cover": "https://i.pinimg.com/564x/aa/5f/ed/aa5fed7fac61cc8f41d1e79db917a7cd.jpg",
            "releaseDate": "1984-08-01T00:00:00.000Z",
            "description": "...",
            "genre": "Salsa",
            "recordLabel": "Elektra"
          }
        ]
        """
        server.enqueue(MockResponse().setResponseCode(200).setBody(albumListJson))

        val albumDetailJson = """
        {
            "id": 100,
            "name": "Buscando América",
            "cover": "https://i.pinimg.com/564x/aa/5f/ed/aa5fed7fac61cc8f41d1e79db917a7cd.jpg",
            "releaseDate": "1984-08-01T00:00:00.000Z",
            "description": "...",
            "genre": "Salsa",
            "recordLabel": "Elektra",
            "tracks": [{"id":1, "name":"Decisiones", "duration":"5:05"}],
            "performers": [{"id":1, "name":"Rubén Blades", "image":"...", "description":"..."}]
        }
        """
        server.enqueue(MockResponse().setResponseCode(200).setBody(albumDetailJson))
        
        val postCommentResponse = """
        {"id": 5, "description": "This is a great album!", "rating": 5}
        """
        server.enqueue(MockResponse().setResponseCode(201).setBody(postCommentResponse))

        val albumDetailWithCommentJson = """
        {
            "id": 100, "name": "Buscando América", "cover": "...",
            "comments": [{"id":5, "description":"This is a great album!", "rating":5, "collector": {"id": 100, "name": "Manolo Bellon"}}]
        }
        """
        server.enqueue(MockResponse().setResponseCode(200).setBody(albumDetailWithCommentJson))

        // Act & Assert
        // 1. Select Collector Role
        composeTestRule.onNodeWithText("Usuario").performClick()
        composeTestRule.onNodeWithText("Coleccionista").performClick()

        // 2. Navigate to the Albums tab
        waitAndClickNavItem("Albumes")

        // 3. Wait for the list item to appear and click it
        waitForTextFlexible("Buscando América", timeoutMs = 15_000L)
        composeTestRule.onNodeWithText("Buscando América", substring = true).performClick()

        // 4. Scroll to and click on 'Add Comment' button
        waitForTextFlexible("Agregar Comentario", timeoutMs = 15_000L)
        composeTestRule.onNodeWithText("Agregar Comentario").performClick()

        // 5. Fill and submit the form
        composeTestRule.onNodeWithText("Calificación (1-5)").performTextInput("5")
        composeTestRule.onNodeWithText("Descripción").performTextInput("This is a great album!")
        composeTestRule.onNodeWithText("Guardar").performClick()

        // 6. Verify the new comment is displayed
        waitForTextFlexible("This is a great album!", timeoutMs = 5_000L)
    }
    
    @Test
    fun e2e_userViewsAlbumDetail_addCommentButtonIsNotVisible() {
        // Arrange: Enqueue responses for navigation

        val albumListJson = """
        [
          {
            "id": 100,
            "name": "Buscando América",
            "cover": "https://i.pinimg.com/564x/aa/5f/ed/aa5fed7fac61cc8f41d1e79db917a7cd.jpg",
            "releaseDate": "1984-08-01T00:00:00.000Z",
            "description": "...",
            "genre": "Salsa",
            "recordLabel": "Elektra"
          }
        ]
        """
        server.enqueue(MockResponse().setResponseCode(200).setBody(albumListJson))

        val albumDetailJson = """
        {
            "id": 100,
            "name": "Buscando América",
            "cover": "https://i.pinimg.com/564x/aa/5f/ed/aa5fed7fac61cc8f41d1e79db917a7cd.jpg",
            "releaseDate": "1984-08-01T00:00:00.000Z",
            "description": "...",
            "genre": "Salsa",
            "recordLabel": "Elektra",
            "tracks": [{"id":1, "name":"Decisiones", "duration":"5:05"}],
            "performers": [{"id":1, "name":"Rubén Blades", "image":"...", "description":"..."}]
        }
        """
        server.enqueue(MockResponse().setResponseCode(200).setBody(albumDetailJson))

        // Act: Role is "Usuario" by default, no selection needed.
        // 1. Navigate to Albums tab
        waitAndClickNavItem("Albumes")

        // 2. Wait for the list item to appear and click it
        waitForTextFlexible("Buscando América", timeoutMs = 15_000L)
        composeTestRule.onNodeWithText("Buscando América", substring = true).performClick()

        // Assert: Verify the button does not exist
        // Scroll to "Comentarios" to ensure it's visible (needed for small screens)
        composeTestRule.onNodeWithTag("album_detail_list").performScrollToNode(hasText("Comentarios"))
        
        waitForTextFlexible("Comentarios", timeoutMs = 5_000L) // Wait for screen to load
        composeTestRule.onNodeWithText("Agregar Comentario").assertDoesNotExist()
    }

    // Helper functions copied from AlbumE2ETest
    private fun waitForTextFlexible(text: String, timeoutMs: Long = 5_000L) {
        try {
            composeTestRule.waitUntil(timeoutMs) {
                try {
                    val merged = try {
                        composeTestRule.onAllNodesWithText(text, substring = true).fetchSemanticsNodes()
                    } catch (_: Throwable) {
                        emptyList()
                    }
                    if (merged.isNotEmpty()) return@waitUntil true

                    val unmerged = try {
                        composeTestRule.onAllNodesWithText(text, useUnmergedTree = true, substring = true).fetchSemanticsNodes()
                    } catch (_: Throwable) {
                        emptyList()
                    }
                    unmerged.isNotEmpty()
                } catch (_: Throwable) {
                    false
                }
            }

            try {
                composeTestRule.onNodeWithText(text, substring = true).assertIsDisplayed()
                return
            } catch (_: Throwable) {}

            composeTestRule.onNodeWithText(text, useUnmergedTree = true, substring = true).assertIsDisplayed()
        } catch (e: Throwable) {
            throw AssertionError("Timed out waiting for text '$text' (${e.message})")
        }
    }

    private fun waitAndClickNavItem(label: String, timeoutMs: Long = 5_000L) {
        val deadline = System.currentTimeMillis() + timeoutMs
        var lastException: Throwable? = null
        while (System.currentTimeMillis() < deadline) {
            try {
                val node = composeTestRule.onAllNodesWithText(label, substring = true).fetchSemanticsNodes().first()
                val center = node.boundsInRoot.center
                composeTestRule.onRoot().performTouchInput { click(Offset(center.x, center.y)) }
                return
            } catch (e: Throwable) {
                lastException = e
            }
            Thread.sleep(200)
        }
        throw AssertionError("Could not find or click nav item '$label' (last: ${lastException?.message})")
    }
}