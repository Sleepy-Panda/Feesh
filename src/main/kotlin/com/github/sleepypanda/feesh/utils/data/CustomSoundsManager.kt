package com.github.sleepypanda.feesh.utils.data

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.constants.RareDrops
import com.github.sleepypanda.feesh.settings.models.AlertableSeaCreatureTypes
import com.github.sleepypanda.feesh.constants.Sounds
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.FileUtils
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import net.fabricmc.loader.api.FabricLoader
import java.io.File
import java.util.concurrent.Executors

data class UserCatchSoundData(
    val source: String
)

data class UserDropSoundData(
    val source: String
)

object CustomSoundsManager {
    private val configDir: File = FabricLoader.getInstance().configDir.toFile() // MC/config
    private val feeshConfigDir: File = File(configDir, FeeshMod.MOD_ID) // MC/config/feesh

    private var catchSoundsData: MutableMap<String, UserCatchSoundData> = mutableMapOf()
    private val catchSoundsFile: File = File(feeshConfigDir, "userCatchSounds.json")
    private var catchSoundsLastModifiedTimestamp = Long.MIN_VALUE
    
    private var dropSoundsData: MutableMap<String, UserDropSoundData> = mutableMapOf()
    private val dropSoundsFile: File = File(feeshConfigDir, "userDropSounds.json")
    private var dropSoundsLastModifiedTimestamp = Long.MIN_VALUE

    val soundsDir: File = File(feeshConfigDir, "sounds")

    private val saveLock = Any()
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val executor = Executors.newSingleThreadExecutor { r ->
        Thread(r, "Feesh-CustomSounds-Saver").apply {
            isDaemon = true
        }
    }
    
    fun init() {
        CommonUtils.runWithCatching("Failed to initialize custom user sounds.") {
            initUserCatchSoundsData()
            initUserDropSoundsData()
            initSoundsFolder()
        }
    }

    private fun initSoundsFolder() {
        val created = !soundsDir.exists()
        soundsDir.mkdirs()
        if (created) {
            FeeshMod.LOGGER.info("[Feesh] Created custom sounds folder: ${soundsDir.absolutePath}")
        }
    }

    private fun initUserCatchSoundsData() {
        loadUserCatchSoundsDataFromFile()
        
        val needsUpdateFile = addDefaultCatchSoundForMissingSeaCreatures()
        if (needsUpdateFile) {
            saveCatchSoundsDataToFileAsync()
        }
    }
    
    private fun addDefaultCatchSoundForMissingSeaCreatures(): Boolean {
        var updated = false
        
        AlertableSeaCreatureTypes.entries.forEach { seaCreature ->
            val key = seaCreature.displayName.uppercase()
            if (!catchSoundsData.containsKey(key)) {
                catchSoundsData[key] = UserCatchSoundData(Sounds.FEESH_NOTIFICATION)
                updated = true
            }
        }
        
        return updated
    }
    
    fun getCatchSoundData(seaCreatureName: String): UserCatchSoundData? {
        reloadCatchSoundsIfChanged()
        val key = seaCreatureName.uppercase()
        return catchSoundsData[key]
    }
       
    private fun initUserDropSoundsData() {
        loadUserDropSoundsDataFromFile()
        
        val needsUpdateFile = addDefaultDropSoundForMissingDrops()
        if (needsUpdateFile) {
            saveDropSoundsDataToFileAsync()
        }
    }
    
    private fun addDefaultDropSoundForMissingDrops(): Boolean {
        var updated = false
        
        RareDrops.rareDrops.forEach { dropInfo ->
            val key = dropInfo.id.uppercase()
            if (!dropSoundsData.containsKey(key)) {
                dropSoundsData[key] = UserDropSoundData(dropInfo.defaultSoundFileName)
                updated = true
            }
        }
        
        return updated
    }
    
    fun getDropSoundData(dropId: String): UserDropSoundData? {
        reloadDropSoundsIfChanged()
        val key = dropId.uppercase()
        return dropSoundsData[key]
    }

    fun resolveUserSoundFile(fileName: String): File? {
        val name = fileName.trim()
        if (name.isEmpty() || name == "." || name == ".." || name.contains('/') || name.contains('\\') || name.contains(':')) {
            return null
        }
        val file = File(soundsDir, name)
        return if (file.isFile) file else null
    }
    
    private fun loadUserDropSoundsDataFromFile() {
        val type = object : TypeToken<Map<String, UserDropSoundData>>() {}.type
        val loaded: Map<String, UserDropSoundData>? = FileUtils.loadJsonFromFile(dropSoundsFile, type, gson, "Drop sounds")
        dropSoundsLastModifiedTimestamp = getLastModifiedTimestamp(dropSoundsFile)

        if (loaded != null) {
            dropSoundsData = loaded.toMutableMap()
            FeeshMod.LOGGER.info("[Feesh] Loaded ${dropSoundsData.size} drop sounds entries")
        }
    }

    private fun reloadDropSoundsIfChanged() {
        if (getLastModifiedTimestamp(dropSoundsFile) == dropSoundsLastModifiedTimestamp) return
        loadUserDropSoundsDataFromFile()
    }
    
    private fun saveDropSoundsDataToFileAsync() {
        FileUtils.saveJsonToFileAsync(
            dropSoundsFile, 
            dropSoundsData, 
            gson, 
            executor, 
            saveLock, 
            "Drop sounds"
        )
    }

    private fun loadUserCatchSoundsDataFromFile() {
        val type = object : TypeToken<Map<String, UserCatchSoundData>>() {}.type
        val loaded: Map<String, UserCatchSoundData>? = FileUtils.loadJsonFromFile(catchSoundsFile, type, gson, "Catch sounds")
        catchSoundsLastModifiedTimestamp = getLastModifiedTimestamp(catchSoundsFile)

        if (loaded != null) {
            catchSoundsData = loaded.toMutableMap()
            FeeshMod.LOGGER.info("[Feesh] Loaded ${catchSoundsData.size} catch sounds entries")
        }
    }

    private fun reloadCatchSoundsIfChanged() {
        if (getLastModifiedTimestamp(catchSoundsFile) == catchSoundsLastModifiedTimestamp) return
        loadUserCatchSoundsDataFromFile()
    }

    private fun getLastModifiedTimestamp(file: File): Long = if (file.isFile) file.lastModified() else 0L
    
    private fun saveCatchSoundsDataToFileAsync() {
        FileUtils.saveJsonToFileAsync(
            catchSoundsFile, 
            catchSoundsData, 
            gson, 
            executor, 
            saveLock, 
            "Catch sounds"
        )
    }
}
