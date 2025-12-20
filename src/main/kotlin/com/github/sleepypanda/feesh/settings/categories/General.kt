package com.github.sleepypanda.feesh.settings.categories

import com.teamresourceful.resourcefulconfig.api.annotations.Category
import com.teamresourceful.resourcefulconfig.api.annotations.Comment
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry
import com.teamresourceful.resourcefulconfig.api.types.options.EntryType

@Category("general")
object General {
    @ConfigEntry(
        type = EntryType.BOOLEAN,
        id = "rare_catches",
        translation = "feesh.config.rare_catches"
    )
    @Comment("Show title notification when catching rare creatures")
    var rareCatchesEnabled = true
}