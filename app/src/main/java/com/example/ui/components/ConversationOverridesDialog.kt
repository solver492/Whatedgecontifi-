package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.AgentEntity
import com.example.data.local.entity.ConversationAgentOverrideEntity
import com.example.domain.baileys.PhoneNumberUtils
import com.example.ui.MainViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationOverridesDialog(
    viewModel: MainViewModel,
    overrides: List<ConversationAgentOverrideEntity>,
    agents: List<AgentEntity>,
    onDismiss: () -> Unit
) {
    var showAddForm by remember { mutableStateOf(false) }
    var newPhoneNumber by remember { mutableStateOf("+33 ") }
    var newContactName by remember { mutableStateOf("") }
    var newIsAiEnabled by remember { mutableStateOf(false) } // Par défaut Mode Humain quand on ajoute manuellement
    var newSelectedAgentId by remember { mutableStateOf<String?>(null) }
    var agentDropdownExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = ElegantDarkSurface,
            border = BorderStroke(1.dp, ElegantDarkBorder),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ElegantPurpleAccent.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = ElegantPurpleAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Contrôle par Numéro Client",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ElegantTextPrimary
                            )
                            Text(
                                text = "Activer / désactiver l'IA ou assigner un agent par numéro",
                                style = MaterialTheme.typography.labelSmall,
                                color = ElegantTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fermer",
                            tint = ElegantTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Actions Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showAddForm = !showAddForm },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (showAddForm) ElegantDarkSurfaceVariant else ElegantPurpleAccent
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = if (showAddForm) Icons.Default.ExpandLess else Icons.Default.PersonAdd,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (showAddForm) ElegantTextPrimary else ElegantPurpleOnAccent
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (showAddForm) "Fermer saisie" else "Ajouter numéro",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (showAddForm) ElegantTextPrimary else ElegantPurpleOnAccent
                        )
                    }

                    if (overrides.isNotEmpty()) {
                        OutlinedButton(
                            onClick = {
                                viewModel.resetAllConversationOverrides()
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFEF5350).copy(alpha = 0.5f)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = null,
                                tint = Color(0xFFEF5350),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Réactiver Tout",
                                fontSize = 11.sp,
                                color = Color(0xFFEF5350),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Add Form (Collapsible)
                AnimatedVisibility(
                    visible = showAddForm,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = ElegantDarkBg),
                        border = BorderStroke(1.dp, ElegantPurpleAccent.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Nouvelle règle client spécifique",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = ElegantPurpleAccent
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = newPhoneNumber,
                                onValueChange = { newPhoneNumber = it },
                                label = { Text("Numéro de téléphone (+33...)") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedTextField(
                                value = newContactName,
                                onValueChange = { newContactName = it },
                                label = { Text("Nom du contact (optionnel)") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Switch IA ON/OFF
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = if (newIsAiEnabled) "IA Automatique Activée" else "Mode Humain (IA Coupée)",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (newIsAiEnabled) WhatsAppGreen else Color(0xFFEF5350)
                                    )
                                    Text(
                                        text = if (newIsAiEnabled) "Les agents répondront automatiquement" else "L'IA ne répondra pas à ce numéro",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ElegantTextSecondary,
                                        fontSize = 10.sp
                                    )
                                }
                                Switch(
                                    checked = newIsAiEnabled,
                                    onCheckedChange = { newIsAiEnabled = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = WhatsAppGreen,
                                        checkedTrackColor = WhatsAppGreen.copy(alpha = 0.3f),
                                        uncheckedThumbColor = Color(0xFFEF5350),
                                        uncheckedTrackColor = Color(0xFFEF5350).copy(alpha = 0.3f)
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Agent Forcé
                            ExposedDropdownMenuBox(
                                expanded = agentDropdownExpanded,
                                onExpandedChange = { agentDropdownExpanded = it }
                            ) {
                                val selectedAgentObj = agents.firstOrNull { it.id == newSelectedAgentId }
                                OutlinedTextField(
                                    value = selectedAgentObj?.let { "${it.name} (${it.role})" } ?: "🤖 Routage Automatique (Tous agents)",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Agent dédié pour ce client") },
                                    shape = RoundedCornerShape(12.dp),
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = agentDropdownExpanded) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                )
                                ExposedDropdownMenu(
                                    expanded = agentDropdownExpanded,
                                    onDismissRequest = { agentDropdownExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("🤖 Routage Automatique (Par défaut)") },
                                        onClick = {
                                            newSelectedAgentId = null
                                            agentDropdownExpanded = false
                                        }
                                    )
                                    agents.forEach { agent ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(agent.name, fontWeight = FontWeight.Bold)
                                                    Text(agent.role, fontSize = 11.sp, color = ElegantTextSecondary)
                                                }
                                            },
                                            onClick = {
                                                newSelectedAgentId = agent.id
                                                agentDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = {
                                    val clean = PhoneNumberUtils.extractCleanDigits(newPhoneNumber)
                                    if (clean.length >= 6) {
                                        val canonical = PhoneNumberUtils.toCanonicalJid(newPhoneNumber)
                                        val cName = newContactName.ifBlank { newPhoneNumber.trim() }
                                        viewModel.setConversationAiEnabled(canonical, cName, newIsAiEnabled)
                                        if (newSelectedAgentId != null) {
                                            viewModel.setConversationForcedAgent(canonical, cName, newSelectedAgentId)
                                        }
                                        newPhoneNumber = "+33 "
                                        newContactName = ""
                                        newIsAiEnabled = false
                                        newSelectedAgentId = null
                                        showAddForm = false
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ElegantPurpleAccent)
                            ) {
                                Text("Enregistrer cette règle", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // List of Overrides
                if (overrides.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.CheckCircleOutline,
                                contentDescription = null,
                                tint = WhatsAppGreen,
                                modifier = Modifier.size(42.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Tous les numéros sont en mode IA automatique",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = ElegantTextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Aucun numéro n'a l'IA désactivée ni d'agent forcé.",
                                style = MaterialTheme.typography.labelSmall,
                                color = ElegantTextSecondary
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Règles actives par numéro (${overrides.size}) :",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = ElegantTextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(overrides, key = { it.remoteJid }) { item ->
                            val assignedAgent = agents.firstOrNull { it.id == item.forcedAgentId }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = ElegantDarkBg),
                                border = BorderStroke(
                                    1.dp,
                                    if (!item.isAiEnabled) Color(0xFFEF5350).copy(alpha = 0.5f) else WhatsAppGreen.copy(alpha = 0.5f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (item.isAiEnabled) WhatsAppGreen.copy(alpha = 0.2f)
                                                        else Color(0xFFEF5350).copy(alpha = 0.2f)
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = if (item.isAiEnabled) Icons.Default.SmartToy else Icons.Default.Person,
                                                    contentDescription = null,
                                                    tint = if (item.isAiEnabled) WhatsAppGreen else Color(0xFFEF5350),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = if (item.contactName.isNotBlank()) item.contactName else item.remoteJid.substringBefore("@"),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = ElegantTextPrimary,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = item.remoteJid,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = ElegantTextSecondary,
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }

                                        // Status badge
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (item.isAiEnabled) WhatsAppGreen.copy(alpha = 0.15f) else Color(0xFFEF5350).copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = if (item.isAiEnabled) "IA Active" else "Mode Humain",
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp,
                                                color = if (item.isAiEnabled) WhatsAppGreen else Color(0xFFEF5350)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Agent dédié info
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = if (assignedAgent != null) "Agent dédié : ${assignedAgent.name}" else "Agent : Routage auto standard",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (assignedAgent != null) ElegantPurpleAccent else ElegantTextSecondary,
                                            fontSize = 11.sp
                                        )

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            // Toggle IA Button
                                            TextButton(
                                                onClick = {
                                                    viewModel.setConversationAiEnabled(
                                                        item.remoteJid,
                                                        item.contactName,
                                                        !item.isAiEnabled
                                                    )
                                                },
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                modifier = Modifier.height(28.dp)
                                            ) {
                                                Text(
                                                    text = if (item.isAiEnabled) "Désactiver IA" else "Activer IA",
                                                    fontSize = 11.sp,
                                                    color = if (item.isAiEnabled) Color(0xFFEF5350) else WhatsAppGreen,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(4.dp))

                                            // Delete rule button
                                            IconButton(
                                                onClick = {
                                                    viewModel.removeConversationOverride(item.remoteJid)
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.DeleteOutline,
                                                    contentDescription = "Supprimer règle",
                                                    tint = ElegantTextSecondary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Footer notice
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = ElegantDarkBg,
                    border = BorderStroke(1.dp, ElegantDarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = Color(0xFF00B0FF),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Isolation garantie : désactiver l'IA pour un client ne coupe JAMAIS les autres numéros.",
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
