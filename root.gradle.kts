plugins {
    id("dev.deftu.gradle.multiversion-root") version "2.73.0"
}

preprocess {
    strictExtraMappings.set(true)

    val fabric261 = createNode("26.1-fabric", 26_01_00, "mojang")
    val fabric12111 = createNode("1.21.11-fabric", 1_21_11, "mojang")
    val fabric12110 = createNode("1.21.10-fabric", 1_21_10, "mojang")

    fabric261.link(fabric12111)
    fabric12111.link(fabric12110)
}
