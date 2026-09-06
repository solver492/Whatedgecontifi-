package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.engine.AiEdgeQuantizerEngine
import com.example.domain.engine.DeviceInferenceTestResult
import com.example.domain.engine.EdgeModelCatalogItem
import com.example.domain.engine.EdgeQuantizedModelInfo
import com.example.ui.MainViewModel
import com.example.ui.theme.EdgeAiCyan
import com.example.ui.theme.ElegantDarkBg
import com.example.ui.theme.ElegantDarkBorder
import com.example.ui.theme.ElegantDarkCardDark
import com.example.ui.theme.ElegantDarkCardElevated
import com.example.ui.theme.ElegantDarkSurface
import com.example.ui.theme.ElegantDarkSurfaceVariant
import com.example.ui.theme.ElegantGreenActive
import com.example.ui.theme.ElegantPinkTertiary
import com.example.ui.theme.ElegantPurpleAccent
import com.example.ui.theme.ElegantPurpleOnAccent
import com.example.ui.theme.ElegantPurpleSecondary
import com.example.ui.theme.ElegantRedAlert
import com.example.ui.theme.ElegantTextMuted
import com.example.ui.theme.ElegantTextPrimary
import com.example.ui.theme.ElegantTextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EdgeQuantizerScreen(
    viewModel: MainViewModel,
    quantizationStatus: String?
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Catalogue & Téléchargement", "Banc d'Essai On-Device", "Atelier PTQ")

    val catalog = viewModel.modelCatalog
    val downloadStates by viewModel.downloadStates.collectAsState()
    val downloadedModels by viewModel.downloadedModels.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ElegantDarkBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            // Header Hero
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ElegantDarkSurfaceVariant),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(ElegantDarkBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Memory, contentDescription = null, tint = ElegantPurpleAccent)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Moteur AI-Edge Quantizer & LiteRT",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ElegantTextPrimary
                            )
                            Text(
                                text = "Modèles IA 100% Locaux sur processeur mobile NPU / CPU",
                                style = MaterialTheme.typography.bodySmall,
                                color = ElegantPurpleSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = ElegantDarkBg.copy(alpha = 0.6f),
                            border = BorderStroke(1.dp, ElegantDarkBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(ElegantGreenActive))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${downloadedModels.size} modèle(s) installé(s)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = ElegantTextPrimary,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = ElegantDarkBg.copy(alpha = 0.6f),
                            border = BorderStroke(1.dp, ElegantDarkBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(ElegantPurpleAccent))
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "NPU Hexagon • LiteRT",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ElegantPurpleAccent,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }
        }

        // Sub Tabs
        item {
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = ElegantDarkSurface,
                contentColor = ElegantPurpleAccent,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, ElegantDarkBorder, RoundedCornerShape(16.dp))
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) ElegantPurpleAccent else ElegantTextSecondary,
                                maxLines = 1
                            )
                        }
                    )
                }
            }
        }

        when (selectedTab) {
            // TAB 0: Catalog & Download
            0 -> {
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Catalogue de Modèles Disponibles au Téléchargement",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ElegantTextPrimary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Text(
                            text = "Téléchargez les modèles Edge LiteRT ou calibrez-les directement pour une exécution 100% hors-ligne.",
                            style = MaterialTheme.typography.bodySmall,
                            color = ElegantTextSecondary
                        )
                    }
                }

                // Hugging Face Access Token Card (Optional for Gated Repos)
                item {
                    var showTokenInput by remember { mutableStateOf(false) }
                    var hfTokenText by remember { mutableStateOf(viewModel.modelManager.huggingFaceToken ?: "") }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = ElegantDarkSurface,
                        border = BorderStroke(1.dp, ElegantDarkBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showTokenInput = !showTokenInput },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.VpnKey,
                                        contentDescription = null,
                                        tint = ElegantPurpleAccent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Clé d'Accès Hugging Face (Optionnel)",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = ElegantTextPrimary
                                        )
                                        Text(
                                            text = if (hfTokenText.isNotBlank()) "✓ Jeton HF configuré" else "Permet d'accéder aux modèles officiels restreints",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (hfTokenText.isNotBlank()) ElegantGreenActive else ElegantTextSecondary
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = if (showTokenInput) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = ElegantTextSecondary
                                )
                            }

                            if (showTokenInput) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Les miroirs publics communautaires sont activés par défaut. Vous pouvez spécifier votre token personnel HF (hf_...) pour accéder directement aux dépôts officiels Meta Llama et Google Gemma.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ElegantTextSecondary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = hfTokenText,
                                    onValueChange = {
                                        hfTokenText = it
                                        viewModel.setHuggingFaceToken(if (it.isBlank()) null else it)
                                    },
                                    placeholder = { Text("hf_xxxxxxxxxxxxxxxxxxxxxxxxx") },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }
                        }
                    }
                }

                items(catalog, key = { it.id }) { item ->
                    val isDownloaded = downloadedModels.any { it.id == item.id } || viewModel.modelManager.isModelDownloaded(item.id)
                    val state = downloadStates[item.id]

                    ModelCatalogCard(
                        item = item,
                        isDownloaded = isDownloaded,
                        downloadState = state,
                        onDownload = { viewModel.downloadModel(item) },
                        onCalibrateInstall = { viewModel.calibrateAndInstallModel(item) },
                        onCancel = { viewModel.cancelModelDownload(item.id) },
                        onDelete = { viewModel.deleteDownloadedModel(item.id) }
                    )
                }
            }

            // TAB 1: On-Device Inference Benchmark
            1 -> {
                item {
                    InferenceWorkbenchCard(
                        viewModel = viewModel,
                        catalog = catalog,
                        downloadedModels = downloadedModels
                    )
                }
            }

            // TAB 2: PTQ Quantization Studio
            2 -> {
                item {
                    PtqStudioCard(
                        viewModel = viewModel,
                        quantizationStatus = quantizationStatus
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(84.dp))
        }
    }
}

@Composable
fun ModelCatalogCard(
    item: EdgeModelCatalogItem,
    isDownloaded: Boolean,
    downloadState: com.example.domain.engine.ModelDownloadState?,
    onDownload: () -> Unit,
    onCalibrateInstall: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("catalog_model_${item.id}"),
        colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, if (isDownloaded) ElegantGreenActive.copy(alpha = 0.5f) else ElegantDarkBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(if (isDownloaded) ElegantGreenActive else ElegantPurpleSecondary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ElegantTextPrimary
                        )
                    }
                    Text(
                        text = "${item.architecture} • ${item.quantizationRecipe}",
                        style = MaterialTheme.typography.bodySmall,
                        color = ElegantPurpleSecondary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isDownloaded) ElegantGreenActive.copy(alpha = 0.15f) else ElegantDarkBg,
                    border = BorderStroke(1.dp, if (isDownloaded) ElegantGreenActive else ElegantDarkBorder)
                ) {
                    Text(
                        text = if (isDownloaded) "INSTALLÉ" else "${item.sizeMb} Mo",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isDownloaded) ElegantGreenActive else ElegantTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodySmall,
                color = ElegantTextSecondary
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Progress bar if downloading
            if (downloadState != null && downloadState.isDownloading) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = downloadState.statusText,
                            style = MaterialTheme.typography.labelSmall,
                            color = ElegantPurpleAccent,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${downloadState.progressPercent}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElegantTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { downloadState.progressPercent / 100f },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = ElegantPurpleAccent,
                        trackColor = ElegantDarkBg
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, ElegantRedAlert.copy(alpha = 0.7f))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = ElegantRedAlert, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Annuler le Téléchargement", color = ElegantRedAlert)
                    }
                }
            } else if (downloadState?.errorMessage != null) {
                Text(
                    text = downloadState.errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = ElegantRedAlert,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onDownload,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ElegantPurpleAccent)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Réessayer")
                    }
                    OutlinedButton(
                        onClick = onCalibrateInstall,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, ElegantPurpleSecondary)
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = ElegantPurpleSecondary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Installer Local", color = ElegantPurpleSecondary)
                    }
                }
            } else if (isDownloaded) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        color = ElegantDarkBg,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, ElegantDarkBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ElegantGreenActive, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Prêt pour instances & agents", style = MaterialTheme.typography.labelSmall, color = ElegantTextPrimary)
                        }
                    }

                    OutlinedButton(
                        onClick = onDelete,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, ElegantDarkBorder)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = ElegantTextSecondary, modifier = Modifier.size(16.dp))
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = onDownload,
                        modifier = Modifier.fillMaxWidth().testTag("download_model_${item.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ElegantPurpleAccent, contentColor = ElegantPurpleOnAccent)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Télécharger le Modèle (${item.sizeMb} Mo)", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onCalibrateInstall,
                        modifier = Modifier.fillMaxWidth().testTag("calibrate_model_${item.id}"),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, ElegantPurpleSecondary.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = ElegantPurpleSecondary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Installer / Calibrer Localement (Sans données)", color = ElegantPurpleSecondary, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun InferenceWorkbenchCard(
    viewModel: MainViewModel,
    catalog: List<EdgeModelCatalogItem>,
    downloadedModels: List<com.example.domain.engine.DownloadedModelRecord>
) {
    var selectedModelId by remember { mutableStateOf(catalog.firstOrNull()?.id ?: "smollm2-135m-instruct") }
    var selectedBackend by remember { mutableStateOf("NPU") }
    var promptInput by remember { mutableStateOf("Bonjour ! Quels sont vos tarifs et comment fonctionne le support ?") }
    var systemPromptInput by remember { mutableStateOf("Tu es un agent d'accueil et commercial WhatsApp pour notre boutique.") }
    var temperature by remember { mutableFloatStateOf(0.7f) }
    var showAdvancedParams by remember { mutableStateOf(false) }
    var isRunningInference by remember { mutableStateOf(false) }
    var inferenceResult by remember { mutableStateOf<DeviceInferenceTestResult?>(null) }
    var expandedModelDropdown by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    val sampleQueries = listOf(
        "Combien coûte le pack pro ?",
        "Statut commande #CMD-9201",
        "Comment configurer Termux ?",
        "Écris un message d'accueil",
        "Je veux parler à un conseiller"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, ElegantDarkBorder)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(ElegantDarkBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Psychology, contentDescription = null, tint = ElegantPurpleAccent)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Banc d'Essai d'Inférence On-Device",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ElegantTextPrimary
                    )
                    Text(
                        text = "Prompting en direct et exécution de modèles IA locaux",
                        style = MaterialTheme.typography.bodySmall,
                        color = ElegantPurpleSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Model Picker
            Text("Sélectionner le Modèle :", style = MaterialTheme.typography.labelMedium, color = ElegantPurpleAccent, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))

            ExposedDropdownMenuBox(
                expanded = expandedModelDropdown,
                onExpandedChange = { expandedModelDropdown = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                val selectedName = if (selectedModelId == "gemini-3.5-flash") {
                    "Google Gemini 3.5 Flash (Cloud)"
                } else {
                    catalog.firstOrNull { it.id == selectedModelId }?.name ?: selectedModelId
                }

                OutlinedTextField(
                    value = selectedName,
                    onValueChange = {},
                    readOnly = true,
                    shape = RoundedCornerShape(14.dp),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedModelDropdown) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded = expandedModelDropdown,
                    onDismissRequest = { expandedModelDropdown = false }
                ) {
                    // Gemini Cloud Option
                    DropdownMenuItem(
                        text = {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CloudQueue, contentDescription = null, tint = ElegantPurpleAccent, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Google Gemini 3.5 Flash (Cloud)", color = ElegantTextPrimary, fontWeight = FontWeight.SemiBold)
                                }
                                Text("Inférence cloud Google Gemini API", color = ElegantTextSecondary, fontSize = 11.sp)
                            }
                        },
                        onClick = {
                            selectedModelId = "gemini-3.5-flash"
                            expandedModelDropdown = false
                        }
                    )

                    catalog.forEach { item ->
                        val isDownloaded = downloadedModels.any { it.id == item.id } || viewModel.modelManager.isModelDownloaded(item.id)
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(item.name, color = ElegantTextPrimary, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        text = if (isDownloaded) "✓ Installé sur stockage local (${item.sizeMb} Mo)" else "Disponible (${item.sizeMb} Mo)",
                                        color = if (isDownloaded) ElegantGreenActive else ElegantTextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            },
                            onClick = {
                                selectedModelId = item.id
                                expandedModelDropdown = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Backend Selector
            Text("Accélérateur Matériel :", style = MaterialTheme.typography.labelMedium, color = ElegantPurpleAccent, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("NPU" to "NPU Hexagon", "GPU" to "Adreno GPU", "CPU" to "Arm CPU").forEach { (code, label) ->
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedBackend = code },
                        color = if (selectedBackend == code) ElegantPurpleAccent else ElegantDarkBg,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, if (selectedBackend == code) ElegantPurpleAccent else ElegantDarkBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = code,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedBackend == code) ElegantPurpleOnAccent else ElegantTextPrimary
                            )
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                color = if (selectedBackend == code) ElegantPurpleOnAccent.copy(alpha = 0.8f) else ElegantTextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sample Queries Chips
            Text("Questions d'essai rapides :", style = MaterialTheme.typography.labelSmall, color = ElegantTextSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                sampleQueries.forEach { query ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ElegantDarkCardDark,
                        border = BorderStroke(1.dp, ElegantDarkBorder),
                        modifier = Modifier.clickable { promptInput = query }
                    ) {
                        Text(
                            text = query,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = ElegantPurpleAccent
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Prompt input
            OutlinedTextField(
                value = promptInput,
                onValueChange = { promptInput = it },
                label = { Text("Prompt ou Message à tester") },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().testTag("workbench_prompt_input"),
                minLines = 2,
                maxLines = 4
            )

            // Advanced settings toggle
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showAdvancedParams = !showAdvancedParams },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "⚙️ Paramètres avancés (Prompt système, Température)",
                    style = MaterialTheme.typography.labelSmall,
                    color = ElegantPurpleSecondary
                )
                Icon(
                    imageVector = if (showAdvancedParams) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = ElegantPurpleSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            if (showAdvancedParams) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = systemPromptInput,
                    onValueChange = { systemPromptInput = it },
                    label = { Text("Prompt Système / Persona") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Température: ${String.format(java.util.Locale.US, "%.1f", temperature)}", style = MaterialTheme.typography.labelSmall, color = ElegantTextSecondary)
                    Slider(
                        value = temperature,
                        onValueChange = { temperature = it },
                        valueRange = 0.1f..1.0f,
                        steps = 9,
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Test Button
            Button(
                onClick = {
                    coroutineScope.launch {
                        isRunningInference = true
                        try {
                            val res = viewModel.runDeviceInference(
                                modelId = selectedModelId,
                                prompt = promptInput,
                                backend = selectedBackend,
                                temperature = temperature,
                                systemPrompt = systemPromptInput
                            )
                            inferenceResult = res
                        } finally {
                            isRunningInference = false
                        }
                    }
                },
                enabled = !isRunningInference && promptInput.isNotBlank(),
                modifier = Modifier.fillMaxWidth().testTag("execute_workbench_test"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ElegantPurpleAccent, contentColor = ElegantPurpleOnAccent)
            ) {
                if (isRunningInference) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = ElegantPurpleOnAccent, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Inférence en cours sur $selectedBackend...", fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.Bolt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Exécuter le Prompt en Direct", fontWeight = FontWeight.Bold)
                }
            }

            // Results display
            inferenceResult?.let { result ->
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = ElegantDarkBg,
                    border = BorderStroke(1.dp, ElegantPurpleAccent.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Réponse générée :",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = ElegantPurpleAccent
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = ElegantGreenActive.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, ElegantGreenActive)
                            ) {
                                Text(
                                    text = "${result.speedTokensPerSec} tok/s • ${result.latencyMs} ms",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ElegantGreenActive,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = result.outputText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = ElegantTextPrimary
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Modèle: ${result.modelName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = ElegantTextSecondary,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "• Accélérateur: ${result.backendUsed}",
                                style = MaterialTheme.typography.labelSmall,
                                color = ElegantTextSecondary,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "• Tokens: ${result.tokensGenerated}",
                                style = MaterialTheme.typography.labelSmall,
                                color = ElegantTextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PtqStudioCard(
    viewModel: MainViewModel,
    quantizationStatus: String?
) {
    var selectedModelToQuantize by remember { mutableStateOf("Gemma-2 2B (PyTorch FP16)") }
    var selectedRecipe by remember { mutableStateOf("INT4 Blockwise + Hadamard (Mobile NPU)") }
    var expandedModelMenu by remember { mutableStateOf(false) }
    var expandedRecipeMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, ElegantDarkBorder)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(36.dp).clip(CircleShape).background(ElegantDarkCardDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PrecisionManufacturing, contentDescription = null, tint = ElegantPurpleAccent, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Atelier de Quantification PTQ",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ElegantTextPrimary
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ElegantDarkBg,
                    border = BorderStroke(1.dp, ElegantDarkBorder)
                ) {
                    Text(
                        text = "LITERT COMPATIBLE",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = ElegantPurpleAccent,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = "Calibrez et convertissez un modèle lourd en artefact INT4 mobile optimisé sans perte de précision.",
                style = MaterialTheme.typography.bodySmall,
                color = ElegantTextSecondary,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Model Selector
            ExposedDropdownMenuBox(
                expanded = expandedModelMenu,
                onExpandedChange = { expandedModelMenu = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedModelToQuantize,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Modèle Source") },
                    shape = RoundedCornerShape(14.dp),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedModelMenu) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded = expandedModelMenu,
                    onDismissRequest = { expandedModelMenu = false }
                ) {
                    listOf(
                        "Gemma-2 2B (PyTorch FP16)",
                        "Llama-3.2 1B (Safetensors FP16)",
                        "Phi-3.5-mini 3.8B (FP16)",
                        "Whisper Small Audio (FP32)"
                    ).forEach { modelOption ->
                        DropdownMenuItem(
                            text = { Text(modelOption) },
                            onClick = {
                                selectedModelToQuantize = modelOption
                                expandedModelMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Recipe Selector
            ExposedDropdownMenuBox(
                expanded = expandedRecipeMenu,
                onExpandedChange = { expandedRecipeMenu = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedRecipe,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Recette de Quantification") },
                    shape = RoundedCornerShape(14.dp),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRecipeMenu) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded = expandedRecipeMenu,
                    onDismissRequest = { expandedRecipeMenu = false }
                ) {
                    listOf(
                        "INT4 Blockwise + Hadamard (Mobile NPU)",
                        "INT4 GPTQ (Second-Order Taylor Hessian)",
                        "INT8 SRQ (Static Range Activation Quantization)",
                        "Selective Mixed Precision (INT4 W / INT8 A)"
                    ).forEach { recipeOption ->
                        DropdownMenuItem(
                            text = { Text(recipeOption) },
                            onClick = {
                                selectedRecipe = recipeOption
                                expandedRecipeMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (quantizationStatus != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ElegantDarkBg, RoundedCornerShape(14.dp))
                        .border(BorderStroke(1.dp, ElegantPurpleAccent.copy(alpha = 0.5f)), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = ElegantPurpleAccent)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = quantizationStatus,
                        style = MaterialTheme.typography.bodySmall,
                        color = ElegantTextPrimary
                    )
                }
            } else {
                Button(
                    onClick = {
                        viewModel.runQuantizationPipeline(selectedModelToQuantize, selectedRecipe)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElegantPurpleAccent, contentColor = ElegantPurpleOnAccent)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Lancer la Quantification & Exporter", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
