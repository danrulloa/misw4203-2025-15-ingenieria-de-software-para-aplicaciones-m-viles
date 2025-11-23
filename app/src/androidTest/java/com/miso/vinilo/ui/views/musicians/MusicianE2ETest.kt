package com.miso.vinilo.ui.views.musicians

import android.app.Application
import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.miso.vinilo.MainActivity
import com.miso.vinilo.data.GlobalRoleState
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

        val albumsListJson = """
            [
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
        """.trimIndent()

        val albumDetailJson = """
            {
              "id": 100,
              "name": "Buscando América",
              "cover": "https://example.com/cover.jpg",
              "releaseDate": "1984-08-01T00:00:00.000Z",
              "description": "Álbum icónico de Rubén Blades",
              "genre": "Salsa",
              "recordLabel": "Elektra",
              "tracks": [],
              "performers": []
            }
        """.trimIndent()

         val addAlbumResponseJson = """
            {
              "id": 100,
              "name": "Buscando América",
              "cover": "https://example.com/cover.jpg",
              "releaseDate": "1984-08-01T00:00:00.000Z",
              "description": "Álbum icónico de Rubén Blades",
              "genre": "Salsa",
              "recordLabel": "Elektra",
              "tracks": [],
              "performers": []
            }
        """.trimIndent()

        val addAlbumJson = """
            {
              "id": 100,
              "name": "Buscando América",
              "cover": "https://example.com/cover.jpg",
              "releaseDate": "1984-08-01T00:00:00.000Z",
              "description": "Álbum icónico",
              "genre": "Salsa",
              "recordLabel": "Elektra",
              "tracks": [],
              "performers": []
            }
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

        server.dispatcher = object : okhttp3.mockwebserver.Dispatcher() {
            override fun dispatch(request: okhttp3.mockwebserver.RecordedRequest): MockResponse {
                val path = request.path ?: ""
                android.util.Log.i("MusicianE2ETest", "Dispatcher path=$path")

                return when {
                    path.startsWith("/musicians/1") ->
                        MockResponse().setResponseCode(200).setBody(musicianDetailJson)

                    path.startsWith("/musicians") ->
                        MockResponse().setResponseCode(200).setBody(musiciansJson)

                    path.startsWith("/albums/100") ->
                        MockResponse().setResponseCode(200).setBody(albumDetailJson)

                    path.startsWith("/albums") ->
                        MockResponse().setResponseCode(200).setBody(albumsListJson)

                    path.startsWith("/musicians/1/albums/100") && request.method == "POST" -> {
                        MockResponse()
                            .setResponseCode(200)
                            .setBody(addAlbumResponseJson)
                    }

                    path.startsWith("/musicians/1/albums/") && request.method == "POST" ->
                        MockResponse().setResponseCode(200).setBody(addAlbumJson)

                    else ->
                        MockResponse().setResponseCode(404).setBody("""{"error":"not mocked"}""")
                }
            }
        }
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

    @Test
    fun e2e_addAlbumMode_showsSearchAndAlbums() {
        // 1) Ir a Artistas y abrir detalle de Juan
        waitAndClickNavItem("Artistas", timeoutMs = 10_000L)
        waitForTextFlexible("Juan Perez", timeoutMs = 20_000L)
        GlobalRoleState.updateRole("Coleccionista")

        composeTestRule.onNodeWithText("Juan Perez", substring = true)
            .assertIsDisplayed()
            .performClick()

        // 2) Scroll down for small screens and wait for "Álbumes" header
        composeTestRule.onNodeWithTag("musicianDetailList")
            .performScrollToNode(hasText("Álbumes", substring = true))
        waitForTextFlexible("Álbumes", timeoutMs = 20_000L)
        waitForTextFlexible("Buscando América", timeoutMs = 20_000L)

        // 3) Pulsar el botón "Añadir álbum"
        waitForTextFlexible("Añadir álbum", timeoutMs = 10_000L)
        composeTestRule.onNodeWithText("Añadir álbum", substring = true)
            .assertIsDisplayed()
            .performClick()

        // 4) Comprobar que aparece el buscador ("Busca el álbum" es el placeholder)
        waitForTextFlexible("Busca el álbum", timeoutMs = 10_000L)
        composeTestRule.onNodeWithText("Busca el álbum", substring = true)
            .assertIsDisplayed()
        // si quieres simular que el usuario toca el campo:
        // .performClick()

        composeTestRule.onNodeWithTag("musicianDetailList")
            .performScrollToNode(hasText("Agregar álbum al artista", substring = true))

        // 5) En modo agregar:
        //    - Debe aparecer el botón "Agregar álbum al artista"
        //    - Debe aparecer el carrusel con "Buscando América" como seleccionable
        waitForTextFlexible("Agregar álbum al artista", timeoutMs = 10_000L)
        composeTestRule.onNodeWithText("Agregar álbum al artista", substring = true)
            .assertIsDisplayed()

        waitForTextFlexible("Buscando América", timeoutMs = 10_000L)
        composeTestRule
            .onAllNodesWithText("Buscando América", substring = true)
            .onFirst()
            .assertIsDisplayed()
    }

    @Test
    fun e2e_addAlbumToMusician_selectAndConfirm() {

        waitAndClickNavItem("Artistas", timeoutMs = 10_000L)
        waitForTextFlexible("Juan Perez", timeoutMs = 20_000L)
        GlobalRoleState.updateRole("Coleccionista")

        composeTestRule.onNodeWithText("Juan Perez", substring = true)
            .assertIsDisplayed()
            .performClick()

        // Scroll down for small screens
        composeTestRule.onNodeWithTag("musicianDetailList")
            .performScrollToNode(hasText("Álbumes", substring = true))
        waitForTextFlexible("Álbumes", timeoutMs = 20_000L)


        waitForTextFlexible("Añadir álbum", timeoutMs = 10_000L)
        composeTestRule.onNodeWithText("Añadir álbum", substring = true)
            .assertIsDisplayed()
            .performClick()

        composeTestRule.onNodeWithTag("musicianDetailList")
            .performScrollToNode(hasText("Agregar álbum al artista", substring = true))


        waitForTextFlexible("Agregar álbum al artista", timeoutMs = 10_000L)
        composeTestRule.onNodeWithText("Agregar álbum al artista", substring = true)
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("musicianDetailList")
            .performScrollToNode(hasText("Buscando América", substring = true))

        composeTestRule.onAllNodesWithText("Buscando América", substring = true, useUnmergedTree = true)
            .onFirst()
            .performClick()

        composeTestRule.onNodeWithText("Agregar álbum al artista", substring = true)
            .assertIsDisplayed()
            .performClick()

    }

    @Test
    fun e2e_addAlbumButton_disabled_forUsuario() {
        GlobalRoleState.updateRole("Usuario")

        waitAndClickNavItem("Artistas", timeoutMs = 10_000L)
        waitForTextFlexible("Juan Perez", timeoutMs = 20_000L)

        composeTestRule.onNodeWithText("Juan Perez", substring = true)
            .assertIsDisplayed()
            .performClick()

        // Scroll down for small screens
        composeTestRule.onNodeWithTag("musicianDetailList")
            .performScrollToNode(hasText("Álbumes", substring = true))
        waitForTextFlexible("Álbumes", timeoutMs = 20_000L)

        composeTestRule.onNodeWithText("Añadir álbum", substring = true)
            .assertIsDisplayed()
            .assertIsNotEnabled()
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