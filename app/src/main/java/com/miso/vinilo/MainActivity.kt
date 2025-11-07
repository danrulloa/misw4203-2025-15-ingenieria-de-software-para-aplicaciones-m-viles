package com.miso.vinilo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.lifecycle.viewmodel.compose.viewModel
import com.miso.vinilo.data.dto.AlbumDto
import com.miso.vinilo.data.dto.MusicianDto
import com.miso.vinilo.ui.theme.BaseWhite
import com.miso.vinilo.ui.theme.PrincipalColor
import com.miso.vinilo.ui.theme.ViniloTheme
import com.miso.vinilo.ui.views.home.HomeScreen
import com.miso.vinilo.ui.views.album.AlbumDetailScreen
import com.miso.vinilo.ui.views.albums.AlbumsScreen
import com.miso.vinilo.ui.views.musicians.MusicianScreen
import com.miso.vinilo.ui.views.collectors.CollectorsScreen
import com.miso.vinilo.ui.viewmodels.MusicianViewModel
import com.miso.vinilo.ui.viewmodels.AlbumViewModel
import com.miso.vinilo.ui.viewmodels.CollectorViewModel
import com.miso.vinilo.data.dto.MusicianDto
import com.miso.vinilo.data.dto.AlbumDto
import com.miso.vinilo.data.dto.CollectorDto
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Do not create controller/VM eagerly here. Create them lazily when the user
        // navigates to the ARTISTAS screen to avoid unnecessary work at app launch.
        setContent {
            ViniloTheme {
                ViniloApp()
            }
        }
    }
}

@Composable
fun ViniloApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.INICIO) }

    NavigationSuiteScaffold(
        containerColor = Color.Transparent,
        navigationSuiteItems = {
            AppDestinations.entries.forEach {

                val isSelected = it == currentDestination
                val tint = if (isSelected) PrincipalColor else BaseWhite

                item(
                    icon = {
                        Icon(
                            it.icon,
                            contentDescription = it.label,
                            tint = tint
                        )
                    },
                    // Force the label to use the app typography so we know it's using Montserrat
                    label = { Text(it.label, style = MaterialTheme.typography.labelSmall) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {
        Scaffold(modifier = Modifier.fillMaxSize(), containerColor = Color.Transparent) { innerPadding ->
            val contentModifier = Modifier.padding(innerPadding)
            when (currentDestination) {
                AppDestinations.INICIO -> HomeScreen(modifier = contentModifier)
                AppDestinations.ALBUMES -> AlbumScreenHost(modifier = contentModifier)
                AppDestinations.ARTISTAS -> MusicianScreenHost(modifier = contentModifier)
                AppDestinations.COLECCIONISTAS -> CollectorsScreen(modifier = contentModifier)
            }
        }
    }
}

@Composable
fun AlbumScreenHost(modifier: Modifier = Modifier) {
    // Instantiate the ViewModel directly; the ViewModel has a no-arg constructor that
    // creates its own repository from BuildConfig, so a factory is no longer necessary.
    val vm: AlbumViewModel = viewModel()
    var selectedAlbumId by rememberSaveable { mutableStateOf<Long?>(null) }

    if (selectedAlbumId == null) {
        val state by vm.state.observeAsState(AlbumViewModel.UiState.Idle)

        LaunchedEffect(Unit) {
            if (state is AlbumViewModel.UiState.Idle) {
                vm.loadAlbums()
            }
        }

        AlbumsScreen(
            state = state,
            modifier = modifier,
            onAlbumClick = { albumId ->
                selectedAlbumId = albumId
            }
        )
    } else {
        AlbumDetailScreen(
            albumId = selectedAlbumId!!,
            viewModel = vm,
            onBackClick = { selectedAlbumId = null }
        )
    }
}

@Composable
fun MusicianScreenHost(modifier: Modifier = Modifier) {
    // Instantiate the ViewModel directly; the ViewModel has a no-arg constructor that
    // creates its own repository from BuildConfig, so a factory is no longer necessary.
    val vm: MusicianViewModel = viewModel()

    // Observe LiveData state so the UI recomposes on updates.
    val state by vm.state.observeAsState(MusicianViewModel.UiState.Idle)

    // Trigger loading only when the composable enters composition and the VM is idle.
    LaunchedEffect(Unit) {
        if (state is MusicianViewModel.UiState.Idle) {
            vm.loadMusicians()
        }
    }

    // Pass the current state to the screen composable.
    MusicianScreen(state = state, modifier = modifier)
}

@Composable
fun CollectorScreenHost(modifier: Modifier = Modifier) {
    // Instantiate the ViewModel directly; the ViewModel has a no-arg constructor that
    // creates its own repository from BuildConfig, so a factory is no longer necessary.
    val vm: CollectorViewModel = viewModel()

    // Observe LiveData state so the UI recomposes on updates.
    val state by vm.state.observeAsState(CollectorViewModel.UiState.Idle)

    // Trigger loading only when the composable enters composition and the VM is idle.
    LaunchedEffect(Unit) {
        if (state is CollectorViewModel.UiState.Idle) {
            vm.loadCollectors()
        }
    }

    // Pass the current state to the screen composable.
    CollectorsScreen(state = state, modifier = modifier)
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    INICIO("Inicio", Icons.Default.Home),
    ALBUMES("Albumes", Icons.Default.PlayArrow),
    ARTISTAS("Artistas", Icons.Default.AccountBox),
    COLECCIONISTAS("Coleccionistas", Icons.Default.AccountBox),
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ViniloTheme {
        Greeting("Android")
    }
}

@PreviewScreenSizes
@Composable
fun AlbumScreenPreview() {
    ViniloTheme {
        val sample = listOf(
            AlbumDto(
                id = 100,
                name = "Buscando América",
                cover = "https://i.pinimg.com/564x/aa/5f/ed/aa5fed7fac61cc8f41d1e79db917a7cd.jpg",
                releaseDate = "1984-08-01T00:00:00.000Z",
                description = "Buscando América es el primer álbum de la banda de Rubén Blades y Seis del Solar lanzado en 1984. La producción, bajo el sello Elektra, fusiona diferentes ritmos musicales tales como la salsa, reggae, rock, y el jazz latino. El disco fue grabado en Eurosound Studios en Nueva York entre mayo y agosto de 1983.",
                genre = "Salsa",
                recordLabel = "Elektra",
                tracks = emptyList(),
                performers = emptyList()
            ),
            AlbumDto(
                id = 101,
                name = "Poeta del pueblo",
                cover = "https://cdn.shopify.com/s/files/1/0275/3095/products/image_4931268b-7acf-4702-9c55-b2b3a03ed999_1024x1024.jpg",
                releaseDate = "1984-08-01T00:00:00.000Z",
                description = "Poeta del pueblo es el primer álbum de estudio de Rubén Blades lanzado en 1984. La producción, bajo el sello Elektra, fusiona diferentes ritmos musicales tales como la salsa, reggae, rock, y el jazz latino.",
                genre = "Salsa",
                recordLabel = "Elektra",
                tracks = emptyList(),
                performers = emptyList()
            ),
            AlbumDto(
                id = 102,
                name = "A Day at the Races",
                cover = "https://i.pinimg.com/564x/ab/50/f1/ab50f1be010a3b5e981207a97e00f8ca.jpg",
                releaseDate = "1976-12-10T00:00:00.000Z",
                description = "A Day at the Races es el quinto álbum de estudio de la banda de rock británica Queen. Fue lanzado el 10 de diciembre de 1976 por EMI Records en el Reino Unido y por Elektra Records en los Estados Unidos.",
                genre = "Rock",
                recordLabel = "EMI",
                tracks = emptyList(),
                performers = emptyList()
            )
        )

        AlbumsScreen(
            state = AlbumViewModel.UiState.Success(sample),
            onAlbumClick = {}
        )
    }
}

@PreviewScreenSizes
@Composable
fun MusicianScreenPreview() {
    ViniloTheme {
        val sample = listOf(
            MusicianDto(
                id = 100,
                name = "Adele Laurie Blue Adkins",
                image = "",
                description = "Singer",
                birthDate = "1988-05-05T00:00:00.000Z"
            ),
            MusicianDto(
                id = 101,
                name = "Metallica",
                image = "",
                description = "Band",
                birthDate = "1981-10-28T00:00:00.000Z"
            )
        )

        MusicianScreen(
            state = MusicianViewModel.UiState.Success(sample)
        )
    }
}
