package de.meinerezepte.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.meinerezepte.data.Recipe
import de.meinerezepte.data.RecipeCategory
import de.meinerezepte.data.RecipeRepository
import de.meinerezepte.share.SharedRecipeDraft
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RecipeEditUiState(
    val id: Long = 0,
    val title: String = "",
    val ingredients: String = "",
    val instructions: String = "",
    val imageUri: String? = null,
    val sourceUrl: String? = null,
    val category: RecipeCategory = RecipeCategory.HAUPTSPEISE,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
)

class RecipeEditViewModel(
    private val repository: RecipeRepository,
    private val recipeId: Long?,
    private val sharedDraft: SharedRecipeDraft? = null,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RecipeEditUiState())
    val uiState: StateFlow<RecipeEditUiState> = _uiState.asStateFlow()

    init {
        when {
            recipeId != null && recipeId > 0 -> loadRecipe(recipeId)
            sharedDraft != null -> applySharedDraft(sharedDraft)
        }
    }

    private fun loadRecipe(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val recipe = repository.getRecipe(id)
            if (recipe != null) {
                _uiState.value = RecipeEditUiState(
                    id = recipe.id,
                    title = recipe.title,
                    ingredients = recipe.ingredients,
                    instructions = recipe.instructions,
                    imageUri = recipe.imageUri,
                    sourceUrl = recipe.sourceUrl,
                    category = recipe.category,
                )
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun applySharedDraft(draft: SharedRecipeDraft) {
        _uiState.value = RecipeEditUiState(
            title = draft.suggestedTitle,
            sourceUrl = draft.sourceUrl,
        )
    }

    fun onTitleChange(value: String) {
        _uiState.update { it.copy(title = value) }
    }

    fun onIngredientsChange(value: String) {
        _uiState.update { it.copy(ingredients = value) }
    }

    fun onInstructionsChange(value: String) {
        _uiState.update { it.copy(instructions = value) }
    }

    fun onImageSelected(uri: String?) {
        _uiState.update { it.copy(imageUri = uri) }
    }

    fun onCategoryChange(category: RecipeCategory) {
        _uiState.update { it.copy(category = category) }
    }

    fun save() {
        val state = _uiState.value
        if (state.title.isBlank()) return

        viewModelScope.launch {
            repository.saveRecipe(
                Recipe(
                    id = state.id,
                    title = state.title.trim(),
                    ingredients = state.ingredients.trim(),
                    instructions = state.instructions.trim(),
                    imageUri = state.imageUri,
                    sourceUrl = state.sourceUrl?.trim()?.ifBlank { null },
                    category = state.category,
                )
            )
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}

class RecipeEditViewModelFactory(
    private val repository: RecipeRepository,
    private val recipeId: Long?,
    private val sharedDraft: SharedRecipeDraft? = null,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecipeEditViewModel::class.java)) {
            return RecipeEditViewModel(repository, recipeId, sharedDraft) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
