package com.github.sleepypanda.feesh.settings.categories

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.teamresourceful.resourcefulconfig.api.annotations.Category
import com.teamresourceful.resourcefulconfig.api.annotations.Comment
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry
import com.teamresourceful.resourcefulconfig.api.types.options.EntryType
import com.teamresourceful.resourcefulconfigkt.api.CategoryKt

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

    var replaceLavaWithWater by boolean(false) {
        this.name = Translated("Replace lava with water")
        this.description = Translated("Replaces lava with water in the Crimson Isle.")
    }
}