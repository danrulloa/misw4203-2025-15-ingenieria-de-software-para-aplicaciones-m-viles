package com.miso.vinilo.ui.views.musicians

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.miso.vinilo.ui.theme.BaseWhite
import com.miso.vinilo.ui.viewmodels.MusicianViewModel

@Composable
fun MusicianDetailScreen(
    state: MusicianViewModel.DetailUiState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (state) {
        MusicianViewModel.DetailUiState.Idle,
        MusicianViewModel.DetailUiState.Loading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is MusicianViewModel.DetailUiState.Error -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Ocurrió un error",
                    style = MaterialTheme.typography.titleLarge,
                    color = BaseWhite
                )
                Text(
                    text = state.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = BaseWhite
                )
                Spacer(Modifier.height(8.dp))
                Button(onClick = onBackClick) {
                    Text("Volver")
                }
            }
        }

        is MusicianViewModel.DetailUiState.Success -> {
            val data = state.data
            val musician = data.musician
            val albums = data.albums

            Scaffold(
                modifier = modifier,
                topBar = {
                    SmallTopAppBar(
                        title = {
                            Text(
                                text = "Detalle de artista",
                                color = BaseWhite
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onBackClick) {
                                Text("‹", color = BaseWhite, style = MaterialTheme.typography.titleLarge)
                            }
                        }
                    )
                }
            ) { padding ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Imagen del artista
                    item {
                        AsyncImage(
                            model = musician.image,
                            contentDescription = musician.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        )
                    }

                    // Nombre
                    item {
                        Text(
                            text = musician.name,
                            style = MaterialTheme.typography.headlineSmall,
                            color = BaseWhite
                        )
                    }

                    // Descripción / bio
                    item {
                        Text(
                            text = musician.description.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = BaseWhite
                        )
                    }

                    // Álbumes
                    if (albums.isNotEmpty()) {
                        item {
                            Text(
                                text = "Álbumes",
                                style = MaterialTheme.typography.titleMedium,
                                color = BaseWhite
                            )
                        }
                        item {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(albums) { album ->
                                    Column(
                                        modifier = Modifier
                                            .width(160.dp)
                                    ) {
                                        AsyncImage(
                                            model = album.cover,
                                            contentDescription = album.name,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(140.dp)
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            text = album.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = BaseWhite,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = album.year,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = BaseWhite
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

