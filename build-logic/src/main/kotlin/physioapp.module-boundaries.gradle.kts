import org.gradle.api.artifacts.ProjectDependency
import physioapp.convention.VerifyModuleBoundaries

// Applied only to the root project (specs/013-enforce-module-boundaries) — registers
// verifyModuleBoundaries and wires it into `check`, so `./gradlew build`/`check` already
// runs it (FR-005), no CI workflow changes needed (same "free ride" pattern 005 used for
// detekt).

val verifyModuleBoundaries = tasks.register<VerifyModuleBoundaries>("verifyModuleBoundaries") {
    group = "verification"
    description = "Fails if any module declares a project dependency outside the allowed " +
        "layer graph (see build-logic/src/main/kotlin/physioapp/convention/ModuleBoundaries.kt)."
}

// Deferred to projectsEvaluated (research.md D2): every subproject must have finished
// declaring its dependencies before this reads them, and the result is captured here as
// plain, already-resolved data — the task itself never holds a live Project/Configuration
// reference, keeping this configuration-cache-safe.
gradle.projectsEvaluated {
    val edges = subprojects
        .filterNot { it.path == ":composeApp" }
        .associate { subproject ->
            val projectDeps = mutableSetOf<String>()
            subproject.configurations
                .matching { configuration ->
                    val name = configuration.name
                    (name.endsWith("Implementation") || name.endsWith("Api")) &&
                        !name.contains("Test", ignoreCase = true)
                }
                .forEach { configuration ->
                    configuration.dependencies.withType(ProjectDependency::class.java).forEach { dependency ->
                        projectDeps += dependency.path
                    }
                }
            subproject.path to projectDeps.toList()
        }
    verifyModuleBoundaries.configure {
        this.edges.set(edges)
    }
}

tasks.named("check") {
    dependsOn(verifyModuleBoundaries)
}
