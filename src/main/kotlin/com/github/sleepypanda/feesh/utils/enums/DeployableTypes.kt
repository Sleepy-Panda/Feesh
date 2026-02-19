package com.github.sleepypanda.feesh.utils.enums

enum class DeployableTypes(val displayName: String) {
    TOTEM_OF_CORRUPTION("Totem of Corruption"),
    BLACK_HOLE("Black Hole"),
    UMBERELLA("Umberella"),
    FLARE("Flare"),
    DWARVEN_LANTERN("Dwarven Lanterns");

    override fun toString(): String = displayName // Show display name in UI, but internally it uses name
}
