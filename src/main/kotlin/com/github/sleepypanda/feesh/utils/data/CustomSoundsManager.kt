package com.github.sleepypanda.feesh.utils.data

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.constants.SeaCreatures
import com.github.sleepypanda.feesh.constants.Sounds
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.SharedConstants
import net.minecraft.resource.ResourceType
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

data class UserCatchSoundData(
    val source: String
)

object CustomSoundsManager {
    private val configDir: File = FabricLoader.getInstance().configDir.toFile() // MC/config
    private val feeshConfigDir: File = File(configDir, FeeshMod.MOD_ID) // MC/config/feesh

    private var catchSoundsData: MutableMap<String, UserCatchSoundData> = mutableMapOf()
    private val catchSoundsFile: File = File(feeshConfigDir, "userCatchSounds.json")
    
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
        //initResourcePackStructure() // Custom user sounds resource pack structure
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
    
    private fun loadUserCatchSoundsDataFromFile() {
        try {
            if (!catchSoundsFile.exists() || !catchSoundsFile.canRead()) {
                FeeshMod.LOGGER.info("[Feesh] Catch sounds file does not exist, will create with defaults")
                return
            }
            
            val content = catchSoundsFile.readText()
            if (content.isBlank()) {
                FeeshMod.LOGGER.info("[Feesh] Catch sounds file is empty, will create with defaults")
                return
            }
            
            val type = object : TypeToken<Map<String, UserCatchSoundData>>() {}.type
            val loaded = gson.fromJson<Map<String, UserCatchSoundData>>(content, type)
            catchSoundsData = loaded?.toMutableMap() ?: mutableMapOf()
            
            FeeshMod.LOGGER.info("[Feesh] Loaded ${catchSoundsData.size} catch sound entries")
        } catch (e: Exception) {
            FeeshMod.LOGGER.error("[Feesh] Failed to load catch sounds data", e)
            catchSoundsData = mutableMapOf()
        }
    }
    
    private fun saveCatchSoundsDataToFileAsync() {
        CompletableFuture.runAsync({
            try {
                synchronized(saveLock) {
                    feeshConfigDir.mkdirs()
                    
                    addDefaultCatchSoundForMissingSeaCreatures()
                    
                    val json = gson.toJson(catchSoundsData)
                    Files.write(
                        catchSoundsFile.toPath(),
                        json.toByteArray(),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING,
                        StandardOpenOption.WRITE
                    )
                }
            } catch (e: Exception) {
                FeeshMod.LOGGER.error("[Feesh] Failed to save catch sounds data", e)
            }
        }, executor)
    }
    
    private fun initResourcePackStructure() {
        try {
            resourcePackSoundsDir.mkdirs()
            // 69.0 for 1.21.10
            // 75.0 for 1.21.11
            // https://minecraft.wiki/w/Pack_format
            val packFormat = SharedConstants.getGameVersion().packVersion(ResourceType.CLIENT_RESOURCES)
            
            if (!resourcePackMcmetaFile.exists()) {
                val packMcmetaContent = """{
    "pack": {
        "pack_format": ${packFormat},
        "description": "Feesh Custom Sounds"
    }
}
"""
                resourcePackMcmetaFile.writeText(packMcmetaContent)
                FeeshMod.LOGGER.info("[Feesh] Created pack.mcmeta in resource pack directory")
            }
            
            // Automatically generate sounds.json from all .ogg files in the sounds directory
            generateSoundsJsonFromFiles()
            
            if (resourcePackDir.exists()) {
                FeeshMod.LOGGER.info("[Feesh] Resource pack directory ready: ${resourcePackDir.absolutePath}")
            }
        } catch (e: Exception) {
            FeeshMod.LOGGER.error("[Feesh] Failed to initialize resource pack structure", e)
        }
    }
    
    private fun generateSoundsJsonFromFiles() {
        try {
            if (!resourcePackSoundsDir.exists()) {
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
                    "sounds" to listOf("feeshcustom:$soundName"),
                    "subtitle" to "Feesh custom sound"
                )
            }
            
            val json = gson.toJson(soundsMap)
            resourcePackSoundsJsonFile.parentFile?.mkdirs()
            resourcePackSoundsJsonFile.writeText(json)
            
            FeeshMod.LOGGER.info("[Feesh] Generated sounds.json with ${oggFiles.size} sound entries")
        } catch (e: Exception) {
            FeeshMod.LOGGER.error("[Feesh] Failed to generate sounds.json for custom user resource pack", e)
        }
    }
}
