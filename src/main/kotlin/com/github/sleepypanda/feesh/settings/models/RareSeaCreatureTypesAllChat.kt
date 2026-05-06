package com.github.sleepypanda.feesh.settings.models

import com.github.sleepypanda.feesh.constants.SeaCreatureNames

enum class RareSeaCreatureTypesAllChat(val displayName: String) {
    THUNDER(SeaCreatureNames.THUNDER),
    LORD_JAWBUS(SeaCreatureNames.LORD_JAWBUS),
    RAGNAROK(SeaCreatureNames.RAGNAROK),
    VANQUISHER(SeaCreatureNames.VANQUISHER),
    WIKI_TIKI(SeaCreatureNames.WIKI_TIKI),
    TITANOBOA(SeaCreatureNames.TITANOBOA);

    override fun toString(): String = displayName // Show display name in UI, but internally it uses name
}