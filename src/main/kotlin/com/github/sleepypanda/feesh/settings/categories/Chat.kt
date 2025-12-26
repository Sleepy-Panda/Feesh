package com.github.sleepypanda.feesh.settings.categories

import com.github.sleepypanda.feesh.constants.RareSeaCreatureTypes
import com.github.sleepypanda.feesh.constants.RareSeaCreatureTypesAllChat
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import com.teamresourceful.resourcefulconfig.api.annotations.Category
import com.teamresourceful.resourcefulconfig.api.annotations.Comment
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry
import com.teamresourceful.resourcefulconfig.api.types.options.EntryType

object Chat : CategoryKt("Chat") {
    init {
        separator {
            this.title = "Sea creatures"
        }
    }

    var compactSeaCreaturesMessages by boolean(false) {
        this.name = Translated("Compact sea creature catch messages")
        this.description = Translated("Shortens double hook message and catch message that says what sea creature you caught.")
    }

    var shareRareSeaCreatures by boolean(true) {
        this.name = Translated("Share rare sea creatures to the PARTY chat")
        this.description = Translated("Sends a PARTY chat message when a rare sea creature is caught by you. Please enable ${YELLOW}Skyblock Settings -> Personal -> Fishing Settings -> Sea Creature Chat")
    }

    var shareRareSeaCreaturesTypes by select(RareSeaCreatureTypes.CARROT_KING, *RareSeaCreatureTypes.values()) {
        this.name = Translated("Select sea creatures to share to the PARTY chat")
    }

    var shareRareSeaCreaturesAllChat by boolean(false) {
        this.name = Translated("Share rare sea creatures location to the ALL chat")
        this.description = Translated("Sends an ALL chat message with coordinates when a rare sea creature is caught by you. Please enable ${YELLOW}Skyblock Settings -> Personal -> Fishing Settings -> Sea Creature Chat")
    }

    var shareRareSeaCreaturesTypesAllChat by select(RareSeaCreatureTypesAllChat.THUNDER, *RareSeaCreatureTypesAllChat.values()) {
        this.name = Translated("Select sea creatures to share location to the ALL chat")
    }
}
