package com.github.sleepypanda.feesh.settings.categories

import com.teamresourceful.resourcefulconfig.api.annotations.Category
import com.teamresourceful.resourcefulconfig.api.annotations.Comment
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry
import com.teamresourceful.resourcefulconfig.api.types.options.EntryType
import com.teamresourceful.resourcefulconfigkt.api.CategoryKt

object General : CategoryKt("General") {
    init {
        separator {
            this.title = "Overlays"
        }
    }

    var rareCatchesAlert by boolean(false) {
        this.name = Translated("Rare Catches")
        this.description = Translated("Show title notification when catching rare creatures")
    }
}