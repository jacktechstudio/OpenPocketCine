package com.opencapture.openpocketcine.pairing

import com.opencapture.opencine.appkit.NearbyCardCopy
import com.opencapture.opencine.appkit.PairingWizard
import com.opencapture.opencine.core.NearbyCamera
import com.opencapture.opencine.core.RouteKind
import com.opencapture.opencine.dji.fromJson
import com.opencapture.opencine.vocabulary.CameraModel
import com.opencapture.opencine.vocabulary.ConnectionPhase
import com.opencapture.opencine.vocabulary.FoundCamera
import com.opencapture.openpocketcine.AppModel
import com.opencapture.openpocketcine.pairingWizard
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** The pair page and nearby cards in the words this app's main showed (PairingExperience, NearbyCameraRow). */
class PairingWizardCopyTest {
    private fun wizard(granted: Boolean = true, needsSettings: Boolean = false, radioOn: Boolean = true) =
        pairingWizard("label", null, vpnActive = false, granted = granted, needsSettings = needsSettings, radioOn = radioOn)

    @Test
    fun theWizardKeepsThePocketPages() {
        val wizard = wizard()
        assertEquals(
            listOf(
                "打开相机，将手机靠近相机。从列表中选择要连接的 Pocket 或 Nano。",
                "如果相机提示配对，请在相机屏幕上确认。首次配对最长可能需要 90 秒。",
                "手机将通过蓝牙读取相机的网络信息，然后连接相机的 Wi-Fi。",
                "图传连接后，即可调整曝光、加载 LUT 并查看示波器。",
            ),
            wizard.bodies,
        )
        assertEquals(listOf("留意相机上的配对提示", "在相机屏幕上确认配对"), wizard.approveLines)
        assertEquals(listOf("保持相机开机，等待相机启动 Wi-Fi", "5.8 GHz 网络启动可能需要约 1 分钟，手机会自动重试"), wizard.joinLines)
        assertEquals(null, wizard.blocker)
    }

    @Test
    fun discoveryBlockersTakeThePrimaryAction() {
        assertEquals("打开设置", wizard(granted = false, needsSettings = true).blocker)
        assertTrue(wizard(granted = false).blocker!!.startsWith("授予"))
        assertEquals("开启蓝牙", wizard(radioOn = false).blocker)
    }

    @Test
    fun theLiveLabelIsThePhase() {
        assertEquals("正在搜索相机…", PairingWizard.phaseLabel(ConnectionPhase.SCANNING))
        assertEquals("请在相机屏幕上确认", PairingWizard.phaseLabel(ConnectionPhase.AWAITING_APPROVAL))
        assertEquals("正在读取 Wi-Fi 连接信息…", PairingWizard.phaseLabel(ConnectionPhase.READING_WIFI_CREDS))
        assertEquals("正在建立数据连接…", PairingWizard.phaseLabel(ConnectionPhase.OPENING_DATALINK))
    }

    @Test
    fun nearbyCardsReadAsMain() {
        val camera = NearbyCamera("n1", name = "OsmoPocket3-4D5E", model = "Osmo Pocket 3", routes = setOf(RouteKind.CAMERA_ACCESS_POINT))
        val found = FoundCamera("n1", "BB", "OsmoPocket3-4D5E", CameraModel.fromJson("""{"name":"Osmo Pocket 3","family":"pocket","verified":true}"""), 0x20)
        assertEquals(
            NearbyCardCopy("OsmoPocket3-4D5E", "Pocket · Osmo Pocket 3 · 附近", "请在相机上确认配对"),
            AppModel.nearbyCard(camera, found),
        )
        val nano = FoundCamera("n2", "CC", "DJI Camera", CameraModel(name = "Osmo Nano", family = "nano"), null)
        assertEquals(
            NearbyCardCopy("Osmo Nano", "Nano · 附近 · 未验证", "请在相机上确认配对"),
            AppModel.nearbyCard(NearbyCamera("n2", name = "Osmo Nano", model = "Osmo Nano", routes = setOf(RouteKind.CAMERA_ACCESS_POINT)), nano),
        )
    }
}
