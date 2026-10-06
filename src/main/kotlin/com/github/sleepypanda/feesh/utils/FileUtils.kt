package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.FeeshMod
import com.google.gson.Gson
import java.io.File
import java.io.IOException
import java.lang.reflect.Type
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

object FileUtils {
    @Volatile
    private var loggedPlainRename = false

    /**
     * Loads JSON data from a file and deserializes it to the specified type.
     * @param file The file to read from
     * @param type The Gson type for deserialization
     * @param gson The Gson instance to use
     * @param logPrefix Prefix for log messages (e.g., "Catch sounds", "Drop sounds")
     * @return The deserialized data, or null if file doesn't exist/is empty/has errors
     */
    fun <T> loadJsonFromFile(
        file: File,
        type: Type,
        gson: Gson,
        logPrefix: String
    ): T? {
        try {
            if (!file.exists() || !file.canRead()) {
                FeeshMod.LOGGER.info("[Feesh] $logPrefix file does not exist, will create with defaults")
                return null
            }
            
            val content = file.readText()
            if (content.isBlank()) {
                FeeshMod.LOGGER.info("[Feesh] $logPrefix file is empty, will create with defaults")
                return null
            }
            
            return gson.fromJson<T>(content, type)
        } catch (e: Exception) {
            FeeshMod.LOGGER.error("[Feesh] Failed to load $logPrefix data", e)
            return null
        }
    }
    
    /**
     * Saves data to a JSON file synchronously.
     * @param file The file to write to
     * @param data The data to serialize
     * @param gson The Gson instance to use
     * @param saveLock Lock object for synchronization
     * @param logPrefix Prefix for log messages (e.g., "Catch sounds", "Drop sounds")
     */
    fun <T> saveJsonToFileSync(
        file: File,
        data: T,
        gson: Gson,
        saveLock: Any,
        logPrefix: String
    ) {
        CommonUtils.runWithCatching("Failed to save $logPrefix data") {
            synchronized(saveLock) {
                saveByRename(file, gson.toJson(data))
            }
        }
    }

    /**
     * Saves pre-serialized JSON text to a file synchronously.
     * @param file The file to write to
     * @param jsonText The JSON text to save
     * @param saveLock Lock object for synchronization
     * @param logPrefix Prefix for log messages (e.g., "Catch sounds", "Drop sounds")
     */
    fun saveJsonTextToFileSync(
        file: File,
        jsonText: String,
        saveLock: Any,
        logPrefix: String
    ) {
        CommonUtils.runWithCatching("Failed to save $logPrefix data") {
            synchronized(saveLock) {
                saveByRename(file, jsonText)
            }
        }
    }

    /** Writes [text] to a sibling draft, then renames that draft onto [file]. */
    private fun saveByRename(file: File, text: String) {
        val parent = file.parentFile ?: throw IOException("JSON file has no parent directory: ${file.path}")
        if (!parent.exists() && !parent.mkdirs()) {
            throw IOException("Failed to create directory: ${parent.path}")
        }
        removeOldDrafts(parent, file.name)

        val tempFile = Files.createTempFile(parent.toPath(), "${file.name}.", ".tmp")
        try {
            Files.write(tempFile, text.toByteArray(Charsets.UTF_8))
            retryRename(tempFile, file.toPath())
        } catch (e: Exception) {
            try {
                Files.deleteIfExists(tempFile)
            } catch (deleteError: IOException) {
                e.addSuppressed(deleteError)
            }
            throw e
        }
    }

    private fun retryRename(source: Path, target: Path) {
        val maxAttempts = 5
        var lastError: IOException? = null
        for (attempt in 1..maxAttempts) {
            try {
                renameDraft(source, target)
                return
            } catch (e: IOException) {
                lastError = e
                if (attempt == maxAttempts) break
                try {
                    Thread.sleep(50L * attempt)
                } catch (interrupted: InterruptedException) {
                    Thread.currentThread().interrupt()
                    throw IOException("Interrupted while replacing ${target.fileName}", interrupted)
                }
            }
        }
        throw lastError ?: IOException("Failed to replace ${target.fileName}")
    }

    private fun renameDraft(source: Path, target: Path) {
        try {
            Files.move(
                source,
                target,
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE
            )
        } catch (_: AtomicMoveNotSupportedException) {
            if (!loggedPlainRename) {
                loggedPlainRename = true
                FeeshMod.LOGGER.warn(
                    "[Feesh] This drive cannot rename ${target.fileName} in one step. Using a plain replace instead."
                )
            }
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING)
        }
    }

    private fun removeOldDrafts(parent: File, fileName: String) {
        val prefix = "$fileName."
        parent.listFiles()?.forEach { candidate ->
            if (candidate.isFile && candidate.name.startsWith(prefix) && candidate.name.endsWith(".tmp")) {
                candidate.delete()
            }
        }
    }

    /**
     * Overwrites a JSON file in place. Used for periodic saves.
     */
    fun <T> overwriteJsonFile(
        file: File,
        data: T,
        gson: Gson,
        saveLock: Any,
        logPrefix: String
    ) {
        CommonUtils.runWithCatching("Failed to save $logPrefix data") {
            synchronized(saveLock) {
                writeJsonBytes(file, gson.toJson(data).toByteArray(Charsets.UTF_8))
            }
        }
    }

    /**
     * Overwrites a JSON file in place with text that is already serialized.
     */
    fun overwriteJsonText(
        file: File,
        jsonText: String,
        saveLock: Any,
        logPrefix: String
    ) {
        CommonUtils.runWithCatching("Failed to save $logPrefix data") {
            synchronized(saveLock) {
                writeJsonBytes(file, jsonText.toByteArray(Charsets.UTF_8))
            }
        }
    }

    private fun writeJsonBytes(file: File, bytes: ByteArray) {
        file.parentFile?.mkdirs()
        Files.write(
            file.toPath(),
            bytes,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.WRITE
        )
    }

    /**
     * Saves data to a JSON file asynchronously by overwriting the file in place.
     */
    fun <T> saveJsonToFileAsync(
        file: File,
        data: T,
        gson: Gson,
        executor: Executor,
        saveLock: Any,
        logPrefix: String
    ) {
        CompletableFuture.runAsync({
            overwriteJsonFile(file, data, gson, saveLock, logPrefix)
        }, executor)
    }

    /**
     * Saves pre-serialized JSON text to a file asynchronously by overwriting the file in place.
     */
    fun saveJsonTextToFileAsync(
        file: File,
        jsonText: String,
        executor: Executor,
        saveLock: Any,
        logPrefix: String
    ) {
        CompletableFuture.runAsync({
            overwriteJsonText(file, jsonText, saveLock, logPrefix)
        }, executor)
    }
}
