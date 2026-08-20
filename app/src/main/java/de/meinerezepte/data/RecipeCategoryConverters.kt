package de.meinerezepte.data

import androidx.room.TypeConverter

class RecipeCategoryConverters {
    @TypeConverter
    fun fromCategory(category: RecipeCategory): String = category.name

    @TypeConverter
    fun toCategory(value: String): RecipeCategory =
        RecipeCategory.entries.firstOrNull { it.name == value } ?: RecipeCategory.HAUPTSPEISE
}
