package com.miso.vinilo.ui.views.albums

import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.click
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.miso.vinilo.MainActivity
import com.miso.vinilo.data.adapter.NetworkConfig
import java.util.concurrent.TimeUnit
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AlbumDetailE2ETest {

    companion object {
        private lateinit var server: MockWebServer

        @JvmStatic
        @BeforeClass
        fun setUpClass() {
            server = MockWebServer()
            server.start()
            NetworkConfig.baseUrl = server.url("/").toString()
        }

        @JvmStatic
        @AfterClass
        fun tearDownClass() {
            try {
                server.shutdown()
            } catch (_: Throwable) { // ignore
            }
        }
    }

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setupServer() {
        // Response for the album list
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
        """.trimIndent()
        server.enqueue(MockResponse().setResponseCode(200).setBody(albumListJson))

        // Response for the album detail
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
        """.trimIndent()
        server.enqueue(MockResponse().setResponseCode(200).setBody(albumDetailJson))
    }

    @After
    fun afterEach() {
        try {
            while (true) {
                server.takeRequest(100, TimeUnit.MILLISECONDS) ?: break
            }
        } catch (_: Throwable) { // ignore
        }
    }

    @Test
    fun e2e_userNavigatesToDetail_and_seesAlbumDetails() {
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