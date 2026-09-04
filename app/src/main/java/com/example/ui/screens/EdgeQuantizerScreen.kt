package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.engine.AiEdgeQuantizerEngine
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
import com.example.ui.theme.ElegantTextMuted
import com.example.ui.theme.ElegantTextPrimary
import com.example.ui.theme.ElegantTextSecondary
import com.example.ui.theme.WhatsAppGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EdgeQuantizerScreen(
    viewModel: MainViewModel,
    quantizationStatus: String?
) {
    val models = AiEdgeQuantizerEngine.availableModels

    var selectedModelToQuantize by remember { mutableStateOf("Gemma-2 2B (PyTorch FP16)") }
    var selectedRecipe by remember { mutableStateOf("INT4 Blockwise + Hadamard (Mobile NPU)") }

    var expandedModelMenu by remember { mutableStateOf(false) }
    var expandedRecipeMenu by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ElegantDarkBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            // Header Banner in Elegant Dark
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = ElegantDarkSurfaceVariant
                ),
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
                            Icon(
                                imageVector = Icons.Default.Memory,
                                contentDescription = null,
                                tint = ElegantPurpleAccent
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Moteur AI-Edge Quantizer",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ElegantTextPrimary
                            )
                            Text(
                                text = "Optimisation PTQ & Exécution LLM on-device pour mobile",
                                style = MaterialTheme.typography.bodySmall,
                                color = ElegantPurpleSecondary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = ElegantDarkBg.copy(alpha = 0.6f),
                            border = BorderStroke(1.dp, ElegantDarkBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(ElegantGreenActive)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "NPU / Hexagon DSP",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ElegantTextPrimary
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = ElegantDarkBg.copy(alpha = 0.6f),
                            border = BorderStroke(1.dp, ElegantDarkBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(ElegantPurpleAccent)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "LiteRT-LM & INT4",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ElegantTextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quantization Workbench card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quantization_workbench_card"),
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
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(ElegantDarkCardDark),
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
                                text = "ON-DEVICE",
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
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

        // Section Title: Deployed Models
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Modèles Locaux Déployés",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ElegantTextPrimary
                )
                Text(
                    text = "${models.size} modèles",
                    style = MaterialTheme.typography.labelSmall,
                    color = ElegantPurpleSecondary
                )
            }
        }

        items(models, key = { it.id }) { model ->
            ModelCard(model = model)
        }

        item {
            Spacer(modifier = Modifier.height(84.dp))
        }
    }
}

@Composable
fun ModelCard(model: EdgeQuantizedModelInfo) {
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
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (model.isLoadedInRam) ElegantGreenActive else ElegantTextMuted)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = model.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ElegantTextPrimary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (model.isLoadedInRam) ElegantDarkBg else ElegantDarkSurfaceVariant,
                    border = BorderStroke(1.dp, if (model.isLoadedInRam) ElegantPurpleAccent.copy(alpha = 0.5f) else ElegantDarkBorder)
                ) {
                    Text(
                        text = if (model.isLoadedInRam) "INT4 • Actif" else "Inactif",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (model.isLoadedInRam) ElegantPurpleAccent else ElegantTextMuted,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Recette : ${model.quantizationRecipe}",
                style = MaterialTheme.typography.bodySmall,
                color = ElegantPurpleSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Stats grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Footprint
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    color = ElegantDarkBg,
                    border = BorderStroke(1.dp, ElegantDarkBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "RAM",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElegantTextSecondary,
                            fontSize = 10.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${model.memoryFootprintMb} Mo",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = ElegantTextPrimary
                        )
                    }
                }

                // Speed
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    color = ElegantDarkBg,
                    border = BorderStroke(1.dp, ElegantDarkBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Vitesse",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElegantTextSecondary,
                            fontSize = 10.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${model.inferenceThroughputTokPerSec} t/s",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = ElegantGreenActive
                        )
                    }
                }

                // Similarity
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    color = ElegantDarkBg,
                    border = BorderStroke(1.dp, ElegantDarkBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Similarité",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElegantTextSecondary,
                            fontSize = 10.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${(model.cosineSimilarity * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = ElegantPurpleAccent
                        )
                    }
                }
            }
        }
    }
}
