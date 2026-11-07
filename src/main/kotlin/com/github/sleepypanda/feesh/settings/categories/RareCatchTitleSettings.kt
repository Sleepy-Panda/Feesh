package com.github.sleepypanda.feesh.settings.categories

import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.teamresourceful.resourcefulconfigkt.api.ObjectKt

class RareCatchTitleSettings(
    title: String,
    subtitle: String,
) : ObjectKt() {
    var titleTemplate by string(title) {
        this.name = Translated("Title")
        this.description = Translated("${GRAY}Leave empty to use default. Placeholders: ${placeholders()}")
    }

    var subtitleTemplate by string(subtitle) {
        this.name = Translated("Subtitle")
        this.description = Translated("${GRAY}Leave empty to use default. Placeholders: ${placeholders()}")
    }

    companion object {
        fun placeholders(): String =
            "${WHITE}{playerName}${GRAY} - player name; ${WHITE}{rarityColor}${GRAY} - sea creature name in its rarity color. Put §l directly before {rarityColor} to make the name bold."
    }
}
