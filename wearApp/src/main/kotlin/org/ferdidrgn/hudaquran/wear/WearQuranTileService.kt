package org.ferdidrgn.hudaquran.wear

import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.TimelineBuilders.Timeline
import androidx.wear.protolayout.material3.MaterialScope
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
                            text(getString(R.string.wear_quran_tile_label).layoutString)
                        },
                        mainSlot = {
                            text(getString(R.string.wear_quran_tile_description).layoutString)
                        },
                        bottomSlot = {
                            textEdgeButton(
                                labelContent = {
                                    text(getString(R.string.wear_quran_tile_action).layoutString)
                                },
                                onClick =
                                    clickable(
                                        action =
                                            ActionBuilders.LaunchAction.Builder()
                                                .setAndroidActivity(
                                                    ActionBuilders.AndroidActivity.Builder()
                                                        .setPackageName(packageName)
                                                        .setClassName(
                                                            "org.ferdidrgn.hudaquran.wear.WearPlaybackActivity",
                                                        )
                                                        .build(),
                                                )
                                                .build()
                                    ),
                            )
                        },
                    )
                )
        )
}
