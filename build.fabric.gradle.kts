plugins {
    id("java")
    id("net.fabricmc.fabric-loom") version "1.17-SNAPSHOT"
}

// Tag this node's loader and version so [fabric."26.1"] keys resolve via bare property("...").
stonecutter {
    val (version, loader) = current.project.split('-', limit = 2)
    properties.tags(version, loader)
}

repositories {
    mavenCentral()
}

base.archivesName = "${property("mod_id")}-fabric-mc${property("minecraft_version")}"
version = property("mod_version").toString()

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

// 26.1+ runtime is already Mojang-mapped, so the accesswidener is in the `official` namespace.
val awFile = "personal-borders.accesswidener"

loom {
    accessWidenerPath = rootProject.file("src/main/resources/accesswidener/$awFile")
}

dependencies {
    // Fabric (26.1+ ships Mojang-mapped, so no `mappings(...)` line is needed)
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    implementation("net.fabricmc:fabric-loader:${property("loader_version")}")

    implementation(fabricApi.module("fabric-lifecycle-events-v1", property("fabric_version").toString()))
    implementation(fabricApi.module("fabric-networking-api-v1", property("fabric_version").toString()))

    // LuckPerms API (provided at runtime by the LuckPerms mod)
    compileOnly("net.luckperms:api:${property("luckperms_version")}")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(25)
}

tasks.jar {
    from("LICENSE")
}

val modExpansions = mapOf(
    "version" to project.version.toString(),
    "supported_minecraft_version" to property("supported_minecraft_version").toString(),
    "accessWidener" to awFile
)

tasks.processResources {
    inputs.properties(modExpansions)
    filesMatching("fabric.mod.json") { expand(modExpansions) }
    // Mojang-mapped runtime needs no mixin refmap and no NeoForge metadata.
    exclude("META-INF/neoforge.mods.toml", "accesstransformer/**")
}

tasks.register<Copy>("collectJars") {
    group = "build"
    from(tasks.jar.map { it.archiveFile })
    into(rootProject.layout.buildDirectory.dir("libs"))
    dependsOn("build")
}
