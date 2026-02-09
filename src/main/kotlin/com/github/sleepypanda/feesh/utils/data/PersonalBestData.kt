package com.github.sleepypanda.feesh.utils.data

import java.util.Date

data class PersonalBestEntry(
    var amount: Int = 0,
    var at: Date? = null
)

data class PersonalBestData(
    val sharksCaught: PersonalBestEntry = PersonalBestEntry(),
    val greatWhiteSharksCaught: PersonalBestEntry = PersonalBestEntry()
)

