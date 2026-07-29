package cl.jfheimpinacap.appwebcam.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.jfheimpinacap.appwebcam.camera.CameraCapabilityInspector
import cl.jfheimpinacap.appwebcam.camera.DiagnosticState
import kotlinx.coroutines.launch

class CameraDiagnosticsViewModel(private val inspector: CameraCapabilityInspector) : ViewModel() {
    var state: DiagnosticState by mutableStateOf(DiagnosticState.Loading)
        private set

    init { analyze() }

    fun analyze() {
        state = DiagnosticState.Loading
        viewModelScope.launch {
            inspector.inspect().fold(
                onSuccess = { state = if (it.cameras.isEmpty()) DiagnosticState.NoCameras(it) else DiagnosticState.Success(it) },
                onFailure = { state = DiagnosticState.Error(it.message ?: "Error inesperado al consultar Camera2") },
            )
        }
    }

    companion object {
        fun factory(inspector: CameraCapabilityInspector): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                CameraDiagnosticsViewModel(inspector) as T
        }
    }
}
