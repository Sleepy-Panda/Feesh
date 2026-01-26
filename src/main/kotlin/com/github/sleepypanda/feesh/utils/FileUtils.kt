package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.FeeshMod
import com.google.gson.Gson
import java.io.File
import java.lang.reflect.Type
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

object FileUtils {
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
     * Saves data to a JSON file asynchronously.
     * @param file The file to write to
     * @param data The data to serialize
     * @param gson The Gson instance to use
     * @param executor The executor for async operation
     * @param saveLock Lock object for synchronization
     * @param logPrefix Prefix for log messages (e.g., "Catch sounds", "Drop sounds")
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
            try {
                synchronized(saveLock) {
                    file.parentFile?.mkdirs()
                                        
                    val json = gson.toJson(data)
                    Files.write(
                        file.toPath(),
                        json.toByteArray(),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING,
                        StandardOpenOption.WRITE
                    )
                }
            } catch (e: Exception) {
                FeeshMod.LOGGER.error("[Feesh] Failed to save $logPrefix data", e)
            }
        }, executor)
    }
}
