package cl.jfheimpinacap.appwebcam.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import cl.jfheimpinacap.appwebcam.camera.CaptureEvaluation
import cl.jfheimpinacap.appwebcam.camera.CaptureMetrics
import cl.jfheimpinacap.appwebcam.camera.CaptureRequest
import cl.jfheimpinacap.appwebcam.camera.CaptureSize
import cl.jfheimpinacap.appwebcam.camera.SessionPhase
import cl.jfheimpinacap.appwebcam.camera.SessionResult
import cl.jfheimpinacap.appwebcam.camera.UsableCamera

data class PreviewUiState(
    val cameras: List<UsableCamera> = emptyList(), val selectedCameraId: String? = null,
    val request: CaptureRequest = CaptureRequest(), val phase: SessionPhase = SessionPhase.STOPPED,
    val metrics: CaptureMetrics = CaptureMetrics(), val error: String? = null,
) { val result get() = CaptureEvaluation.evaluate(request, metrics, phase) }

class CameraPreviewViewModel : ViewModel() {
    var state by mutableStateOf(PreviewUiState()); private set
    fun camerasLoaded(cameras: List<UsableCamera>) { state = state.copy(cameras = cameras, selectedCameraId = cameras.firstOrNull { it.cameraId == state.selectedCameraId }?.cameraId ?: cameras.firstOrNull()?.cameraId, error = if (cameras.isEmpty()) "CameraX no expuso cámaras utilizables" else null) }
    fun selectCamera(id: String) { state = state.copy(selectedCameraId = id, phase = SessionPhase.STOPPED, metrics = CaptureMetrics()) }
    fun selectSize(size: CaptureSize) { state = state.copy(request = state.request.copy(size = size), phase = SessionPhase.STOPPED, metrics = CaptureMetrics()) }
    fun selectFps(fps: Int) { state = state.copy(request = state.request.copy(fps = fps), phase = SessionPhase.STOPPED, metrics = CaptureMetrics()) }
    fun preparing() { state = state.copy(phase = SessionPhase.PREPARING, metrics = CaptureMetrics(), error = null) }
    fun bound() { state = state.copy(phase = SessionPhase.WAITING) }
    fun metrics(value: CaptureMetrics) { state = state.copy(phase = SessionPhase.ACTIVE, metrics = value) }
    fun stopped(interrupted: Boolean = false) { state = state.copy(phase = if (interrupted) SessionPhase.INTERRUPTED else SessionPhase.STOPPED) }
    fun error(message: String) { state = state.copy(phase = SessionPhase.ERROR, error = message) }
}
