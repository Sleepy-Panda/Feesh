import net.fabricmc.loom.task.RemapJarTask
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import java.lang.module.ModuleDescriptor.Version

plugins {
    java
    kotlin("jvm")
    kotlin("plugin.serialization") version "2.3.0"
    id("net.fabricmc.fabric-loom-remap") version "1.17.11"
    id("dev.deftu.gradle.multiversion")
    id("dev.deftu.gradle.tools.bloom")
}

private val mcProject: String = project.name
private val mcVersion: String = mcProject.removeSuffix("-fabric")

private fun versionedProperty(name: String): String {
    return project.findProperty("${name}.${mcVersion}")?.toString()
        ?: throw AssertionError("build.gradle.kts needs updating for $mcProject — missing ${name}.${mcVersion}")
}

private fun isUnobfuscatedMCVersion(): Boolean =
    Version.parse(mcVersion) >= Version.parse("26.1")

configurations.all {
    resolutionStrategy {
        force("org.jetbrains.kotlin:kotlin-stdlib:2.3.0")
    }
}

repositories {
    maven("https://repo.essential.gg/repository/maven-public")
    maven("https://maven.teamresourceful.com/repository/maven-public/")
}

val jarName = project.property("mod.name").toString() + "-" + project.property("mod.version").toString() + "+" + mcProject

if (isUnobfuscatedMCVersion()) {
    val nestedJars = configurations.create("nestedJars") {
        isCanBeResolved = true
        isCanBeConsumed = false
        extendsFrom(configurations.getByName("include"))
    }

    tasks.named<Jar>("jar").configure {
        from(nestedJars) {
            into("META-INF/jars")
            include("resourcefulconfig-*.jar")
            include("resourcefulconfigkt-*.jar")
        }
    }

    tasks.named<Copy>("processResources").configure {
        doLast {
            val fmj = destinationDir.resolve("fabric.mod.json")
            if (!fmj.exists() || fmj.readText().contains("\"jars\":")) return@doLast

            val nestedFiles = nestedJars.resolve()
                .filter { it.name.startsWith("resourcefulconfig-") || it.name.startsWith("resourcefulconfigkt-") }
                .map { it.name }
                .sorted()
            if (nestedFiles.isEmpty()) return@doLast

            val jarsBlock = buildString {
                append("\t\"jars\": [\n")
                nestedFiles.forEachIndexed { index, name ->
                    append("\t\t{ \"file\": \"META-INF/jars/$name\" }")
                    if (index < nestedFiles.size - 1) append(",")
                    append("\n")
                }
                append("\t],\n")
            }

            val original = fmj.readText()
            val anchor = "\t\"depends\":"
            fmj.writeText(
                if (original.contains(anchor)) original.replace(anchor, jarsBlock + anchor)
                else original.replaceFirst(Regex("(?m)^\\s*\"depends\":"), jarsBlock + "\t\"depends\":")
            )
        }
    }
}

afterEvaluate {
    val outputDir = rootProject.layout.buildDirectory.asFile.get().resolve("versions")

    tasks.named<Jar>("jar").configure {
        destinationDirectory.set(outputDir)
        archiveBaseName.set(jarName)
    }

    if (!isUnobfuscatedMCVersion()) {
        tasks.named<RemapJarTask>("remapJar").configure {
            destinationDirectory.set(outputDir)
            archiveBaseName.set(jarName)
        }
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(versionedProperty("java.version").toInt())
}

tasks.withType<KotlinJvmCompile>().configureEach {
    compilerOptions.jvmTarget.set(JvmTarget.fromTarget(versionedProperty("java.version")))
}

tasks.named<Copy>("processResources") {
    val expandedFiles = listOf("fabric.mod.json", "feesh.mixins.json")

    val modName = project.property("mod.name")
    val modId = project.property("mod.id")
    val modVersion = project.property("mod.version")
    val javaVersion = versionedProperty("java.version")

    inputs.property("mod_name", modName)
    inputs.property("mod_id", modId)
    inputs.property("mod_version", modVersion)
    inputs.property("minor_mc_version", mcVersion)
    inputs.property("java_version", javaVersion)

    filesMatching(expandedFiles) {
        expand(
            mapOf(
                "mod_name" to modName,
                "mod_id" to modId,
                "mod_version" to modVersion,
                "minor_mc_version" to mcVersion,
                "java_version" to javaVersion,
            )
        )
    }
}

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")

    configurations.findByName("mappings")?.let { mappingsConfig ->
        add(mappingsConfig.name, loom.officialMojangMappings())
    }

    val modImplementationConfig = configurations.findByName("modImplementation")

    fun org.gradle.api.artifacts.dsl.DependencyHandler.maybeModImplementation(dependencyNotation: Any) {
        if (modImplementationConfig != null) {
            add(modImplementationConfig.name, dependencyNotation)
        } else {
            implementation(dependencyNotation)
        }
    }

    maybeModImplementation("net.fabricmc:fabric-loader:${property("fabricloader.version")}")
    maybeModImplementation("net.fabricmc.fabric-api:fabric-api:${versionedProperty("fabric-api.version")}")
    implementation("net.fabricmc:fabric-language-kotlin:${property("fabriclanguagekotlin.version")}")

    when (mcProject) {
        "1.21.10-fabric" -> {
            maybeModImplementation(include("com.teamresourceful.resourcefulconfigkt:resourcefulconfigkt-fabric-1.21.5:${property("resourcefulconfig-kt.version.1.21.10")}")!!)
            maybeModImplementation(include("com.teamresourceful.resourcefulconfig:resourcefulconfig-fabric-1.21.9:${property("resourcefulconfig.version.1.21.10")}")!!)
        }
        "1.21.11-fabric" -> {
            maybeModImplementation(include("com.teamresourceful.resourcefulconfigkt:resourcefulconfigkt-fabric-1.21.5:${property("resourcefulconfig-kt.version.1.21.11")}")!!)
            maybeModImplementation(include("com.teamresourceful.resourcefulconfig:resourcefulconfig-fabric-1.21.11:${property("resourcefulconfig.version.1.21.11")}")!!)
        }
        "26.1-fabric" -> {
            implementation("net.fabricmc:sponge-mixin:0.17.0+mixin.0.8.7")
            implementation(include("com.teamresourceful.resourcefulconfig:resourcefulconfig-fabric-26.1:${property("resourcefulconfig.version.26.1")}")!!)
            implementation(include("com.teamresourceful.resourcefulconfigkt:resourcefulconfigkt-26.1-rc-1:${property("resourcefulconfig-kt.version.26.1-rc-1")}")!!)
        }
        else -> throw AssertionError("build.gradle.kts needs updating for $mcProject")
    }
}
