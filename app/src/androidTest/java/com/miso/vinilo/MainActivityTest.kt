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
        // La MainActivity ya infla su propio contenido, no es necesario un setContent aquí.

        // Verificar título y subtítulo
        composeTestRule.onNodeWithText("Bienvenido a Vinilo").assertIsDisplayed()
        composeTestRule.onNodeWithText("Seleccione su rol para continuar").assertIsDisplayed()

        // Verificar que el logo (contentDescription = "Logo") está visible
        composeTestRule.onNodeWithContentDescription("Logo").assertIsDisplayed()

        // Verificar dropdown: valor por defecto - usar la primera aparición de "Usuario"
        composeTestRule.onAllNodesWithText("Usuario")[0].assertIsDisplayed()

        // Intentar abrir el dropdown haciendo click sobre el campo (primera aparición)
        composeTestRule.onAllNodesWithText("Usuario")[0].performClick()

        // Comprobar que la opción "Coleccionista" aparece después del click
        composeTestRule.onNodeWithText("Coleccionista").assertIsDisplayed()
    }
}
