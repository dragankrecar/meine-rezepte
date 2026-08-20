package de.meinerezepte.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import de.meinerezepte.data.RecipeRepository
import de.meinerezepte.share.SharedRecipeDraft
import de.meinerezepte.ui.navigation.Routes
import de.meinerezepte.ui.screens.RecipeDetailScreen
import de.meinerezepte.ui.screens.RecipeEditScreen
import de.meinerezepte.ui.screens.RecipeListScreen
import de.meinerezepte.ui.viewmodel.RecipeEditViewModel
import de.meinerezepte.ui.viewmodel.RecipeEditViewModelFactory
import de.meinerezepte.ui.viewmodel.RecipeListViewModel
import de.meinerezepte.ui.viewmodel.RecipeListViewModelFactory

@Composable
fun MeineRezepteApp(
    repository: RecipeRepository,
    sharedDraft: SharedRecipeDraft? = null,
    onSharedDraftConsumed: () -> Unit = {},
) {
    val navController = rememberNavController()
    var pendingEditDraft by remember { mutableStateOf<SharedRecipeDraft?>(null) }

    LaunchedEffect(sharedDraft) {
        if (sharedDraft != null) {
            pendingEditDraft = sharedDraft
            navController.navigate(Routes.edit()) {
                launchSingleTop = true
            }
            onSharedDraftConsumed()
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.LIST,
    ) {
        composable(Routes.LIST) {
            val viewModel: RecipeListViewModel = viewModel(
                factory = RecipeListViewModelFactory(repository),
            )
            RecipeListScreen(
                viewModel = viewModel,
                repository = repository,
                onRecipeClick = { id ->
                    navController.navigate(Routes.detail(id))
                },
                onAddClick = {
                    pendingEditDraft = null
                    navController.navigate(Routes.edit())
                },
            )
        }

        composable(
            route = Routes.DETAIL,
            arguments = listOf(navArgument("recipeId") { type = NavType.LongType }),
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getLong("recipeId") ?: return@composable
            RecipeDetailScreen(
                recipeId = recipeId,
                repository = repository,
                onBack = { navController.popBackStack() },
                onEdit = { id ->
                    pendingEditDraft = null
                    navController.navigate(Routes.edit(id))
                },
            )
        }

        composable(
            route = Routes.EDIT,
            arguments = listOf(
                navArgument("recipeId") {
                    type = NavType.LongType
                    defaultValue = 0L
                },
            ),
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getLong("recipeId")?.takeIf { it > 0L }
            val draftForScreen = if (recipeId == null) pendingEditDraft else null
            val viewModel: RecipeEditViewModel = viewModel(
                key = "edit-${recipeId ?: "new"}-${draftForScreen?.sourceUrl.orEmpty()}",
                factory = RecipeEditViewModelFactory(repository, recipeId, draftForScreen),
            )
            RecipeEditScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onSaved = {
                    pendingEditDraft = null
                    navController.popBackStack()
                },
            )
        }
    }
}
