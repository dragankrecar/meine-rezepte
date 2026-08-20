package de.meinerezepte.data

import kotlinx.coroutines.flow.Flow

class RecipeRepository(private val recipeDao: RecipeDao) {
    fun observeRecipes(searchQuery: String, category: RecipeCategory?): Flow<List<Recipe>> {
        return recipeDao.observeRecipes(searchQuery.trim(), category?.name)
    }

    suspend fun getRecipe(id: Long): Recipe? = recipeDao.getRecipeById(id)

    suspend fun getAllRecipes(): List<Recipe> = recipeDao.getAllRecipesOnce()

    suspend fun saveRecipe(recipe: Recipe): Long {
        return if (recipe.id == 0L) {
            recipeDao.insert(recipe)
        } else {
            recipeDao.update(recipe)
            recipe.id
        }
    }

    suspend fun deleteRecipe(recipe: Recipe) {
        recipeDao.delete(recipe)
    }
}
