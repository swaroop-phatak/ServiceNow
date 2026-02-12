package androidx.navigation.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController

/**
 * Minimal Navigation-Compose-like API used only by this project.
 *
 * It supports a simple string route and renders the composable
 * matching the current route.
 */

class NavBackStackEntry

class NavGraphBuilder internal constructor(
    private val currentRoute: String
) {
    @Composable
    fun composable(
        route: String,
        content: @Composable () -> Unit
    ) {
        if (route == currentRoute) {
            content()
        }
    }
}

@Composable
fun rememberNavController(): NavHostController {
    // Single instance remembered for the composition.
    return remember { NavHostController() }
}

@Composable
fun NavHost(
    navController: NavHostController,
    startDestination: String,
    builder: @Composable NavGraphBuilder.() -> Unit
) {
    var currentRoute by remember { mutableStateOf(startDestination) }

    LaunchedEffect(navController) {
        navController.onNavigate = { route ->
            currentRoute = route
        }
    }

    val graphBuilder = NavGraphBuilder(currentRoute)
    graphBuilder.builder()
}

