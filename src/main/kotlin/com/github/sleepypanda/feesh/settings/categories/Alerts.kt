package com.github.sleepypanda.feesh.settings.categories

import com.teamresourceful.resourcefulconfig.api.annotations.Category
import com.teamresourceful.resourcefulconfig.api.annotations.Comment
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry
import com.teamresourceful.resourcefulconfig.api.types.options.EntryType
import com.teamresourceful.resourcefulconfigkt.api.CategoryKt

object Alerts : CategoryKt("Alerts") {
    enum class RareSeaCreatureTypes {
        YETI, THE_LOCH_EMPEROR
    }

    init {
        separator {
            this.title = "Alerts"
        }
    }

    var AlertOnSeaCreatures by select(RareSeaCreatureTypes.YETI, RareSeaCreatureTypes.THE_LOCH_EMPEROR) {
        this.name = Translated("Select Sea Creatures")
        this.description = Translated("Alert on catching the following Sea Creatures")
    }

    var alertOnRareSeaCreatures by boolean(true) {
        this.name = Translated("Alert on catching rare sea creatures")
        this.description = Translated("")
    }
}
