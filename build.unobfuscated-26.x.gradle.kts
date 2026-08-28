import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.tasks.Jar
import org.gradle.language.jvm.tasks.ProcessResources
import java.io.File
import java.net.URLClassLoader

plugins {
    java
    kotlin("jvm")
    kotlin("plugin.serialization") version "2.3.0"
    id("dev.deftu.gradle.multiversion")
    id("dev.deftu.gradle.tools.bloom")
    id("net.fabricmc.fabric-loom") version "1.17.11"
}

val mcProject = project.name
val mcVersion = mcProject.removeSuffix("-fabric")

fun propertyByMcVersion(name: String): String =
    findProperty("$name.$mcVersion")?.toString()
        ?: error("Missing property $name.$mcVersion for $mcProject")

val javaVersion = propertyByMcVersion("java.version")
val jarName = "${property("mod.name")}-${property("mod.version")}+$mcProject"
val modTargetConfiguration =
    if (configurations.findByName("modImplementation") != null) "modImplementation" else "implementation"

fun includeDependency(notation: String, targetConfiguration: String) {
    dependencies.add("include", notation)
    dependencies.add(targetConfiguration, notation)
}

configurations.configureEach {
    resolutionStrategy {
        force("org.jetbrains.kotlin:kotlin-stdlib:2.3.0")
    }
}

repositories {
    maven("https://maven.fabricmc.net/")
    maven("https://repo.essential.gg/repository/maven-public")
    maven("https://maven.teamresourceful.com/repository/maven-public/")
}

val includeResolvable: Configuration = configurations.create("includeResolvable") {
    isCanBeResolved = true
    isCanBeConsumed = false
    extendsFrom(configurations.getByName("include"))
}

val fabricLoomJar: Configuration = configurations.create("fabricLoomJar") {
    isCanBeResolved = true
    isCanBeConsumed = false
}

dependencies {
    fabricLoomJar("net.fabricmc:fabric-loom:1.17.11")
    add("minecraft", "com.mojang:minecraft:$mcVersion")
    add(modTargetConfiguration, "net.fabricmc:fabric-loader:${property("fabricloader.version")}")
    add(modTargetConfiguration, "net.fabricmc.fabric-api:fabric-api:${propertyByMcVersion("fabric-api.version")}")
    add("implementation", "net.fabricmc:fabric-language-kotlin:${property("fabriclanguagekotlin.version")}")
    add("implementation", "net.fabricmc:sponge-mixin:0.17.0+mixin.0.8.7")
    when (mcProject) {
        "26.1-fabric" -> {
            includeDependency(
                "com.teamresourceful.resourcefulconfig:resourcefulconfig-fabric-26.1:${property("resourcefulconfig.version.26.1")}",
                "implementation",
            )
            includeDependency(
                "com.teamresourceful.resourcefulconfigkt:resourcefulconfigkt-26.1-rc-1:${property("resourcefulconfig-kt.version.26.1-rc-1")}",
                "implementation",
            )
        }
        "26.2-fabric" -> {
            includeDependency(
                "com.teamresourceful.resourcefulconfig:resourcefulconfig-fabric-26.2:${property("resourcefulconfig.version.26.2")}",
                "implementation",
            )
            includeDependency(
                "com.teamresourceful.resourcefulconfigkt:resourcefulconfigkt-26.1-rc-1:${property("resourcefulconfig-kt.version.26.2-rc-1")}",
                "implementation",
            )
        }
        else -> error("Unsupported Minecraft project $mcProject")
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(javaVersion.toInt())
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile>().configureEach {
    compilerOptions.jvmTarget.set(
        org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget(javaVersion)
    )
}

tasks.named<ProcessResources>("processResources").configure {
    val expandedFiles = listOf("fabric.mod.json", "feesh.mixins.json")
    val modName = project.property("mod.name")
    val modId = project.property("mod.id")
    val modVersion = project.property("mod.version")
    val modDescription = project.property("mod.description")
    val mixinCompatibilityLevel = "JAVA_$javaVersion"

    inputs.property("mod_name", modName)
    inputs.property("mod_id", modId)
    inputs.property("mod_version", modVersion)
    inputs.property("mod_description", modDescription)
    inputs.property("minor_mc_version", mcVersion)
    inputs.property("java_version", mixinCompatibilityLevel)
    inputs.property("mod_java_version", javaVersion)

    filesMatching(expandedFiles) {
        expand(
            mapOf(
                "mod_name" to modName,
                "mod_id" to modId,
                "mod_version" to modVersion,
                "mod_description" to modDescription,
                "minor_mc_version" to mcVersion,
                "java_version" to mixinCompatibilityLevel,
                "mod_java_version" to javaVersion,
            )
        )
    }
}

tasks.named<Jar>("jar").configure {
    destinationDirectory.set(rootProject.layout.buildDirectory.dir("versions"))
    archiveBaseName.set(jarName)
    doLast {
        val nestedJars = includeResolvable.files.filter { it.name.contains("resourcefulconfig", ignoreCase = true) }
        if (nestedJars.isEmpty()) return@doLast

        val loader = URLClassLoader(fabricLoomJar.files.map { it.toURI().toURL() }.toTypedArray(), javaClass.classLoader)
        val jarNester = loader.loadClass("net.fabricmc.loom.build.nesting.JarNester")
        jarNester.getMethod("nestJars", Collection::class.java, File::class.java)
            .invoke(null, nestedJars, archiveFile.get().asFile)
    }
}
