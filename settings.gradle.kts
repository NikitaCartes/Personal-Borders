pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        maven("https://maven.neoforged.net/releases")
        maven("https://maven.kikugie.dev/releases")
        maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9.2"
}

stonecutter {
    create(rootProject) {
        // 26.1+ ships Mojang-mapped (deobfuscated). 26.1 and 26.2 are split because the entity
        // movement-collision border lookup moved methods in 26.2 (see EntityMixin).
        versions("26.1-fabric" to "26.1").buildscript("build.fabric.gradle.kts")
        versions("26.2-fabric" to "26.2").buildscript("build.fabric.gradle.kts")
        versions("26.1-neoforge" to "26.1").buildscript("build.neoforge.gradle.kts")
        versions("26.2-neoforge" to "26.2").buildscript("build.neoforge.gradle.kts")
        vcsVersion = "26.1-fabric"
    }
}
