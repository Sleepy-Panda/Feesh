package com.github.sleepypanda.feesh.utils.data

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.GameClosedEvent
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.FileUtils
import com.github.sleepypanda.feesh.utils.gui.FeeshGui
import com.github.sleepypanda.feesh.utils.enums.Alignment
import com.github.sleepypanda.feesh.features.overlays.TreasureFishingTracker
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import net.fabricmc.loader.api.FabricLoader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.ConcurrentModificationException
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object PersistentDataManager {
    private val configDir: File = FabricLoader.getInstance().configDir.toFile()
    private val feeshConfigDir: File = File(configDir, "feesh")

    private const val DATA_FILE_NAME = "data.json"
    private const val OVERLAY_COORDS_FILE_NAME = "overlayCoordsData.json"
    private const val CONFIG_FILE_NAME = "config.jsonc"
    private const val BACKUP_DIR_NAME = "backup"
    private const val MAX_BACKUPS = 20

    private val backupFileNames = listOf(
        DATA_FILE_NAME,
        CONFIG_FILE_NAME,
        OVERLAY_COORDS_FILE_NAME
    )

    val backupDir: File = File(feeshConfigDir, BACKUP_DIR_NAME) // MC/config/feesh/backup

    var feeshData: FeeshData = FeeshData()
    private val feeshDataFile: File = File(feeshConfigDir, DATA_FILE_NAME)

    private var overlayCoordsData: MutableMap<String, OverlayCoordsData> = mutableMapOf()
    private val overlayCoordsFile: File = File(feeshConfigDir, OVERLAY_COORDS_FILE_NAME)

    private val saveLock = Any()
    private val saveStateLock = Any() // For debouncing intensive file saves
    private val saveDebounceMs = 1000L
    private var isFeeshDataSaveScheduled = false
    private var lastFeeshDataSaveAtMs = 0L

    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(Date::class.java, UtcDateTypeAdapter)
        .create()
        
    private val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor { r ->
        Thread(r, "Feesh-Data-Saver").apply {
            isDaemon = true
        }
    }
    
    fun init() {
        loadFeeshDataFromFile()
        loadOverlayCoordsDataFromFile()
        EventBus.subscribe(GameClosedEvent::class, ::onGameClosed)
    }

    private fun onGameClosed(@Suppress("UNUSED_PARAMETER") event: GameClosedEvent) {
        saveAndBackupDataSync()
    }

    private fun saveAndBackupDataSync() {
        CommonUtils.runWithCatching("Failed to save and backup data on game close") {
            val startNanos = System.nanoTime()

            FileUtils.saveJsonToFileSync(overlayCoordsFile, overlayCoordsData, gson, saveLock, "Overlay coords")
            forceSaveFeeshDataToFileSync()
            backupFiles()

            val elapsedMs = (System.nanoTime() - startNanos) / 1_000_000
            val elapsedSec = elapsedMs / 1000.0
            FeeshMod.LOGGER.info("[Feesh] Save and backup data finished in ${elapsedMs}ms (${elapsedSec}s).")
        }
    }

    private fun backupFiles() {
        val filesToBackup = backupFileNames.map { File(feeshConfigDir, it) }
        val missing = filesToBackup.filter { !it.exists() }
        if (missing.isNotEmpty()) {
            FeeshMod.LOGGER.warn("[Feesh] Not all config files exist to backup. Missing: ${missing.map { it.name }.joinToString(", ")}")
        }

        val backupDir = File(feeshConfigDir, BACKUP_DIR_NAME)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd-HHmmss")
        val zipName = "feesh-backup-${dateFormat.format(Date())}.zip"
        val zipFile = File(backupDir, zipName)
        CommonUtils.runWithCatching("Failed to backup config files") {
            backupDir.mkdirs()
            FileOutputStream(zipFile).use { fileOut ->
                ZipOutputStream(fileOut).use { zipOut ->
                    for (file in filesToBackup) {
                        CommonUtils.runWithCatching("Failed to backup config file ${file.name}") {
                            ZipEntry(file.name).let { zipOut.putNextEntry(it) }
                            FileInputStream(file).use { it.copyTo(zipOut) }
                            zipOut.closeEntry()
                        }
                    }
                }
            }
            FeeshMod.LOGGER.info("[Feesh] Config backup succeeded: ${zipFile.absolutePath}")
            pruneOldBackups(backupDir)
        }
    }

    private fun pruneOldBackups(backupDir: File) {
        val pattern = Regex("^feesh-backup-\\d{4}-\\d{2}-\\d{2}-\\d{6}\\.zip$")
        val backups = backupDir.listFiles()?.filter { it.isFile && pattern.matches(it.name) } ?: return
        if (backups.size <= MAX_BACKUPS) return
        val sorted = backups.sortedByDescending { it.lastModified() }
        for (oldBackup in sorted.drop(MAX_BACKUPS)) {
            CommonUtils.runWithCatching("Failed to delete old backup ${oldBackup.name}") {
                if (oldBackup.delete()) {
                    FeeshMod.LOGGER.info("[Feesh] Removed old backup: ${oldBackup.name}")
                } else {
                    FeeshMod.LOGGER.warn("[Feesh] Could not delete old backup: ${oldBackup.absolutePath}")
                }
            }
        }
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
        if (loaded != null) { // TODO: Remove migration code
            val feeshDataFileContent = feeshDataFile.readText()
            if (jsonHasNestedKey(feeshDataFileContent, "treasureFishing") &&
                !jsonHasNestedKey(feeshDataFileContent, "treasureFishing", "total", "treasureDyes", "catchesBreakdown")) {
                TreasureFishingTracker.migrateCatchesSinceLastDye()
                FeeshMod.LOGGER.info("[Feesh] Successfully migrated catches since last Treasure Dye")
            }
        }
        if (loaded != null) {
            FeeshMod.LOGGER.info("[Feesh] Successfully loaded Feesh data entries")
        }
    }

    private fun jsonHasNestedKey(content: String, vararg path: String): Boolean {
        return try {
            var obj = JsonParser.parseString(content).asJsonObject
            for (i in 0 until path.size - 1) {
                if (!obj.has(path[i]) || !obj.get(path[i]).isJsonObject) return false
                obj = obj.getAsJsonObject(path[i])
            }
            obj.has(path.last())
        } catch (_: Exception) {
            false
        }
    }

    private fun saveOverlayCoordsDataToFileAsync() {
        FileUtils.saveJsonToFileAsync(overlayCoordsFile, overlayCoordsData, gson, executor, saveLock, "Overlay coords")
    }

    /*
     * Schedules save of feeshData to JSON file asynchronously.
     * Debounces saves to prevent too often file writes.
     */
    fun saveFeeshDataToFileAsync() {
        synchronized(saveStateLock) {
            if (isFeeshDataSaveScheduled) return

            val now = System.currentTimeMillis()
            val delayMs = (saveDebounceMs - (now - lastFeeshDataSaveAtMs)).coerceAtLeast(0L)
            isFeeshDataSaveScheduled = true
            
            executor.schedule({
                val json = serializeFeeshDataToJson()
                if (json != null) {
                    FileUtils.saveJsonTextToFileAsync(feeshDataFile, json, executor, saveLock, "Feesh data")
                    synchronized(saveStateLock) {
                        isFeeshDataSaveScheduled = false
                        lastFeeshDataSaveAtMs = System.currentTimeMillis()
                    }
                } else {
                    FeeshMod.LOGGER.error("[Feesh] Failed to serialize Feesh data, skipped saving.")
                    synchronized(saveStateLock) {
                        isFeeshDataSaveScheduled = false
                    }
                }
            }, delayMs, TimeUnit.MILLISECONDS)
        }
    }

    /*
     * Forces immediate save of feeshData to JSON file synchronously. Applicable for cases when we need to save data immediately, e.g. on game closed.
     * No debouncing is applied.
     */
    fun forceSaveFeeshDataToFileSync() {
        val json = serializeFeeshDataToJson()
        if (json != null) {
            FileUtils.saveJsonTextToFileSync(feeshDataFile, json, saveLock, "Feesh data")
        } else {
            FeeshMod.LOGGER.error("[Feesh] Failed to serialize Feesh data, skipped force saving.")
        }
    }

    private fun serializeFeeshDataToJson(): String? {
        return try {
            gson.toJson(feeshData)
        } catch (ex: ConcurrentModificationException) {
            FeeshMod.LOGGER.error("[Feesh] ConcurrentModificationException occurred while serializing Feesh data.", ex)
            null
        } catch (ex: Exception) {
            FeeshMod.LOGGER.error("[Feesh] Failed to serialize Feesh data.", ex)
            null
        }
    }
}
