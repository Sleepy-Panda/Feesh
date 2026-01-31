package com.github.sleepypanda.feesh.settings.categories

import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.teamresourceful.resourcefulconfig.api.annotations.Category
import com.teamresourceful.resourcefulconfig.api.annotations.Comment
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry
import com.teamresourceful.resourcefulconfig.api.types.options.EntryType
import com.teamresourceful.resourcefulconfigkt.api.CategoryKt

enum class SoundMode(val displayName: String) {
    MEME("Meme"),
    NORMAL("Normal"),
    OFF("Off");

    override fun toString(): String = displayName
}

object General : CategoryKt("General") {
    init {
        separator {
            this.title = "${AQUA}${BOLD}General"
        }
    }

    var soundMode by enum(SoundMode.MEME) {
        this.name = Translated("Sound mode")
        this.description = Translated("Setups the sound mode for the mod. Meme mode plays meme sounds (customizable), Normal mode plays default MC sounds, Off mode disables all sounds.")
    }
}