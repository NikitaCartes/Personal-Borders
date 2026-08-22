plugins {
    id("java")
    kotlin("jvm") version "2.4.0"
    id("net.neoforged.moddev") version "2.0.141"
    id("com.google.devtools.ksp") version "2.3.9"
    id("dev.kikugie.fletching-table.neoforge") version "0.1.0-alpha.22"
    id("me.modmuss50.mod-publish-plugin") version "2.2.0"
}

// Tags make [neoforge."26.1"] keys resolve via bare property("...").
stonecutter {
    val (version, loader) = current.project.split('-', limit = 2)
    properties.tags(version, loader)
}

repositories {
    mavenCentral()
    maven("https://maven.neoforged.net/releases")
}

val javaVersion = property("java_version").toString().toInt()

base.archivesName = "${property("mod_id")}-neoforge-mc${property("minecraft_version")}"
version = property("mod_version").toString()

val atFile = "${property("access_file")}.cfg"
val atSource = rootProject.file("src/main/resources/accesstransformer/$atFile")

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(javaVersion)) }
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
    // MixinExtras via jar-in-jar: Fabric ships it with the loader, NeoForge does not.
    implementation("io.github.llamalad7:mixinextras-neoforge:0.5.4")
    jarJar("io.github.llamalad7:mixinextras-neoforge:0.5.4") {
        version { strictly("[0.5.4,)"); prefer("0.5.4") }
    }

    // LuckPerms API (provided at runtime by the LuckPerms mod)
    compileOnly("net.luckperms:api:${property("luckperms_version")}")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(javaVersion)
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

    // Ship the access transformer at the path neoforge.mods.toml references.
    exclude("accesstransformer/**", "accesswidener/**", "fabric.mod.json")
    from(atSource) {
        into("META-INF")
        rename { "accesstransformer.cfg" }
    }

    inputs.properties(modExpansions)
    filesMatching("META-INF/neoforge.mods.toml") { expand(modExpansions) }
}

// Generated sources must exist before the MC artifacts are built.
tasks.named("createMinecraftArtifacts") {
    dependsOn(tasks.named("stonecutterGenerate"))
}

tasks.jar {
    from("LICENSE")
}

fletchingTable {
    // neoforge.mods.toml already declares the config.
    neoforge { applyMixinConfig = false }
    mixins.create("main") {
        mixin("default", "personal-borders.mixins.json")
    }
}

tasks.register<Copy>("collectJars") {
    group = "build"
    from(tasks.jar.map { it.archiveFile })
    into(rootProject.layout.buildDirectory.dir("libs"))
    dependsOn("build", rootProject.tasks.named("cleanCollectedJars"))
}

publishMods {
    val modrinthToken = System.getenv("MODRINTH_TOKEN") ?: ""
    val curseforgeToken = System.getenv("CURSEFORGE_TOKEN") ?: ""
    val githubToken = System.getenv("GITHUB_TOKEN") ?: ""

    file = tasks.jar.get().archiveFile
    dryRun = modrinthToken.isEmpty() || curseforgeToken.isEmpty() || githubToken.isEmpty()
    displayName = "${property("display_name")} ${project.version}"
    version = project.version.toString()
    changelog = rootProject.file("RELEASE_NOTE.md").readText()
    type = STABLE
    modLoaders.add("neoforge")

    val targets = property("supported_versions").toString().split(",")
    modrinth {
        projectId = "JlLlrYIT"
        accessToken = modrinthToken
        targets.forEach(minecraftVersions::add)
        optional("luckperms")
    }
    curseforge {
        projectId = "1202751"
        accessToken = curseforgeToken
        targets.forEach(minecraftVersions::add)
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
