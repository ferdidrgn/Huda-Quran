package org.ferdidrgn.hudaquran.wear

import android.content.ComponentName
import androidx.wear.protolayout.TimelineBuilders.Timeline
import androidx.wear.protolayout.material3.MaterialScope
import androidx.wear.protolayout.material3.launchAction
import androidx.wear.protolayout.material3.primaryLayout
import androidx.wear.protolayout.material3.text
import androidx.wear.protolayout.material3.textEdgeButton
import androidx.wear.protolayout.modifiers.clickable
import androidx.wear.protolayout.types.layoutString
import androidx.wear.tiles.Material3TileService
import androidx.wear.tiles.RequestBuilders.TileRequest
import androidx.wear.tiles.TileBuilders.Tile
import androidx.wear.tiles.tile

class WearQuranTileService : Material3TileService() {
    override suspend fun MaterialScope.tileResponse(requestParams: TileRequest): Tile =
        tile(
            timeline =
                Timeline.fromLayoutElement(
                    primaryLayout(
                        titleSlot = {
                            text("Huda Kur'an".layoutString)
                        },
                        mainSlot = {
                            text("Telefondaki Kur'an oynatıcısına hızlıca dön.".layoutString)
                        },
                        bottomSlot = {
                            textEdgeButton(
                                labelContent = { text("Oynatıcıyı aç".layoutString) },
                                onClick =
                                    clickable(
                                        action =
                                            launchAction(
                                                ComponentName(
                                                    packageName,
                                                    "org.ferdidrgn.hudaquran.wear.WearPlaybackActivity",
                                                )
                                            )
                                    ),
                            )
                        },
                    )
                )
        )
}
