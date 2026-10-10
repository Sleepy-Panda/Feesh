package com.github.sleepypanda.feesh.settings.categories

import com.github.sleepypanda.feesh.constants.ModVersionConstants
import com.github.sleepypanda.feesh.features.rendering.ReplaceAndTintLava
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.features.rendering.HidePlayersNearBobber
import com.github.sleepypanda.feesh.features.rendering.RareMobHighlight
import com.github.sleepypanda.feesh.settings.models.HighlightableSeaCreatureTypes
import com.github.sleepypanda.feesh.settings.models.LavaReplacementWorlds
import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue
import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import com.teamresourceful.resourcefulconfigkt.api.ObservableEntry
import java.awt.Color

object WorldRendering : CategoryKt("World Rendering") {
    override val description: TranslatableValue
        get() = Literal(
            "Features that modify the world and entities."
        )

    init {
        separator {
            this.title = "${AQUA}${BOLD}Lava rendering"
        }
    }

    var replaceLavaWithWater by ObservableEntry(
        boolean(false) {
            this.name = Translated("Replace lava with water")
            this.description = Translated("Replaces lava with transparent water and removes fog when under lava in the selected worlds.")
            this.searchTerms = listOf(ModVersionConstants.VERSION_1_16_0)
        }
    ) { prev, new ->
        if (prev != new) {
            ReplaceAndTintLava.reloadRenderedLava()
        }
    }
   
    var lavaReplacementWorlds by ObservableEntry(select(LavaReplacementWorlds.CRIMSON_ISLE) {
        this.name = Translated("Worlds to replace lava")
        this.description = Translated("Worlds where lava is replaced with water.")
        this.searchTerms = LavaReplacementWorlds.entries.map { it.worldName } + ModVersionConstants.VERSION_1_16_0
    }) { prev, new ->
        if (!prev.contentEquals(new)) {
            ReplaceAndTintLava.reloadRenderedLava()
        }
    }

    var tintReplacedLava by ObservableEntry(
        boolean(false) {
            this.name = Translated("Tint water")
            this.description = Translated("Adds a custom color to the water that replaces lava. Used together with \"Replace lava with water\".")
            this.searchTerms = listOf(ModVersionConstants.VERSION_1_16_0)
        }
    ) { prev, new ->
        if (prev != new) {
            ReplaceAndTintLava.reloadRenderedLava()
        }
    }
  
    var lavaTintColor by ObservableEntry(
        color(Color(0x280008).rgb) {
            this.name = Translated("Tint water color")
            this.description = Translated("Color applied to the water that replaces lava when \"Tint water\" is on.")
            this.allowAlpha = false
            this.searchTerms = listOf(ModVersionConstants.VERSION_1_16_0)
        }
    ) { prev, new ->
        if (prev != new) {
            ReplaceAndTintLava.reloadRenderedLava()
        }
    }

    init {
        separator {
            this.title = "${AQUA}${BOLD}Highlight"
        }
    }

    var highlightSeaCreatures by ObservableEntry(boolean(false) {
        this.name = Translated("Highlight sea creatures")
        this.description = Translated("Applies glowing outline to selected sea creatures. Outline is colored depending on sea creature rarity. ${RED}Not visible through walls, but use at your own risk anyway!")
    }
    ) { prev, new ->
        if (prev != new) {
            RareMobHighlight.clearHighlightedEntities()
        }
    }

    var highlightSeaCreaturesList by ObservableEntry(select(
            *HighlightableSeaCreatureTypes.entries.filter { it.isEnabledByDefault }.toTypedArray(),
        ) {
            this.name = Translated("Select sea creatures")
            this.description = Translated("Which sea creatures should have glowing outline applied to.")
            this.searchTerms = HighlightableSeaCreatureTypes.entries.map { it.displayName }.toList()
        }
    ) { prev, new ->
        if (!prev.contentEquals(new)) {
            RareMobHighlight.updateEnabledMobTypes()
        }
    }

    init {
        separator {
            this.title = "${AQUA}${BOLD}Players"
        }
    }

    var hideOtherPlayersFishingHooks by boolean(false) {
        this.name = Translated("Hide other players' fishing hooks")
        this.description = Translated("Hides fishing hooks that belong to other players.")
    }

    var hidePlayersNearBobber by boolean(false) {
        this.name = Translated("Hide players near your bobber")
        this.description = Translated("Hides other players when your fishing rod is casted, if they are within the configured distance from your fishing hook.")
    }

    var hidePlayersNearBobberDistance by int(5) {
        this.name = Translated("Distance from bobber")
        this.description = Translated("Maximum distance (blocks) from your fishing hook within which other players are hidden.")
        this.range = 1..10
        this.slider = true
    }

    var hidePlayersNearBobberUnhideDelay by ObservableEntry(int(0) {
        this.name = Translated("Unhide delay")
        this.description = Translated("Delay in seconds to keep players hidden after your bobber disappears.")
        this.range = 0..5
        this.slider = true
    }) { prev, new ->
        if (prev != new) {
            HidePlayersNearBobber.onUnhideDelayChanged()
        }
    }

    init {
        separator {
            this.title = "${AQUA}${BOLD}Nametags"
        }
    }

    var hideTadgangNametags by boolean(false) {
        this.name = Translated("Hide Tadgang nametags")
        this.description = Translated("Hides Tadgang tadpoles nametags in Moonglade Marsh. Tadgang frogs nametags are still visible!.")
        this.searchTerms = listOf(ModVersionConstants.VERSION_1_12_0)
    }

    init {
        separator {
            this.title = "${AQUA}${BOLD}World sounds"
        }
    }

    var muteJadeDragon by boolean(false) {
        this.name = Translated("Mute Jade Dragon")
        this.description = Translated("Mutes Jade dragon sounds 'entity.ender_dragon.*' while you are in dragon's cave. ${YELLOW}If you use Sound Controller mod, this setting might be overridden by it!")
    }

    var muteReindrakeGifts by boolean(false) {
        this.name = Translated("Mute Reindrake gifts")
        this.description = Translated("Mutes loud 'item.totem.use' sounds while picking up gifts from a Reindrake. ${YELLOW}If you use Sound Controller mod, this setting might be overridden by it!")
    }
}
