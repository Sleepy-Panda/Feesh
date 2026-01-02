package com.github.sleepypanda.feesh.settings.categories

import com.github.sleepypanda.feesh.utils.gui.MoveGuis
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.teamresourceful.resourcefulconfig.api.annotations.Category
import com.teamresourceful.resourcefulconfig.api.annotations.Comment
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry
import com.teamresourceful.resourcefulconfig.api.types.options.EntryType
import com.teamresourceful.resourcefulconfigkt.api.CategoryKt

object General : CategoryKt("General") {
    init {
        separator {
            this.title = "General"
        }

        button {
            title = "Move GUIs"
            description = "Allows to move and resize all GUIs enabled in the Overlays settings section. Executes ${AQUA}/feeshMoveAllGuis"
            text = "Click to move"
            onClick {
                MoveGuis.moveAllGuis()
            }
        }
    }
}