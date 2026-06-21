plugins {
    java
    kotlin("jvm")
    kotlin("plugin.serialization") version "2.3.0"
    id("net.fabricmc.fabric-loom-remap") version "1.17.11"
    id("dev.deftu.gradle.multiversion") // Applies preprocessing for multiple versions of Minecraft and/or multiple mod loaders.
    id("dev.deftu.gradle.tools.bloom") // Applies the Bloom plugin, which allows us to replace tokens in our source files, such as being able to use `@MOD_VERSION` in our source files.
}

apply(from = rootProject.file("build.common.gradle"))

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile>().configureEach {
    compilerOptions.jvmTarget.set(
        org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget(
            project.findProperty("java.version.${project.name.removeSuffix("-fabric")}")!!.toString()
        )
    )
}

dependencies {
    configurations.findByName("mappings")?.let { mappingsConfig ->
        add(mappingsConfig.name, loom.officialMojangMappings())
    }
}

afterEvaluate {
    val outputDir = rootProject.layout.buildDirectory.dir("versions").get().asFile
    val jarName = project.property("mod.name").toString() + "-" + project.property("mod.version").toString() + "+" + project.name

    tasks.named<net.fabricmc.loom.task.RemapJarTask>("remapJar").configure {
        destinationDirectory.set(outputDir)
        archiveBaseName.set(jarName)
    }
}
