package de.meinerezepte.data

import kotlinx.serialization.Serializable

@Serializable
data class RecipeBackupEntry(
    val title: String,
    val ingredients: String,
    val instructions: String,
    val sourceUrl: String? = null,
    val category: String,
    val createdAt: Long,
    val imageFileName: String? = null,
)

@Serializable
data class RecipeBackupFile(
    val version: Int = 1,
    val recipes: List<RecipeBackupEntry>,
)
