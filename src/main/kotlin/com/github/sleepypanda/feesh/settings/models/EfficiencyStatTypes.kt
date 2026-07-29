package com.github.sleepypanda.feesh.settings.models

enum class EfficiencyStatTypes(val displayName: String) {
    CATCHES_PER_HOUR("Catches/hour"),
    SC_CATCHES_PER_HOUR("SC catches/hour"),
    SC_PER_HOUR("SC/hour"),
    SC_PER_HOUR_WITH_BS("SC/hour with BS"),
    XP_PER_HOUR("XP/hour");

    override fun toString(): String = displayName
}
