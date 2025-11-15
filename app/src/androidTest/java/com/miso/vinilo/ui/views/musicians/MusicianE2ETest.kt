package com.miso.vinilo.ui.views.musicians

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
class MusicianE2ETest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()
    private lateinit var server: MockWebServer

    @Before
    fun setupServer() {
        // 1) Crear y arrancar un servidor nuevo para CADA test
        server = MockWebServer()
        server.start()

        // 2) Apuntar SIEMPRE el baseUrl al server recien creado
        NetworkConfig.baseUrl = server.url("/").toString()
        Log.i("MusicianE2ETest", "BaseUrl para este test = ${NetworkConfig.baseUrl}")

        // 3) Respuestas mockeadas
        val musiciansJson = """
            [
              {
                "id": 1,
                "name": "Juan Perez",
                "image": "",
                "birthDate": "1980-05-20T00:00:00.000Z",
                "description": "Guitarrista",
                "performerPrizes": [],
                "albums": []
              },
              {
                "id": 2,
                "name": "María López",
                "image": "",
                "birthDate": "1990-08-15T00:00:00.000Z",
                "description": "Cantante",
                "performerPrizes": [],
                "albums": []
              }
            ]
        """.trimIndent()

        val musicianDetailJson = """
            {
              "id": 1,
              "name": "Rubén Blades Bellido de Luna",
              "image": "",
              "birthDate": "1948-07-16T00:00:00.000Z",
              "description": "Cantante y compositor",
              "performerPrizes": [],
              "albums": [
                {
                  "id": 100,
                  "name": "Buscando América",
                  "cover": "https://example.com/cover.jpg",
                  "releaseDate": "1984-08-01T00:00:00.000Z",
                  "description": "Álbum icónico de Rubén Blades",
                  "genre": "Salsa",
                  "recordLabel": "Elektra"
                }
              ]
            }
        """.trimIndent()

        // IMPORTANTE: el orden en que los encolamos debe
        // corresponder al orden en que la app hace las llamadas.
        server.enqueue(MockResponse().setResponseCode(200).setBody(musiciansJson))
        server.enqueue(MockResponse().setResponseCode(200).setBody(musicianDetailJson))
    }

    @After
    fun tearDown() {
        try {
            // Limpiamos requests grabadas (no obligatorio pero ayuda a debug)
            while (true) {
                val req = server.takeRequest(100, TimeUnit.MILLISECONDS) ?: break
                Log.i("MusicianE2ETest", "Request al apagar server: ${req.path}")
            }
        } catch (_: Throwable) {
        }

        try {
            server.shutdown()
        } catch (_: Throwable) {
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

    @Test
    fun e2e_openMusicianDetail() {
        Log.i("MusicianE2ETest", "Test2: click nav item Artistas")
        waitAndClickNavItem("Artistas")

        Log.i("MusicianE2ETest", "Test2: waiting for title 'Artistas'")
        waitForTextFlexible("Artistas", timeoutMs = 5_000L)

        Log.i("MusicianE2ETest", "Test2: waiting for musician names")
        waitForTextFlexible("Juan Perez", timeoutMs = 15_000L)

        // Abrir el primer músico (ej: Juan Perez)
        composeTestRule.onNodeWithText("Juan Perez", substring = true)
            .assertIsDisplayed()
            .performClick()

        // Ahora deberíamos estar en el detalle del músico
        waitForTextFlexible("Rubén Blades Bellido de Luna", timeoutMs = 15_000L)
        composeTestRule.onNodeWithText("Rubén Blades Bellido de Luna", substring = true)
            .assertIsDisplayed()

        Log.i("MusicianE2ETest", "Test2: detalle del músico visible")
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