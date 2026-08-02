package cl.jfheimpinacap.appwebcam

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ActivityScenario
import org.junit.Rule
import org.junit.Test

class AppWebcamScreenTest {
    @get:Rule
    val composeTestRule = createEmptyComposeRule()

    @Test
    fun diagnosticsScreenExplainsScopeAndOffersControl() {
        ActivityScenario.launch(CameraDiagnosticsTestActivity::class.java).use { scenario ->
            composeTestRule.onNodeWithText("App Webcam").assertIsDisplayed()
            composeTestRule.onNodeWithText("Investigación de capacidades Camera2").assertIsDisplayed()
            composeTestRule.onNodeWithText(
                "Aviso: estos datos son metadatos declarados por Android; no representan una prueba real de captura ni garantizan combinaciones de resolución y FPS.",
            ).assertIsDisplayed()
            composeTestRule.onNodeWithText("Estado: consulta fallida").performScrollTo().assertIsDisplayed()
            composeTestRule.onNodeWithText("Error recuperable").performScrollTo().assertIsDisplayed()
            composeTestRule.onNodeWithText("Analizar nuevamente").performScrollTo().assertIsDisplayed().performClick()
            scenario.onActivity { activity -> check(activity.analysisRequested) }
        }
    }
}
