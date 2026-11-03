package com.opencapture.openpocketcine.lut

import com.opencapture.opencine.cameracontrols.LutPickerCopy
import com.opencapture.opencine.vocabulary.LutCatalog
import kotlin.test.Test
import kotlin.test.assertEquals

/** Pocket's LUT picker words and Auto, as on main. */
class AppLutPickerCopyTest {
    @Test fun officialCubesSitOnTheDjiTab() {
        assertEquals(
            LutPickerCopy(
                cameraTab = "DJI",
                autoId = LutCatalog.OFFICIAL_AUTO,
                customHint = "导入的 LUT 会显示在这里。自动模式仅选用内置或 DJI LUT。",
                noLookCaption = "没有适用于当前色彩模式和机型的 LUT",
                officialCubeCaption = "DJI 官方 Rec.709 LUT",
                cameraLooksCaption = "适用于当前色彩模式和机型的 DJI 官方 LUT",
            ),
            AppLutPickerCopy,
        )
        assertEquals("djiAuto", AppLutPickerCopy.autoId)
    }
}
