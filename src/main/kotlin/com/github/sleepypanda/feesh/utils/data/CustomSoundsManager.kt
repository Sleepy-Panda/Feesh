package com.github.sleepypanda.feesh.utils.data

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.constants.RareDrops
import com.github.sleepypanda.feesh.constants.SeaCreatures
import com.github.sleepypanda.feesh.constants.Sounds
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.FileUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.SharedConstants
import net.minecraft.server.packs.PackType
import java.io.File
import java.io.FileOutputStream
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
    
    private var dropSoundsData: MutableMap<String, UserDropSoundData> = mutableMapOf()
    private val dropSoundsFile: File = File(feeshConfigDir, "userDropSounds.json")
    
    val resourcePackDir: File = File(feeshConfigDir, "feesh-custom-sounds")
    private val resourcePackSoundsDir: File = File(resourcePackDir, "assets/feesh/sounds")
    private val resourcePackSoundsJsonFile: File = File(resourcePackDir, "assets/feesh/sounds.json")
    private val resourcePackMcmetaFile: File = File(resourcePackDir, "pack.mcmeta")

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
        initResourcePackStructure() // Custom user sounds resource pack structure
        generateSoundsJsonFromFiles() // Generate sounds.json from all .ogg files in the sounds directory (for resource pack)
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
        
        SeaCreatures.seaCreaturesWithAlert.forEach { seaCreature ->
            val key = seaCreature.name.uppercase()
            if (!catchSoundsData.containsKey(key)) {
                catchSoundsData[key] = UserCatchSoundData(Sounds.FEESH_NOTIFICATION)
                updated = true
            }
        }
        
        return updated
    }
    
    fun getCatchSoundData(seaCreatureName: String): UserCatchSoundData? {
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
        val key = dropId.uppercase()
        return dropSoundsData[key]
    }
    
    private fun loadUserDropSoundsDataFromFile() {
        val type = object : TypeToken<Map<String, UserDropSoundData>>() {}.type
        val loaded: Map<String, UserDropSoundData>? = FileUtils.loadJsonFromFile(dropSoundsFile, type, gson, "Drop sounds")
        dropSoundsData = loaded?.toMutableMap() ?: mutableMapOf()
        if (loaded != null) {
            FeeshMod.LOGGER.info("[Feesh] Loaded ${dropSoundsData.size} drop sounds entries")
        }
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
        catchSoundsData = loaded?.toMutableMap() ?: mutableMapOf()
        if (loaded != null) {
            FeeshMod.LOGGER.info("[Feesh] Loaded ${catchSoundsData.size} catch sounds entries")
        }
    }
    
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
    
    private fun initResourcePackStructure() {
        CommonUtils.runWithCatching("Failed to initialize resource pack structure") {
            if (resourcePackDir.exists()) {
                FeeshMod.LOGGER.error("[Feesh] Resource pack sounds directory already exists. Skipping initialization.")
                return
            }

            resourcePackSoundsDir.mkdirs()
            // 69.0 for 1.21.10
            // 75.0 for 1.21.11
            // https://minecraft.wiki/w/Pack_format
            val packFormat = SharedConstants.getCurrentVersion().packVersion(PackType.CLIENT_RESOURCES)
            
            if (!resourcePackMcmetaFile.exists()) {
                val packMcmetaContent = """
{
    "pack": {
        "pack_format": ${packFormat},
        "min_format": 65,
        "max_format": 999,
        "description": "Custom sounds for Feesh mod"
    }
}
"""
                resourcePackMcmetaFile.writeText(packMcmetaContent)
                FeeshMod.LOGGER.info("[Feesh] Created pack.mcmeta in resource pack directory")
            }

            // Copy mod icon as pack icon (pack.png)
            val packIconFile = File(resourcePackDir, "pack.png")
            CustomSoundsManager::class.java.classLoader.getResourceAsStream("assets/feesh/icon.png")?.use { input ->
                FileOutputStream(packIconFile).use { output -> input.copyTo(output) }
                FeeshMod.LOGGER.info("[Feesh] Copied pack icon to resource pack directory")
            }
                   
            if (resourcePackDir.exists()) {
                FeeshMod.LOGGER.info("[Feesh] Resource pack directory ready: ${resourcePackDir.absolutePath}")
            }
        }
    }
    
    private fun generateSoundsJsonFromFiles() {
        CommonUtils.runWithCatching("Failed to generate sounds.json for custom user resource pack") {
            if (!resourcePackSoundsDir.exists()) {
                FeeshMod.LOGGER.info("[Feesh] Resource pack sounds directory does not exist. Please create it and add your .ogg files to it.")
                return
            }
            
            val oggFiles = resourcePackSoundsDir.listFiles { file ->
                file.isFile && file.name.endsWith(".ogg", ignoreCase = true)
            } ?: emptyArray()
            
            if (oggFiles.isEmpty()) {
                // Create empty sounds.json if no files exist
                resourcePackSoundsJsonFile.parentFile?.mkdirs()
                resourcePackSoundsJsonFile.writeText("{\n}\n")
                return
            }
            
            // Build sounds.json structure
            val soundsMap = mutableMapOf<String, Map<String, Any>>()
            
            oggFiles.forEach { file ->
                val soundName = file.name.removeSuffix(".ogg")
                if (!soundName.matches(Regex("[a-z0-9_-]+"))) {
                    FeeshMod.LOGGER.warn("[Feesh] Invalid filename: $soundName. Only lowercase letters (a-z), numbers (0-9), `-` and `_` are allowed.")
                    return@forEach
                }

                // File structure: https://minecraft.wiki/w/Sounds.json
                soundsMap[soundName] = mapOf(
                    "sounds" to listOf(mapOf(
                        "name" to "${SoundUtils.SOUNDS_IDENTIFIER_PREFIX}:$soundName",
                        "stream" to true
                    ))
                )
            }
     
            val json = gson.toJson(soundsMap)
            resourcePackSoundsJsonFile.parentFile?.mkdirs()
            resourcePackSoundsJsonFile.writeText(json)
            
            FeeshMod.LOGGER.info("[Feesh] Generated sounds.json with ${soundsMap.size} sound entries")
        }
    }
}
