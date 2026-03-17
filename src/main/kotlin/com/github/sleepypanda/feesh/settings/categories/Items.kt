package com.github.sleepypanda.feesh.settings.categories

import com.github.sleepypanda.feesh.features.items.background.TrashBooksHighlighter
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import com.teamresourceful.resourcefulconfigkt.api.ObservableEntry

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

    var trashBooksHighlighter by boolean(false) {
        this.name = Translated("Trash enchanted books")
        this.description = Translated(
            "Highlights slots containing trash enchanted books flooding your inventory while fishing. You can use it to quickly find books to throw away or insta sell."
        )
    }

    var trashBooksHighlighterSubtitles by ObservableEntry(
        strings("Corruption I,Corruption 1") {
            this.name = Translated("Trash enchanted books to search for")
            this.description = Translated("Comma-separated book names to search for. Should be exact book name. Example: Corruption I,Frail VI,No Pain No Gain I.")
        }
    ) { prev, new ->
        if (prev != new) {
            TrashBooksHighlighter.setSearchSubtitles()
        }
    }
}
