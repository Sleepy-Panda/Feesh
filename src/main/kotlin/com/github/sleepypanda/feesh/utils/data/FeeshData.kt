package com.github.sleepypanda.feesh.utils.data

import com.github.sleepypanda.feesh.utils.enums.Alignment

object FeeshData {
    data class OverlayCoordsData(val x: Int = 10, val y: Int = 10, val scale: Float = 1.0f, val alignment: Alignment = Alignment.LEFT)
}
