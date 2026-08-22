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
    // Nodes need Java 21 and Java 25, so a missing toolchain is downloaded.
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}

stonecutter {
    create(rootProject) {
        // One node for each range of Minecraft versions with the same hooked members:
        //   1.21.1 -> 1.21 .. 1.21.1     1.21.3 -> 1.21.2 .. 1.21.4    1.21.5 -> 1.21.5
        //   1.21.6 -> 1.21.6 .. 1.21.8   1.21.9 -> 1.21.9 .. 1.21.11
        //   26.1   -> 26.1 .. 26.1.2     26.2   -> 26.2+
        val obfuscated = listOf("1.21.1", "1.21.3", "1.21.5", "1.21.6", "1.21.9")
        val deobfuscated = listOf("26.1", "26.2")

        // Obfuscated: Fabric needs the remapping Loom plugin.
        obfuscated.forEach { mc ->
            versions("$mc-fabric" to mc).buildscript("build.fabric-obf.gradle.kts")
        }
        // Mojang-mapped: Fabric needs no remapping.
        deobfuscated.forEach { mc ->
            versions("$mc-fabric" to mc).buildscript("build.fabric-deobf.gradle.kts")
        }
        // NeoForge is on Mojang names everywhere, so one buildscript covers every node.
        (obfuscated + deobfuscated).forEach { mc ->
            versions("$mc-neoforge" to mc).buildscript("build.neoforge.gradle.kts")
        }
        vcsVersion = "26.1-fabric"
    }
}
