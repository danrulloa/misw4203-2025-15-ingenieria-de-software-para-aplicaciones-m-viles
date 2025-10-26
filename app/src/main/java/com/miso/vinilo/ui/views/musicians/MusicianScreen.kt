package com.miso.vinilo.ui.views.musicians

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import android.util.Log
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miso.vinilo.BuildConfig
import com.miso.vinilo.data.model.Musician
import com.miso.vinilo.ui.theme.BaseWhite
import com.miso.vinilo.viewmodels.MusicianViewModel
import androidx.compose.ui.platform.LocalContext
import coil.request.ImageRequest

@Composable
fun MusicianScreen(
    state: MusicianViewModel.UiState?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text(
            text = "Artistas",
            style = MaterialTheme.typography.titleLarge,
            color = BaseWhite,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            textAlign = TextAlign.Center
        )

        when (val s = state) {
            null, is MusicianViewModel.UiState.Loading, is MusicianViewModel.UiState.Idle -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Cargando...", color = BaseWhite)
                }
            }
            is MusicianViewModel.UiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = s.message, color = BaseWhite)
                }
            }
            is MusicianViewModel.UiState.Success -> {
                MusicianList(musicians = s.data)
            }
        }
    }
}

@Composable
private fun MusicianList(musicians: List<Musician>) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(vertical = 8.dp)) {
        // Use stable key to avoid unnecessary row recompositions/rebinds
        items(items = musicians, key = { it.id }) { musician ->
            MusicianRow(musician = musician)
        }
    }
}

@Composable
private fun MusicianRow(musician: Musician) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Resolve image URL (handle relative paths) and show remote image if available; otherwise use initials fallback
        val resolvedImage = resolveImageUrl(musician.image)
        // container always draws a circular gray background
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color.Gray),
            contentAlignment = Alignment.Center
        ) {
            if (!resolvedImage.isNullOrBlank()) {
                Log.d("MusicianRow", "Attempting to load image: $resolvedImage")
                val ctx = LocalContext.current
                // Build and remember the ImageRequest so it isn't recreated on every recomposition.
                val userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                val referer = try {
                    val u = java.net.URL(resolvedImage)
                    "${u.protocol}://${u.host}/"
                } catch (_: Exception) {
                    "https://commons.wikimedia.org/"
                }

                val request = remember(resolvedImage) {
                    ImageRequest.Builder(ctx)
                        .data(resolvedImage)
                        .crossfade(true)
                        .addHeader("User-Agent", userAgent)
                        .addHeader("Referer", referer)
                        .listener(
                            onSuccess = { _, result ->
                                // result.drawable is non-null here per Coil callback signature
                                Log.d("MusicianRow", "Coil success: $resolvedImage, size=${result.drawable.intrinsicWidth}x${result.drawable.intrinsicHeight}")
                            },
                            onError = { _, result ->
                                Log.e("MusicianRow", "Coil failed to load image: $resolvedImage", result.throwable)
                            }
                        )
                        .build()
                }

                val painter = rememberAsyncImagePainter(request)

                Image(
                    painter = painter,
                    contentDescription = musician.name,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop
                )

                // Overlay initials on top; hide (alpha 0) only when image successfully loaded
                val initials = musician.name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("")
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .wrapContentSize(Alignment.Center)
                ) {
                    val showAlpha = if (painter.state is AsyncImagePainter.State.Success) 0f else 1f
                    Text(
                        text = initials,
                        color = Color.White,
                        fontSize = 26.sp,
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
                // no image -> show initials
                Log.d("MusicianRow", "No image, showing initials for ${musician.name}")
                val initials = musician.name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("")
                Text(
                    text = initials,
                    color = Color.White,
                    fontSize = 26.sp,
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

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = musician.name,
                style = MaterialTheme.typography.titleMedium,
                color = BaseWhite,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = formatBirthDate(musician.birthDate),
                style = MaterialTheme.typography.bodyMedium,
                color = BaseWhite
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Trailing chevron using a simple character to avoid icon package dependency
        Text(text = "›", color = BaseWhite, style = MaterialTheme.typography.titleLarge)
    }
}

internal fun resolveImageUrl(url: String?): String? {
    if (url.isNullOrBlank()) return null
    val trimmed = url.trim()
    // Replace localhost addresses so emulator can reach host machine
    val replaced = trimmed
        .replace("localhost", "10.0.2.2", ignoreCase = true)
        .replace("127.0.0.1", "10.0.2.2")
    return if (replaced.startsWith("http://") || replaced.startsWith("https://")) {
        replaced
    } else if (trimmed.startsWith("/")) {
        // prepend BASE_URL without double slash
        val base = BuildConfig.BASE_URL.trimEnd('/')
        "$base$trimmed"
    } else {
        // treat as relative path
        val base = BuildConfig.BASE_URL.trimEnd('/')
        "$base/$trimmed"
    }
}

internal fun formatBirthDate(birthDateIso: String?): String {
    if (birthDateIso.isNullOrBlank()) return ""
    return try {
        val odt = java.time.OffsetDateTime.parse(birthDateIso)
        odt.toLocalDate().format(java.time.format.DateTimeFormatter.ofLocalizedDate(java.time.format.FormatStyle.MEDIUM))
    } catch (_: Exception) {
        // Fallback: return raw string
        birthDateIso
    }
}
