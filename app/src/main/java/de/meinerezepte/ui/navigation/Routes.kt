package de.meinerezepte.ui.navigation

object Routes {
    const val LIST = "list"
    const val DETAIL = "detail/{recipeId}"
    const val EDIT = "edit/{recipeId}"

    fun detail(recipeId: Long) = "detail/$recipeId"
    fun edit(recipeId: Long = 0L) = "edit/$recipeId"
}
