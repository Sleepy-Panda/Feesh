package com.github.sleepypanda.feesh.utils.data

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.utils.enums.Alignment
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import net.fabricmc.loader.api.FabricLoader
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

object PersistentDataManager {
    private val configDir: File = FabricLoader.getInstance().configDir.toFile()
    private val feeshConfigDir: File = File(configDir, "feesh")

    var feeshData: FeeshData = FeeshData()
    private val feeshDataFile: File = File(feeshConfigDir, "data.json")

    private var overlayCoordsData: MutableMap<String, OverlayCoordsData> = mutableMapOf()
    private val overlayCoordsFile: File = File(feeshConfigDir, "overlayCoordsData.json")

    private val saveLock = Any()
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
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
        try {
            if (!overlayCoordsFile.exists() || !overlayCoordsFile.canRead()) {
                FeeshMod.LOGGER.info("[Feesh] Overlay coords file does not exist, using defaults")
                return
            }
            
            val content = overlayCoordsFile.readText()
            if (content.isBlank()) {
                FeeshMod.LOGGER.info("[Feesh] Overlay coords file is empty, using defaults")
                return
            }
            
            val type = object : TypeToken<Map<String, OverlayCoordsData>>() {}.type
            val loaded = gson.fromJson<Map<String, OverlayCoordsData>>(content, type)
            overlayCoordsData = loaded?.toMutableMap() ?: mutableMapOf()
            
            FeeshMod.LOGGER.info("[Feesh] Loaded ${overlayCoordsData.size} overlay coordinate entries")
        } catch (e: Exception) {
            FeeshMod.LOGGER.error("[Feesh] Failed to load overlay coords data", e)
            overlayCoordsData = mutableMapOf()
        }
    }

    private fun loadFeeshDataFromFile() {
        try {
            if (!feeshDataFile.exists() || !feeshDataFile.canRead()) {
                FeeshMod.LOGGER.info("[Feesh] Data file does not exist, using defaults")
                return
            }
            
            val content = feeshDataFile.readText()
            if (content.isBlank()) {
                FeeshMod.LOGGER.info("[Feesh] Data file is empty, using defaults")
                return
            }
            
            val type = object : TypeToken<FeeshData>() {}.type
            val loaded = gson.fromJson<FeeshData>(content, type)
            feeshData = loaded ?: FeeshData()
            
            FeeshMod.LOGGER.info("[Feesh] Successfully loaded data entries")
        } catch (e: Exception) {
            FeeshMod.LOGGER.error("[Feesh] Failed to load data, using defaults", e)
            feeshData = FeeshData()
        }
    }
    
    private fun saveOverlayCoordsDataToFileAsync() {
        CompletableFuture.runAsync({
            try {
                synchronized(saveLock) {
                    feeshConfigDir.mkdirs()
                    
                    val json = gson.toJson(overlayCoordsData)
                    Files.write(
                        overlayCoordsFile.toPath(),
                        json.toByteArray(),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING,
                        StandardOpenOption.WRITE
                    )
                }
            } catch (e: Exception) {
                FeeshMod.LOGGER.error("[Feesh] Failed to save overlay coords data", e)
            }
        }, executor)
    }

    fun saveFeeshDataToFileAsync() {
        CompletableFuture.runAsync({
            try {
                synchronized(saveLock) {
                    feeshConfigDir.mkdirs()
                    
                    val json = gson.toJson(feeshData)
                    Files.write(
                        feeshDataFile.toPath(),
                        json.toByteArray(),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING,
                        StandardOpenOption.WRITE
                    )
                }
            } catch (e: Exception) {
                FeeshMod.LOGGER.error("[Feesh] Failed to save data", e)
            }
        }, executor)
    }
}
