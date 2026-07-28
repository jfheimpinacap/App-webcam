package cl.jfheimpinacap.appwebcam

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import cl.jfheimpinacap.appwebcam.ui.theme.AppWebcamTheme
import org.junit.Rule
import org.junit.Test

class AppWebcamScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun initialScreenShowsApplicationAndCameraStatus() {
        composeTestRule.setContent {
            AppWebcamTheme(dynamicColor = false) {
                AppWebcamScreen()
            }
        }

        composeTestRule.onNodeWithText("App Webcam").assertIsDisplayed()
        composeTestRule.onNodeWithText("Base Android inicial").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cámara no configurada").assertIsDisplayed()
    }
}
