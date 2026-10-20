package com.github.sleepypanda.feesh.events.models

enum class TreasureCatchType {
    GOOD,
    GREAT,
    OUTSTANDING,
    MUSICAL,
    GOOD_JUNK,
    GREAT_JUNK,
    OUTSTANDING_JUNK;

    val isJunk: Boolean
        get() = this == GOOD_JUNK || this == GREAT_JUNK || this == OUTSTANDING_JUNK

    val isRegularTreasure: Boolean
        get() = this == GOOD || this == GREAT || this == OUTSTANDING

    /**
     * Get the TreasureCatchType from the chat label.
     * @param label The unformatted label from the chat message, e.g. GOOD or GREAT JUNK.
     * @return The TreasureCatchType, or null if the label is not a valid TreasureCatchType.
     */
    companion object {
        fun getTypeByChatLabel(label: String): TreasureCatchType? = when (label.uppercase()) {
            "GOOD" -> GOOD
            "GREAT" -> GREAT
            "OUTSTANDING" -> OUTSTANDING
            "MUSICAL" -> MUSICAL
            "GOOD JUNK" -> GOOD_JUNK
            "GREAT JUNK" -> GREAT_JUNK
            "OUTSTANDING JUNK" -> OUTSTANDING_JUNK
            else -> null
        }
    }
}

/**
 * Published when a treasure or junk catch message is received.
 * @param type Catch quality.
 * @param unformattedText Full unformatted chat line.
 */
data class TreasureCatchEvent(
    val type: TreasureCatchType,
    val unformattedText: String,
)
