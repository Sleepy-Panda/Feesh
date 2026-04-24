import dev.deftu.gradle.utils.version.MinecraftVersions
import org.gradle.api.attributes.java.TargetJvmVersion
import org.gradle.api.tasks.compile.JavaCompile

plugins {
    java
    kotlin("jvm")
    kotlin("plugin.serialization") version "2.3.0"

    // https://github.com/Deftu/Gradle-Toolkit
    id("dev.deftu.gradle.multiversion") // Applies preprocessing for multiple versions of Minecraft and/or multiple mod loaders.
    id("dev.deftu.gradle.tools") // Applies several configurations to things such as the Java version, project name/version, etc.
    id("dev.deftu.gradle.tools.resources") // Applies resource processing so that we can replace tokens, such as our mod name/version, in our resources.
    id("dev.deftu.gradle.tools.bloom") // Applies the Bloom plugin, which allows us to replace tokens in our source files, such as being able to use `@MOD_VERSION` in our source files.
    id("dev.deftu.gradle.tools.shadow") // Applies the Shadow plugin, which allows us to shade our dependencies into our mod JAR. This is NOT recommended for Fabric mods, but we have an *additional* configuration for those!
    id("dev.deftu.gradle.tools.minecraft.loom") // Applies the Loom plugin, which automagically configures Essential's Architectury Loom plugin for you.
    id("dev.deftu.gradle.tools.minecraft.releases") // Applies the Minecraft auto-releasing plugin, which allows you to automatically release your mod to CurseForge and Modrinth.
}

configurations.all {
    resolutionStrategy {
        force("org.jetbrains.kotlin:kotlin-stdlib:2.3.0")
    }
}

repositories {
    maven("https://repo.essential.gg/repository/maven-public")
    maven("https://maven.teamresourceful.com/repository/maven-public/")
}

toolkitMultiversion {
    moveBuildsToRootProject.set(true)
}

if (mcData.version == MinecraftVersions.VERSION_26_1) {
    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(25))
        }
        sourceCompatibility = JavaVersion.VERSION_25
        targetCompatibility = JavaVersion.VERSION_25
    }
    kotlin {
        jvmToolchain(25)
    }
    tasks.withType<JavaCompile>().configureEach {
        options.release.set(25)
    }
    configurations.configureEach {
        attributes.attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 25)
    }
}

dependencies {
    compileOnly("org.spongepowered:mixin:0.8.7")

    when (mcData.version) {
        MinecraftVersions.VERSION_1_21_10 -> {
            maybeModImplementation("net.fabricmc:fabric-language-kotlin:${mcData.dependencies.fabric.fabricLanguageKotlinVersion}")
            maybeModImplementation(include("com.teamresourceful.resourcefulconfigkt:resourcefulconfigkt-fabric-1.21.5:${property("rconfig.version.1.21.5")}")!!)
            maybeModImplementation("net.fabricmc.fabric-api:fabric-api:0.138.3+1.21.10")
            maybeModImplementation(include("com.teamresourceful.resourcefulconfig:resourcefulconfig-fabric-1.21.9:${property("rconfig.version.1.21.10")}")!!)
        }
        MinecraftVersions.VERSION_1_21_11 -> {
            maybeModImplementation("net.fabricmc:fabric-language-kotlin:${mcData.dependencies.fabric.fabricLanguageKotlinVersion}")
            maybeModImplementation(include("com.teamresourceful.resourcefulconfigkt:resourcefulconfigkt-fabric-1.21.5:${property("rconfig.version.1.21.5")}")!!)
            maybeModImplementation("net.fabricmc.fabric-api:fabric-api:0.141.3+1.21.11")
            maybeModImplementation(include("com.teamresourceful.resourcefulconfig:resourcefulconfig-fabric-1.21.11:${property("rconfig.version.1.21.11")}")!!)
        }
        MinecraftVersions.VERSION_26_1 -> {
            implementation("net.fabricmc:fabric-language-kotlin:1.13.10+kotlin.2.3.20")
            implementation("net.fabricmc.fabric-api:fabric-api:0.145.4+26.1.2")
            //implementation("com.teamresourceful.resourcefulconfigkt:resourcefulconfigkt-fabric-1.21.11:3.11.0")
            implementation("com.teamresourceful.resourcefulconfig:resourcefulconfig-fabric-26.1-rc-1:4.0.0-beta.2")
            implementation("com.teamresourceful.resourcefulconfigkt:resourcefulconfigkt-26.1-rc-1:4.0.0-beta.1")
        }
        else -> {}
    }
}
