package com.github.sleepypanda.feesh.utils

import com.github.sleepypanda.feesh.FeeshMod
import com.github.sleepypanda.feesh.events.EventBus
import com.github.sleepypanda.feesh.events.models.PricesUpdatedEvent
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

data class BazaarItemPrice(
    val instaSell: Double,
    val sellOffer: Double
)

data class BazaarApiResponse(
    val success: Boolean,
    val products: Map<String, BazaarProduct>?
)

data class BazaarProduct(
    val sell_summary: List<BazaarSummaryItem>?,
    val buy_summary: List<BazaarSummaryItem>?,
    val quick_status: BazaarQuickStatus?
)

data class BazaarSummaryItem(
    val pricePerUnit: Double
)

data class BazaarQuickStatus(
    val sellPrice: Double?,
    val buyPrice: Double?
)

data class AuctionItemPrice(
    val lbin: Double
)

object PriceUtils {
    private const val BAZAAR_API_URL = "https://api.hypixel.net/skyblock/bazaar"
    private const val AUCTION_API_URL = "https://api.eliteskyblock.com/resources/auctions/neu" // No more often than every 2 minutes
    private const val UPDATE_INTERVAL_MINUTES = 5L
    private const val REQUEST_TIMEOUT_MS = 45000
    
    private var bazaarPrices: MutableMap<String, BazaarItemPrice> = mutableMapOf()
    private var auctionPrices: MutableMap<String, AuctionItemPrice> = mutableMapOf()
    private val pricesLock = Any()
    private val auctionPricesLock = Any()
    private val gson = Gson()
    private var scheduler: ScheduledExecutorService? = null
    private var httpExecutor: ExecutorService? = null
    
    fun init() {
        httpExecutor = Executors.newCachedThreadPool { r ->
            Thread(r, "Feesh-HTTP-Request").apply {
                isDaemon = true
            }
        }
        
        trackAllPrices()
        
        scheduler = Executors.newScheduledThreadPool(1) { r ->
            Thread(r, "Feesh-Price-Tracker").apply {
                isDaemon = true
            }
        }
        
        scheduler?.scheduleAtFixedRate(
            { trackAllPrices() },
            UPDATE_INTERVAL_MINUTES,
            UPDATE_INTERVAL_MINUTES,
            TimeUnit.MINUTES
        )
    }
    
    private fun trackAllPrices() {
        val bazaarFuture = trackBazaarPrices()
        val auctionFuture = trackAuctionPrices()

        // Fire a single aggregated event when at least one source updated successfully.
        CompletableFuture.allOf(bazaarFuture, auctionFuture).thenRun {
            val bazaarUpdated = bazaarFuture.get()
            val auctionUpdated = auctionFuture.get()

            if (bazaarUpdated || auctionUpdated) {
                EventBus.publish(
                    PricesUpdatedEvent(
                        bazaarUpdated = bazaarUpdated,
                        auctionUpdated = auctionUpdated
                    )
                )
            }
        }
    }
    
    fun getBazaarItemPrices(itemId: String?): BazaarItemPrice? {
        if (itemId.isNullOrEmpty()) return null
        
        synchronized(pricesLock) {
            return bazaarPrices[itemId]
        }
    }
    
    fun getAuctionItemPrice(itemId: String?): AuctionItemPrice? {
        if (itemId.isNullOrEmpty()) return null
        
        synchronized(auctionPricesLock) {
            return auctionPrices[itemId]
        }
    }
    
    private fun trackBazaarPrices(): CompletableFuture<Boolean> {
        val executor = httpExecutor ?: return CompletableFuture.completedFuture(false)
        
        return CompletableFuture.supplyAsync({
                try {
                    val url = URI(BAZAAR_API_URL).toURL()
                    val connection = url.openConnection() as HttpURLConnection
                    
                    connection.requestMethod = "GET"
                    connection.connectTimeout = REQUEST_TIMEOUT_MS
                    connection.readTimeout = REQUEST_TIMEOUT_MS
                    
                    val responseCode = connection.responseCode
                    if (responseCode != HttpURLConnection.HTTP_OK) {
                        throw Exception("HTTP error code: $responseCode")
                    }
                    
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = reader.readText()
                    reader.close()
                    connection.disconnect()
                    
                    val bazaarResponse = gson.fromJson(response, BazaarApiResponse::class.java)
                    
                    if (!bazaarResponse.success) {
                        throw Exception("[Feesh] Error loading bazaar data: response contains success = false")
                    }
                    
                    val products = bazaarResponse.products ?: emptyMap()
                    val newPrices = mutableMapOf<String, BazaarItemPrice>()
                    
                    products.forEach { (itemId, product) ->
                        val sellSummary = product.sell_summary
                        val buySummary = product.buy_summary
                        val quickStatus = product.quick_status
                        
                        val instaSell = when {
                            !sellSummary.isNullOrEmpty() -> {
                                sellSummary[0].pricePerUnit
                            }
                            quickStatus != null -> {
                                quickStatus.sellPrice ?: 0.0
                            }
                            else -> 0.0
                        }
                        
                        val sellOffer = when {
                            !buySummary.isNullOrEmpty() -> {
                                buySummary[0].pricePerUnit
                            }
                            quickStatus != null -> {
                                quickStatus.buyPrice ?: 0.0
                            }
                            else -> 0.0
                        }
                        
                        newPrices[itemId] = BazaarItemPrice(instaSell, sellOffer)
                    }
                    
                    synchronized(pricesLock) {
                        bazaarPrices = newPrices
                    }
                    
                    FeeshMod.LOGGER.info("[Feesh] Successfully updated bazaar prices (${newPrices.size} items)")
                    true
                } catch (error: Exception) {
                    FeeshMod.LOGGER.error("[Feesh] Error loading bazaar data: ", error)
                    false
                }
            }, executor)
    }
    
    private fun trackAuctionPrices(): CompletableFuture<Boolean> {
        val executor = httpExecutor ?: return CompletableFuture.completedFuture(false)
        
        return CompletableFuture.supplyAsync({
                try {
                    val url = URI(AUCTION_API_URL).toURL()
                    val connection = url.openConnection() as HttpURLConnection
                    
                    connection.requestMethod = "GET"
                    connection.connectTimeout = REQUEST_TIMEOUT_MS
                    connection.readTimeout = REQUEST_TIMEOUT_MS
                    
                    val responseCode = connection.responseCode
                    if (responseCode != HttpURLConnection.HTTP_OK) {
                        throw Exception("HTTP error code: $responseCode")
                    }
                    
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = reader.readText()
                    reader.close()
                    connection.disconnect()
                    
                    if (response.isBlank()) {
                        throw Exception("[Feesh] Error loading auctions data: response is empty")
                    }
                    
                    val type = object : TypeToken<Map<String, Double>>() {}.type
                    val lowestBinMap = gson.fromJson<Map<String, Double>>(response, type)
                    
                    if (lowestBinMap == null) {
                        throw Exception("[Feesh] Error loading auctions data: response is null")
                    }
                    
                    val newPrices = mutableMapOf<String, AuctionItemPrice>()
                    
                    lowestBinMap.forEach { (itemId, itemLowestBin) ->
                        if (itemLowestBin > 0) {
                            newPrices[itemId] = AuctionItemPrice(lbin = itemLowestBin)
                        }
                    }
                    
                    synchronized(auctionPricesLock) {
                        auctionPrices = newPrices
                    }
                    
                    FeeshMod.LOGGER.info("[Feesh] Successfully updated auction prices (${newPrices.size} items)")
                    true
                } catch (error: Exception) {
                    FeeshMod.LOGGER.error("[Feesh] Error loading auctions data: ", error)
                    false
                }
            }, executor)
    }
    
    fun shutdown() {
        scheduler?.shutdown()
        scheduler = null
        httpExecutor?.shutdown()
        httpExecutor = null
    }
}