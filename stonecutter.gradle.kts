plugins {
    id("dev.kikugie.stonecutter")
}
stonecutter active "26.1-fabric"

stonecutter parameters {
    // Loader flag constants: `//? if fabric { ... }` / `//? if neoforge { ... }`.
    constants.match(current.project.substringAfterLast('-'), "fabric", "neoforge")
}
