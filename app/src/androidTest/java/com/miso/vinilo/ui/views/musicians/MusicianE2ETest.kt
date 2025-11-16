package com.miso.vinilo.ui.views.musicians

import android.app.Application
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
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.miso.vinilo.MainActivity
import com.miso.vinilo.data.adapter.NetworkConfig
import com.miso.vinilo.data.database.ViniloDatabase
import com.miso.vinilo.di.appModule
import java.util.concurrent.TimeUnit
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.*
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.android.ext.koin.androidContext

@RunWith(AndroidJUnit4::class)
class MusicianE2ETest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    companion object {
        private lateinit var server: MockWebServer

        @JvmStatic
        @BeforeClass
        fun beforeClass() {
            // Iniciar el server y ajustar el baseUrl ANTES de lanzar la Activity/DI
            server = MockWebServer()
            server.start()
            NetworkConfig.baseUrl = server.url("/").toString()
            Log.i("MusicianE2ETest", "[BeforeClass] BaseUrl=${'$'}{NetworkConfig.baseUrl}")

            // Reiniciar Koin para que el adapter use el nuevo baseUrl
            try { stopKoin() } catch (_: Throwable) {}
            val appContext = ApplicationProvider.getApplicationContext<Application>()
            startKoin {
                androidContext(appContext)
                modules(appModule)
            }
        }

        @JvmStatic
        @AfterClass
        fun afterClass() {
            try { server.shutdown() } catch (_: Throwable) {}
        }
    }

    @Before
    fun setupServer() {
        // Limpia Room para forzar refresh desde red
        try {
            val ctx = InstrumentationRegistry.getInstrumentation().targetContext
            ViniloDatabase.getDatabase(ctx).clearAllTables()
        } catch (_: Throwable) {}

        // Respuestas mock para lista y detalle
        val musiciansJson = """
            [
              {"id":1,"name":"Juan Perez","image":"","birthDate":"1980-05-20T00:00:00.000Z","description":"Guitarrista","performerPrizes":[],"albums":[]},
              {"id":2,"name":"María López","image":"","birthDate":"1990-08-15T00:00:00.000Z","description":"Cantante","performerPrizes":[],"albums":[]}
            ]
        """.trimIndent()
        val musicianDetailJson = """
            {"id":1,"name":"Rubén Blades Bellido de Luna","image":"","birthDate":"1948-07-16T00:00:00.000Z","description":"Cantante y compositor","performerPrizes":[],"albums":[{"id":100,"name":"Buscando América","cover":"https://example.com/cover.jpg","releaseDate":"1984-08-01T00:00:00.000Z","description":"Álbum icónico de Rubén Blades","genre":"Salsa","recordLabel":"Elektra"}]}
        """.trimIndent()

        // Drenar requests anteriores si quedaron
        try {
            var drained = 0
            while (true) {
                val r = server.takeRequest(50, TimeUnit.MILLISECONDS) ?: break
                drained++
            }
            if (drained > 0) Log.d("MusicianE2ETest", "[Before] drained=${'$'}drained")
        } catch (_: Throwable) {}

        // Encolar llamadas esperadas (si hay reintentos, podrías duplicar estos enqueue)
        server.enqueue(MockResponse().setResponseCode(200).setBody(musiciansJson))
        server.enqueue(MockResponse().setResponseCode(200).setBody(musicianDetailJson))
    }

    @After
    fun tearDown() {
        // Solo drenar para debug; no apagar server aquí
        try {
            while (true) {
                val req = server.takeRequest(100, TimeUnit.MILLISECONDS) ?: break
                Log.i("MusicianE2ETest", "[After] Request: ${'$'}{req.path}")
            }
        } catch (_: Throwable) {}
    }

    @Test
    fun e2e_userNavigatesToMusicians_and_seesMusiciansFromNetwork() {
        Log.i("MusicianE2ETest", "Click nav 'Artistas'")
        waitAndClickNavItem("Artistas", timeoutMs = 10_000L)

        Log.i("MusicianE2ETest", "Esperando título 'Artistas'")
        waitForTextFlexible("Artistas", timeoutMs = 12_000L)

        Log.i("MusicianE2ETest", "Verificando petición al server")
        try {
            val recorded = server.takeRequest(1_500, TimeUnit.MILLISECONDS)
            if (recorded != null) Log.i("MusicianE2ETest", "Recorded: ${'$'}{recorded.path}")
        } catch (_: Throwable) {}

        Log.i("MusicianE2ETest", "Esperando nombres")
        waitForTextFlexible("Juan Perez", timeoutMs = 20_000L)
        waitForTextFlexible("María López", timeoutMs = 20_000L)

        composeTestRule.onNodeWithText("Juan Perez", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("María López", substring = true).assertIsDisplayed()
    }

    @Test
    fun e2e_openMusicianDetail() {
        waitAndClickNavItem("Artistas", timeoutMs = 10_000L)
        waitForTextFlexible("Artistas", timeoutMs = 12_000L)
        waitForTextFlexible("Juan Perez", timeoutMs = 20_000L)

        composeTestRule.onNodeWithText("Juan Perez", substring = true)
            .assertIsDisplayed()
            .performClick()

        waitForTextFlexible("Rubén Blades Bellido de Luna", timeoutMs = 20_000L)
        composeTestRule.onNodeWithText("Rubén Blades Bellido de Luna", substring = true)
            .assertIsDisplayed()
    }

    private fun waitForTextFlexible(text: String, timeoutMs: Long = 5_000L) {
        try {
            composeTestRule.waitUntil(timeoutMs) {
                try {
                    val merged = try { composeTestRule.onAllNodesWithText(text, substring = true).fetchSemanticsNodes() } catch (_: Throwable) { emptyList() }
                    if (merged.isNotEmpty()) return@waitUntil true
                    val unmerged = try { composeTestRule.onAllNodesWithText(text, useUnmergedTree = true, substring = true).fetchSemanticsNodes() } catch (_: Throwable) { emptyList() }
                    unmerged.isNotEmpty()
                } catch (_: Throwable) { false }
            }
            // Afirmar cualquier nodo visible
            val mergedCollection = composeTestRule.onAllNodesWithText(text, substring = true)
            val mergedNodes = try { mergedCollection.fetchSemanticsNodes() } catch (_: Throwable) { emptyList() }
            for (i in mergedNodes.indices) { try { mergedCollection[i].assertIsDisplayed(); return } catch (_: Throwable) {} }
            val unmergedCollection = composeTestRule.onAllNodesWithText(text, useUnmergedTree = true, substring = true)
            val unmergedNodes = try { unmergedCollection.fetchSemanticsNodes() } catch (_: Throwable) { emptyList() }
            for (i in unmergedNodes.indices) { try { unmergedCollection[i].assertIsDisplayed(); return } catch (_: Throwable) {} }
            throw AssertionError("No visible node found for text '$text'")
        } catch (e: Throwable) {
            throw AssertionError("Timed out waiting for text '$text' (${e.message})")
        }
    }

    private fun waitAndClickNavItem(label: String, timeoutMs: Long = 5_000L) {
        val candidates = listOf(label, label.replace("A", "Á"), label.replace("Á", "A"), "Artista", "Artistas")
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            // 1) contentDescription primero (íconos sin texto)
            for (candidate in candidates) {
                try {
                    val node = composeTestRule.onNodeWithContentDescription(candidate, useUnmergedTree = true)
                    node.performClick()
                    composeTestRule.waitForIdle()
                    return
                } catch (_: Throwable) {}
            }
            // 2) texto visible
            for (candidate in candidates) {
                try {
                    val collection = composeTestRule.onAllNodesWithText(candidate, substring = true)
                    val nodes = try { collection.fetchSemanticsNodes() } catch (_: Throwable) { emptyList() }
                    if (nodes.isNotEmpty()) {
                        val center = nodes[0].boundsInRoot.center
                        composeTestRule.onRoot().performTouchInput { click(Offset(center.x, center.y)) }
                        composeTestRule.waitForIdle()
                        return
                    }
                } catch (_: Throwable) {}
            }
            Thread.sleep(150)
        }
        throw AssertionError("Could not find or click nav item '$label'")
    }
}