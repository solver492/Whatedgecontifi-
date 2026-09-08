package com.example.domain.whatsapp

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.example.util.ProductMediaManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URLEncoder
import java.util.UUID

data class PromptCommand(
    val command: String,
    val label: String,
    val category: PromptCategory,
    val iconEmoji: String = ""
)

enum class PromptCategory(val title: String, val iconEmoji: String, val colorValue: Long) {
    CLEANUP("Commandes de Nettoyage", "🪓", 0xFFEF4444),
    ECOMMERCE_STRUCTURE("Structure E-Commerce", "📸", 0xFF3B82F6),
    AMBIANCE_LIGHT("Ambiance & Lumière", "💡", 0xFFF59E0B)
}

object PromptCommandCatalog {
    val CLEANUP_COMMANDS = listOf(
        PromptCommand("/DELETE background", "Supprimer l'arrière-plan", PromptCategory.CLEANUP, "🪓"),
        PromptCommand("/DELETE logo", "Effacer le logo ou texte", PromptCategory.CLEANUP, "🏷️"),
        PromptCommand("/REMOVE shadow", "Éliminer les ombres portées", PromptCategory.CLEANUP, "🌑"),
        PromptCommand("/CLEAN reflections", "Atténuer les reflets parasites", PromptCategory.CLEANUP, "✨")
    )

    val ECOMMERCE_COMMANDS = listOf(
        PromptCommand("/packshot", "Rendu studio ultra-pro, centré", PromptCategory.ECOMMERCE_STRUCTURE, "📦"),
        PromptCommand("/producthero", "Produit dynamique au premier plan", PromptCategory.ECOMMERCE_STRUCTURE, "🌟"),
        PromptCommand("/birdsview", "Vue de dessus à 90° (Flatlay)", PromptCategory.ECOMMERCE_STRUCTURE, "📐"),
        PromptCommand("/flatlay", "Mise à plat flatlay e-commerce", PromptCategory.ECOMMERCE_STRUCTURE, "👕"),
        PromptCommand("/fullbody", "Visibilité complète mannequin", PromptCategory.ECOMMERCE_STRUCTURE, "🧍"),
        PromptCommand("/negativespace", "Zone vide pour texte publicitaire", PromptCategory.ECOMMERCE_STRUCTURE, "🖼️")
    )

    val AMBIANCE_COMMANDS = listOf(
        PromptCommand("/white-studio", "Fond blanc pur Amazon/Google", PromptCategory.AMBIANCE_LIGHT, "⬜"),
        PromptCommand("/bokeh effect", "Arrière-plan flouté haut de gamme", PromptCategory.AMBIANCE_LIGHT, "🔮"),
        PromptCommand("/goldenhour", "Lumière chaude de fin de journée", PromptCategory.AMBIANCE_LIGHT, "🌅"),
        PromptCommand("/softlight", "Éclairage diffus sans contraste agressif", PromptCategory.AMBIANCE_LIGHT, "🕯️")
    )

    val ALL_COMMANDS = CLEANUP_COMMANDS + ECOMMERCE_COMMANDS + AMBIANCE_COMMANDS
}

data class ActiveEnhancementState(
    val isWaiting: Boolean = false,
    val targetMediaId: String? = null,
    val originalMediaPath: String? = null,
    val selectedCommands: Set<String> = emptySet(),
    val productTitle: String = "",
    val productDescription: String = "",
    val requestTimestamp: Long = 0L,
    val lastInterceptedImagePath: String? = null
)

object WhatsAppPromptAutomationManager {
    private const val TAG = "WhatsAppPromptManager"

    const val TARGET_WHATSAPP_PHONE = "+18002428478"
    const val TARGET_WHATSAPP_CLEAN = "18002428478"
    const val TARGET_WHATSAPP_JID = "18002428478@s.whatsapp.net"

    private val _activeState = MutableStateFlow(ActiveEnhancementState())
    val activeState = _activeState.asStateFlow()

    /**
     * Formate le message WhatsApp intégrant les commandes d'injection sélectionnées et la description
     */
    fun formatPromptPayload(commands: Set<String>, description: String): String {
        val cmds = commands.joinToString(" ")
        val cleanDesc = description.trim()
        return if (cleanDesc.isNotBlank()) {
            "$cmds\n\n$cleanDesc".trim()
        } else {
            cmds.trim()
        }
    }

    /**
     * Active le suivi du pipeline d'automatisation
     */
    fun startEnhancementPipeline(
        targetMediaId: String,
        originalMediaPath: String,
        commands: Set<String>,
        productTitle: String,
        productDescription: String
    ) {
        _activeState.value = ActiveEnhancementState(
            isWaiting = true,
            targetMediaId = targetMediaId,
            originalMediaPath = originalMediaPath,
            selectedCommands = commands,
            productTitle = productTitle,
            productDescription = productDescription,
            requestTimestamp = System.currentTimeMillis()
        )
    }

    /**
     * Envoie l'image + les commandes d'injection + description du produit vers WhatsApp (+18002428478)
     */
    suspend fun dispatchToWhatsApp(
        context: Context,
        imagePathOrUrl: String,
        commands: Set<String>,
        productDescription: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val payloadText = formatPromptPayload(commands, productDescription)

            // 1. Résoudre le fichier image local
            val localPath = ProductMediaManager.cacheMediaLocally(context, imagePathOrUrl, prefix = "wa_send_")
            val file = if (!localPath.isNullOrBlank()) File(localPath) else null

            if (file == null || !file.exists()) {
                Log.e(TAG, "Impossible de localiser le fichier image pour l'envoi WhatsApp: $imagePathOrUrl")
                return@withContext false
            }

            // 2. Générer l'URI de partage via FileProvider
            val contentUri: Uri = try {
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            } catch (e: Exception) {
                Log.e(TAG, "Erreur FileProvider: ${e.message}")
                Uri.fromFile(file)
            }

            // 3. Préparer l'intent WhatsApp
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_TEXT, payloadText)
                putExtra("jid", TARGET_WHATSAPP_JID)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            // Vérifier packages WhatsApp
            val pm = context.packageManager
            val waPackages = listOf("com.whatsapp", "com.whatsapp.w4b")
            var targeted = false
            for (pkg in waPackages) {
                try {
                    pm.getPackageInfo(pkg, 0)
                    intent.setPackage(pkg)
                    targeted = true
                    break
                } catch (_: Exception) {}
            }

            withContext(Dispatchers.Main) {
                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    // Fallback vers le sélecteur général ou lien universel
                    try {
                        val chooser = Intent.createChooser(intent, "Envoyer vers WhatsApp (+18002428478)").apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(chooser)
                    } catch (e2: Exception) {
                        val webUrl = "https://api.whatsapp.com/send?phone=$TARGET_WHATSAPP_CLEAN&text=${URLEncoder.encode(payloadText, "UTF-8")}"
                        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl)).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(webIntent)
                    }
                }
            }
            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de l'envoi vers WhatsApp: ${e.message}", e)
            return@withContext false
        }
    }

    /**
     * Intercepte une image (depuis URI reçue ou choisie) et l'enregistre dans l'application
     */
    suspend fun interceptAndSaveImage(context: Context, sourceUri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val mediaDir = File(context.filesDir, "product_media").apply { if (!exists()) mkdirs() }
            val destFile = File(mediaDir, "wa_gen_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg")

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }

            if (destFile.exists() && destFile.length() > 0) {
                _activeState.value = _activeState.value.copy(
                    isWaiting = false,
                    lastInterceptedImagePath = destFile.absolutePath
                )
                return@withContext destFile.absolutePath
            }
            return@withContext null
        } catch (e: Exception) {
            Log.e(TAG, "Erreur intercepte image: ${e.message}", e)
            return@withContext null
        }
    }

    /**
     * Simule / Génère la retouche studio demandée (Packshot, suppression d'arrière-plan, éclairage studio, etc.)
     * Permet une validation immédiate même sans réseau externe ou en environnement de test.
     */
    suspend fun simulateStudioAiEnhancement(
        context: Context,
        originalPathOrUrl: String,
        commands: Set<String>
    ): String? = withContext(Dispatchers.IO) {
        try {
            // Résoudre l'image originale
            val localPath = ProductMediaManager.cacheMediaLocally(context, originalPathOrUrl, prefix = "sim_orig_")
            val srcFile = if (!localPath.isNullOrBlank()) File(localPath) else null
            if (srcFile == null || !srcFile.exists()) return@withContext null

            val originalBitmap = BitmapFactory.decodeFile(srcFile.absolutePath) ?: return@withContext null

            val width = originalBitmap.width
            val height = originalBitmap.height
            val resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(resultBitmap)

            val paint = Paint(Paint.ANTI_ALIAS_FLAG)

            val isWhiteStudio = commands.any { it.contains("white-studio") || it.contains("DELETE background") }
            val isGoldenHour = commands.any { it.contains("goldenhour") }
            val isBokeh = commands.any { it.contains("bokeh") }
            val isPackshot = commands.any { it.contains("packshot") || it.contains("producthero") }

            // 1. Rendu d'arrière-plan professionnel
            if (isWhiteStudio) {
                // Fond studio blanc pur Amazon/Google avec subtil dégradé
                val studioShader = LinearGradient(
                    0f, 0f, 0f, height.toFloat(),
                    AndroidColor.rgb(255, 255, 255),
                    AndroidColor.rgb(245, 247, 250),
                    Shader.TileMode.CLAMP
                )
                paint.shader = studioShader
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
                paint.shader = null
            } else if (isGoldenHour) {
                // Ambiance chaude golden hour
                val goldenShader = LinearGradient(
                    0f, 0f, width.toFloat(), height.toFloat(),
                    AndroidColor.rgb(255, 237, 213),
                    AndroidColor.rgb(254, 215, 170),
                    Shader.TileMode.CLAMP
                )
                paint.shader = goldenShader
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
                paint.shader = null
            } else {
                // Fond studio sombre élégant par défaut
                val darkShader = LinearGradient(
                    0f, 0f, 0f, height.toFloat(),
                    AndroidColor.rgb(240, 243, 246),
                    AndroidColor.rgb(226, 232, 240),
                    Shader.TileMode.CLAMP
                )
                paint.shader = darkShader
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
                paint.shader = null
            }

            // 2. Ombre portée douce de packshot studio
            if (isPackshot || isWhiteStudio) {
                val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = AndroidColor.argb(45, 15, 23, 42)
                }
                val shadowRect = RectF(
                    width * 0.18f,
                    height * 0.82f,
                    width * 0.82f,
                    height * 0.88f
                )
                canvas.drawOval(shadowRect, shadowPaint)
            }

            // 3. Dessin du produit recentré / optimisé
            val scale = if (isPackshot) 0.88f else 0.94f
            val destW = (width * scale).toInt()
            val destH = (height * scale).toInt()
            val left = (width - destW) / 2
            val top = if (isPackshot) (height - destH) / 2 - (height * 0.03f).toInt() else (height - destH) / 2

            val srcRect = Rect(0, 0, width, height)
            val destRect = Rect(left, top, left + destW, top + destH)
            val imgPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

            canvas.drawBitmap(originalBitmap, srcRect, destRect, imgPaint)

            // 4. Teinte douce de lumière ambiante
            if (isGoldenHour) {
                val warmFilter = Paint().apply {
                    color = AndroidColor.argb(30, 245, 158, 11)
                }
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), warmFilter)
            } else if (isBokeh) {
                val vignettePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = AndroidColor.argb(40, 0, 0, 0)
                }
                canvas.drawCircle(width / 2f, height / 2f, width * 0.55f, vignettePaint)
            }

            // Sauvegarder dans product_media
            val mediaDir = File(context.filesDir, "product_media").apply { if (!exists()) mkdirs() }
            val enhancedFile = File(mediaDir, "wa_enhanced_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg")
            FileOutputStream(enhancedFile).use { out ->
                resultBitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
            }

            _activeState.value = _activeState.value.copy(
                isWaiting = false,
                lastInterceptedImagePath = enhancedFile.absolutePath
            )

            return@withContext enhancedFile.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Erreur simulation studio: ${e.message}", e)
            return@withContext null
        }
    }

    /**
     * Réinitialise le pipeline
     */
    fun resetPipeline() {
        _activeState.value = ActiveEnhancementState()
    }
}
