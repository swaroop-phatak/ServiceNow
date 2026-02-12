package androidx.navigation

/**
 * Minimal stub implementation of Navigation types so that
 * the app can compile without relying on the full Navigation
 * Compose library at runtime.
 *
 * This only supports simple route-based navigation used in
 * this project.
 */
class NavHostController {
    internal var onNavigate: ((String) -> Unit)? = null

    fun navigate(
        route: String,
        builder: (NavOptionsBuilder.() -> Unit)? = null
    ) {
        // We ignore options for this simple implementation.
        onNavigate?.invoke(route)
    }
}

class NavOptionsBuilder {
    fun popUpTo(route: String, block: PopUpToBuilder.() -> Unit = {}) {
        // No-op in this minimal implementation.
        PopUpToBuilder().apply(block)
    }
}

class PopUpToBuilder {
    var inclusive: Boolean = false
}

