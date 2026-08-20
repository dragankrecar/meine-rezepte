package de.meinerezepte.data

import android.content.Context
import android.net.Uri
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object RecipeBackupManager {

    suspend fun export(context: Context, targetUri: Uri, recipes: List<Recipe>) {
        withContext(Dispatchers.IO) {
            context.contentResolver.openOutputStream(targetUri)?.use { output ->
                ZipOutputStream(output).use { zip ->
                    val entries = recipes.mapIndexed { index, recipe ->
                        val imageBytes = recipe.imageUri?.let { imageUriString ->
                            runCatching {
                                context.contentResolver.openInputStream(Uri.parse(imageUriString))
                                    ?.use { it.readBytes() }
                            }.getOrNull()
                        }
                        val imageFileName = if (imageBytes != null) "image_$index.jpg" else null
                        if (imageFileName != null) {
                            zip.putNextEntry(ZipEntry("images/$imageFileName"))
                            zip.write(imageBytes)
                            zip.closeEntry()
                        }
                        RecipeBackupEntry(
                            title = recipe.title,
                            ingredients = recipe.ingredients,
                            instructions = recipe.instructions,
                            sourceUrl = recipe.sourceUrl,
                            category = recipe.category.name,
                            createdAt = recipe.createdAt,
                            imageFileName = imageFileName,
                        )
                    }

                    zip.putNextEntry(ZipEntry("recipes.json"))
                    zip.write(Json.encodeToString(RecipeBackupFile(recipes = entries)).toByteArray())
                    zip.closeEntry()
                }
            }
        }
    }

    suspend fun import(context: Context, sourceUri: Uri, repository: RecipeRepository): Int {
        return withContext(Dispatchers.IO) {
            var backupFile: RecipeBackupFile? = null
            val images = mutableMapOf<String, ByteArray>()

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                ZipInputStream(input).use { zip ->
                    var entry = zip.nextEntry
                    while (entry != null) {
                        val name = entry.name
                        when {
                            name == "recipes.json" -> {
                                backupFile = Json.decodeFromString(zip.readBytes().decodeToString())
                            }
                            name.startsWith("images/") -> {
                                images[name.removePrefix("images/")] = zip.readBytes()
                            }
                        }
                        zip.closeEntry()
                        entry = zip.nextEntry
                    }
                }
            }

            val entries = backupFile?.recipes.orEmpty()
            val imagesDir = File(context.filesDir, "imported_images").apply { mkdirs() }

            entries.forEach { entry ->
                val imageUri = entry.imageFileName
                    ?.let { images[it] }
                    ?.let { bytes ->
                        val file = File(imagesDir, "${System.currentTimeMillis()}_${entry.imageFileName}")
                        file.writeBytes(bytes)
                        Uri.fromFile(file).toString()
                    }

                repository.saveRecipe(
                    Recipe(
                        title = entry.title,
                        ingredients = entry.ingredients,
                        instructions = entry.instructions,
                        imageUri = imageUri,
                        sourceUrl = entry.sourceUrl,
                        category = RecipeCategory.entries.firstOrNull { it.name == entry.category }
                            ?: RecipeCategory.HAUPTSPEISE,
                        createdAt = entry.createdAt,
                    )
                )
            }

            entries.size
        }
    }
}
