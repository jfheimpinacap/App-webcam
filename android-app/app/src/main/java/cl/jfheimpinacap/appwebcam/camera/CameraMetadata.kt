package cl.jfheimpinacap.appwebcam.camera

import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CaptureRequest

object CameraMetadata {
    fun lensFacing(value: Int?): String = when (value) {
        CameraCharacteristics.LENS_FACING_FRONT -> "Frontal"
        CameraCharacteristics.LENS_FACING_BACK -> "Trasera"
        CameraCharacteristics.LENS_FACING_EXTERNAL -> "Externa"
        else -> "Desconocida${value?.let { " ($it)" }.orEmpty()}"
    }

    fun hardwareLevel(value: Int?): String = when (value) {
        CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY -> "LEGACY"
        CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED -> "LIMITED"
        CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL -> "FULL"
        CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3 -> "LEVEL_3"
        CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_EXTERNAL -> "EXTERNAL"
        else -> "Desconocido${value?.let { " ($it)" }.orEmpty()}"
    }

    fun capability(value: Int): String = when (value) {
        CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_BACKWARD_COMPATIBLE -> "BACKWARD_COMPATIBLE"
        CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_SENSOR -> "MANUAL_SENSOR"
        CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_POST_PROCESSING -> "MANUAL_POST_PROCESSING"
        CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_RAW -> "RAW"
        CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_PRIVATE_REPROCESSING -> "PRIVATE_REPROCESSING"
        CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_READ_SENSOR_SETTINGS -> "READ_SENSOR_SETTINGS"
        CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_BURST_CAPTURE -> "BURST_CAPTURE"
        CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_YUV_REPROCESSING -> "YUV_REPROCESSING"
        CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_DEPTH_OUTPUT -> "DEPTH_OUTPUT"
        CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_CONSTRAINED_HIGH_SPEED_VIDEO -> "CONSTRAINED_HIGH_SPEED_VIDEO"
        CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_MOTION_TRACKING -> "MOTION_TRACKING"
        CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_LOGICAL_MULTI_CAMERA -> "LOGICAL_MULTI_CAMERA"
        CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_MONOCHROME -> "MONOCHROME"
        CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_SECURE_IMAGE_DATA -> "SECURE_IMAGE_DATA"
        else -> "DESCONOCIDA ($value)"
    }

    fun autofocus(value: Int): String = when (value) {
        CaptureRequest.CONTROL_AF_MODE_OFF -> "OFF"
        CaptureRequest.CONTROL_AF_MODE_AUTO -> "AUTO"
        CaptureRequest.CONTROL_AF_MODE_MACRO -> "MACRO"
        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO -> "CONTINUOUS_VIDEO"
        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE -> "CONTINUOUS_PICTURE"
        CaptureRequest.CONTROL_AF_MODE_EDOF -> "EDOF"
        else -> "DESCONOCIDO ($value)"
    }

    fun theoreticalFps(durationNs: Long?): Double? = durationNs?.takeIf { it > 0 }?.let { 1_000_000_000.0 / it }

    fun normalize(sizes: List<OutputSize>): List<OutputSize> = sizes
        .distinctBy { it.width to it.height }
        .sortedWith(compareByDescending<OutputSize> { isMvp(it) }.thenByDescending { it.width.toLong() * it.height })

    fun evaluate(label: String, width: Int, height: Int, fps: Int, sizes: List<OutputSize>): TargetEvaluation {
        val size = sizes.firstOrNull { it.width == width && it.height == height }
            ?: return TargetEvaluation(label, "Resolución no declarada")
        val maximum = size.theoreticalMaxFps
        val status = when {
            maximum == null -> "Resolución declarada; FPS todavía no verificados. Requiere prueba de captura posterior"
            maximum + 0.01 >= fps -> "Potencial teórico compatible según metadatos; requiere prueba de captura posterior"
            else -> "Información insuficiente para $fps FPS; requiere prueba de captura posterior"
        }
        return TargetEvaluation(label, status)
    }

    private fun isMvp(size: OutputSize) =
        (size.width == 1280 && size.height == 720) || (size.width == 1920 && size.height == 1080)
}
