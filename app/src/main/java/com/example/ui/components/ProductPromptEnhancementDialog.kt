package com.example.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.domain.whatsapp.PromptCategory
import com.example.domain.whatsapp.PromptCommand
import com.example.domain.whatsapp.PromptCommandCatalog
import com.example.domain.whatsapp.WhatsAppPromptAutomationManager
import com.example.util.ProductMediaManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Modèle générique pour les médias passés au dialogue de prompt & retouche
 */
data class PromptTargetMedia(
    val id: String,
    val urlOrPath: String,
    val isVideo: Boolean = false
)

/**
 * Bouton "P" personnalisé inséré entre "Ajouter Médias (Galerie)" et "URL"
 */
@Composable
fun ProductPromptButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF1E1B2E),
        border = BorderStroke(
            1.dp,
            Brush.linearGradient(
                listOf(Color(0xFFA855F7), Color(0xFF6366F1))
            )
        ),
        modifier = modifier
            .testTag("prompt_p_button")
            .height(40.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color(0xFFA855F7),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "P",
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
                color = Color.White
            )
        }
    }
}

/**
 * Boîte de dialogue interactive pour les commandes Prompt "P", sélection multiple,
 * formatage du prompt et pipeline d'envoi WhatsApp (+18002428478) avec remplacement de l'image.
 */
@Composable
fun ProductPromptEnhancementDialog(
    mediaList: List<PromptTargetMedia>,
    primaryMediaId: String?,
    productTitle: String,
    initialProductDescription: String,
    onDismiss: () -> Unit,
    onMediaReplaced: (oldMediaId: String, newPath: String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Sélection de l'image cible (par défaut la miniature principale ou la première photo)
    var selectedMediaId by remember(primaryMediaId, mediaList) {
        mutableStateOf(
            primaryMediaId ?: mediaList.firstOrNull { !it.isVideo }?.id ?: mediaList.firstOrNull()?.id
        )
    }

    val currentTargetMedia = remember(selectedMediaId, mediaList) {
        mediaList.firstOrNull { it.id == selectedMediaId } ?: mediaList.firstOrNull()
    }

    // Commandes sélectionnées (sélection multiple)
    val selectedCommands = remember { mutableStateListOf<String>() }

    // Description textuelle du produit (éditable pour l'injection WhatsApp)
    var promptDescription by remember {
        mutableStateOf(initialProductDescription.ifBlank { productTitle })
    }

    // Filtrage par catégorie
    var selectedCategoryFilter by remember { mutableStateOf<PromptCategory?>(null) }

    // État du pipeline WhatsApp
    var isSendingToWhatsApp by remember { mutableStateOf(false) }
    var isPipelineActive by remember { mutableStateOf(false) }
    var isSimulatingAi by remember { mutableStateOf(false) }
    var interceptedResultPath by remember { mutableStateOf<String?>(null) }

    // Sélecteur pour intercepter manuellement l'image reçue dans WhatsApp
    val interceptImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null && currentTargetMedia != null) {
            coroutineScope.launch {
                val savedPath = WhatsAppPromptAutomationManager.interceptAndSaveImage(context, uri)
                if (savedPath != null) {
                    interceptedResultPath = savedPath
                    onMediaReplaced(currentTargetMedia.id, savedPath)
                    Toast.makeText(context, "Image WhatsApp interceptée et enregistrée !", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Aperçu dynamique du texte formaté
    val liveFormattedPayload = remember(selectedCommands.toList(), promptDescription) {
        WhatsAppPromptAutomationManager.formatPromptPayload(selectedCommands.toSet(), promptDescription)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .testTag("prompt_enhancement_dialog"),
        containerColor = Color(0xFF13111C),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                Brush.linearGradient(listOf(Color(0xFFA855F7), Color(0xFF6366F1))),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("P", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            "Studio Prompt & Retouche IA",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Pipeline WhatsApp (+18002428478)",
                            color = Color(0xFF25D366),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Fermer", tint = Color(0xFF9CA3AF))
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. IMAGE CIBLE & SÉLECTION SI PLUSIEURS
                Text(
                    "Image source à retoucher :",
                    color = Color(0xFFD1D5DB),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                if (mediaList.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        mediaList.forEach { item ->
                            val isSelected = item.id == currentTargetMedia?.id
                            val displayModel = remember(item.urlOrPath) {
                                ProductMediaManager.resolveMediaDisplayModel(context, item.urlOrPath)
                            }

                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF1E1B2E))
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) Color(0xFFA855F7) else Color(0xFF2D2A4A),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedMediaId = item.id }
                            ) {
                                SubcomposeAsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(displayModel)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Image source",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                if (isSelected) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFFA855F7),
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(3.dp)
                                            .size(16.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.padding(2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1F1D36),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Text(
                            "Aucune photo sélectionnée. Veuillez d'abord ajouter une photo au produit.",
                            color = Color(0xFFF87171),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                // 2. BANNIÈRE PIPELINE ACTIF (SI DÉCLENCHÉ)
                if (isPipelineActive) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF102A1E),
                        border = BorderStroke(1.dp, Color(0xFF25D366)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = Color(0xFF25D366)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Pipeline d'envoi WhatsApp actif",
                                        color = Color(0xFF25D366),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text("+18002428478", color = Color.White, fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "L'image et le prompt d'injection ont été transmis. Dès que l'image est générée en retour :",
                                color = Color(0xFFD1D5DB),
                                fontSize = 10.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Intercepter image reçue
                                Button(
                                    onClick = {
                                        interceptImagePicker.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Intercepter le retour", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                }

                                // Simuler / Générer immédiat
                                OutlinedButton(
                                    onClick = {
                                        if (currentTargetMedia != null) {
                                            coroutineScope.launch {
                                                isSimulatingAi = true
                                                val enhancedPath = WhatsAppPromptAutomationManager.simulateStudioAiEnhancement(
                                                    context,
                                                    currentTargetMedia.urlOrPath,
                                                    selectedCommands.toSet()
                                                )
                                                isSimulatingAi = false
                                                if (enhancedPath != null) {
                                                    interceptedResultPath = enhancedPath
                                                    onMediaReplaced(currentTargetMedia.id, enhancedPath)
                                                    Toast.makeText(context, "Image remplacée avec succès !", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    },
                                    border = BorderStroke(1.dp, Color(0xFFA855F7)),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (isSimulatingAi) {
                                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 1.5.dp, color = Color(0xFFA855F7))
                                    } else {
                                        Icon(Icons.Default.FlashOn, contentDescription = null, tint = Color(0xFFA855F7), modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Simuler l'IA", fontSize = 10.sp, color = Color(0xFFA855F7), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. DESCRIPTION TEXTUELLE POUR L'IA (INJECTION)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Description textuelle du produit (injectée au prompt) :",
                        color = Color(0xFFD1D5DB),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = promptDescription,
                        onValueChange = { promptDescription = it },
                        placeholder = { Text("Ex: Sacoche bandoulière en cuir imperméable...", fontSize = 11.sp, color = Color(0xFF6B7280)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFA855F7),
                            unfocusedBorderColor = Color(0xFF2D2A4A),
                            focusedContainerColor = Color(0xFF1E1B2E),
                            unfocusedContainerColor = Color(0xFF1E1B2E)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // 4. LISTE DÉROULANTE INTERACTIVE DES COMMANDES (SÉLECTION MULTIPLE)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Commandes d'injection (${selectedCommands.size} active${if (selectedCommands.size > 1) "s" else ""}) :",
                        color = Color(0xFFD1D5DB),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (selectedCommands.isNotEmpty()) {
                        Text(
                            "Effacer tout",
                            color = Color(0xFFF87171),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable { selectedCommands.clear() }
                        )
                    }
                }

                // Filtres de catégorie
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedCategoryFilter == null,
                        onClick = { selectedCategoryFilter = null },
                        label = { Text("Toutes (${PromptCommandCatalog.ALL_COMMANDS.size})", fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFA855F7),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF1E1B2E),
                            labelColor = Color(0xFF9CA3AF)
                        )
                    )
                    PromptCategory.values().forEach { cat ->
                        FilterChip(
                            selected = selectedCategoryFilter == cat,
                            onClick = { selectedCategoryFilter = if (selectedCategoryFilter == cat) null else cat },
                            label = { Text("${cat.iconEmoji} ${cat.title}", fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(cat.colorValue),
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFF1E1B2E),
                                labelColor = Color(0xFF9CA3AF)
                            )
                        )
                    }
                }

                // Groupes de commandes interactives
                val displayedCategories = if (selectedCategoryFilter != null) {
                    listOf(selectedCategoryFilter!!)
                } else {
                    PromptCategory.values().toList()
                }

                displayedCategories.forEach { category ->
                    val commandsForCat = when (category) {
                        PromptCategory.CLEANUP -> PromptCommandCatalog.CLEANUP_COMMANDS
                        PromptCategory.ECOMMERCE_STRUCTURE -> PromptCommandCatalog.ECOMMERCE_COMMANDS
                        PromptCategory.AMBIANCE_LIGHT -> PromptCommandCatalog.AMBIANCE_COMMANDS
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF171524))
                            .border(1.dp, Color(category.colorValue).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(category.iconEmoji, fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                category.title,
                                color = Color(category.colorValue),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        commandsForCat.forEach { cmd ->
                            val isChecked = selectedCommands.contains(cmd.command)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isChecked) Color(category.colorValue).copy(alpha = 0.15f) else Color.Transparent)
                                    .clickable {
                                        if (isChecked) selectedCommands.remove(cmd.command)
                                        else selectedCommands.add(cmd.command)
                                    }
                                    .padding(horizontal = 6.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { checked ->
                                        if (checked) selectedCommands.add(cmd.command)
                                        else selectedCommands.remove(cmd.command)
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = Color(category.colorValue),
                                        uncheckedColor = Color(0xFF4B5563)
                                    ),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        cmd.command,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (isChecked) Color.White else Color(0xFFE5E7EB)
                                    )
                                    Text(
                                        cmd.label,
                                        fontSize = 10.sp,
                                        color = Color(0xFF9CA3AF)
                                    )
                                }
                            }
                        }
                    }
                }

                // 5. APERÇU DU PROMPT FORMATÉ EN DIRECT (LIVE PREVIEW)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F0E17))
                        .border(1.dp, Color(0xFF2D2A4A), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Aperçu formaté du message WhatsApp :",
                            color = Color(0xFF9CA3AF),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF25D366).copy(alpha = 0.2f)
                        ) {
                            Text(
                                "+18002428478",
                                color = Color(0xFF25D366),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (liveFormattedPayload.isNotBlank()) liveFormattedPayload else "(Sélectionnez des commandes ci-dessus)",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = if (liveFormattedPayload.isNotBlank()) Color(0xFF38BDF8) else Color(0xFF6B7280)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (currentTargetMedia == null) {
                        Toast.makeText(context, "Veuillez d'abord ajouter une image au produit", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    coroutineScope.launch {
                        isSendingToWhatsApp = true
                        WhatsAppPromptAutomationManager.startEnhancementPipeline(
                            targetMediaId = currentTargetMedia.id,
                            originalMediaPath = currentTargetMedia.urlOrPath,
                            commands = selectedCommands.toSet(),
                            productTitle = productTitle,
                            productDescription = promptDescription
                        )

                        val success = WhatsAppPromptAutomationManager.dispatchToWhatsApp(
                            context = context,
                            imagePathOrUrl = currentTargetMedia.urlOrPath,
                            commands = selectedCommands.toSet(),
                            productDescription = promptDescription
                        )
                        isSendingToWhatsApp = false
                        isPipelineActive = true
                        Toast.makeText(
                            context,
                            "Message formaté et envoyé vers WhatsApp (+18002428478) !",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                },
                enabled = !isSendingToWhatsApp && currentTargetMedia != null,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("validate_prompt_whatsapp_button")
            ) {
                if (isSendingToWhatsApp) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Formatage et envoi...", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                } else {
                    Icon(Icons.Default.Send, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Valider & Envoyer vers WhatsApp (+18002428478)",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFF4B5563))
            ) {
                Text("Fermer", color = Color(0xFFD1D5DB), fontSize = 12.sp)
            }
        }
    )
}
