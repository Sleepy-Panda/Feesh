package com.github.sleepypanda.feesh.utils.data

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.utils.FileUtils
import com.github.sleepypanda.feesh.utils.gui.FeeshGui
import com.github.sleepypanda.feesh.utils.enums.Alignment
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import net.fabricmc.loader.api.FabricLoader
import java.io.File
import java.util.Date
import java.util.concurrent.Executors

object PersistentDataManager {
    private val configDir: File = FabricLoader.getInstance().configDir.toFile()
    private val feeshConfigDir: File = File(configDir, "feesh")

    var feeshData: FeeshData = FeeshData()
    private val feeshDataFile: File = File(feeshConfigDir, "data.json")

    private var overlayCoordsData: MutableMap<String, OverlayCoordsData> = mutableMapOf()
    private val overlayCoordsFile: File = File(feeshConfigDir, "overlayCoordsData.json")

    private val saveLock = Any()
    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(Date::class.java, UtcDateTypeAdapter)
        .create()
    private val executor = Executors.newSingleThreadExecutor { r ->
        Thread(r, "Feesh-Data-Saver").apply {
            isDaemon = true
        }
    }
    
    fun init() {
        loadFeeshDataFromFile()
        loadOverlayCoordsDataFromFile()
    }
    
    fun getOverlayCoordsData(key: String): OverlayCoordsData {
        return overlayCoordsData[key] ?: OverlayCoordsData()
    }
    
    fun updateOverlayCoordsData(key: String, x: Int, y: Int, scale: Float, alignment: Alignment) {
        synchronized(saveLock) {
            overlayCoordsData[key] = OverlayCoordsData(x, y, scale, alignment)
            saveOverlayCoordsDataToFileAsync()
        }
    }
    
    private fun loadOverlayCoordsDataFromFile() {
        val type = object : TypeToken<Map<String, OverlayCoordsData>>() {}.type
        val loaded: Map<String, OverlayCoordsData>? = FileUtils.loadJsonFromFile(overlayCoordsFile, type, gson, "Overlay coords")
        overlayCoordsData = loaded?.toMutableMap() ?: mutableMapOf()
        FeeshGui.applyOverlayCoordsToAllGuis()

        if (loaded != null) {
            FeeshMod.LOGGER.info("[Feesh] Loaded ${overlayCoordsData.size} overlay coordinate entries")
        }
    }

    private fun loadFeeshDataFromFile() {
        val type = object : TypeToken<FeeshData>() {}.type
        val loaded: FeeshData? = FileUtils.loadJsonFromFile(feeshDataFile, type, gson, "Feesh data")
        feeshData = loaded ?: FeeshData()
        if (loaded != null) {
            FeeshMod.LOGGER.info("[Feesh] Successfully loaded Feesh data entries")
        }
    }
    
    private fun saveOverlayCoordsDataToFileAsync() {
        FileUtils.saveJsonToFileAsync(overlayCoordsFile, overlayCoordsData, gson, executor, saveLock, "Overlay coords")
    }

    fun saveFeeshDataToFileAsync() {
        FileUtils.saveJsonToFileAsync(feeshDataFile, feeshData, gson, executor, saveLock, "Feesh data")
    }
}
