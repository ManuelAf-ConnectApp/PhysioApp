package physioapp.convention

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

/**
 * Classification of every Gradle module in this project, derived from its `path` — same
 * prefix criterion `physioapp.kmp.base.gradle.kts` already uses to derive each module's
 * Android namespace. See specs/013-enforce-module-boundaries/data-model.md.
 */
enum class ModuleLayer {
    DOMAIN,
    CORE,
    DATA,
    FEATURE,
    PRESENTATION,
    COMMON_RESOURCES,
    APP,
}

/**
 * No `else` branch: an unrecognized module path must fail loudly (specs/013.../research.md
 * D4, FR-007), never be silently treated as unrestricted.
 */
fun classify(path: String): ModuleLayer = when {
    path.startsWith(":domain:") -> ModuleLayer.DOMAIN
    path.startsWith(":core:") -> ModuleLayer.CORE
    path.startsWith(":data:") -> ModuleLayer.DATA
    path.startsWith(":feature:") -> ModuleLayer.FEATURE
    path == ":presentation" -> ModuleLayer.PRESENTATION
    path == ":commonResources" -> ModuleLayer.COMMON_RESOURCES
    path == ":composeApp" -> ModuleLayer.APP
    else -> error(
        "Unclassified module path: '$path' — add it to physioapp.convention.classify() " +
            "in build-logic/src/main/kotlin/physioapp/convention/ModuleBoundaries.kt " +
            "(see specs/013-enforce-module-boundaries)."
    )
}

/** Shared modules a feature may use besides the domain: resources, MVI base and PDF layout. */
private val featureCoreModules = setOf(":commonResources", ":core:mvi", ":core:pdf")

/**
 * The single source of truth for the allowed module dependency graph (FR-004) — see
 * specs/013-enforce-module-boundaries/data-model.md for the table this implements.
 */
fun isAllowedDependency(from: String, to: String): Boolean {
    val fromLayer = classify(from)
    val toLayer = classify(to)
    return when (fromLayer) {
        // A :domain:* module may depend on another :domain:* module (e.g. a use case that
        // orchestrates several bounded contexts, such as reports reading patients) but never
        // reaches into CORE/DATA/FEATURE/etc.
        ModuleLayer.DOMAIN -> toLayer == ModuleLayer.DOMAIN
        ModuleLayer.CORE, ModuleLayer.COMMON_RESOURCES -> false
        ModuleLayer.DATA -> toLayer == ModuleLayer.DOMAIN || toLayer == ModuleLayer.CORE
        // :core:pdf lays out the PDFs a feature exports (phase 9); it has no data access.
        ModuleLayer.FEATURE -> toLayer == ModuleLayer.DOMAIN || to in featureCoreModules
        ModuleLayer.PRESENTATION -> toLayer == ModuleLayer.DOMAIN || to == ":commonResources"
        ModuleLayer.APP -> true
    }
}

/**
 * Fails the build if any module declares a production dependency outside the allowed graph
 * above. See specs/013-enforce-module-boundaries/contracts/module-boundaries-contract.md.
 *
 * [edges] is populated by the root build.gradle.kts inside `gradle.projectsEvaluated { }`
 * (specs/013.../research.md D2) — plain, already-resolved data, so this task never needs a
 * live `Project`/`Configuration` reference and stays configuration-cache-safe.
 */
abstract class VerifyModuleBoundaries : DefaultTask() {

    @get:Input
    abstract val edges: MapProperty<String, List<String>>

    @TaskAction
    fun verify() {
        val violations = mutableListOf<String>()
        edges.get().forEach { (from, tos) ->
            tos.forEach { to ->
                val allowed = runCatching { isAllowedDependency(from, to) }
                    .getOrElse { error ->
                        violations += "$from -> $to (${error.message})"
                        return@forEach
                    }
                if (!allowed) {
                    violations += "$from -> $to"
                }
            }
        }
        if (violations.isNotEmpty()) {
            throw GradleException(
                "Module boundary violation(s) found (see " +
                    "build-logic/src/main/kotlin/physioapp/convention/ModuleBoundaries.kt " +
                    "for the allowed dependency graph):\n" +
                    violations.joinToString("\n") { "  - $it" }
            )
        }
    }
}
