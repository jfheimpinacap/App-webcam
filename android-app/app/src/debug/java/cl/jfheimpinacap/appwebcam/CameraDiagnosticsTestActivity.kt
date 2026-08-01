package cl.jfheimpinacap.appwebcam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import cl.jfheimpinacap.appwebcam.camera.DiagnosticState
import cl.jfheimpinacap.appwebcam.ui.CameraDiagnosticsScreen
import cl.jfheimpinacap.appwebcam.ui.theme.AppWebcamTheme

/** Debug-only host that creates the tested Compose hierarchy during its own lifecycle. */
class CameraDiagnosticsTestActivity : ComponentActivity() {
    var analysisRequested = false
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppWebcamTheme(dynamicColor = false) {
                CameraDiagnosticsScreen(
                    state = DiagnosticState.Error("Error recuperable"),
                    onAnalyze = { analysisRequested = true },
                )
            }
        }
    }
}
