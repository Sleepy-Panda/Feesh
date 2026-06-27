package com.github.sleepypanda.feesh.settings.models

enum class EfficiencyStatTypes(val displayName: String) {
    CASTS_PER_HOUR("Casts/hour"),
    SC_CATCHES_PER_HOUR("SC catches/hour"),
    SC_PER_HOUR_WITH_DH("SC/hour with DH"),
    SC_PER_HOUR_WITH_DH_AND_BS("SC/hour with DH and BS");

    override fun toString(): String = displayName
}
