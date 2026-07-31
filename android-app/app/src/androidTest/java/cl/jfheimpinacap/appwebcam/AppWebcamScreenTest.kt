package cl.jfheimpinacap.appwebcam

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import cl.jfheimpinacap.appwebcam.camera.DiagnosticState
import cl.jfheimpinacap.appwebcam.ui.CameraDiagnosticsScreen
import cl.jfheimpinacap.appwebcam.ui.theme.AppWebcamTheme
import org.junit.Rule
import org.junit.Test

class AppWebcamScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComposeTestActivity>()

    @Test
    fun diagnosticsScreenExplainsScopeAndOffersControl() {
        var analysisRequested = false

        composeTestRule.setContent {
            AppWebcamTheme(dynamicColor = false) {
                CameraDiagnosticsScreen(
                    state = DiagnosticState.Error("Error recuperable"),
                    onAnalyze = { analysisRequested = true },
                )
            }
        }
        composeTestRule.onNodeWithText("App Webcam").assertIsDisplayed()
        composeTestRule.onNodeWithText(
            "Aviso: estos datos son metadatos declarados por Android; no representan una prueba real de captura ni garantizan combinaciones de resolución y FPS.",
        ).assertIsDisplayed()
        composeTestRule.onNodeWithText("Estado: consulta fallida").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Error recuperable").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Analizar nuevamente").performScrollTo().assertIsDisplayed().performClick()
        composeTestRule.runOnIdle { check(analysisRequested) }
    }
}
