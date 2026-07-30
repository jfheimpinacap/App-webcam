package cl.jfheimpinacap.appwebcam.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import cl.jfheimpinacap.appwebcam.camera.CameraSessionController
import cl.jfheimpinacap.appwebcam.camera.CaptureSize
import cl.jfheimpinacap.appwebcam.camera.SessionPhase
import java.util.Locale

@Composable
fun CameraPreviewScreen(viewModel: CameraPreviewViewModel, onDiagnostics: () -> Unit) {
    val context = LocalContext.current
    val activity = context as Activity
    val owner = LocalLifecycleOwner.current
    val controller = remember { CameraSessionController(context.applicationContext) }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    var requestedBefore by rememberSaveable { mutableStateOf(false) }
    var permissionGranted by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { permissionGranted = it; requestedBefore = true; if (!it) controller.stop() }
    val permanentlyDenied = requestedBefore && !permissionGranted && !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA)
    val state = viewModel.state

    LaunchedEffect(permissionGranted) {
        if (permissionGranted) controller.loadCameras { result -> result.fold(viewModel::camerasLoaded) { viewModel.error(it.message ?: "No fue posible enumerar cámaras") } }
    }
    DisposableEffect(Unit) { onDispose { controller.stop(); viewModel.stopped(interrupted = state.phase != SessionPhase.STOPPED) } }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("App Webcam", style = MaterialTheme.typography.headlineLarge)
        OutlinedButton(onClick = onDiagnostics) { Text("Abrir diagnóstico Camera2") }
        Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Permiso CAMERA", style = MaterialTheme.typography.titleMedium)
            Text(if (permissionGranted) "Concedido. La cámara solo se abre al pulsar Iniciar." else "Se necesita para mostrar una vista previa local y medir frames. No se solicita hasta que elijas concederlo.")
            if (!permissionGranted && !permanentlyDenied) Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) { Text(if (requestedBefore) "Volver a solicitar permiso" else "Conceder permiso") }
            if (permanentlyDenied) { Text("El diálogo ya no está disponible. Habilita Cámara en los ajustes de la aplicación."); Button(onClick = { context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))) }) { Text("Abrir ajustes") } }
        } }
        if (permissionGranted) {
            Text("Cámara", style = MaterialTheme.typography.titleMedium)
            state.cameras.forEach { camera -> FilterChip(selected = state.selectedCameraId == camera.cameraId, onClick = { controller.stop(); viewModel.selectCamera(camera.cameraId) }, label = { Text("${camera.lens} · cameraId ${camera.cameraId}${if (camera.supports60Fps) " · metadatos ≥60" else ""}") }) }
            Text("Resolución solicitada", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf(CaptureSize(1280,720), CaptureSize(1920,1080)).forEach { size -> FilterChip(state.request.size == size, { controller.stop(); viewModel.selectSize(size) }, { Text(size.toString()) }) } }
            Text("FPS solicitados", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf(30,60).forEach { fps -> FilterChip(state.request.fps == fps, { controller.stop(); viewModel.selectFps(fps) }, { Text("$fps FPS") }, enabled = fps == 30 || state.cameras.firstOrNull { it.cameraId == state.selectedCameraId }?.supports60Fps == true) } }
            Text("60 FPS es exploratorio: puede rechazarse, reducirse o no estar disponible para esta resolución; solo una sesión estable aporta evidencia provisional.", style = MaterialTheme.typography.bodySmall)
            Box(Modifier.fillMaxWidth().heightIn(min = 260.dp)) { AndroidView(factory = { PreviewView(it).apply { scaleType = PreviewView.ScaleType.FIT_CENTER; implementationMode = PreviewView.ImplementationMode.COMPATIBLE }.also { view -> previewView = view } }, modifier = Modifier.fillMaxSize()) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(enabled = state.selectedCameraId != null && state.phase !in listOf(SessionPhase.PREPARING, SessionPhase.ACTIVE, SessionPhase.WAITING), onClick = {
                    val camera = state.cameras.firstOrNull { it.cameraId == state.selectedCameraId }; val view = previewView
                    if (camera != null && view != null) { viewModel.preparing(); controller.start(owner, view, camera, state.request, viewModel::metrics, viewModel::bound, viewModel::error) }
                }) { Text("Iniciar") }
                OutlinedButton(onClick = { controller.stop(); viewModel.stopped() }, enabled = state.phase != SessionPhase.STOPPED) { Text("Detener") }
            }
        }
        MetricsCard(state)
    }
}

@Composable private fun MetricsCard(state: PreviewUiState) {
    fun Double?.shown() = this?.let { String.format(Locale.US, "%.1f", it) } ?: "Sin observar"
    Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Estado de sesión: ${state.phase}", style = MaterialTheme.typography.titleMedium)
        state.error?.let { Text("Error recuperable: $it") }
        Text("Configuración solicitada: ${state.request.size} a ${state.request.fps} FPS")
        Text("Resolución observada: ${state.metrics.observedSize ?: "Sin observar"}")
        Text("FPS de ventana: ${state.metrics.windowFps.shown()}")
        Text("FPS promedio: ${state.metrics.averageFps.shown()} · mínimo ${state.metrics.minimumWindowFps.shown()} · máximo ${state.metrics.maximumWindowFps.shown()}")
        Text("Duración: ${String.format(Locale.US, "%.1f", state.metrics.durationSeconds)} s · frames: ${state.metrics.frameCount}")
        Text("Resultado prudente: ${state.result}")
        Text("Los FPS son los frames entregados a ImageAnalysis, no una garantía de grabación ni transmisión futura.", style = MaterialTheme.typography.bodySmall)
    } }
}
