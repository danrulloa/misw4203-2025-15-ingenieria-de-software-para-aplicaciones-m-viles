package com.miso.vinilo.ui.artists

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.miso.vinilo.data.dto.MusicianDto
import com.miso.vinilo.ui.viewmodels.MusicianViewModel
import com.miso.vinilo.ui.views.musicians.MusicianScreen
import org.junit.Rule
import org.junit.Test

class ArtistsListTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun showsArtists() {
        val list = listOf(
            MusicianDto(1, "Freddie Mercury", null, "Lorem Ipsum", "1946-09-05T00:00:00Z"),
            MusicianDto(2, "David Bowie", null, "Lorem Ipsum", "1947-01-08T00:00:00Z")
        )
        rule.setContent { MusicianScreen(state = MusicianViewModel.UiState.Success(list)) }

        rule.onNodeWithText("Freddie Mercury").assertIsDisplayed()
        rule.onNodeWithText("David Bowie").assertIsDisplayed()
    }

    @Test
    fun scrollLoadsMoreItems() {
        val many = (1..40).map { i -> MusicianDto(i.toLong(), "Artist $i", null, null, null) }
        rule.setContent { MusicianScreen(state = MusicianViewModel.UiState.Success(many)) }
        rule.onNode(hasScrollAction())
            .performScrollToNode(hasText("Artist 40"))

        rule.onNodeWithText("Artist 40").assertIsDisplayed()

    }

    @Test
    fun errorState_isShown() {
        rule.setContent {
            MusicianScreen(state = MusicianViewModel.UiState.Error("Ups, no fue posible cargar"))
        }
        rule.onNodeWithText("Ups, no fue posible cargar").assertIsDisplayed()
    }

    @Test
    fun loadingState_isShown() {
        rule.setContent { MusicianScreen(state = MusicianViewModel.UiState.Loading) }
        rule.onNodeWithText("Cargando...").assertIsDisplayed()
    }
}
