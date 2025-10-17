package com.miso.vinilo.ui.collectors

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CollectorsScreen(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text("Coleccionistas", style = MaterialTheme.typography.titleLarge)
        Text("Perfiles de coleccionistas aquí.", style = MaterialTheme.typography.bodyMedium)
    }
}

