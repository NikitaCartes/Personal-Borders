plugins {
    id("dev.kikugie.stonecutter")
    id("me.modmuss50.mod-publish-plugin") version "2.2.0"
}
stonecutter active "26.1-fabric"

stonecutter parameters {
    // Loader flag constants: `//? if fabric { ... }` / `//? if neoforge { ... }`.
    constants.match(current.project.substringAfterLast('-'), "fabric", "neoforge")
}

stonecutter.tasks {
    // Sort published artifacts by version.
    order("publishMods")
}

tasks.register<Delete>("cleanCollectedJars") {
    delete(layout.buildDirectory.dir("libs"))
}

// One GitHub release for the whole matrix: this task creates it, nodes upload into it.
publishMods {
    val githubToken = System.getenv("GITHUB_TOKEN") ?: ""
    val modVersion = findProperty("mod_version")?.toString()
        ?: file("stonecutter.properties.toml").readLines()
            .first { it.trim().startsWith("mod_version") }
            .substringAfter('=').trim().trim('"')

    dryRun = githubToken.isEmpty()
    version = modVersion
    displayName = modVersion
    changelog = rootProject.file("RELEASE_NOTE.md").readText()
    type = STABLE

    github {
        accessToken = githubToken
        repository = "NikitaCartes/Personal-Borders"
        commitish = "stonecutter"
        tagName = modVersion
        allowEmptyFiles = true
    }
}
