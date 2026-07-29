package cl.jfheimpinacap.appwebcam

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import cl.jfheimpinacap.appwebcam.camera.DiagnosticState
import cl.jfheimpinacap.appwebcam.ui.CameraDiagnosticsScreen
import cl.jfheimpinacap.appwebcam.ui.theme.AppWebcamTheme
import org.junit.Rule
import org.junit.Test

class AppWebcamScreenTest {
    @get:Rule val composeTestRule = createComposeRule()

    @Test fun diagnosticsScreenExplainsScopeAndOffersControl() {
        composeTestRule.setContent {
            AppWebcamTheme(dynamicColor = false) {
                CameraDiagnosticsScreen(DiagnosticState.Error("Error recuperable"), {})
            }
        }
        composeTestRule.onNodeWithText("App Webcam").assertIsDisplayed()
        composeTestRule.onNodeWithText("Analizar nuevamente").assertIsDisplayed()
        composeTestRule.onNodeWithText(
            "Aviso: estos datos son metadatos declarados por Android; no representan una prueba real de captura ni garantizan combinaciones de resolución y FPS.",
        ).assertIsDisplayed()
        composeTestRule.onNodeWithText("Estado: consulta fallida").assertIsDisplayed()
    }
}
