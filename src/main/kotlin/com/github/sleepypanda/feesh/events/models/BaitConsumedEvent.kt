package com.github.sleepypanda.feesh.events.models

data class BaitConsumedEvent(
    val baitName: String,
    val baitDisplayName: String,
    val baitId: String
)
