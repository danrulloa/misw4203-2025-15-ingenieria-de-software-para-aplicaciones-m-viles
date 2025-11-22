package com.miso.vinilo.ui.views.collectors

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.miso.vinilo.data.dto.CollectorDto
import com.miso.vinilo.ui.theme.BaseWhite
import com.miso.vinilo.ui.viewmodels.CollectorViewModel

@Composable
fun CollectorsScreen(
    state: CollectorViewModel.UiState?,
    modifier: Modifier = Modifier,
    onCollectorClick: (Long) -> Unit
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
                CollectorList(collectors = s.data, onCollectorClick = onCollectorClick)
            }
        }
    }
}

@Composable
private fun CollectorList(collectors: List<CollectorDto>, onCollectorClick: (Long) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(vertical = 8.dp)) {
        // Use stable key to avoid unnecessary row recompositions/rebinds
        items(items = collectors, key = { it.id }) { collector ->
            CollectorRow(collector = collector, onClick = { onCollectorClick(collector.id) })
        }
    }
}

@Composable
private fun CollectorRow(collector: CollectorDto, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Información del coleccionista
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = collector.name,
                style = MaterialTheme.typography.titleMedium,
                color = BaseWhite,
                fontWeight = FontWeight.SemiBold,
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