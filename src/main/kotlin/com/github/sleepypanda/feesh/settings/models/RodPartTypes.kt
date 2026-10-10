package com.github.sleepypanda.feesh.settings.models

enum class RodPartTypes(val displayName: String) {
    HOOK("Hook"),
    LINE("Line"),
    SINKER("Sinker");

    override fun toString(): String = displayName
}
