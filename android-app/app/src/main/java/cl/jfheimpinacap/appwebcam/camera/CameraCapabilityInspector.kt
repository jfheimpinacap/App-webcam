package cl.jfheimpinacap.appwebcam.camera

import android.content.Context
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.util.Size
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CameraCapabilityInspector(context: Context) {
    private val cameraManager = context.getSystemService(CameraManager::class.java)

    suspend fun inspect(): Result<CameraDiagnosticReport> = withContext(Dispatchers.IO) {
        runCatching {
            val ids = try {
                cameraManager.cameraIdList
            } catch (error: CameraAccessException) {
                throw CameraInspectionException("Android no permitió consultar las cámaras: ${error.reason}", error)
            }
            CameraDiagnosticReport(
                manufacturer = Build.MANUFACTURER.orUnknown(),
                brand = Build.BRAND.orUnknown(),
                model = Build.MODEL.orUnknown(),
                androidVersion = Build.VERSION.RELEASE.orUnknown(),
                apiLevel = Build.VERSION.SDK_INT,
                cameras = ids.map(::inspectCamera),
            )
        }
    }

    @Throws(CameraAccessException::class)
    private fun inspectCamera(id: String): CameraReport {
        val values = cameraManager.getCameraCharacteristics(id)
        val capabilities = values[CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES]?.toList().orEmpty()
        val map = values[CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP]
        val privateSizes = map?.getOutputSizes(SurfaceTexture::class.java).orEmpty()
            .map { it.toOutput { size -> map?.safeMinDuration(SurfaceTexture::class.java, size) } }
        val yuvSizes = map?.getOutputSizes(ImageFormat.YUV_420_888).orEmpty()
            .map { it.toOutput { size -> map?.safeMinDuration(ImageFormat.YUV_420_888, size) } }
        val usefulSizes = CameraMetadata.normalize(privateSizes + yuvSizes)
        val logical = capabilities.contains(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_LOGICAL_MULTI_CAMERA)
        return CameraReport(
            id = id,
            lensFacing = CameraMetadata.lensFacing(values[CameraCharacteristics.LENS_FACING]),
            sensorOrientation = values[CameraCharacteristics.SENSOR_ORIENTATION],
            hardwareLevel = CameraMetadata.hardwareLevel(values[CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL]),
            capabilities = capabilities.map(CameraMetadata::capability),
            isLogical = logical,
            physicalIds = if (logical) values.physicalCameraIds else emptySet(),
            zoomRange = zoomDescription(values),
            minimumFocusDistance = values[CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE],
            autofocusModes = values[CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES]?.map(CameraMetadata::autofocus).orEmpty(),
            opticalStabilizationModes = values[CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION]
                ?.map { if (it == 1) "ON" else if (it == 0) "OFF" else "DESCONOCIDO ($it)" }.orEmpty(),
            videoStabilizationModes = values[CameraCharacteristics.CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES]
                ?.map { if (it == 1) "ON" else if (it == 0) "OFF" else "DESCONOCIDO ($it)" }.orEmpty(),
            aeFpsRanges = values[CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES]
                ?.map { "${it.lower}–${it.upper} FPS" }.orEmpty(),
            privateOutputs = CameraMetadata.normalize(privateSizes),
            yuvOutputs = CameraMetadata.normalize(yuvSizes),
            targetEvaluations = listOf(
                CameraMetadata.evaluate("720p30", 1280, 720, 30, usefulSizes),
                CameraMetadata.evaluate("1080p30", 1920, 1080, 30, usefulSizes),
                CameraMetadata.evaluate("1080p60 (exploratorio)", 1920, 1080, 60, usefulSizes),
            ),
        )
    }

    private fun zoomDescription(values: CameraCharacteristics): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            values[CameraCharacteristics.CONTROL_ZOOM_RATIO_RANGE]?.let { return "${it.lower}× – ${it.upper}×" }
        }
        return values[CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM]?.let { "1× – ${it}× (digital)" }
            ?: "No declarado"
    }

    private fun Size.toOutput(duration: (Size) -> Long?): OutputSize =
        OutputSize(width, height, CameraMetadata.theoreticalFps(duration(this)))

    private fun <T> android.hardware.camera2.params.StreamConfigurationMap.safeMinDuration(
        klass: Class<T>,
        size: Size,
    ): Long? = runCatching { getOutputMinFrameDuration(klass, size) }.getOrNull()

    private fun android.hardware.camera2.params.StreamConfigurationMap.safeMinDuration(format: Int, size: Size): Long? =
        runCatching { getOutputMinFrameDuration(format, size) }.getOrNull()

    private fun String?.orUnknown() = this?.takeIf(String::isNotBlank) ?: "Desconocido"
}

class CameraInspectionException(message: String, cause: Throwable) : Exception(message, cause)
