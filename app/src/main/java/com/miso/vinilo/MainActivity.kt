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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.miso.vinilo.ui.theme.BaseWhite
import com.miso.vinilo.ui.theme.PrincipalColor
import com.miso.vinilo.ui.theme.ViniloTheme
import com.miso.vinilo.ui.views.home.HomeScreen
import com.miso.vinilo.ui.views.albums.AlbumsScreen
import com.miso.vinilo.ui.views.artists.ArtistsScreen
import com.miso.vinilo.ui.views.collectors.CollectorsScreen

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

@PreviewScreenSizes
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
                AppDestinations.ARTISTAS -> ArtistsScreen(modifier = contentModifier)
                AppDestinations.COLECCIONISTAS -> CollectorsScreen(modifier = contentModifier)
            }
        }
    }
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