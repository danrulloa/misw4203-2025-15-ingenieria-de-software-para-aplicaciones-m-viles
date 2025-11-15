package com.miso.vinilo.ui.views.musicians

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.miso.vinilo.ui.theme.BaseWhite
import com.miso.vinilo.ui.viewmodels.MusicianViewModel

@Composable
fun MusicianDetailScreen(
    state: MusicianViewModel.DetailUiState,
    onBackClick: () -> Unit,
    onAlbumClick: (Long) -> Unit,
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
                        BoxWithConstraints {
                            val screenWidth = maxWidth

                            AsyncImage(
                                model = musician.image,
                                contentDescription = musician.name,
                                modifier = Modifier
                                    .width(screenWidth)
                                    .height(260.dp)
                                    .clip(
                                        RoundedCornerShape(
                                            bottomStart = 24.dp,
                                            bottomEnd = 24.dp
                                        )
                                    ),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }


                    // Nombre
                    item {
                        Text(
                            text = musician.name,
                            style = MaterialTheme.typography.headlineLarge,
                            color = BaseWhite
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Álbumes",
                                style = MaterialTheme.typography.titleMedium,
                                color = BaseWhite
                            )

                            Button(
                                onClick = { /* TODO: implementar acción para añadir álbum */ },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF4F3A67),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(50),
                                contentPadding = PaddingValues(
                                    horizontal = 12.dp,
                                    vertical = 6.dp
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Añadir álbum",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Añadir álbum",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    if (albums.isNotEmpty()) {
                        // Carrusel de álbumes
                        item {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(albums) { album ->
                                    Column(
                                        modifier = Modifier
                                            .width(160.dp)
                                            .clickable { onAlbumClick(album.id.toLong()) },
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        AsyncImage(
                                            model = album.cover,
                                            contentDescription = album.name,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(140.dp)
                                                .clip(RoundedCornerShape(16.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            text = album.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = BaseWhite,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Text(
                                            text = album.year,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = BaseWhite,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Opcional: mensaje cuando no hay álbumes
                        item {
                            Text(
                                text = "Este artista aún no tiene álbumes.",
                                style = MaterialTheme.typography.bodySmall,
                                color = BaseWhite.copy(alpha = 0.7f),
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }



                }
            }
        }
    }
}

