package com.github.sleepypanda.feesh.settings.categories

import com.github.sleepypanda.feesh.constants.RareSeaCreatureTypes
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.teamresourceful.resourcefulconfig.api.annotations.Category
import com.teamresourceful.resourcefulconfig.api.annotations.Comment
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry
import com.teamresourceful.resourcefulconfig.api.types.options.EntryType
import com.teamresourceful.resourcefulconfigkt.api.CategoryKt

object Chat : CategoryKt("Chat") {
    init {
        separator {
            this.title = "Chat"
        }
    }

    init {
        separator {
            this.title = "Sea creatures"
        }
    }

    var shareRareSeaCreatures by boolean(true) {
        this.name = Translated("Share rare sea creatures to the PARTY chat")
        this.description = Translated("Sends a PARTY chat message when a rare sea creature is caught by you. Please enable ${YELLOW}Skyblock Settings -> Personal -> Fishing Settings -> Sea Creature Chat")
    }

    var shareSeaCreaturesTypes by select(RareSeaCreatureTypes.CARROT_KING, *RareSeaCreatureTypes.values()) {
        this.name = Translated("Select sea creatures to share to the PARTY chat")
    }

    var compactSeaCreaturesMessages by boolean(false) {
        this.name = Translated("Compact sea creature catch messages")
        this.description = Translated("Shortens double hook message and catch message that says what sea creature you caught.")
    }
}
