package de.meinerezepte.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Query(
        """
        SELECT * FROM recipes
        WHERE (:category IS NULL OR category = :category)
          AND (title LIKE '%' || :query || '%' OR ingredients LIKE '%' || :query || '%')
        ORDER BY createdAt DESC
        """
    )
    fun observeRecipes(query: String, category: String?): Flow<List<Recipe>>

    @Query("SELECT * FROM recipes")
    suspend fun getAllRecipesOnce(): List<Recipe>

    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun getRecipeById(id: Long): Recipe?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recipe: Recipe): Long

    @Update
    suspend fun update(recipe: Recipe)

    @Delete
    suspend fun delete(recipe: Recipe)
}
