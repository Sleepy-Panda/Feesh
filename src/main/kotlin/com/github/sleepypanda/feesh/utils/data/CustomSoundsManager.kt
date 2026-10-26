package com.github.sleepypanda.feesh.utils.data

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.constants.RareDrops
import com.github.sleepypanda.feesh.settings.models.AlertableSeaCreatureTypes
import com.github.sleepypanda.feesh.constants.Sounds
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
    private var catchSoundsStamp = Long.MIN_VALUE
    
    private var dropSoundsData: MutableMap<String, UserDropSoundData> = mutableMapOf()
    private val dropSoundsFile: File = File(feeshConfigDir, "userDropSounds.json")
    private var dropSoundsStamp = Long.MIN_VALUE

    val soundsDir: File = File(feeshConfigDir, "sounds")
    private val legacySoundsDir: File = File(feeshConfigDir, "feesh-custom-sounds/assets/feesh/sounds")

    private val saveLock = Any()
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val executor = Executors.newSingleThreadExecutor { r ->
        Thread(r, "Feesh-CustomSounds-Saver").apply {
            isDaemon = true
        }
    }
    
    fun init() {
        initUserCatchSoundsData()
        initUserDropSoundsData()
        initSoundsFolder()
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
        
        AlertableSeaCreatureTypes.values().forEach { seaCreature ->
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
        return listOf(File(soundsDir, name), File(legacySoundsDir, name)).firstOrNull { it.isFile }
    }
    
    private fun loadUserDropSoundsDataFromFile() {
        val type = object : TypeToken<Map<String, UserDropSoundData>>() {}.type
        val loaded: Map<String, UserDropSoundData>? = FileUtils.loadJsonFromFile(dropSoundsFile, type, gson, "Drop sounds")
        dropSoundsStamp = fileStamp(dropSoundsFile)
        if (loaded != null) {
            dropSoundsData = loaded.toMutableMap()
            FeeshMod.LOGGER.info("[Feesh] Loaded ${dropSoundsData.size} drop sounds entries")
        }
    }

    private fun reloadDropSoundsIfChanged() {
        if (fileStamp(dropSoundsFile) == dropSoundsStamp) return
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
        catchSoundsStamp = fileStamp(catchSoundsFile)
        if (loaded != null) {
            catchSoundsData = loaded.toMutableMap()
            FeeshMod.LOGGER.info("[Feesh] Loaded ${catchSoundsData.size} catch sounds entries")
        }
    }

    private fun reloadCatchSoundsIfChanged() {
        if (fileStamp(catchSoundsFile) == catchSoundsStamp) return
        loadUserCatchSoundsDataFromFile()
    }

    private fun fileStamp(file: File): Long = if (file.isFile) file.lastModified() else 0L
    
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
    
    private fun initSoundsFolder() {
        val created = !soundsDir.exists()
        soundsDir.mkdirs()
        if (created) {
            migrateLegacySounds()
        }
        FeeshMod.LOGGER.info("[Feesh] Custom sounds folder: ${soundsDir.absolutePath}")
    }

    private fun migrateLegacySounds() {
        if (!legacySoundsDir.isDirectory) return

        val oggFiles = legacySoundsDir.listFiles { file ->
            file.isFile && file.name.endsWith(".ogg", ignoreCase = true)
        } ?: return
        if (oggFiles.isEmpty()) return

        var copied = 0
        oggFiles.forEach { source ->
            val target = File(soundsDir, source.name)
            if (target.exists()) return@forEach
            runCatching {
                source.copyTo(target)
                copied++
            }.onFailure {
                FeeshMod.LOGGER.error("[Feesh] Failed to copy legacy sound ${source.name}", it)
            }
        }

        if (copied > 0) {
            FeeshMod.LOGGER.info(
                "[Feesh] Copied $copied sound(s) from ${legacySoundsDir.absolutePath} to ${soundsDir.absolutePath}"
            )
        }
    }
}
