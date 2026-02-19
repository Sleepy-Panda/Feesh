package com.github.sleepypanda.feesh.settings.categories

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.features.rendering.LavaRendering
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.teamresourceful.resourcefulconfig.api.annotations.Category
import com.teamresourceful.resourcefulconfig.api.annotations.Comment
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry
import com.teamresourceful.resourcefulconfig.api.types.options.EntryType
import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import com.teamresourceful.resourcefulconfigkt.api.ObservableEntry
import java.awt.Color

// TODO: Reload lava after Crimson Isle detected as some lava is loaded before the world name is known
object WorldRendering : CategoryKt("World Rendering") {
    init {
        separator {
            this.title = "${AQUA}${BOLD}Fishing hooks"
        }
    }

    var hideOtherPlayersFishingHooks by boolean(false) {
        this.name = Translated("Hide other players' fishing hooks")
        this.description = Translated("Hides fishing hooks that belong to other players.")
    }

    init {
        separator {
            this.title = "${AQUA}${BOLD}Lava rendering"
        }
    }

    var replaceLavaWithWater by ObservableEntry(
        boolean(false) {
            this.name = Translated("Replace lava with defaultwater texture")
            this.description = Translated("Replaces lava texture with water texture when in the Crimson Isle.")
        }
    ) { prev, new ->
        if (prev != new) {
            LavaRendering.reloadRenderedLava()
        }
    }

    var replaceLavaWithTinted by ObservableEntry(
        boolean(false) {
            this.name = Translated("Replace lava with tinted water texture")
            this.description = Translated("Shows water blocks with your chosen color instead of lava when in the Crimson Isle.")
        }
    ) { prev, new ->
        if (prev != new) {
            LavaRendering.reloadRenderedLava()
        }
    }

    var lavaTintColor by ObservableEntry(
        color(Color(0xD9A7B8).rgb) {
            this.name = Translated("Tinted water color")
            this.description = Translated("Color of the water that replaces lava. Only applies when \"Replace lava with tinted water texture\" is enabled.")
            this.allowAlpha = false
        }
    ) { prev, new ->
        if (prev != new) {
            LavaRendering.reloadRenderedLava()
        }
    }
}