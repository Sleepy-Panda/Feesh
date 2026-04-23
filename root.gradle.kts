plugins {
    id("dev.deftu.gradle.multiversion-root")
}

preprocess {
    strictExtraMappings.set(true)
    
    "26.1-fabric"(26_1, "mojang") {
        "1.21.11-fabric"(1_21_11, "mojang") {
            "1.21.10-fabric"(1_21_10, "mojang")
        }
    }
}