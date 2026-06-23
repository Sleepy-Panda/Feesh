plugins {
    id("dev.deftu.gradle.multiversion-root") version "2.73.0" // Applies preprocessing for multiple versions of Minecraft and/or multiple mod loaders.
}

preprocess {
    strictExtraMappings.set(true)

    val fabric_26_2 = createNode("26.2-fabric", 26_02_00, "mojang")
    val fabric_26_1 = createNode("26.1-fabric", 26_01_00, "mojang")
    val fabric_1_21_11 = createNode("1.21.11-fabric", 1_21_11, "mojang")
    val fabric_1_21_10 = createNode("1.21.10-fabric", 1_21_10, "mojang")

    fabric_26_2.link(fabric_26_1)
    fabric_26_1.link(fabric_1_21_11)
    fabric_1_21_11.link(fabric_1_21_10)
}
