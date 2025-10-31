package com.miso.vinilo.ui.views.musicians

import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.click
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
class MusicianE2ETest {

    companion object {
        private lateinit var server: MockWebServer

        @JvmStatic
        @BeforeClass
        fun setUpClass() {
            server = MockWebServer()
            server.start()
            // Point app network to the mock server before the activity is launched
            NetworkConfig.baseUrl = server.url("/").toString()
        }

        @JvmStatic
        @AfterClass
        fun tearDownClass() {
            try {
                server.shutdown()
            } catch (_: Throwable) {
                // ignore
            }
        }
    }

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setupServer() {
        // Enqueue response for musicians endpoint before each test
        val json = """
        [
          {
            "id": 1,
            "name": "Juan Perez",
            "image": "",
            "birthDate": "1980-05-20T00:00:00.000Z",
            "description": "Guitarrista",
            "nationality": "Colombia"
          },
          {
            "id": 2,
            "name": "María López",
            "image": "",
            "birthDate": "1990-08-15T00:00:00.000Z",
            "description": "Cantante",
            "nationality": "Perú"
          }
        ]
        """.trimIndent()

        server.enqueue(MockResponse().setResponseCode(200).setBody(json))

        // No need to recreate activity because baseUrl was set in @BeforeClass
    }

    @After
    fun afterEach() {
        // Drain any remaining requests so the next test starts clean
        try {
            while (true) {
                server.takeRequest(100, TimeUnit.MILLISECONDS) ?: break
            }
        } catch (_: Throwable) {
            // ignore
        }
    }

    @Test
    fun e2e_userNavigatesToMusicians_and_seesMusiciansFromNetwork() {
        Log.i("MusicianE2ETest", "Test start: clicking nav item Artistas")
        // Simulate user clicking the "Artistas" tab in the bottom navigation
        waitAndClickNavItem("Artistas")

        Log.i("MusicianE2ETest", "Clicked nav item; waiting for title")
        // Wait for the Musicians screen title to appear (it uses 'Artistas' in UI)
        waitForTextFlexible("Artistas", timeoutMs = 5_000L)

        Log.i("MusicianE2ETest", "Title visible; polling MockWebServer for request")
        try {
            val recorded = server.takeRequest(500, TimeUnit.MILLISECONDS)
            if (recorded != null) Log.i("MusicianE2ETest", "Recorded request: path=${'$'}{recorded.path}")
            else Log.w("MusicianE2ETest", "No request observed in short interval")
        } catch (e: Throwable) {
            Log.w("MusicianE2ETest", "Error while taking request: ${'$'}{e.message}")
        }

        // Wait for musician names loaded from network
        Log.i("MusicianE2ETest", "Waiting for musician name texts")
        waitForTextFlexible("Juan Perez", timeoutMs = 15_000L)
        waitForTextFlexible("María López", timeoutMs = 15_000L)

        Log.i("MusicianE2ETest", "Musician texts visible; doing final assertions")
        composeTestRule.onNodeWithText("Juan Perez", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("María López", substring = true).assertIsDisplayed()
        Log.i("MusicianE2ETest", "Test finished successfully")
    }

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

            // After waitUntil returns, try asserting visibility on any matching node (merged then unmerged).
            try {
                val mergedCollection = composeTestRule.onAllNodesWithText(text, substring = true)
                val mergedNodes = try { mergedCollection.fetchSemanticsNodes() } catch (_: Throwable) { emptyList() }
                if (mergedNodes.isNotEmpty()) {
                    for (i in mergedNodes.indices) {
                        try {
                            mergedCollection[i].assertIsDisplayed()
                            return
                        } catch (_: Throwable) {
                            // try next
                        }
                    }
                }

                val unmergedCollection = composeTestRule.onAllNodesWithText(text, useUnmergedTree = true, substring = true)
                val unmergedNodes = try { unmergedCollection.fetchSemanticsNodes() } catch (_: Throwable) { emptyList() }
                if (unmergedNodes.isNotEmpty()) {
                    for (i in unmergedNodes.indices) {
                        try {
                            unmergedCollection[i].assertIsDisplayed()
                            return
                        } catch (_: Throwable) {
                            // try next
                        }
                    }
                }

                // If we reach here no node asserted as displayed
                throw AssertionError("No visible node found for text '${'$'}text'")
            } catch (e: Throwable) {
                throw AssertionError("Timed out waiting for text '${'$'}text' (${e.message})")
            }
        } catch (e: Throwable) {
            throw AssertionError("Timed out waiting for text '${'$'}text' (${e.message})")
        }
    }

    private fun waitForContentDescription(desc: String, timeoutMs: Long = 5_000L) {
        try {
            composeTestRule.waitUntil(timeoutMs) {
                try {
                    val node = try {
                        composeTestRule.onAllNodesWithContentDescription(desc).fetchSemanticsNodes()
                    } catch (_: Throwable) {
                        emptyList()
                    }
                    if (node.isNotEmpty()) return@waitUntil true
                    val unmerged = try {
                        composeTestRule.onAllNodesWithContentDescription(desc, useUnmergedTree = true).fetchSemanticsNodes()
                    } catch (_: Throwable) {
                        emptyList()
                    }
                    unmerged.isNotEmpty()
                } catch (_: Throwable) {
                    false
                }
            }
        } catch (e: Throwable) {
            throw AssertionError("Timed out waiting for contentDescription '${'$'}desc' (${e.message})")
        }
    }

    private fun waitAndClickNavItem(label: String, timeoutMs: Long = 5_000L) {
        val candidates = listOf(label, label.replace("A", "Á"), label.replace("Á", "A"), "Artista", "Artistas", "Artistas")
        val deadline = System.currentTimeMillis() + timeoutMs
        var lastException: Throwable? = null
        while (System.currentTimeMillis() < deadline) {
            for (candidate in candidates) {
                try {
                    val collection = composeTestRule.onAllNodesWithText(candidate, substring = true)
                    val nodes = try {
                        collection.fetchSemanticsNodes()
                    } catch (_: Throwable) {
                        emptyList()
                    }
                    if (nodes.isNotEmpty()) {
                        val node = collection.fetchSemanticsNodes()[0]
                        val center = node.boundsInRoot.center
                        composeTestRule.onRoot().performTouchInput { click(Offset(center.x, center.y)) }
                        return
                    }
                } catch (e: Throwable) {
                    lastException = e
                }

                try {
                    val node = composeTestRule.onNodeWithContentDescription(candidate, useUnmergedTree = true)
                    node.performClick()
                    return
                } catch (e: Throwable) {
                    lastException = e
                }
            }

            Thread.sleep(200)
        }
        throw AssertionError("Could not find or click nav item '${'$'}label' (last: ${'$'}{lastException?.message})")
    }
}