plugins {
    id("net.fabricmc.fabric-loom") apply false // "apply false" puts loom/kotlin on the root buildscript classpath so the preprocessor can reference their types
    kotlin("jvm") apply false
    id("com.replaymod.preprocess-root") // Preprocessing for multiple versions of Minecraft and/or mod loaders.
}

preprocess {
    // Mappings are null: all targets are unobfuscated (fabric.loom.disableObfuscation=true),
    // so only comment preprocessing is needed, no name remapping between versions.
    val fabric_26_3 = createNode("26.3-fabric", 26_03_00, null)
    val fabric_26_2 = createNode("26.2-fabric", 26_02_00, null)
    val fabric_26_1 = createNode("26.1-fabric", 26_01_00, null)

    fabric_26_3.link(fabric_26_2)
    fabric_26_2.link(fabric_26_1)
}
