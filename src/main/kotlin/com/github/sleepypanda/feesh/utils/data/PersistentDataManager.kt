package com.github.sleepypanda.feesh.utils.data

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.GameClosedEvent
import com.github.sleepypanda.feesh.utils.FileUtils
import com.github.sleepypanda.feesh.utils.gui.FeeshGui
import com.github.sleepypanda.feesh.utils.enums.Alignment
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import net.fabricmc.loader.api.FabricLoader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.Executors
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
        EventBus.subscribe(GameClosedEvent::class, ::onGameClosed)
    }

    private fun onGameClosed(@Suppress("UNUSED_PARAMETER") event: GameClosedEvent) {
        saveAndBackupData()
    }

    private fun saveAndBackupData() {
        executor.execute {
            val startNanos = System.nanoTime()
            FileUtils.saveJsonToFileSync(overlayCoordsFile, overlayCoordsData, gson, saveLock, "Overlay coords")
            FileUtils.saveJsonToFileSync(feeshDataFile, feeshData, gson, saveLock, "Feesh data")
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
        try {
            backupDir.mkdirs()
            FileOutputStream(zipFile).use { fileOut ->
                ZipOutputStream(fileOut).use { zipOut ->
                    for (file in filesToBackup) {
                        try {
                            ZipEntry(file.name).let { zipOut.putNextEntry(it) }
                            FileInputStream(file).use { it.copyTo(zipOut) }
                            zipOut.closeEntry()
                        } catch (e: Exception) {
                            FeeshMod.LOGGER.error("[Feesh] Backup error adding file ${file.name}", e)
                        }
                    }
                }
            }
            FeeshMod.LOGGER.info("[Feesh] Config backup succeeded: ${zipFile.absolutePath}")
            pruneOldBackups(backupDir)
        } catch (e: Exception) {
            FeeshMod.LOGGER.error("[Feesh] Config backup failed", e)
        }
    }

    private fun pruneOldBackups(backupDir: File) {
        val pattern = Regex("^feesh-backup-\\d{4}-\\d{2}-\\d{2}-\\d{6}\\.zip$")
        val backups = backupDir.listFiles()?.filter { it.isFile && pattern.matches(it.name) } ?: return
        if (backups.size <= MAX_BACKUPS) return
        val sorted = backups.sortedByDescending { it.lastModified() }
        for (oldBackup in sorted.drop(MAX_BACKUPS)) {
            try {
                if (oldBackup.delete()) {
                    FeeshMod.LOGGER.info("[Feesh] Removed old backup: ${oldBackup.name}")
                } else {
                    FeeshMod.LOGGER.warn("[Feesh] Could not delete old backup: ${oldBackup.absolutePath}")
                }
            } catch (e: Exception) {
                FeeshMod.LOGGER.error("[Feesh] Error deleting old backup ${oldBackup.name}", e)
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
