package com.miso.vinilo.ui.views.collectors

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.miso.vinilo.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CollectorDetailTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testCollectorDetailNavigation() {
        // 1. Wait for the "Coleccionistas" tab to appear and click it
        composeTestRule.onNodeWithText("Coleccionistas").performClick()

        // 2. Wait for the list to load (assuming at least one collector exists)
        // We look for a node that has a click action (the row)
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasClickAction()).fetchSemanticsNodes().isNotEmpty()
        }

        // 3. Click the first collector in the list
        composeTestRule.onAllNodes(hasClickAction()).onFirst().performClick()

        // 4. Verify we are on the detail screen
        // Check for the title "Detalle Coleccionista"
        composeTestRule.onNodeWithText("Detalle Coleccionista").assertIsDisplayed()

        // 5. Verify some content is displayed (e.g., email icon or text)
        // Since data is dynamic, we check for static labels or structure if possible,
        // or just ensure the screen didn't crash and shows the header.
        // We can check if "Artistas Favoritos" or "Álbumes en Colección" or "Comentarios" titles exist if data is present,
        // but they might not be there if lists are empty.
        // However, the name should be there. We can't know the name beforehand easily without mocking data in E2E.
        // So we stick to checking the TopBar title which confirms navigation.
    }
}
