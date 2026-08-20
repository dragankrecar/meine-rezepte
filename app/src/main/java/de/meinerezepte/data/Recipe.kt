package de.meinerezepte.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val ingredients: String,
    val instructions: String,
    val imageUri: String? = null,
    val sourceUrl: String? = null,
    val category: RecipeCategory = RecipeCategory.HAUPTSPEISE,
    val createdAt: Long = System.currentTimeMillis(),
)
