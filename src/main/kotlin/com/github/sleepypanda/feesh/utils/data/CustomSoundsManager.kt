package com.github.sleepypanda.feesh.utils.data

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.constants.RareDrops
import com.github.sleepypanda.feesh.constants.SeaCreatures
import com.github.sleepypanda.feesh.constants.Sounds
import com.github.sleepypanda.feesh.utils.RegisterUtils
import com.github.sleepypanda.feesh.utils.FileUtils
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.PlayerUtils
import com.github.sleepypanda.feesh.utils.SoundUtils
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.SharedConstants
import net.minecraft.resource.ResourceType
import java.io.File
import java.util.concurrent.Executors

data class UserCatchSoundData(
    val source: String
)

data class UserDropSoundData(
    val source: String
)

object CustomSoundsManager {
    val GENERATE_SOUNDS_RESOURCE_PACK_COMMAND = "feeshGenerateSoundsResourcePack"

    private val configDir: File = FabricLoader.getInstance().configDir.toFile() // MC/config
    private val feeshConfigDir: File = File(configDir, FeeshMod.MOD_ID) // MC/config/feesh

    private var catchSoundsData: MutableMap<String, UserCatchSoundData> = mutableMapOf()
    private val catchSoundsFile: File = File(feeshConfigDir, "userCatchSounds.json")
    
    private var dropSoundsData: MutableMap<String, UserDropSoundData> = mutableMapOf()
    private val dropSoundsFile: File = File(feeshConfigDir, "userDropSounds.json")
    
    val resourcePackDir: File = File(feeshConfigDir, "feesh-custom-sounds")
    private val resourcePackSoundsDir: File = File(resourcePackDir, "assets/feeshcustom/sounds")
    private val resourcePackSoundsJsonFile: File = File(resourcePackDir, "assets/feeshcustom/sounds.json")
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
        registerCommands()
    }

    private fun registerCommands() {
        RegisterUtils.command(GENERATE_SOUNDS_RESOURCE_PACK_COMMAND) {
            generateSoundsJsonFromFiles()
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
        
        SeaCreatures.rareSeaCreatures.forEach { seaCreature ->
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
        try {
            if (resourcePackSoundsDir.exists()) {
                FeeshMod.LOGGER.error("[Feesh] Resource pack sounds directory already exists. Skipping initialization.")
                return
            }

            resourcePackSoundsDir.mkdirs()
            // 69.0 for 1.21.10
            // 75.0 for 1.21.11
            // https://minecraft.wiki/w/Pack_format
            val packFormat = SharedConstants.getGameVersion().packVersion(ResourceType.CLIENT_RESOURCES)
            
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
                   
            if (resourcePackDir.exists()) {
                FeeshMod.LOGGER.info("[Feesh] Resource pack directory ready: ${resourcePackDir.absolutePath}")
            }
        } catch (e: Exception) {
            FeeshMod.LOGGER.error("[Feesh] Failed to initialize resource pack structure", e)
        }
    }
    
    fun generateSoundsJsonFromFiles() {
        try {
            if (!resourcePackSoundsDir.exists()) {
                ChatUtils.sendLocalChat("Resource pack sounds directory does not exist. Please create it and add your .ogg files to it.", true)
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
                soundsMap[soundName] = mapOf(
                    "sounds" to listOf("${SoundUtils.SOUNDS_IDENTIFIER_PREFIX}:$soundName"),
                    "subtitle" to "Feesh custom sound: ${soundName}"
                )
            }
            
            val json = gson.toJson(soundsMap)
            resourcePackSoundsJsonFile.parentFile?.mkdirs()
            resourcePackSoundsJsonFile.writeText(json)
            
            ChatUtils.sendLocalChat("Generated sounds.json with ${oggFiles.size} sound entries", true)
        } catch (e: Exception) {
            FeeshMod.LOGGER.error("[Feesh] Failed to generate sounds.json for custom user resource pack", e)
        }
    }
}
