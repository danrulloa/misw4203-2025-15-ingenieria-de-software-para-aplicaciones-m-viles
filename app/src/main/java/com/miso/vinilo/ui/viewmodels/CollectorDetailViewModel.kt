package com.miso.vinilo.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miso.vinilo.data.adapter.NetworkResult
import com.miso.vinilo.data.dto.PerformerDto
import com.miso.vinilo.data.repository.CollectorRepository
import com.miso.vinilo.data.repository.AlbumRepository
import com.miso.vinilo.data.repository.MusicianRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Enriched data models for UI display
data class EnrichedCollectorAlbum(
    val id: Long,
    val name: String,
    val cover: String,
    val price: Int
)

data class EnrichedCollector(
    val id: Long,
    val name: String,
    val telephone: String,
    val email: String,
    val collectorAlbums: List<EnrichedCollectorAlbum>,
    val favoritePerformers: List<PerformerDto>
)

class CollectorDetailViewModel(
    private val collectorRepository: CollectorRepository,
    private val albumRepository: AlbumRepository,
    private val musicianRepository: MusicianRepository
) : ViewModel() {

    sealed class UiState {
        object Loading : UiState()
        data class Success(val collector: EnrichedCollector) : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun getCollectorDetail(id: Long) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            // Fetch collector, albums, and musicians in parallel
            val collectorDeferred = async { collectorRepository.getCollectorDetail(id) }
            val albumsDeferred = async { albumRepository.getAlbums() }

            val collectorResult = collectorDeferred.await()
            val albumsResult = albumsDeferred.await()

            when (collectorResult) {
                is NetworkResult.Success -> {
                    val collector = collectorResult.data

                    // Get album map for enrichment
                    val albumMap = when (albumsResult) {
                        is NetworkResult.Success -> albumsResult.data.associateBy { it.id }
                        is NetworkResult.Error -> emptyMap()
                    }

                    // Enrich collector albums with name and cover
                    val enrichedAlbums = collector.collectorAlbums?.map { collectorAlbum ->
                        val album = albumMap[collectorAlbum.id]
                        if (album != null) {
                            EnrichedCollectorAlbum(
                                id = collectorAlbum.id,
                                name = album.name,
                                cover = album.cover,
                                price = collectorAlbum.price
                            )
                        } else {
                            // If album not found, still show with placeholder
                            EnrichedCollectorAlbum(
                                id = collectorAlbum.id,
                                name = "Álbum #${collectorAlbum.id}",
                                cover = "",
                                price = collectorAlbum.price
                            )
                        }
                    } ?: emptyList()

                    // Enrich performers with images from musicians endpoint if missing
                    val enrichedPerformers = enrichPerformersWithImages(collector.favoritePerformers)

                    val enrichedCollector = EnrichedCollector(
                        id = collector.id,
                        name = collector.name,
                        telephone = collector.telephone,
                        email = collector.email,
                        collectorAlbums = enrichedAlbums,
                        favoritePerformers = enrichedPerformers
                    )

                    _uiState.value = UiState.Success(enrichedCollector)
                }
                is NetworkResult.Error -> {
                    _uiState.value = UiState.Error(collectorResult.message ?: "Unknown error")
                }
            }
        }
    }

    private suspend fun enrichPerformersWithImages(performers: List<PerformerDto>?): List<PerformerDto> {
        if (performers.isNullOrEmpty()) return emptyList()

        // Check which performers need image enrichment
        val performersNeedingImages = performers.filter { it.image.isBlank() }

        if (performersNeedingImages.isEmpty()) {
            return performers
        }

        // Get all musicians to match by name
        val musiciansResult = musicianRepository.getMusicians()
        val musicians = when (musiciansResult) {
            is NetworkResult.Success -> musiciansResult.data
            is NetworkResult.Error -> return performers // Can't enrich without musicians data
        }

        // Match performers with musicians by name
        return performers.map { performer ->
            if (performer.image.isBlank()) {
                // Find musician by name matching (use first two words for more tolerance)
                val matchedMusician = musicians.find { musician ->
                    val performerWords = performer.name.lowercase().split(" ").take(2)
                    val musicianWords = musician.name.lowercase().split(" ").take(2)
                    performerWords == musicianWords ||
                    performer.name.lowercase().contains(musician.name.lowercase()) ||
                    musician.name.lowercase().contains(performer.name.lowercase())
                }

                if (matchedMusician != null && !matchedMusician.image.isNullOrBlank()) {
                    performer.copy(image = matchedMusician.image)
                } else {
                    performer
                }
            } else {
                performer
            }
        }
    }
}
