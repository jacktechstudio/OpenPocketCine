package com.opencapture.openpocketcine.multiview

import com.opencapture.opencine.appkit.MultiviewCameraAction
import com.opencapture.opencine.appkit.MultiviewReadout
import com.opencapture.opencine.appkit.MultiviewRecordContext
import com.opencapture.opencine.appkit.MultiviewTile
import com.opencapture.opencine.appkit.MultiviewTileStatus
import com.opencapture.opencine.appkit.withPower
import com.opencapture.opencine.dji.hasMultiviewPreview
import com.opencapture.opencine.core.CommandRejection
import com.opencapture.opencine.multicam.TargetOutcome
import com.opencapture.opencine.core.PowerStatus
import com.opencapture.opencine.cameracontrols.cameraBatteryReadoutLabel
import com.opencapture.opencine.vocabulary.CameraCommands
import com.opencapture.opencine.vocabulary.CameraStatus
import com.opencapture.opencine.vocabulary.StorageReadout
import com.opencapture.opencine.vocabulary.storageReadout
import com.opencapture.opencine.media.storageReadout
import com.opencapture.opencine.core.StorageStatus
import com.opencapture.opencine.vocabulary.VideoResolution

/** Display values come from the existing throttled tile status; no telemetry polling. */
internal data class MultiviewTileReadouts(
    val letter: String,
    val name: String,
    val model: String,
    val timecode: String,
    val battery: String,
    val storage: String,
    val recording: String,
    val isRecording: Boolean,
    val format: String,
    val recovery: String?,
)

internal fun multiviewTileReadouts(
    index: Int, name: String, model: String, settings: CameraStatus, timecode: String?,
    recordingObservation: Boolean?, recordingAvailable: Boolean, recordingBusy: Boolean,
    recovering: Boolean, hasPicture: Boolean, failure: String?, recordingOutcome: TargetOutcome? = null,
    power: PowerStatus,
    colorFamily: String = "pocket",
    storage: StorageStatus? = null,
): MultiviewTileReadouts {
    val elapsed = settings.recordElapsedSec.coerceAtLeast(0)
    val recording = when {
        recordingBusy -> "等待"
        recordingOutcome == TargetOutcome.TimedOut || recordingOutcome == TargetOutcome.OutcomeUnknown ||
            recordingOutcome == TargetOutcome.Pending || recordingOutcome == TargetOutcome.Accepted ||
            (recordingOutcome is TargetOutcome.Rejected && recordingOutcome.rejection == CommandRejection.CameraRefused) -> "REC ?"
        recovering -> "保持"
        !recordingAvailable || recordingObservation == null -> "REC —"
        recordingObservation -> "REC ${elapsed / 60}:${(elapsed % 60).toString().padStart(2, '0')}"
        else -> "STBY"
    }
    val resolution = VideoResolution.fromRaw(settings.resolutionCode)?.label ?: "—"
    val fps = if (settings.fps > 0) "${settings.fps}p" else "—"
    val color = if (settings.colorMode < 0) "—" else CameraCommands.colorLabel(settings.colorMode, colorFamily)
    return MultiviewTileReadouts(
        letter = ('A' + index.coerceIn(0, 3)).toString(), name = name, model = model,
        timecode = timecode ?: "\u2014",
        battery = cameraBatteryReadoutLabel(power.batteryReading) ?: "\u2014",
        storage = (if (storage != null) storage.storageReadout else settings.storageReadout)?.free ?: StorageReadout.UNAVAILABLE,
        recording = recording, isRecording = recordingObservation == true,
        format = "$resolution · $fps · $color",
        recovery = when {
            failure != null -> "请检查连接"
            recovering -> "正在恢复图传…"
            !hasPicture -> "正在等待图传…"
            else -> null
        },
    )
}

internal fun multiviewExposureReadouts(settings: CameraStatus): List<Pair<String, String>> = listOf(
    "ISO" to if (settings.iso > 0) settings.iso.toString() else "—",
    "快门" to if (settings.shutterDenom > 0) "1/${settings.shutterDenom}" else "—",
    "WB" to when {
        settings.wbMode == CameraCommands.WB_AUTO -> "自动"
        settings.wbMode < 0 -> "—"
        settings.wbKelvin > 0 -> "${settings.wbKelvin}K"
        else -> "—"
    },
    "对焦" to settings.focusLabel,
)

/** The program readouts and Grid's in-tile row. */
internal fun multiviewStageReadouts(settings: CameraStatus): List<MultiviewReadout> =
    multiviewExposureReadouts(settings).map { (title, value) -> MultiviewReadout(title, value) }

/** Pocket's camera option ids beside the shared Live View id. */
internal object MultiviewTileAction {
    const val RECORD = "record"
    const val LUT = "lut"
    const val RECONNECT = "reconnect"
    const val EXPERIMENTAL = "experimental"
    const val REMOVE = "remove"
}

/** The shared stage's values for one slot, null while it is empty. */
internal fun MultiviewSession.stageTile(index: Int): MultiviewTile? {
    val tile = tiles[index]
    val camera = tile.camera ?: return null
    val power = tile.control.current.power
    val readouts = multiviewTileReadouts(
        index, camera.name, camera.model.name, tile.settings, tile.timecodeReadout,
        tile.recordingObservation?.first, tile.recordingAvailable,
        tile.recordingBusy, tile.recovering, tile.hasPicture, tile.failureMessage, tile.recordingOutcome,
        power = power, colorFamily = camera.model.family, storage = tile.control.snapshots.value.storage,
    )
    val settled = !busy && !tile.connecting && !tile.recovering
    val experimental = tile.failureMessage != null && !tile.experimentalNetwork && tile.identity == null
    val record = recordPlan(tile)
    return MultiviewTile(
        name = camera.name, model = camera.model.name, format = readouts.format,
        storage = readouts.storage, timecode = tile.timecodeReadout,
        tally = readouts.recording, recording = readouts.isRecording, lut = tile.lutEnabled,
        readouts = multiviewStageReadouts(tile.settings),
        status = readouts.recovery?.let { text ->
            MultiviewTileStatus(
                text, detail = tile.failureMessage ?: tile.status,
                inProgress = tile.failureMessage == null && !tile.networkVerified,
                actions = if (tile.failureMessage == null) emptyList() else listOfNotNull(
                    MultiviewCameraAction(MultiviewTileAction.EXPERIMENTAL, "试用共享 Wi-Fi（实验功能）", enabled = settled)
                        .takeIf { experimental },
                    MultiviewCameraAction(MultiviewTileAction.RECONNECT, "重新连接", enabled = settled),
                    MultiviewCameraAction(
                        MultiviewTileAction.REMOVE, "移除", MultiviewCameraAction.Kind.DESTRUCTIVE, enabled = settled,
                    ),
                ),
            )
        },
        failure = tile.failureMessage,
        footnote = "移除此机位后，即可对其余相机执行全部录制。"
            .takeIf { tile.networkVerified && !camera.hasMultiviewPreview },
        actions = listOfNotNull(
            MultiviewCameraAction(
                MultiviewCameraAction.LIVE_VIEW, "打开实时取景", enabled = tile.controlHost != null && !tile.connecting,
            ),
            MultiviewCameraAction(
                MultiviewTileAction.RECORD,
                if (tile.recordingObservation?.first == true) "停止录制" else "开始录制",
                MultiviewCameraAction.Kind.RECORD,
                enabled = record.isExecutable && !groupRecordingBusy && !closing,
            ),
            MultiviewCameraAction(MultiviewTileAction.LUT, if (tile.lutEnabled) "关闭自动 LUT" else "开启自动 LUT"),
            MultiviewCameraAction(MultiviewTileAction.RECONNECT, "重新连接", enabled = settled),
            MultiviewCameraAction(MultiviewTileAction.EXPERIMENTAL, "试用共享 Wi-Fi（实验功能）", enabled = settled)
                .takeIf { experimental },
            MultiviewCameraAction(
                MultiviewTileAction.REMOVE, "移除相机", MultiviewCameraAction.Kind.DESTRUCTIVE,
                enabled = !busy && !tile.connecting && !groupRecordingBusy && !tile.recordingBusy && !closing,
            ),
        ),
    ).withPower(power)
}

/** The record plan for every camera (null) or one slot, with what blocks it now. */
internal fun MultiviewSession.recordContext(slot: Int?): MultiviewRecordContext = MultiviewRecordContext(
    recordPlan(slot?.let { tiles[it] }), busy = busy || groupRecordingBusy, closing = closing,
)
