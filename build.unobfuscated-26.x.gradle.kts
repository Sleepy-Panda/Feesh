import org.gradle.api.artifacts.Configuration
import org.gradle.jvm.tasks.Jar
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

apply(from = rootProject.file("build.common.gradle"))

repositories {
    maven("https://maven.fabricmc.net/")
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
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile>().configureEach {
    compilerOptions.jvmTarget.set(
        org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget(
            project.findProperty("java.version.${project.name.removeSuffix("-fabric")}")!!.toString()
        )
    )
}

tasks.named<Jar>("jar").configure {
    doLast {
        val nestedJars = includeResolvable.files.filter { it.name.contains("resourcefulconfig", ignoreCase = true) }
        if (nestedJars.isEmpty()) return@doLast

        val loader = URLClassLoader(fabricLoomJar.files.map { it.toURI().toURL() }.toTypedArray(), javaClass.classLoader)
        val jarNester = loader.loadClass("net.fabricmc.loom.build.nesting.JarNester")
        jarNester.getMethod("nestJars", Collection::class.java, File::class.java)
            .invoke(null, nestedJars, archiveFile.get().asFile)
    }
}
