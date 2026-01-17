package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.FeeshMod
import java.util.Hashtable
import javax.naming.directory.Attribute
import javax.naming.directory.InitialDirContext
import javax.naming.NamingException

object SrvUtils {
    data class SrvRecord(val priority: Int, val weight: Int, val port: Int, val target: String)

    private fun extractDomain(address: String?): String? {
        if (address.isNullOrBlank()) return null

        // Remove port number if present
        val domain = address.substringBefore(":")

        if (!domain.contains(".") || domain.startsWith(".") || domain.endsWith(".")) {
            return null
        }

        return domain
    }

    private fun resolveMinecraftSrv(domain: String?): SrvRecord? {
        val cleanDomain = extractDomain(domain) ?: run {
            FeeshMod.LOGGER.warn("[Feesh] Invalid domain format: $domain")
            return null
        }

        val env = Hashtable<String, String>().apply {
            put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory")
            put("java.naming.provider.url", "dns:")
            put("com.sun.jndi.dns.timeout.initial", "5000")
            put("com.sun.jndi.dns.timeout.retries", "1")
        }

        return try {
            val ctx = InitialDirContext(env)
            try {
                val queryDomain = "_minecraft._tcp.$cleanDomain"
                FeeshMod.LOGGER.info("[Feesh] Querying SRV record for: $queryDomain")

                val srvAttribute: Attribute = ctx.getAttributes(queryDomain, arrayOf("SRV")).get("SRV")
                    ?: run {
                        FeeshMod.LOGGER.warn("[Feesh] No SRV record found for: $cleanDomain")
                        return null
                    }

                // Getting the first SRV record
                val rawRecord = srvAttribute.get()?.toString() ?: run {
                    FeeshMod.LOGGER.warn("[Feesh] SRV attribute is empty for: $cleanDomain")
                    return null
                }

                FeeshMod.LOGGER.info("[Feesh] Raw SRV record: $rawRecord")

                // Parse SRV record format: "priority weight port target."
                val parts = rawRecord.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }

                if (parts.size >= 4) {
                    val record = SrvRecord(
                        priority = parts[0].toIntOrNull() ?: 0,
                        weight = parts[1].toIntOrNull() ?: 0,
                        port = parts[2].toIntOrNull() ?: 25565,
                        target = parts[3].removeSuffix(".")
                    )
                    FeeshMod.LOGGER.info("[Feesh] Parsed SRV record: $record")
                    record
                } else {
                    FeeshMod.LOGGER.warn("[Feesh] Invalid SRV record format. Expected 4 parts, got ${parts.size}: $parts")
                    null
                }
            } finally {
                ctx.close()
            }
        } catch (e: NamingException) {
            FeeshMod.LOGGER.error("[Feesh] DNS lookup failed for $cleanDomain: ${e.message}")
            null
        } catch (e: NumberFormatException) {
            FeeshMod.LOGGER.error("[Feesh] Failed to parse SRV record numbers for $cleanDomain: ${e.message}")
            null
        } catch (e: Exception) {
            FeeshMod.LOGGER.error("[Feesh] Unexpected error resolving SRV for $cleanDomain: ${e.javaClass.simpleName}: ${e.message}", e)
            null
        }
    }

    fun isHypixel(domain: String?): Boolean {
        if (domain.isNullOrBlank()) {
            FeeshMod.LOGGER.warn("[Feesh] isHypixel called with null/blank domain")
            return false
        }

        FeeshMod.LOGGER.info("[Feesh] Checking if domain is Hypixel: $domain")

        val srvRecord = resolveMinecraftSrv(domain)
        if (srvRecord != null && srvRecord.target.contains("hypixel", ignoreCase = true)) {
            FeeshMod.LOGGER.info("[Feesh] SRV lookup result: ${srvRecord.target} -> isHypixel: true")
            return true
        }

        // Fallback: check the domain directly
        val cleanDomain = extractDomain(domain) ?: return false
        val isHypixel = cleanDomain.contains("hypixel", ignoreCase = true)
        FeeshMod.LOGGER.info("[Feesh] Fallback domain check: $cleanDomain -> isHypixel: $isHypixel")
        return isHypixel
    }
}
