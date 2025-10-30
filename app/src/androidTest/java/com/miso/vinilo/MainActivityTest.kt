package com.miso.vinilo

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun homeScreen_showsTextsLogoAndDropdownOptions() {

        // checking title and sub-title
        composeTestRule.onNodeWithText("Bienvenido a Vinilo").assertIsDisplayed()
        composeTestRule.onNodeWithText("Seleccione su rol para continuar").assertIsDisplayed()

        // check logo is displayed
        composeTestRule.onNodeWithContentDescription("Logo").assertIsDisplayed()

        // check dropdown: default value is "Usuario"
        composeTestRule.onAllNodesWithText("Usuario")[0].assertIsDisplayed()

        // open dropdown clicking over the field
        composeTestRule.onAllNodesWithText("Usuario")[0].performClick()

        // checking option = "Coleccionista" appears after click
        composeTestRule.onNodeWithText("Coleccionista").assertIsDisplayed()
    }
}
