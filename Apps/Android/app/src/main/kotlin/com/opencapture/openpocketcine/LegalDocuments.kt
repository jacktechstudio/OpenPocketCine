package com.opencapture.openpocketcine

import com.opencapture.opencine.cameracontrols.MonitorLegalDocument
import com.opencapture.opencine.cameracontrols.MonitorLegalDocuments
import com.opencapture.opencine.cameracontrols.MonitorPlatformLegalNotices
import com.opencapture.opencine.cameracontrols.MonitorSettingsCopy

private val PRIVACY_BODY =
    """
OpenPocketCine 通过蓝牙和相机自身的 Wi-Fi 连接 Osmo Pocket。无需注册账号。自动错误报告为可选功能，默认关闭。

OpenCapture 是数据控制者。如需咨询隐私问题或申请删除数据，请发邮件至 support@openpocketcine.app。

${MonitorSettingsCopy.reliabilityReportsPrivacy}

手动报告问题
点按“发送报告”后，问题描述和选填的回复邮箱将通过 Sentry 发送至 OpenCapture。只有您主动选择时才会附上技术详情，发送前可预览。您最多可以添加三张照片或屏幕截图，并在发送前查看或移除。所选图片会缩小尺寸并重新编码，原文件名和位置信息将被移除。请仅分享您有权发送的图片，应用不会自动附加图片。单次提交不会开启自动报告。待发送的报告仅存放在本机，离开相机 Wi-Fi 后才会发送，发送前可删除。报告将在 7 天后过期，于应用下次运行时移除。请勿在描述中填写密码等敏感信息。

保存在本机的数据
• 已保存的相机名称和上次连接的 Wi-Fi 名称（SSID）。相机的 Wi-Fi 密码仅通过本机 Android Keystore 保存。
• 保持屏幕常亮、上次选用的 LUT 等使用偏好。
• 您主动导入的 .cube 文件。

仅在本地传输的数据
• HEVC 实时图传、DUML 遥测数据和配对通信仅通过本地连接传输。我们不运营云端服务。

第三方服务
• Google 可能依据其政策处理权限请求和系统诊断数据。Google Play 测试版可能依据 Google 的条款向开发者发送崩溃报告。
• 保存或分享片段均由您主动发起。文件随后由 Android 或您选择的应用处理。
• AF-C 连续自动对焦的人脸框由本机根据实时取景画面计算，人脸几何数据不会上传。
• 上游源代码位于 github.com/erik-sutton95/OpenPocketCine。

Android 可能会请求位置信息权限，以连接相机 Wi-Fi 或扫描低功耗蓝牙设备。OpenPocketCine 不会将此权限用于地图、广告或记录位置历史。

完整隐私政策见 https://openpocketcine.app/privacy/
    """.trimIndent()

/**
 * Pocket's legal set: privacy ships with the app and the terms are the web page. Licenses and
 * NOTICE are the repository's LICENSE, THIRD-PARTY-NOTICES.md and NOTICE, staged as resources at
 * build time, with the platform notices; a build missing one shows the unavailable page.
 */
internal val PocketLegalDocuments = MonitorLegalDocuments(
    wordmark = "OPENPOCKETCINE",
    privacy = MonitorLegalDocument.Text(PRIVACY_BODY),
    terms = MonitorLegalDocument.Link(OpenPocketCineLinks.TERMS),
    licenses = MonitorLegalDocument.bundledText(listOf(
        MonitorLegalDocument.resource("openpocketcine/legal/LICENSE"),
        MonitorLegalDocument.resource("openpocketcine/legal/THIRD-PARTY-NOTICES.md"),
        MonitorPlatformLegalNotices.text,
    )),
    notice = MonitorLegalDocument.bundledText(listOf(MonitorLegalDocument.resource("openpocketcine/legal/NOTICE"))),
)
