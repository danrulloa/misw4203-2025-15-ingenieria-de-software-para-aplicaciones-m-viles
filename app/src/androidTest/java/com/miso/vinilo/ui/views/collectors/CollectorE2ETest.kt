package com.miso.vinilo.ui.views.collectors

import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
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
class CollectorE2ETest {

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
        // Enqueue response for collectors endpoint before each test
        val json = """
        [
          {
            "id": 100,
            "name": "Manolo Bellon",
            "telephone": "3502457896",
            "email": "manollo@caracol.com.co",
            "collectorAlbums": [
              {
                "id": 100,
                "price": 35,
                "status": "Active"
              }
            ],
            "comments": [
              {
                "id": 100,
                "description": "The most relevant album of Ruben Blades",
                "rating": 5
              }
            ],
            "favoritePerformers": [
              {
                "id": 100,
                "name": "Rubén Blades Bellido de Luna",
                "image": "https://upload.wikimedia.org/wikipedia/commons/thumb/b/bb/Ruben_Blades_by_Gage_Skidmore.jpg/800px-Ruben_Blades_by_Gage_Skidmore.jpg",
                "description": "Es un cantante, compositor, músico, actor, abogado, político y activista panameño.",
                "birthDate": "1948-07-16T00:00:00.000Z"
              }
            ]
          },
          {
            "id": 101,
            "name": "Jaime Monsalve",
            "telephone": "3012357936",
            "email": "jmonsalve@rtvc.com.co",
            "collectorAlbums": [
              {
                "id": 101,
                "price": 25,
                "status": "Active"
              }
            ],
            "comments": [
              {
                "id": 101,
                "description": "I love this album of Queen",
                "rating": 5
              }
            ],
            "favoritePerformers": [
              {
                "id": 101,
                "name": "Queen",
                "image": "https://pm1.narvii.com/6724/a8b29909071e9d08517b40c748b6689649372852v2_hq.jpg",
                "description": "Queen es una banda británica de rock formada en 1970.",
                "creationDate": "1970-01-01T00:00:00.000Z"
              }
            ]
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
    fun e2e_userNavigatesToCollectors_and_seesCollectorsFromNetwork() {
        Log.i("CollectorE2ETest", "Test start: clicking nav item Coleccionistas")
        // Simulate user clicking the "Coleccionistas" tab in the bottom navigation
        waitAndClickNavItem("Coleccionistas")

        Log.i("CollectorE2ETest", "Clicked nav item; waiting for title")
        // Wait for the Collectors screen title to appear (it uses 'Coleccionistas' in UI)
        waitForTextFlexible("Coleccionistas", timeoutMs = 5_000L)

        Log.i("CollectorE2ETest", "Title visible; polling MockWebServer for request")
        try {
            val recorded = server.takeRequest(500, TimeUnit.MILLISECONDS)
            if (recorded != null) Log.i("CollectorE2ETest", "Recorded request: path=${recorded.path}")
            else Log.w("CollectorE2ETest", "No request observed in short interval")
        } catch (e: Throwable) {
            Log.w("CollectorE2ETest", "Error while taking request: ${e.message}")
        }

        // Wait for collector names loaded from network
        Log.i("CollectorE2ETest", "Waiting for collector name texts")
        waitForTextFlexible("Manolo Bellon", timeoutMs = 15_000L)
        waitForTextFlexible("Jaime Monsalve", timeoutMs = 15_000L)

        Log.i("CollectorE2ETest", "Collector texts visible; doing final assertions")
        composeTestRule.onNodeWithText("Manolo Bellon", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Jaime Monsalve", substring = true).assertIsDisplayed()

        // Verify album counts are displayed
        Log.i("CollectorE2ETest", "Checking album counts")
        waitForTextFlexible("1 album", timeoutMs = 5_000L)

        Log.i("CollectorE2ETest", "Test finished successfully")
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
                throw AssertionError("No visible node found for text '$text'")
            } catch (e: Throwable) {
                throw AssertionError("Timed out waiting for text '$text' (${e.message})")
            }
        } catch (e: Throwable) {
            throw AssertionError("Timed out waiting for text '$text' (${e.message})")
        }
    }

    private fun waitAndClickNavItem(label: String, timeoutMs: Long = 5_000L) {
        val candidates = listOf(label, "Coleccionista", "Coleccionistas")
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
        throw AssertionError("Could not find or click nav item '$label' (last: ${lastException?.message})")
    }
}