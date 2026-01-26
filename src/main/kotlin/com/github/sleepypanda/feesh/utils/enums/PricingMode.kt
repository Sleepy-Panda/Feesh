enum class PricingMode(val displayName: String) {
    SELL_OFFER("Sell Offer"),
    INSTA_SELL("Insta Sell");

    override fun toString(): String = displayName // Show display name in UI, but internally it uses name
}

enum class PricingModeWithNpc(val displayName: String) {
    SELL_OFFER("Sell Offer"),
    INSTA_SELL("Insta Sell"),
    NPC_SELL("NPC Sell");

    override fun toString(): String = displayName // Show display name in UI, but internally it uses name
}