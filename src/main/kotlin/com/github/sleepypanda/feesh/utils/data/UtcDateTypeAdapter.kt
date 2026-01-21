package com.github.sleepypanda.feesh.utils.data

import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.Exception
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Gson TypeAdapter for java.util.Date that stores dates in UTC ISO-8601 format:
 * yyyy-MM-dd'T'HH:mm:ss'Z' (without milliseconds)
 * 
 * When deserializing, supports both formats:
 * - yyyy-MM-dd'T'HH:mm:ss'Z' (without milliseconds)
 * - yyyy-MM-dd'T'HH:mm:ss.SSS'Z' (with milliseconds, for backward compatibility)
 */
object UtcDateTypeAdapter : JsonSerializer<Date>, JsonDeserializer<Date> {

    private fun utcDateFormat(): SimpleDateFormat {
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }

    private fun utcDateFormatWithMillis(): SimpleDateFormat {
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }

    override fun serialize(
        src: Date?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        if (src == null) return JsonPrimitive("")
        return JsonPrimitive(utcDateFormat().format(src))
    }

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: com.google.gson.JsonDeserializationContext?
    ): Date {
        if (json == null || json.isJsonNull) return Date(0)
        val str = json.asString
        if (str.isBlank()) return Date(0)

        // Try parsing with milliseconds first (for backward compatibility)
        return try {
            utcDateFormatWithMillis().parse(str) ?: Date(0)
        } catch (e: Exception) {
            // If that fails, try without milliseconds
            try {
                utcDateFormat().parse(str) ?: Date(0)
            } catch (e2: Exception) {
                throw JsonParseException("Failed to parse date: $str", e2)
            }
        }
    }
}

