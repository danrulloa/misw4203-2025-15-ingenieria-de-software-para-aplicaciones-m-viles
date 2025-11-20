package com.miso.vinilo.ui.views.album

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.miso.vinilo.data.dto.AlbumDto
import com.miso.vinilo.data.dto.CommentDto
import com.miso.vinilo.data.dto.TrackDto
import com.miso.vinilo.ui.theme.BaseWhite
import com.miso.vinilo.ui.viewmodels.AlbumViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumDetailScreen(
    albumId: Long,
    viewModel: AlbumViewModel = viewModel(),
    onBackClick: () -> Unit
) {
    val state = viewModel.albumDetailState.observeAsState().value

    LaunchedEffect(key1 = albumId) {
        viewModel.loadAlbum(albumId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Álbum") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (state) {
                is AlbumViewModel.AlbumDetailUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is AlbumViewModel.AlbumDetailUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = state.message)
                    }
                }
                is AlbumViewModel.AlbumDetailUiState.Success -> {
                    AlbumDetailContent(album = state.data, viewModel = viewModel)
                }
                else -> {
                    // Idle or other states
                }
            }
        }
    }
}

@Composable
fun AlbumDetailContent(album: AlbumDto, viewModel: AlbumViewModel) {
    var showAddCommentForm by remember { mutableStateOf(false) }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            AsyncImage(
                model = album.cover,
                contentDescription = "Album cover for ${album.name}",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop
            )
        }

        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = album.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    style = MaterialTheme.typography.headlineLarge
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = album.performers?.firstOrNull()?.name?.takeIf { it.isNotBlank() } ?: "Artista desconocido",
                    style = MaterialTheme.typography.titleMedium,
                    color = BaseWhite.copy(alpha = 0.8f)
                )
            }
        }

        album.tracks?.let { tracks ->
            if (tracks.isNotEmpty()) {
                item {
                    Text(
                        text = "Canciones",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
                items(tracks) { track ->
                    TrackItem(track = track, coverUrl = album.cover)
                }
            }
        }

        // --- Comments Section ---
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Comentarios",
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    style = MaterialTheme.typography.headlineSmall
                )
                if (!showAddCommentForm) {
                    Button(onClick = { showAddCommentForm = true }) {
                        Text(text = "Agregar Comentario")
                    }
                }
            }
        }

        // --- Add Comment Form ---
        if (showAddCommentForm) {
            item {
                AddCommentForm(
                    albumId = album.id,
                    viewModel = viewModel,
                    onCommentPosted = { showAddCommentForm = false },
                    onCancel = { showAddCommentForm = false }
                )
            }
        }

        // --- Comments List (Corrected) ---
        val comments = album.comments
        if (comments.isNullOrEmpty()) {
            item {
                Text(
                    text = "No hay comentarios aún.",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        } else {
            items(comments) { comment ->
                CommentItem(comment = comment)
            }
        }
    }
}

@Composable
fun TrackItem(track: TrackDto, coverUrl: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = coverUrl,
            contentDescription = "Track image",
            modifier = Modifier
                .size(56.dp)
                .clip(MaterialTheme.shapes.small),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.size(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.name,
                fontSize = 16.sp,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = track.duration,
                fontSize = 14.sp,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun CommentItem(comment: CommentDto) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Calificación: ${comment.rating}",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = comment.description,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun AddCommentForm(
    albumId: Long,
    viewModel: AlbumViewModel,
    onCommentPosted: () -> Unit,
    onCancel: () -> Unit
) {
    var rating by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val postState by viewModel.postCommentState.observeAsState()

    // When the post is successful, trigger the callback to close the form
    LaunchedEffect(postState) {
        if (postState is AlbumViewModel.PostCommentUiState.Success) {
            onCommentPosted()
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "Agregar tu Comentario", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = rating,
                onValueChange = { rating = it },
                label = { Text("Calificación (1-5)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                isError = rating.isNotEmpty() && rating.toIntOrNull() !in 1..5
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth(),
                isError = description.isBlank() && description.isNotEmpty()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Show error message if post fails
            if (postState is AlbumViewModel.PostCommentUiState.Error) {
                Text(
                    text = (postState as AlbumViewModel.PostCommentUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (postState is AlbumViewModel.PostCommentUiState.Loading) {
                    CircularProgressIndicator()
                } else {
                    Button(onClick = onCancel) { // Correctly uses onCancel
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                    Button(
                        onClick = {
                            val ratingValue = rating.toIntOrNull()
                            if (ratingValue != null && description.isNotBlank()) {
                                viewModel.postComment(albumId, ratingValue, description)
                            }
                        },
                        enabled = rating.toIntOrNull() in 1..5 && description.isNotBlank()
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}
