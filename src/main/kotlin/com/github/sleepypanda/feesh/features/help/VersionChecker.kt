package com.github.sleepypanda.feesh.features.help

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.WorldChangedEvent
import com.github.sleepypanda.feesh.utils.ChatUtils
import com.github.sleepypanda.feesh.utils.CommonUtils
import com.github.sleepypanda.feesh.utils.enums.ColorCodes.*
import com.github.sleepypanda.feesh.utils.enums.FormattingCodes.*
import com.google.gson.JsonParser
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors

private const val MODRINTH_MOD_ID = "feesh"
private const val MODRINTH_VERSIONS_PAGE = "https://modrinth.com/mod/$MODRINTH_MOD_ID/versions"

// https://docs.modrinth.com/api/operations/getprojectversions/
private const val MODRINTH_VERSIONS_URL = "https://api.modrinth.com/v2/project/$MODRINTH_MOD_ID/version"
private val GAME_VERSION: String = FeeshMod.mcVersion
private val GAME_VERSIONS = """["$GAME_VERSION"]"""
private const val LOADERS = """["fabric"]"""
private const val REQUEST_TIMEOUT_MS = 10000

object VersionChecker {
    var cachedLatestVersion: String? = null
        private set

    var isLatestVersion: Boolean = true
        private set

    private val executor = Executors.newSingleThreadExecutor { r ->
        Thread(r, "Feesh-Version-Check").apply { isDaemon = true }
    }

    private var hasChecked = false

    fun init() {
        EventBus.subscribe(WorldChangedEvent::class, ::onWorldChanged)
    }

    private fun onWorldChanged(@Suppress("UNUSED_PARAMETER") event: WorldChangedEvent) {
        if (FeeshMod.mc.player == null || FeeshMod.mc.gui == null) return
        if (hasChecked) return

        hasChecked = true
        checkForNewVersion()
    }

    private fun checkForNewVersion() {
        executor.execute {
            CommonUtils.runWithCatching("Failed to check new Modrinth version") {
                // https://api.modrinth.com/v2/project/feesh/version?game_versions=%5B%221.21.10%22%5D&loaders=%5B%22fabric%22%5D&include_changelog=false
                val query = "game_versions=${URLEncoder.encode(GAME_VERSIONS, StandardCharsets.UTF_8)}&loaders=${URLEncoder.encode(LOADERS, StandardCharsets.UTF_8)}&include_changelog=false"
                val url = URI("$MODRINTH_VERSIONS_URL?$query").toURL()
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = REQUEST_TIMEOUT_MS
                connection.readTimeout = REQUEST_TIMEOUT_MS
                connection.setRequestProperty("User-Agent", "Feesh/${FeeshMod.version}")

                val responseCode = connection.responseCode
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    connection.disconnect()
                    FeeshMod.LOGGER.error("[Feesh] Version check on Modrinth failed: HTTP response code is $responseCode, message: ${connection.responseMessage}")
                    return@execute
                }

                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                reader.close()
                connection.disconnect()

                val versions = JsonParser.parseString(response).asJsonArray
                if (versions.size() == 0) {
                    FeeshMod.LOGGER.error("[Feesh] Version check on Modrinth failed: No versions found")
                    return@execute
                }

                val latestVersion = versions[0]?.asJsonObject ?: return@execute
                val latestVersionNumber = latestVersion.get("version_number")?.asString ?: return@execute

                FeeshMod.mc.execute {
                    cachedLatestVersion = latestVersionNumber
                }
                
                val currentVersion = FeeshMod.version

                if (isNewerVersion(latestVersionNumber, currentVersion)) {
                    FeeshMod.mc.execute {
                        isLatestVersion = false
                        showNewVersionMessage(currentVersion, latestVersionNumber)
                    }
                }
            }
        }
    }

    /**
     * Returns true if `remote` is a newer version than `current`.
     * Handles semver with pre-release suffixes (e.g. 1.0.0-beta, 1.1.0-alpha).
     */
    private fun isNewerVersion(remote: String, current: String): Boolean {
        val remoteParts = parseVersion(remote) ?: return false
        val currentParts = parseVersion(current) ?: return false

        val (rMajor, rMinor, rPatch, rPre) = remoteParts
        val (cMajor, cMinor, cPatch, cPre) = currentParts

        return when {
            rMajor != cMajor -> rMajor > cMajor
            rMinor != cMinor -> rMinor > cMinor
            rPatch != cPatch -> rPatch > cPatch
            cPre.isNotEmpty() && rPre.isEmpty() -> true  // 1.1.0 > 1.1.0-alpha
            cPre.isEmpty() && rPre.isNotEmpty() -> false // 1.1.0-alpha < 1.1.0
            else -> rPre.isNotEmpty() && cPre.isNotEmpty() && preReleaseOrder(rPre) > preReleaseOrder(cPre)
        }
    }

    private fun parseVersion(version: String): VersionParts? {
        val base = version.split("-", "+").firstOrNull() ?: return null
        val preRelease = version.substringAfter("-", "").substringBefore("+", "").trim()
        val parts = base.split(".").mapNotNull { it.toIntOrNull() }
        if (parts.size < 3) return null
        return VersionParts(parts[0], parts[1], parts[2], preRelease)
    }

    // 1.0.0-alpha < 1.0.0-beta < 1.0.0-rc < 1.0.0
    private fun preReleaseOrder(pre: String): Int = when {
        pre.startsWith("alpha", ignoreCase = true) -> 0
        pre.startsWith("beta", ignoreCase = true) -> 1
        pre.startsWith("rc", ignoreCase = true) -> 2
        else -> -1
    }

    private data class VersionParts(val major: Int, val minor: Int, val patch: Int, val preRelease: String)

    private fun showNewVersionMessage(current: String, latest: String) {
        val chatBreak = "${GRAY}${ChatUtils.getChatBreak("-")}"
        ChatUtils.sendLocalChat(chatBreak)

        val message = "${YELLOW}New version available: ${WHITE}${BOLD}$latest\n${YELLOW}Your current version is ${WHITE}${BOLD}$current"
        val linkText = "${GREEN}${UNDERLINE}Open on Modrinth"
        ChatUtils.sendLocalChatWithUrl(message, linkText, MODRINTH_VERSIONS_PAGE, true)
        ChatUtils.sendLocalChat(chatBreak)
    }
}
