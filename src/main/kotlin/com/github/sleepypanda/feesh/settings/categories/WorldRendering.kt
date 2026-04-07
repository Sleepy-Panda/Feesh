package com.github.sleepypanda.feesh.settings.categories

import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.features.rendering.RareMobHighlight
import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue
import com.teamresourceful.resourcefulconfigkt.api.ObservableEntry
import com.teamresourceful.resourcefulconfigkt.api.CategoryKt

object WorldRendering : CategoryKt("World Rendering") {
    override val description: TranslatableValue
        get() = Literal(
            "Features that modify the world and entities."
        )

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

    var highlightSeaCreatures by ObservableEntry(boolean(false) {
        this.name = Translated("Highlight rare sea creatures")
        this.description = Translated("Applies glowing outline to the rare sea creatures, Jawbus Followers and Wiki Tiki Laser Totems. Outline is colored depending on sea creature rarity. Not visible through walls!")
    }
    ) { prev, new ->
        if (prev != new) {
            RareMobHighlight.clearHighlightedEntities()
        }
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

    init {
        separator {
            this.title = "${AQUA}${BOLD}World sounds"
        }
    }

    var muteJadeDragon by boolean(false) {
        this.name = Translated("Mute Jade Dragon")
        this.description = Translated("Mutes Jade dragon sounds while you are in dragon's cave.")
    }
}
