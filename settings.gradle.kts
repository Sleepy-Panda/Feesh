pluginManagement {
    val preprocessorVersion = providers.gradleProperty("preprocessor.version").get()

    plugins {
        kotlin("jvm") version("2.3.0")
        // https://github.com/ReplayMod/preprocessor
        id("com.replaymod.preprocess-root") version(preprocessorVersion) // Applies preprocessing for multiple versions of Minecraft and/or multiple mod loaders.
        id("net.fabricmc.fabric-loom") version("1.17.11")
    }

    repositories {
        gradlePluginPortal()

        maven("https://jitpack.io/")
        maven("https://maven.fabricmc.net")

        mavenLocal()
        mavenCentral()
    }

    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.replaymod.preprocess-root") {
                useModule("com.github.replaymod:preprocessor:$preprocessorVersion")
            }
        }
    }
}

rootProject.buildFileName = "root.gradle.kts"

listOf(
    "26.1-fabric",
    "26.2-fabric",
    "26.3-fabric",
).forEach { version ->
    include(":$version")
    project(":$version").apply {
        projectDir = file("versions/$version")
        buildFileName = "../../build.unobfuscated-26.x.gradle.kts"
    }
}
