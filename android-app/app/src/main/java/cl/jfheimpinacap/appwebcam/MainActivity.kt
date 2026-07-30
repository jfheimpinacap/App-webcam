package cl.jfheimpinacap.appwebcam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.jfheimpinacap.appwebcam.camera.CameraCapabilityInspector
import cl.jfheimpinacap.appwebcam.ui.CameraDiagnosticsScreen
import cl.jfheimpinacap.appwebcam.ui.CameraDiagnosticsViewModel
import cl.jfheimpinacap.appwebcam.ui.CameraPreviewScreen
import cl.jfheimpinacap.appwebcam.ui.CameraPreviewViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import cl.jfheimpinacap.appwebcam.ui.theme.AppWebcamTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppWebcamTheme {
                val diagnosticsViewModel: CameraDiagnosticsViewModel = viewModel(
                    factory = CameraDiagnosticsViewModel.factory(CameraCapabilityInspector(applicationContext)),
                )
                val previewViewModel: CameraPreviewViewModel = viewModel()
                var diagnostics by rememberSaveable { mutableStateOf(false) }
                if (diagnostics) CameraDiagnosticsScreen(
                    state = diagnosticsViewModel.state, onAnalyze = diagnosticsViewModel::analyze,
                    onBack = { diagnostics = false },
                ) else CameraPreviewScreen(previewViewModel, onDiagnostics = { diagnostics = true })
            }
        }
    }
}
