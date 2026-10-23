package com.connectapp.presentation.navigation

/** The back stack of the app; never empty, the current route is the last one. */
data class NavigationState(
    val backStack: List<NavigationRoute>,
) {
    init {
        require(backStack.isNotEmpty()) { "The back stack can never be empty" }
    }

    val route: NavigationRoute get() = backStack.last()

    /**
     * - A root route replaces the whole stack (see [NavigationRoute.isRoot]).
     * - A route already in the stack pops back to it, so switching between bottom bar tabs or
     *   re-opening a section never grows the stack without bound.
     * - Any other route is pushed.
     */
    fun navigate(route: NavigationRoute): NavigationState = when {
        route.isRoot -> NavigationState(listOf(route))
        route in backStack -> NavigationState(backStack.subList(0, backStack.indexOf(route) + 1).toList())
        else -> NavigationState(backStack + route)
    }

    /** Replaces the current screen, e.g. a just-saved form by the record it created. */
    fun replaceCurrent(route: NavigationRoute): NavigationState =
        pop()?.navigate(route) ?: navigate(route)

    /** Null when there is nothing to go back to (the system then leaves the app). */
    fun pop(): NavigationState? = if (backStack.size > 1) NavigationState(backStack.dropLast(1)) else null
}

data class SnackBarState(
    val message: String,
    val actionLabel: String? = null,
    val onAction: (() -> Unit)? = null,
)
