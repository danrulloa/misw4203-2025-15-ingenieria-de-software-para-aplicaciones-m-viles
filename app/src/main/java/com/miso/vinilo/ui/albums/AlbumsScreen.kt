package com.miso.vinilo.ui.albums

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.miso.vinilo.ui.theme.ViniloTheme

@Composable
fun AlbumsScreen(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text("Álbumes", style = MaterialTheme.typography.titleLarge)
        Text("Lista de álbumes aquí.", style = MaterialTheme.typography.bodyMedium)
    }
}

@Preview(showBackground = true)
@Composable
fun AlbumsScreenPreview() {
    ViniloTheme {
        AlbumsScreen()
    }
}
