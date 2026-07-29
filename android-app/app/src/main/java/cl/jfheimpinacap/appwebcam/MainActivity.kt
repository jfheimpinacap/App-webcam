package cl.jfheimpinacap.appwebcam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.jfheimpinacap.appwebcam.camera.CameraCapabilityInspector
import cl.jfheimpinacap.appwebcam.ui.CameraDiagnosticsScreen
import cl.jfheimpinacap.appwebcam.ui.CameraDiagnosticsViewModel
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
                CameraDiagnosticsScreen(
                    state = diagnosticsViewModel.state,
                    onAnalyze = diagnosticsViewModel::analyze,
                )
            }
        }
    }
}
