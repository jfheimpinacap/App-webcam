package cl.jfheimpinacap.appwebcam.camera

import android.hardware.camera2.CameraCharacteristics
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CameraMetadataTest {
    @Test fun translatesKnownAndUnknownLensFacing() {
        assertEquals("Frontal", CameraMetadata.lensFacing(CameraCharacteristics.LENS_FACING_FRONT))
        assertTrue(CameraMetadata.lensFacing(99).startsWith("Desconocida"))
    }

    @Test fun translatesHardwareAndCapabilities() {
        assertEquals("FULL", CameraMetadata.hardwareLevel(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL))
        assertEquals("RAW", CameraMetadata.capability(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_RAW))
        assertTrue(CameraMetadata.capability(999).startsWith("DESCONOCIDA"))
    }

    @Test fun theoreticalFpsRejectsMissingAndNonPositiveDurations() {
        assertNull(CameraMetadata.theoreticalFps(null))
        assertNull(CameraMetadata.theoreticalFps(0))
        assertNull(CameraMetadata.theoreticalFps(-1))
        assertEquals(30.0, CameraMetadata.theoreticalFps(33_333_333)!!, 0.01)
    }

    @Test fun normalizationPrioritizesMvpAndRemovesDuplicateSizes() {
        val normalized = CameraMetadata.normalize(
            listOf(OutputSize(640, 480, null), OutputSize(1920, 1080, 30.0), OutputSize(1920, 1080, 60.0), OutputSize(1280, 720, 30.0)),
        )
        assertEquals(listOf(1920 to 1080, 1280 to 720, 640 to 480), normalized.map { it.width to it.height })
    }

    @Test fun evaluationIsConservativeForAbsentUnknownAndTheoreticalTargets() {
        assertEquals("Resolución no declarada", CameraMetadata.evaluate("720p30", 1280, 720, 30, emptyList()).status)
        assertTrue(CameraMetadata.evaluate("720p30", 1280, 720, 30, listOf(OutputSize(1280, 720, null))).status.contains("no verificados"))
        assertTrue(CameraMetadata.evaluate("1080p60", 1920, 1080, 60, listOf(OutputSize(1920, 1080, 60.0))).status.contains("Potencial teórico"))
        assertTrue(CameraMetadata.evaluate("1080p60", 1920, 1080, 60, listOf(OutputSize(1920, 1080, 30.0))).status.contains("Información insuficiente"))
    }
}
