package cl.jfheimpinacap.appwebcam.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.jfheimpinacap.appwebcam.camera.CameraDiagnosticReport
import cl.jfheimpinacap.appwebcam.camera.CameraReport
import cl.jfheimpinacap.appwebcam.camera.DiagnosticState
import cl.jfheimpinacap.appwebcam.camera.OutputSize
import cl.jfheimpinacap.appwebcam.ui.theme.AppWebcamTheme
import java.util.Locale

@Composable
fun CameraDiagnosticsScreen(
    state: DiagnosticState,
    onAnalyze: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).verticalScroll(rememberScrollState())
                .padding(PaddingValues(horizontal = 20.dp, vertical = 16.dp)),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text("App Webcam", style = MaterialTheme.typography.headlineLarge)
            Text("Investigación de capacidades Camera2", style = MaterialTheme.typography.titleLarge)
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Aviso: estos datos son metadatos declarados por Android; no representan una prueba real de captura ni garantizan combinaciones de resolución y FPS.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            when (state) {
                DiagnosticState.Loading -> {
                    Text("Estado: analizando metadatos…", style = MaterialTheme.typography.titleMedium)
                    CircularProgressIndicator()
                }
                is DiagnosticState.Error -> StatusSection("Estado: consulta fallida", state.message)
                is DiagnosticState.NoCameras -> {
                    DeviceSummary(state.report, "Sin cámaras declaradas")
                    StatusSection("Estado: sin cámaras", "Android no informó identificadores de cámara.")
                }
                is DiagnosticState.Success -> {
                    DeviceSummary(state.report, "Diagnóstico completado")
                    state.report.cameras.forEach { CameraCard(it) }
                }
            }
            Button(onClick = onAnalyze, enabled = state !is DiagnosticState.Loading) {
                Text(if (state is DiagnosticState.Loading) "Analizando…" else "Analizar nuevamente")
            }
        }
    }
}

@Composable
private fun DeviceSummary(report: CameraDiagnosticReport, status: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text("Resumen del dispositivo", style = MaterialTheme.typography.titleMedium)
            Detail("Fabricante", report.manufacturer)
            Detail("Marca", report.brand)
            Detail("Modelo", report.model)
            Detail("Android", "${report.androidVersion} (API ${report.apiLevel})")
            Detail("Cámaras detectadas", report.cameras.size.toString())
            Detail("Estado general", status)
        }
    }
}

@Composable
private fun StatusSection(title: String, message: String) {
    Text(title, style = MaterialTheme.typography.titleMedium)
    Text(message)
}

@Composable
private fun CameraCard(camera: CameraReport) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Cámara ${camera.id}", style = MaterialTheme.typography.headlineSmall)
            Detail("Lente", camera.lensFacing)
            Detail("Orientación física", camera.sensorOrientation?.let { "$it°" } ?: "No declarada")
            Detail("Nivel Camera2", camera.hardwareLevel)
            Detail("Capacidades", camera.capabilities.ifEmpty { listOf("No declaradas") }.joinToString())
            Detail("Cámara lógica", if (camera.isLogical) "Sí" else "No")
            Detail("IDs físicos", camera.physicalIds.ifEmpty { setOf("No expuestos") }.joinToString())
            Detail("Zoom", camera.zoomRange)
            Detail("Enfoque mínimo", camera.minimumFocusDistance?.let { "$it dioptrías" } ?: "No declarado")
            Detail("Autoenfoque", camera.autofocusModes.ifEmpty { listOf("No declarado") }.joinToString())
            Detail("Estabilización óptica", camera.opticalStabilizationModes.ifEmpty { listOf("No declarada") }.joinToString())
            Detail("Estabilización de video", camera.videoStabilizationModes.ifEmpty { listOf("No declarada") }.joinToString())
            Detail("Rangos AE", camera.aeFpsRanges.ifEmpty { listOf("No declarados") }.joinToString())
            HorizontalDivider(Modifier.padding(vertical = 4.dp))
            Text("Objetivos (no validados)", style = MaterialTheme.typography.titleMedium)
            camera.targetEvaluations.forEach { Detail(it.label, it.status) }
            OutputList("Salida PRIVATE / SurfaceTexture", camera.privateOutputs)
            OutputList("Salida YUV_420_888", camera.yuvOutputs)
        }
    }
}

@Composable
private fun OutputList(title: String, outputs: List<OutputSize>) {
    Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 6.dp))
    if (outputs.isEmpty()) Text("No declarada") else outputs.take(10).forEach {
        val fps = it.theoreticalMaxFps?.let { value ->
            " — Máximo teórico declarado: ${String.format(Locale.US, "%.1f", value)} FPS"
        }.orEmpty()
        Text("${it.width} × ${it.height}$fps", style = MaterialTheme.typography.bodySmall)
    }
    if (outputs.size > 10) Text("… y ${outputs.size - 10} tamaños adicionales", style = MaterialTheme.typography.bodySmall)
}

@Composable
private fun Detail(label: String, value: String) {
    Text("$label: $value", style = MaterialTheme.typography.bodyMedium)
}

@Preview(showBackground = true)
@Composable
private fun DiagnosticsPreview() {
    AppWebcamTheme(dynamicColor = false) {
        CameraDiagnosticsScreen(DiagnosticState.Error("Ejemplo de error recuperable"), {})
    }
}
