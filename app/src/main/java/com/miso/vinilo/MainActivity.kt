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
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.lifecycle.viewmodel.compose.viewModel
import com.miso.vinilo.domain.MusicianUseCaseImpl
import com.miso.vinilo.viewmodels.MusicianViewModelFactory
import com.miso.vinilo.ui.theme.BaseWhite
import com.miso.vinilo.ui.theme.PrincipalColor
import com.miso.vinilo.ui.theme.ViniloTheme
import com.miso.vinilo.ui.views.home.HomeScreen
import com.miso.vinilo.ui.views.albums.AlbumsScreen
import com.miso.vinilo.ui.views.musicians.MusicianScreen
import com.miso.vinilo.ui.views.collectors.CollectorsScreen
import com.miso.vinilo.viewmodels.MusicianViewModel
import com.miso.vinilo.data.model.Musician

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
                AppDestinations.ALBUMES -> AlbumsScreen(modifier = contentModifier)
                AppDestinations.ARTISTAS -> MusicianScreenHost(modifier = contentModifier)
                AppDestinations.COLECCIONISTAS -> CollectorsScreen(modifier = contentModifier)
            }
        }
    }
}

@Composable
fun MusicianScreenHost(modifier: Modifier = Modifier) {
    // Create the controller + factory + ViewModel when the user navigates to this screen.
    // Use the viewModel() composable so we don't need to cast an activity from the context.
    val controller = remember { MusicianUseCaseImpl.create(BuildConfig.BASE_URL) }
    val factory = remember(controller) { MusicianViewModelFactory(controller) }
    val vm: MusicianViewModel = viewModel(factory = factory)

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
fun MusicianScreenPreview() {
    ViniloTheme {
        val sample = listOf(
            Musician(
                id = 100,
                name = "Adele Laurie Blue Adkins",
                image = "",
                description = "Singer",
                birthDate = "1988-05-05T00:00:00.000Z"
            ),
            Musician(
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
