package com.github.sleepypanda.feesh.settings.categories

import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.teamresourceful.resourcefulconfigkt.api.CategoryKt

object Items : CategoryKt("Items") {

    init {
        separator {
            this.title = "${AQUA}${BOLD}Background"
        }
    }

    var katWrongPetsHighlighter by boolean(false) {
        this.name = Translated("Wrong pets offered to Kat")
        this.description = Translated(
            "${GRAY}Highlights Kat's GUI slot when you offer Kat some pets (Epic Megalodon, Epic Baby Yeti) potentially by mistake. ${DARK_GRAY}For those who regularly gets scammed by Kat, giving her Megalodons instead of George (that's me)."
        )
    }
}
