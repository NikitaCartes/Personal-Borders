plugins {
    id("java")
    kotlin("jvm") version "2.4.0"
    id("fabric-loom") version "1.17-SNAPSHOT"
    id("com.google.devtools.ksp") version "2.3.9"
    id("dev.kikugie.fletching-table.fabric") version "0.1.0-alpha.22"
    id("me.modmuss50.mod-publish-plugin") version "2.2.0"
}

// Tags make [fabric."1.21.1"] keys resolve via bare property("...").
stonecutter {
    val (version, loader) = current.project.split('-', limit = 2)
    properties.tags(version, loader)
}

repositories {
    mavenCentral()
}

val javaVersion = property("java_version").toString().toInt()

base.archivesName = "${property("mod_id")}-fabric-mc${property("minecraft_version")}"
version = property("mod_version").toString()

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(javaVersion)) }
}

// Loom remaps it, so the accesswidener is in the `named` namespace: Mojang names.
val awFile = "${property("access_file")}.accesswidener"

loom {
    accessWidenerPath = rootProject.file("src/main/resources/accesswidener/$awFile")
}

dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")

    modImplementation(fabricApi.module("fabric-lifecycle-events-v1", property("fabric_version").toString()))
    modImplementation(fabricApi.module("fabric-networking-api-v1", property("fabric_version").toString()))

    // LuckPerms API (provided at runtime by the LuckPerms mod)
    compileOnly("net.luckperms:api:${property("luckperms_version")}")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(javaVersion)
}

tasks.jar {
    from("LICENSE")
}

val modExpansions = mapOf(
    "version" to project.version.toString(),
    "supported_minecraft_version" to property("supported_minecraft_version").toString(),
    "accessWidener" to awFile,
    "loader_version" to property("loader_version").toString()
)

tasks.processResources {
    inputs.properties(modExpansions)

    filesMatching("fabric.mod.json") { expand(modExpansions) }
    // Fabric needs no NeoForge metadata, and only one accesswidener is shipped.
    exclude("META-INF/neoforge.mods.toml", "accesstransformer/**")
    eachFile {
        if (path.startsWith("accesswidener/") && name != awFile) exclude()
    }
}

fletchingTable {
    mixins.create("main") {
        mixin("default", "personal-borders.mixins.json")
    }
}

tasks.register<Copy>("collectJars") {
    group = "build"
    from(tasks.remapJar.map { it.archiveFile })
    into(rootProject.layout.buildDirectory.dir("libs"))
    dependsOn("build", rootProject.tasks.named("cleanCollectedJars"))
}

publishMods {
    val modrinthToken = System.getenv("MODRINTH_TOKEN") ?: ""
    val curseforgeToken = System.getenv("CURSEFORGE_TOKEN") ?: ""
    val githubToken = System.getenv("GITHUB_TOKEN") ?: ""

    file = tasks.remapJar.get().archiveFile
    dryRun = modrinthToken.isEmpty() || curseforgeToken.isEmpty() || githubToken.isEmpty()
    displayName = "${property("display_name")} ${project.version}"
    version = project.version.toString()
    changelog = rootProject.file("RELEASE_NOTE.md").readText()
    type = STABLE
    modLoaders.add("fabric")

    val targets = property("supported_versions").toString().split(",")
    modrinth {
        projectId = "JlLlrYIT"
        accessToken = modrinthToken
        targets.forEach(minecraftVersions::add)
        requires("fabric-api")
        optional("luckperms")
    }
    curseforge {
        projectId = "1202751"
        accessToken = curseforgeToken
        targets.forEach(minecraftVersions::add)
        requires("fabric-api")
        optional("luckperms")
        client.set(true)
        server.set(true)
    }
    // Uploads into the release created by the root publishGithub task.
    github {
        accessToken = githubToken
        parent(rootProject.tasks.named("publishGithub"))
    }
}
