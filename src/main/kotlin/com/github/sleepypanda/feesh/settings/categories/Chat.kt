package com.github.sleepypanda.feesh.settings.categories

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

    var shareRareSeaCreatures by boolean(true) {
        this.name = Translated("Share rare sea creatures to PARTY chat")
        this.description = Translated("Send party chat message when catching rare creatures")
    }

    var compactSeaCreaturesMessages by boolean(false) {
        this.name = Translated("Compact chat messages")
        this.description = Translated("")
    }
}
