package com.miso.vinilo.ui.views.collectors

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
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
import com.miso.vinilo.data.dto.CollectorDto
import com.miso.vinilo.ui.theme.BaseWhite
import com.miso.vinilo.ui.viewmodels.CollectorViewModel

@Composable
fun CollectorsScreen(
    state: CollectorViewModel.UiState?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text(
            text = "Coleccionistas",
            style = MaterialTheme.typography.titleLarge,
            color = BaseWhite,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            textAlign = TextAlign.Center
        )

        when (val s = state) {
            null, is CollectorViewModel.UiState.Loading, is CollectorViewModel.UiState.Idle -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Cargando...", color = BaseWhite)
                }
            }
            is CollectorViewModel.UiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = s.message, color = BaseWhite)
                }
            }
            is CollectorViewModel.UiState.Success -> {
                CollectorList(collectors = s.data)
            }
        }
    }
}

@Composable
private fun CollectorList(collectors: List<CollectorDto>) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(vertical = 8.dp)) {
        // Use stable key to avoid unnecessary row recompositions/rebinds
        items(items = collectors, key = { it.id }) { collector ->
            CollectorRow(collector = collector)
        }
    }
}

@Composable
private fun CollectorRow(collector: CollectorDto) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar circular con iniciales (el API no retorna imagen para coleccionistas)
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color.Gray),
            contentAlignment = Alignment.Center
        ) {
            // Mostrar iniciales del nombre
            val initials = collector.name.split(" ")
                .mapNotNull { it.firstOrNull()?.toString() }
                .take(2)
                .joinToString("")

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

        Spacer(modifier = Modifier.width(16.dp))

        // Información del coleccionista
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = collector.name,
                style = MaterialTheme.typography.titleMedium,
                color = BaseWhite,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Cantidad de álbumes
            val albumCount = collector.collectorAlbums?.size ?: 0
            val albumText = if (albumCount == 1) "album" else "albumes"
            Text(
                text = "$albumCount $albumText",
                style = MaterialTheme.typography.bodyMedium,
                color = BaseWhite.copy(alpha = 0.7f)
            )
        }

        // Chevron de navegación
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Ver detalle",
            tint = BaseWhite.copy(alpha = 0.5f),
            modifier = Modifier.size(24.dp)
        )
    }
}