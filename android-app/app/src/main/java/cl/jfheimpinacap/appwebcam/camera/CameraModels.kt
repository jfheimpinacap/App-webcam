package cl.jfheimpinacap.appwebcam.camera

data class CameraDiagnosticReport(
    val manufacturer: String,
    val brand: String,
    val model: String,
    val androidVersion: String,
    val apiLevel: Int,
    val cameras: List<CameraReport>,
)

data class CameraReport(
    val id: String,
    val lensFacing: String,
    val sensorOrientation: Int?,
    val hardwareLevel: String,
    val capabilities: List<String>,
    val isLogical: Boolean,
    val physicalIds: Set<String>,
    val zoomRange: String,
    val minimumFocusDistance: Float?,
    val autofocusModes: List<String>,
    val opticalStabilizationModes: List<String>,
    val videoStabilizationModes: List<String>,
    val aeFpsRanges: List<String>,
    val privateOutputs: List<OutputSize>,
    val yuvOutputs: List<OutputSize>,
    val targetEvaluations: List<TargetEvaluation>,
)

data class OutputSize(val width: Int, val height: Int, val theoreticalMaxFps: Double?)

data class TargetEvaluation(val label: String, val status: String)

sealed interface DiagnosticState {
    data object Loading : DiagnosticState
    data class NoCameras(val report: CameraDiagnosticReport) : DiagnosticState
    data class Success(val report: CameraDiagnosticReport) : DiagnosticState
    data class Error(val message: String) : DiagnosticState
}
