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
import com.miso.vinilo.ui.theme.BaseWhite
import com.miso.vinilo.ui.theme.PrincipalColor
import com.miso.vinilo.ui.theme.ViniloTheme
import com.miso.vinilo.ui.views.home.HomeScreen
import com.miso.vinilo.ui.views.albums.AlbumsScreen
import com.miso.vinilo.ui.views.album.AlbumDetailScreen
import com.miso.vinilo.ui.views.musicians.MusicianScreen
import com.miso.vinilo.ui.views.collectors.CollectorsScreen
import com.miso.vinilo.ui.viewmodels.MusicianViewModel
import com.miso.vinilo.ui.viewmodels.AlbumViewModel
import com.miso.vinilo.data.dto.MusicianDto
import com.miso.vinilo.data.dto.AlbumDto

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
                    label = { Text(it.label, style = MaterialTheme.typography.labelSmall) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) { // NO innerPadding parameter here
        Scaffold(
            modifier = Modifier.fillMaxSize(), // The inner Scaffold now just fills the available space
            containerColor = Color.Transparent
        ) { scaffoldPadding -> // Declare padding from this Scaffold
            val contentModifier = Modifier.padding(scaffoldPadding)
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
            onAlbumClick = { albumId -> selectedAlbumId = albumId }
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
    val vm: MusicianViewModel = viewModel()
    val state by vm.state.observeAsState(MusicianViewModel.UiState.Idle)
    LaunchedEffect(Unit) {
        if (state is MusicianViewModel.UiState.Idle) {
            vm.loadMusicians()
        }
    }
    MusicianScreen(state = state, modifier = modifier)
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
