package com.miso.vinilo.ui.views.musicians

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.miso.vinilo.data.GlobalRoleState
import com.miso.vinilo.ui.theme.BaseWhite
import com.miso.vinilo.ui.viewmodels.AlbumViewModel
import com.miso.vinilo.ui.viewmodels.MusicianViewModel

@Composable
fun MusicianDetailScreen(
    state: MusicianViewModel.DetailUiState,
    onBackClick: () -> Unit,
    onAlbumClick: (Long) -> Unit,
    onAddAlbumConfirm: (Long) -> Unit,
    selectableAlbums: List<MusicianViewModel.AlbumUi>,
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

            // --- estado para modo agregar y selección ---
            var addMode by remember { mutableStateOf(false) }
            var query by rememberSaveable { mutableStateOf("") }
            var selectedAlbumToAdd by rememberSaveable { mutableStateOf<Long?>(null) }
            val isCollector = GlobalRoleState.selectedRole == "Coleccionista"

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
                                Text(
                                    "‹",
                                    color = BaseWhite,
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                        }
                    )
                }
            ) { padding ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                        .testTag("musicianDetailList"),
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

                    // Header "Álbumes" + botón Agregar
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
                                onClick = {
                                    if (!isCollector) return@Button
                                    addMode = !addMode
                                    if (!addMode) {
                                        selectedAlbumToAdd = null
                                        query = ""
                                    }
                                },
                                enabled = isCollector,
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
                                    text = if (addMode) "Cancelar" else "Añadir álbum",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    // ================== MODO AGREGAR ÁLBUM ==================
                    if (addMode) {
                        // 1) Fuente de datos: TODOS los álbumes seleccionables
                        val sourceAlbums = selectableAlbums.ifEmpty {
                            // fallback por si aún no tienes el catálogo completo:
                            albums
                        }

                        // 2) Barra de búsqueda
                        item {
                            OutlinedTextField(
                                value = query,
                                onValueChange = { query = it },
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null
                                    )
                                },
                                trailingIcon = {
                                    if (query.isNotBlank()) {
                                        IconButton(onClick = { query = "" }) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Limpiar búsqueda"
                                            )
                                        }
                                    }
                                },
                                placeholder = { Text("Busca el álbum") },
                                singleLine = true
                            )
                        }

                        // 3) Filtro sobre TODOS los álbumes
                        val filtered = if (query.isBlank()) {
                            sourceAlbums
                        } else {
                            sourceAlbums.filter { it.name.contains(query, ignoreCase = true) }
                        }

                        // 4) Carrusel con selección visual
                        item {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(filtered, key = { it.id }) { album ->
                                    val isSelected = selectedAlbumToAdd == album.id.toLong()

                                    Column(
                                        modifier = Modifier
                                            .width(160.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(
                                                if (isSelected)
                                                    Color(0x334F3A67)   // sombreado cuando está seleccionado
                                                else
                                                    Color.Transparent
                                            )
                                            .clickable {
                                                selectedAlbumToAdd =
                                                    if (isSelected) null else album.id.toLong()
                                            }
                                            .padding(4.dp),
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

                                        if (isSelected) {
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                text = "Seleccionado",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF4F3A67),
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 5) Botón de confirmación (igual que antes)
                        item {
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    selectedAlbumToAdd?.let { id ->
                                        onAddAlbumConfirm(id)
                                        addMode = false
                                        selectedAlbumToAdd = null
                                        query = ""
                                    }
                                },
                                enabled = selectedAlbumToAdd != null,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF4F3A67),
                                    contentColor = Color.White,
                                    disabledContainerColor = Color(0xFF4F3A67).copy(alpha = 0.3f),
                                    disabledContentColor = Color.White.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(50)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Agregar álbum al artista")
                            }
                        }
                    }

                    // ================== CARRUSEL NORMAL DEL ARTISTA ==================
                    if (albums.isNotEmpty()) {
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