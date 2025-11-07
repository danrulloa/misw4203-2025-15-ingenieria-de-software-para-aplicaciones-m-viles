package com.miso.vinilo.ui.views.albums

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.miso.vinilo.BuildConfig
import com.miso.vinilo.data.dto.AlbumDto
import com.miso.vinilo.ui.theme.BaseWhite
import com.miso.vinilo.ui.viewmodels.AlbumViewModel

@Composable
fun AlbumsScreen(
    state: AlbumViewModel.UiState?,
    modifier: Modifier = Modifier,
    onAlbumClick: (Long) -> Unit // Added this callback
) {
    Column(modifier = modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text(
            text = "Álbumes",
            style = MaterialTheme.typography.titleLarge,
            color = BaseWhite,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            textAlign = TextAlign.Center
        )

        when (val s = state) {
            null, is AlbumViewModel.UiState.Loading, is AlbumViewModel.UiState.Idle -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Cargando...", color = BaseWhite)
                }
            }
            is AlbumViewModel.UiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = s.message, color = BaseWhite)
                }
            }
            is AlbumViewModel.UiState.Success -> {
                AlbumList(albums = s.data, onAlbumClick = onAlbumClick) // Pass the callback
            }
        }
    }
}

@Composable
private fun AlbumList(albums: List<AlbumDto>, onAlbumClick: (Long) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items = albums, key = { it.id }) { album ->
            AlbumCard(album = album, onAlbumClick = onAlbumClick) // Pass the callback
        }
    }
}

@Composable
private fun AlbumCard(album: AlbumDto, onAlbumClick: (Long) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clickable { onAlbumClick(album.id) } // Made the card clickable
    ) {
        val resolvedImage = resolveImageUrl(album.cover)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Gray),
            contentAlignment = Alignment.Center
        ) {
            if (!resolvedImage.isNullOrBlank()) {
                Log.d("AlbumCard", "Attempting to load image: $resolvedImage")
                val ctx = LocalContext.current
                val userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                val referer = try {
                    val u = java.net.URL(resolvedImage)
                    "${u.protocol}://${u.host}/"
                } catch (_: Exception) {
                    BuildConfig.BASE_URL
                }

                val request = remember(resolvedImage) {
                    ImageRequest.Builder(ctx)
                        .data(resolvedImage)
                        .crossfade(true)
                        .addHeader("User-Agent", userAgent)
                        .addHeader("Referer", referer)
                        .listener(
                            onSuccess = { _, result ->
                                Log.d("AlbumCard", "Coil success: $resolvedImage, size=${result.drawable.intrinsicWidth}x${result.drawable.intrinsicHeight}")
                            },
                            onError = { _, result ->
                                Log.e("AlbumCard", "Coil failed to load image: $resolvedImage", result.throwable)
                            }
                        )
                        .build()
                }

                val painter = rememberAsyncImagePainter(request)

                Image(
                    painter = painter,
                    contentDescription = album.name,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop
                )

                val initials = album.name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("")
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .wrapContentSize(Alignment.Center)
                ) {
                    val showAlpha = if (painter.state is AsyncImagePainter.State.Success) 0f else 1f
                    Text(
                        text = initials,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium.copy(
                            shadow = Shadow(
                                color = Color.Black,
                                offset = Offset(1f, 1f),
                                blurRadius = 2f
                            )
                        ),
                        modifier = Modifier.graphicsLayer { alpha = showAlpha }
                    )
                }
            } else {
                Log.d("AlbumCard", "No image, showing initials for ${album.name}")
                val initials = album.name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("")
                Text(
                    text = initials,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium.copy(
                        shadow = Shadow(
                            color = Color.Black,
                            offset = Offset(1f, 1f),
                            blurRadius = 2f
                        )
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column {
            Text(
                text = album.name,
                style = MaterialTheme.typography.titleSmall,
                color = BaseWhite,
                maxLines = 2,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${album.genre} • ${album.recordLabel}",
                style = MaterialTheme.typography.bodySmall,
                color = BaseWhite.copy(alpha = 0.8f),
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = extractYear(album.releaseDate),
                style = MaterialTheme.typography.bodySmall,
                color = BaseWhite.copy(alpha = 0.7f)
            )
        }
    }
}

internal fun resolveImageUrl(url: String?): String? {
    if (url.isNullOrBlank()) return null
    val trimmed = url.trim()
    val replaced = trimmed
        .replace("localhost", "10.0.2.2", ignoreCase = true)
        .replace("127.0.0.1", "10.0.2.2")
    return if (replaced.startsWith("http://") || replaced.startsWith("https://")) {
        replaced
    } else if (trimmed.startsWith("/")) {
        val base = BuildConfig.BASE_URL.trimEnd('/')
        "$base$trimmed"
    } else {
        val base = BuildConfig.BASE_URL.trimEnd('/')
        "$base/$trimmed"
    }
}

internal fun extractYear(releaseDate: String?): String {
    if (releaseDate.isNullOrBlank()) return ""
    return try {
        val odt = java.time.OffsetDateTime.parse(releaseDate)
        odt.year.toString()
    } catch (_: Exception) {
        val yearPattern = Regex("\\d{4}")
        yearPattern.find(releaseDate)?.value ?: releaseDate
    }
}