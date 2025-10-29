package com.miso.vinilo

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.miso.vinilo.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun welcome_text_is_displayed() {
       composeTestRule.onNodeWithText("Bienvenido a Vinilo").assertIsDisplayed()
    }

    @Test
    fun role_selection_text_is_displayed() {
        composeTestRule.onNodeWithText("Seleccione su rol para continuar").assertIsDisplayed()
    }
}
