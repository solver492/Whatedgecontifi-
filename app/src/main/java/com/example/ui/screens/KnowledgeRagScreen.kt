package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.AgentEntity
import com.example.data.local.entity.KnowledgeSourceEntity
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

@Composable
fun KnowledgeRagScreen(
    viewModel: MainViewModel,
    sources: List<KnowledgeSourceEntity>,
    agents: List<AgentEntity>
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(ElegantDarkBg)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                // Header Banner
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
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = null,
                                    tint = ElegantPurpleAccent
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Base de Connaissances RAG",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ElegantTextPrimary
                                )
                                Text(
                                    text = "Alimentez vos agents IA en Supabase, Liens Web, PDF et Textes",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ElegantPurpleSecondary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = ElegantDarkBg.copy(alpha = 0.6f),
                            border = BorderStroke(1.dp, ElegantDarkBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(ElegantGreenActive)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${sources.size} sources indexées pour injection de contexte WhatsApp",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = ElegantTextPrimary
                                )
                            }
                        }
                    }
                }
            }

            items(sources, key = { it.id }) { source ->
                KnowledgeCard(
                    source = source,
                    onDelete = { viewModel.deleteKnowledgeSource(source.id) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(84.dp))
            }
        }

        // Add source FAB
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("add_knowledge_fab"),
            containerColor = ElegantPurpleAccent,
            contentColor = ElegantPurpleOnAccent,
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Ajouter Source")
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Ajouter Source", fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showAddDialog) {
        AddKnowledgeSourceDialog(
            agents = agents,
            onDismiss = { showAddDialog = false },
            onAdd = { type, title, target, content, agentId, anonKey, table ->
                viewModel.addKnowledgeSource(
                    type = type,
                    title = title,
                    targetUrlOrConfig = target,
                    contentData = content,
                    agentId = agentId,
                    supabaseAnonKey = anonKey,
                    supabaseTable = table
                )
                showAddDialog = false
            }
        )
    }
}

@Composable
fun KnowledgeCard(
    source: KnowledgeSourceEntity,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("knowledge_card_${source.id}"),
        colors = CardDefaults.cardColors(
            containerColor = ElegantDarkSurface
        ),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, ElegantDarkBorder)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    val (icon, tintColor) = when (source.type) {
                        "SUPABASE" -> Pair(Icons.Default.Storage, ElegantGreenActive)
                        "WEB_URL" -> Pair(Icons.Default.Language, ElegantPurpleAccent)
                        "PDF_DOC" -> Pair(Icons.Default.PictureAsPdf, ElegantPinkTertiary)
                        else -> Pair(Icons.Default.TextSnippet, Color(0xFFFFD54F))
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(ElegantDarkCardDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = icon, contentDescription = null, tint = tintColor, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = source.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ElegantTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${source.type} • ${source.chunkCount} chunks indexés",
                            style = MaterialTheme.typography.bodySmall,
                            color = ElegantPurpleSecondary
                        )
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Supprimer",
                        tint = ElegantTextSecondary.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // URL or Config target
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = ElegantDarkBg,
                border = BorderStroke(1.dp, ElegantDarkBorder)
            ) {
                Text(
                    text = source.targetUrlOrConfig,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = ElegantPurpleAccent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Text preview
            Text(
                text = source.contentData,
                style = MaterialTheme.typography.bodySmall,
                color = ElegantTextSecondary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun AddKnowledgeSourceDialog(
    agents: List<AgentEntity>,
    onDismiss: () -> Unit,
    onAdd: (
        type: String,
        title: String,
        target: String,
        content: String,
        agentId: String,
        supabaseAnonKey: String,
        supabaseTable: String
    ) -> Unit
) {
    var selectedType by remember { mutableStateOf("SUPABASE") }
    var title by remember { mutableStateOf("") }
    var targetUrl by remember { mutableStateOf("") }
    var contentText by remember { mutableStateOf("") }
    var selectedAgentId by remember { mutableStateOf("*") }
    var supabaseAnonKey by remember { mutableStateOf("") }
    var supabaseTable by remember { mutableStateOf("documents") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = ElegantDarkSurface,
        title = {
            Text(
                "Nouvelle Source de Connaissances",
                fontWeight = FontWeight.Bold,
                color = ElegantTextPrimary
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        "Type de source :",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = ElegantPurpleAccent
                    )
                    val types = listOf(
                        "SUPABASE" to "Base Supabase DB",
                        "WEB_URL" to "Lien Page Web (Crawler)",
                        "PDF_DOC" to "Fichier Document PDF",
                        "TEXT_SNIPPET" to "Texte / FAQ Manuel"
                    )
                    Column {
                        types.forEach { (type, label) ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedType = type
                                        if (title.isBlank()) {
                                            title = when (type) {
                                                "SUPABASE" -> "Base Supabase Données"
                                                "WEB_URL" -> "Documentation Site Web"
                                                "PDF_DOC" -> "Manuel_Produit.pdf"
                                                else -> "Politique & Tarifs"
                                            }
                                        }
                                    }
                            ) {
                                RadioButton(selected = selectedType == type, onClick = { selectedType = type })
                                Text(label, style = MaterialTheme.typography.bodySmall, color = ElegantTextPrimary)
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Titre de la source") },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                when (selectedType) {
                    "SUPABASE" -> {
                        item {
                            OutlinedTextField(
                                value = targetUrl,
                                onValueChange = { targetUrl = it },
                                label = { Text("URL Projet Supabase") },
                                placeholder = { Text("https://xxx.supabase.co") },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = supabaseTable,
                                onValueChange = { supabaseTable = it },
                                label = { Text("Nom de la table (ex: clients, faq)") },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = supabaseAnonKey,
                                onValueChange = { supabaseAnonKey = it },
                                label = { Text("Supabase Anon Key") },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = contentText,
                                onValueChange = { contentText = it },
                                label = { Text("Extrait de données ou test synchro") },
                                placeholder = { Text("Table produits: Pack Pro 79€, Pack Entreprise...") },
                                shape = RoundedCornerShape(14.dp),
                                minLines = 2,
                                maxLines = 4,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    "WEB_URL" -> {
                        item {
                            OutlinedTextField(
                                value = targetUrl,
                                onValueChange = { targetUrl = it },
                                label = { Text("Lien URL du site web") },
                                placeholder = { Text("https://monsite.com/documentation") },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = contentText,
                                onValueChange = { contentText = it },
                                label = { Text("Contenu extrait de la page") },
                                placeholder = { Text("Collez ou inspectez les données de la page web...") },
                                shape = RoundedCornerShape(14.dp),
                                minLines = 3,
                                maxLines = 5,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    "PDF_DOC" -> {
                        item {
                            OutlinedTextField(
                                value = targetUrl,
                                onValueChange = { targetUrl = it },
                                label = { Text("Nom ou chemin du PDF") },
                                placeholder = { Text("Conditions_Generales_2025.pdf") },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = contentText,
                                onValueChange = { contentText = it },
                                label = { Text("Texte extrait du document PDF") },
                                placeholder = { Text("Articles, paragraphes et clauses à faire apprendre à l'agent...") },
                                shape = RoundedCornerShape(14.dp),
                                minLines = 3,
                                maxLines = 5,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    else -> {
                        item {
                            OutlinedTextField(
                                value = contentText,
                                onValueChange = { contentText = it },
                                label = { Text("Contenu textuel (FAQs, politiques, tarifs)") },
                                shape = RoundedCornerShape(14.dp),
                                minLines = 3,
                                maxLines = 6,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                item {
                    Text(
                        "Agent associé :",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = ElegantPurpleAccent
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = selectedAgentId == "*", onClick = { selectedAgentId = "*" })
                        Text("Tous les agents (*)", style = MaterialTheme.typography.bodySmall, color = ElegantTextPrimary)
                    }
                    agents.forEach { agent ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = selectedAgentId == agent.id, onClick = { selectedAgentId = agent.id })
                            Text(agent.name, style = MaterialTheme.typography.bodySmall, color = ElegantTextPrimary)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val finalContent = contentText.ifEmpty { "Source $title initialisée avec succès." }
                        val finalTarget = targetUrl.ifEmpty { "Source locale" }
                        onAdd(selectedType, title, finalTarget, finalContent, selectedAgentId, supabaseAnonKey, supabaseTable)
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ElegantPurpleAccent, contentColor = ElegantPurpleOnAccent)
            ) {
                Text("Enregistrer Source", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", color = ElegantTextSecondary)
            }
        }
    )
}
