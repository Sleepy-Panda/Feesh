import dev.deftu.gradle.utils.version.MinecraftVersions

plugins {
    java
    kotlin("jvm")
    kotlin("plugin.serialization") version "2.2.21"

    id("dev.deftu.gradle.multiversion") // Applies preprocessing for multiple versions of Minecraft and/or multiple mod loaders.
    id("dev.deftu.gradle.tools") // Applies several configurations to things such as the Java version, project name/version, etc.
    id("dev.deftu.gradle.tools.resources") // Applies resource processing so that we can replace tokens, such as our mod name/version, in our resources.
    id("dev.deftu.gradle.tools.bloom") // Applies the Bloom plugin, which allows us to replace tokens in our source files, such as being able to use `@MOD_VERSION` in our source files.
    id("dev.deftu.gradle.tools.shadow") // Applies the Shadow plugin, which allows us to shade our dependencies into our mod JAR. This is NOT recommended for Fabric mods, but we have an *additional* configuration for those!
    id("dev.deftu.gradle.tools.minecraft.loom") // Applies the Loom plugin, which automagically configures Essential's Architectury Loom plugin for you.
    id("dev.deftu.gradle.tools.minecraft.releases") // Applies the Minecraft auto-releasing plugin, which allows you to automatically release your mod to CurseForge and Modrinth.
}

repositories {
    //maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
    maven("https://repo.essential.gg/repository/maven-public")
    maven("https://maven.teamresourceful.com/repository/maven-public/")
    //maven("https://maven.terraformersmc.com/")
    //maven("https://maven.azureaaron.net/releases")
}

toolkitMultiversion {
    moveBuildsToRootProject.set(true)
}

dependencies {
    modImplementation("net.fabricmc:fabric-language-kotlin:${mcData.dependencies.fabric.fabricLanguageKotlinVersion}")

    //modImplementation(include("gg.essential:elementa:${property("elementa.version")}")!!)
   // modImplementation(include("net.azureaaron:hm-api:${property("hmapi.version")}")!!)
    modImplementation(include("com.teamresourceful.resourcefulconfigkt:resourcefulconfigkt-fabric-1.21.5:${property("rconfig.version.1.21.5")}")!!)

    // modImplementation(include("xyz.meowing:vexel-${mcData}:${property("vexel.version")}")!!)
    when (mcData.version) {
        MinecraftVersions.VERSION_1_21_10 -> {
            modImplementation("net.fabricmc.fabric-api:fabric-api:0.138.3+1.21.10")
            modImplementation("com.terraformersmc:modmenu:${property("modmenu.version.1.21.10")}")
            modImplementation(include("com.teamresourceful.resourcefulconfig:resourcefulconfig-fabric-1.21.9:${property("rconfig.version.1.21.10")}")!!)
            //modImplementation(include("gg.essential:universalcraft-1.21.9-fabric:${property("uc.version")}")!!)
        }
        else -> {}
    }

    //runtimeOnly("me.djtheredstoner:DevAuth-fabric:${property("devauth.version")}")
}

//tasks.findByName("preprocessCode")?.apply {
//    when (mcData.version) {
//        MinecraftVersions.VERSION_1_21_10 -> dependsOn(":1.21.7-fabric:kspKotlin")
//        else -> {}
//    }
//}
