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
            this.title = "${AQUA}${BOLD}Highlight"
        }
    }

    var highlightSeaCreatures by boolean(false) {
        this.name = Translated("Highlights rare sea creatures")
        this.description = Translated("Applies glowing border to the rare sea creatures. Not visible through walls.")
    }

    init {
        separator {
            this.title = "${AQUA}${BOLD}Players"
        }
    }

    var hidePlayersNearBobber by boolean(false) {
        this.name = Translated("Hide players near bobber")
        this.description = Translated("Hides other players when your fishing rod is cast, if they are within the configured distance from your fishing hook.")
    }

    var hidePlayersNearBobberDistance by int(5) {
        this.name = Translated("Distance from bobber")
        this.description = Translated("Maximum distance (blocks) from your fishing hook within which other players are hidden. Only applies when fishing rod is casted.")
        this.range = 1..10
        this.slider = true
    }
}