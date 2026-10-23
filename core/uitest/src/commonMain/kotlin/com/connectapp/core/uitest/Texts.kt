package com.connectapp.core.uitest

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * String resources resolved inside the composition, in whatever language the device or
 * simulator running the tests uses, so assertions never hard-code English or Spanish.
 */
class Texts {
    private val resolved = mutableMapOf<StringResource, String>()

    /** Call from the test content before the screen. */
    @Composable
    fun resolve(vararg resources: StringResource) {
        resources.forEach { resolved[it] = stringResource(it) }
    }

    operator fun get(resource: StringResource): String =
        resolved[resource] ?: error("Resolve the string in the test content before using it")
}
