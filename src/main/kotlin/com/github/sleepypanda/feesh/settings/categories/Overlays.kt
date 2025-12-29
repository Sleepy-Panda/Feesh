package com.github.sleepypanda.feesh.settings.categories

import com.teamresourceful.resourcefulconfig.api.annotations.Category
import com.teamresourceful.resourcefulconfig.api.annotations.Comment
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry
import com.teamresourceful.resourcefulconfig.api.types.options.EntryType
import com.teamresourceful.resourcefulconfigkt.api.CategoryKt

object Overlays : CategoryKt("Overlays") {
    init {
        separator {
            this.title = "Jerry's Workshop"
        }
    }

    var jerryWorkshopTrackerOverlay by boolean(false) {
        this.name = Translated("Jerry's Workshop tracker")
        this.description = Translated("Shows an overlay with Yeti / Reindrake catch statistics while in the Jerry Workshop.")
    }

    init {
        separator {
            this.title = "Legion & Bobbin' Time"
        }
    }

    var legionBobbingTimeTrackerOverlay by boolean(false) {
        this.name = Translated("Legion & Bobbin' Time tracker")
        this.description = Translated("Shows an overlay with the amount of players within 30 blocks (excluding you), and amount of fishing hooks within 30 blocks (including your own hook). Hidden if you have no fishing rod in your hotbar!")
    }
}