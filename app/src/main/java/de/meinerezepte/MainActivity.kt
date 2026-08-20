package de.meinerezepte

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import de.meinerezepte.data.RecipeDatabase
import de.meinerezepte.data.RecipeRepository
import de.meinerezepte.share.ShareIntentParser
import de.meinerezepte.share.SharedRecipeDraft
import de.meinerezepte.ui.MeineRezepteApp
import de.meinerezepte.ui.theme.MeineRezepteTheme

class MainActivity : ComponentActivity() {
    private var sharedDraft by mutableStateOf<SharedRecipeDraft?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        sharedDraft = ShareIntentParser.parse(intent)

        val repository = RecipeRepository(
            RecipeDatabase.getInstance(applicationContext).recipeDao()
        )

        setContent {
            MeineRezepteTheme {
                MeineRezepteApp(
                    repository = repository,
                    sharedDraft = sharedDraft,
                    onSharedDraftConsumed = { sharedDraft = null },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        sharedDraft = ShareIntentParser.parse(intent)
    }
}
