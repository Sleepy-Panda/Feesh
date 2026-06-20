pluginManagement {
    repositories {
        maven("https://maven.deftu.dev/releases")
        maven("https://maven.deftu.dev/snapshots")

        maven("https://jitpack.io/")
        maven("https://maven.fabricmc.net")
        maven("https://maven.architectury.dev/")
        maven("https://maven.minecraftforge.net")

        maven("https://repo.essential.gg/repository/maven-public")

        mavenLocal()
        mavenCentral()

        gradlePluginPortal()
    }

    plugins {
        kotlin("jvm") version("2.3.0")
        id("dev.deftu.gradle.multiversion-root") version("2.73.0")
        id("net.fabricmc.fabric-loom") version("1.17.11")
        id("net.fabricmc.fabric-loom-remap") version("1.17.11")
    }
}

rootProject.buildFileName = "root.gradle.kts"

listOf(
    "1.21.10-fabric",
    "1.21.11-fabric",
).forEach { version ->
    include(":$version")
    project(":$version").apply {
        projectDir = file("versions/$version")
        buildFileName = "../../build.remap.gradle.kts"
    }
}

include(":26.1-fabric")
project(":26.1-fabric").apply {
    projectDir = file("versions/26.1-fabric")
}
