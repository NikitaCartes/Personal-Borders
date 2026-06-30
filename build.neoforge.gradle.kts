plugins {
    id("java")
    id("net.neoforged.moddev") version "2.0.141"
}

// Tag this node's loader and version so [neoforge."26.1"] keys resolve via bare property("...").
stonecutter {
    val (version, loader) = current.project.split('-', limit = 2)
    properties.tags(version, loader)
}

repositories {
    mavenCentral()
    maven("https://maven.neoforged.net/releases")
}

base.archivesName = "${property("mod_id")}-neoforge-mc${property("minecraft_version")}"
version = property("mod_version").toString()

val atFile = "personal-borders.cfg"
val atSource = rootProject.file("src/main/resources/accesstransformer/$atFile")

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(25)) }
}

neoForge {
    version = property("neoforge_version").toString()
    accessTransformers.from(atSource)

    runs {
        create("server") {
            server()
            gameDirectory.set(file("run"))
        }
    }
    mods {
        create(property("mod_id").toString()) {
            sourceSet(sourceSets.main.get())
        }
    }
}

dependencies {
    // MixinExtras (bundled via jar-in-jar; Fabric ships it with the loader, NeoForge does not)
    implementation("io.github.llamalad7:mixinextras-neoforge:0.5.4")
    jarJar("io.github.llamalad7:mixinextras-neoforge:0.5.4") {
        version { strictly("[0.5.4,)"); prefer("0.5.4") }
    }

    // LuckPerms API (provided at runtime by the LuckPerms mod)
    compileOnly("net.luckperms:api:${property("luckperms_version")}")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(25)
}

val modExpansions = mapOf(
    "version" to project.version.toString(),
    "supported_minecraft_version" to property("supported_minecraft_version").toString(),
    "neoforge_version" to property("neoforge_version").toString(),
    "mod_id" to property("mod_id").toString(),
    "mod_name" to property("mod_name").toString()
)

tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE

    // Ship only the access transformer at the canonical path neoforge.mods.toml references.
    exclude("accesstransformer/**", "accesswidener/**", "fabric.mod.json")
    from(atSource) {
        into("META-INF")
        rename { "accesstransformer.cfg" }
    }

    inputs.properties(modExpansions)
    filesMatching("META-INF/neoforge.mods.toml") { expand(modExpansions) }
}

tasks.named("createMinecraftArtifacts") {
    dependsOn(tasks.named("stonecutterGenerate"))
}

tasks.jar {
    from("LICENSE")
}

tasks.register<Copy>("collectJars") {
    group = "build"
    from(tasks.jar.map { it.archiveFile })
    into(rootProject.layout.buildDirectory.dir("libs"))
    dependsOn("build")
}
