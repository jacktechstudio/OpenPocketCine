package com.opencapture.openpocketcine.pairing

import com.opencapture.opencine.dji.DjiFailures
import com.opencapture.opencine.vocabulary.ConnectionPhase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** The vendor-app tip reaches the pair page, the scan hint and the failure cards (SHELL-02). */
class StartupConnectionCopyTest {
    private val tip = "请关闭 DJI FrameTap，并在相机附近的所有手机上彻底退出 DJI Mimo。"

    @Test
    fun aCameraThatNeverAnswersNamesFrameTapAndMimo() {
        assertEquals(tip, DjiFailures.DJI_APPS_TIP)
        assertTrue(StartupConnectionCopy.PAIRING_PRECHECK.contains("DJI FrameTap") && StartupConnectionCopy.PAIRING_PRECHECK.contains("DJI Mimo"))
        assertTrue(StartupConnectionCopy.STILL_LOOKING.startsWith(tip))
        val card = DjiFailures.classify(null, ConnectionPhase.OPENING_DATALINK, "camera never answered the datalink handshake")
        // PAB-C-23: the baseline friendly sentence, never the raw handshake text (OpenCine #122).
        assertEquals("相机未响应图传连接请求。 $tip", card.message)
        assertTrue(DjiFailures.classify(null, ConnectionPhase.AWAITING_APPROVAL, "pairing timed out").message.endsWith(tip))
    }

    @Test
    fun otherFailuresKeepTheirCopy() {
        assertFalse(DjiFailures.classify(null, ConnectionPhase.JOINING_WIFI, "the camera disconnected").message.contains("FrameTap"))
    }
}
