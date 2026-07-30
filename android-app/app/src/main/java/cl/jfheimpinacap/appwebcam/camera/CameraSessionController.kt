package cl.jfheimpinacap.appwebcam.camera

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CaptureRequest as Camera2Request
import android.util.Range
import android.util.Size
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@OptIn(ExperimentalCamera2Interop::class)
class CameraSessionController(private val context: Context) {
    private var provider: ProcessCameraProvider? = null
    private var executor: ExecutorService? = null
    private var generation = 0L

    fun loadCameras(onResult: (Result<List<UsableCamera>>) -> Unit) {
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            runCatching {
                val value = future.get().also { provider = it }
                value.availableCameraInfos.map { info ->
                    val camera2 = Camera2CameraInfo.from(info)
                    val id = camera2.cameraId
                    val facing = when (info.lensFacing) {
                        CameraSelector.LENS_FACING_FRONT -> "Frontal"
                        CameraSelector.LENS_FACING_BACK -> "Trasera"
                        else -> "Externa o desconocida"
                    }
                    val ranges = camera2.getCameraCharacteristic(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES)
                        ?.map { it.lower..it.upper }.orEmpty()
                    UsableCamera(id, facing, CaptureEvaluation.canExplore60(ranges))
                }
            }.let(onResult)
        }, ContextCompat.getMainExecutor(context))
    }

    fun start(
        owner: LifecycleOwner,
        previewView: PreviewView,
        camera: UsableCamera,
        request: CaptureRequest,
        onMetrics: (CaptureMetrics) -> Unit,
        onBound: () -> Unit,
        onError: (String) -> Unit,
    ) {
        stop()
        val token = ++generation
        val cameraProvider = provider ?: return onError("CameraX todavía no está disponible")
        val worker = Executors.newSingleThreadExecutor().also { executor = it }
        val mainExecutor = ContextCompat.getMainExecutor(context)
        val meter = FrameRateMeter()
        runCatching {
            val strategy = ResolutionStrategy(Size(request.size.width, request.size.height), ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER)
            val selector = ResolutionSelector.Builder().setResolutionStrategy(strategy).build()
            val previewBuilder = Preview.Builder().setResolutionSelector(selector).setTargetRotation(previewView.display.rotation)
            val analysisBuilder = ImageAnalysis.Builder().setResolutionSelector(selector)
                .setTargetRotation(previewView.display.rotation)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            val fpsRange = Range(request.fps, request.fps)
            Camera2Interop.Extender(previewBuilder).setCaptureRequestOption(Camera2Request.CONTROL_AE_TARGET_FPS_RANGE, fpsRange)
            Camera2Interop.Extender(analysisBuilder).setCaptureRequestOption(Camera2Request.CONTROL_AE_TARGET_FPS_RANGE, fpsRange)
            val preview = previewBuilder.build().also { it.surfaceProvider = previewView.surfaceProvider }
            val analysis = analysisBuilder.build().also { useCase ->
                useCase.setAnalyzer(worker) { image ->
                    try {
                        meter.record(System.nanoTime(), CaptureSize(image.width, image.height))?.let { metrics ->
                            if (generation == token) mainExecutor.execute {
                                if (generation == token) onMetrics(metrics)
                            }
                        }
                    } finally { image.close() }
                }
            }
            val selectorById = CameraSelector.Builder().addCameraFilter { infos ->
                infos.filter { Camera2CameraInfo.from(it).cameraId == camera.cameraId }
            }.build()
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(owner, selectorById, preview, analysis)
            onBound()
        }.onFailure {
            stop()
            onError(it.message ?: "No fue posible enlazar la configuración")
        }
    }

    fun stop() {
        generation++
        provider?.unbindAll()
        executor?.shutdownNow()
        executor = null
    }
}
