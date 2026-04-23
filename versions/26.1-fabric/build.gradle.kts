plugins {
    java
    kotlin("jvm")
    kotlin("plugin.serialization") version "2.2.21"
}

configurations.all {
    resolutionStrategy {
        force("org.jetbrains.kotlin:kotlin-stdlib:2.2.21")
    }
}

repositories {
    maven("https://maven.fabricmc.net")
    maven("https://repo.essential.gg/repository/maven-public")
    maven("https://maven.teamresourceful.com/repository/maven-public/")
    mavenCentral()
}

java {
    withSourcesJar()
}

sourceSets {
    named("main") {
        java.srcDirs("bin/main")
        resources.srcDirs("bin/main")
    }
}

dependencies {
    implementation("com.mojang:minecraft:26.1.2")
    implementation("net.fabricmc:fabric-loader:0.18.6")
    implementation("net.fabricmc:fabric-language-kotlin:1.13.10+kotlin.2.3.20")
    implementation("net.fabricmc.fabric-api:fabric-api:0.145.4+26.1.2")
    implementation("com.teamresourceful.resourcefulconfig:resourcefulconfig-fabric-26.1:${property("rconfig.version.26.1")}")
}
