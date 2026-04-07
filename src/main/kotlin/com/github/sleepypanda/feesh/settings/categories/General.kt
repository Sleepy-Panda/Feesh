package com.github.sleepypanda.feesh.settings.categories

import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.github.sleepypanda.feesh.utils.data.PersistentDataManager
import com.teamresourceful.resourcefulconfig.api.annotations.Category
import com.teamresourceful.resourcefulconfig.api.annotations.Comment
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry
import com.teamresourceful.resourcefulconfig.api.types.options.EntryType
import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import net.minecraft.util.Util

enum class SoundMode(val displayName: String) {
    MEME("Meme"),
    NORMAL("Normal"),
    OFF("Off");

    override fun toString(): String = displayName
}

object General : CategoryKt("General") {
    init {
        separator {
            this.title = "${AQUA}${BOLD}Sounds"
        }
    }

    var soundMode by enum(SoundMode.NORMAL) {
        this.name = Translated("Sound mode")
        this.description = Translated("Setups the sound mode for the mod. Meme mode plays meme sounds (customizable), Normal mode plays default MC sounds, Off mode disables all sounds.")
    }

    init {
        button {
            title = "Custom sounds guide"
            description = "Opens the guide for setting up custom sounds for Meme sound mode."
            text = "Open"
            onClick {
                Util.getOperatingSystem().open("https://github.com/Sleepy-Panda/Feesh/blob/develop/docs/Custom%20sounds%20guide.md")
            }
        }
    }

    init {
        separator {
            this.title = "${AQUA}${BOLD}Other"
        }

        button {
            title = "Open backups folder"
            description = "Opens the folder where data backups are stored on game close."
            text = "Open"
            onClick {
                val dir = PersistentDataManager.backupDir
                if (!dir.exists()) dir.mkdirs()
                Util.getOperatingSystem().open(dir.toURI().toString())
            }
        }
    }
}