package com.miso.vinilo.ui.views.collectors

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.miso.vinilo.data.dto.CollectorDto
import com.miso.vinilo.ui.theme.BaseWhite
import com.miso.vinilo.ui.viewmodels.CollectorViewModel

@Composable
fun CollectorsScreen(
    viewModel: CollectorViewModel,
    modifier: Modifier = Modifier,
    onCollectorClick: (Long) -> Unit = {}
) {
    val collectors = viewModel.collectors.collectAsLazyPagingItems()

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
        CollectorPagedList(collectors = collectors, onCollectorClick = onCollectorClick)
    }
}

@Composable
private fun CollectorPagedList(
    collectors: LazyPagingItems<CollectorDto>,
    onCollectorClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(
            count = collectors.itemCount,
            key = { index -> collectors[index]?.id ?: index }
        ) { index ->
            collectors[index]?.let { collector ->
                CollectorRow(collector = collector, onClick = { onCollectorClick(collector.id) })
            }
        }
    }
}

@Composable
private fun CollectorRow(
    collector: CollectorDto,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
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

            val albumCount = collector.albumCountForUi ?: 0
            val albumText = if (albumCount == 1) "album" else "albumes"
            Text(
                text = "$albumCount $albumText",
                style = MaterialTheme.typography.bodyMedium,
                color = BaseWhite.copy(alpha = 0.7f)
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Ver detalle",
            tint = BaseWhite.copy(alpha = 0.5f),
            modifier = Modifier.size(24.dp)
        )
    }
}
