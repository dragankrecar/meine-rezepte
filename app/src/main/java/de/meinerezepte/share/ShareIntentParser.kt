package de.meinerezepte.share

import android.content.Intent

object ShareIntentParser {
    private val urlPattern = Regex("""https?://[^\s<>"]+""")

    fun parse(intent: Intent?): SharedRecipeDraft? {
        if (intent?.action != Intent.ACTION_SEND || intent.type != "text/plain") {
            return null
        }

        val sharedText = sequenceOf(
            intent.getStringExtra(Intent.EXTRA_TEXT),
            intent.getStringExtra(Intent.EXTRA_SUBJECT),
        ).filterNot { it.isNullOrBlank() }
            .joinToString("\n")
            .trim()

        if (sharedText.isEmpty()) return null

        val sourceUrl = extractUrl(sharedText) ?: return null

        return SharedRecipeDraft(
            suggestedTitle = suggestTitle(sourceUrl, sharedText),
            sourceUrl = sourceUrl,
            sharedText = sharedText,
        )
    }

    private fun extractUrl(text: String): String? {
        return urlPattern.find(text)?.value?.trimEnd('.', ',', ')', ']', '"', '\'')
    }

    private fun suggestTitle(sourceUrl: String, sharedText: String): String {
        val url = sourceUrl.lowercase()
        return when {
            "instagram.com" in url -> "Rezept von Instagram"
            "facebook.com" in url || "fb.watch" in url || "fb.com" in url -> "Rezept von Facebook"
            else -> {
                val firstLine = sharedText.lineSequence().firstOrNull()?.trim().orEmpty()
                if (firstLine.isNotBlank() && !firstLine.startsWith("http", ignoreCase = true)) {
                    firstLine.take(80)
                } else {
                    "Geteiltes Rezept"
                }
            }
        }
    }
}
