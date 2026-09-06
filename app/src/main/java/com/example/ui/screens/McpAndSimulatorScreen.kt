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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Webhook
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.AgentEntity
import com.example.data.local.entity.McpToolEntity
import com.example.data.local.entity.WebhookConfigEntity
import com.example.data.local.entity.WhatsAppInstanceEntity
import com.example.data.local.entity.WhatsAppMessageEntity
import com.example.domain.baileys.NodeJsBridgeScript
import com.example.domain.baileys.TermuxSyncEngine
import com.example.domain.engine.EdgeModelCatalogItem
import com.example.ui.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
import com.example.ui.theme.GrayBubble
import com.example.ui.theme.GreenBubble
import com.example.ui.theme.WhatsAppGreen

@Composable
fun McpAndSimulatorScreen(
    viewModel: MainViewModel,
    instances: List<WhatsAppInstanceEntity>,
    mcpTools: List<McpToolEntity>,
    messages: List<WhatsAppMessageEntity>,
    webhooks: List<WebhookConfigEntity>,
    initialInstanceId: String?
) {
    var selectedSubTab by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize().background(ElegantDarkBg)) {
        TabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = ElegantDarkSurface,
            contentColor = ElegantPurpleAccent,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedSubTab]),
                    color = ElegantPurpleAccent
                )
            }
        ) {
            Tab(
                selected = selectedSubTab == 0,
                onClick = { selectedSubTab = 0 },
                text = {
                    Text(
                        "Messages Reçus & Threads",
                        fontWeight = if (selectedSubTab == 0) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedSubTab == 0) ElegantPurpleAccent else ElegantTextSecondary,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            )
            Tab(
                selected = selectedSubTab == 1,
                onClick = { selectedSubTab = 1 },
                text = {
                    Text(
                        "Outils MCP & Webhooks",
                        fontWeight = if (selectedSubTab == 1) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedSubTab == 1) ElegantPurpleAccent else ElegantTextSecondary,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            )
        }

        when (selectedSubTab) {
            0 -> LiveChatSimulator(
                viewModel = viewModel,
                instances = instances,
                messages = messages,
                initialInstanceId = initialInstanceId
            )
            1 -> McpAndWebhooksTab(
                viewModel = viewModel,
                mcpTools = mcpTools,
                webhooks = webhooks
            )
        }
    }
}

data class ContactThreadSummary(
    val remoteJid: String,
    val contactName: String,
    val messageCount: Int,
    val lastMessage: String,
    val lastTimestamp: Long,
    val lastSender: String
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LiveChatSimulator(
    viewModel: MainViewModel,
    instances: List<WhatsAppInstanceEntity>,
    messages: List<WhatsAppMessageEntity>,
    initialInstanceId: String?
) {
    val selectedInstanceId by viewModel.selectedInstanceId.collectAsState()
    val isSimulating by viewModel.isSimulatingReply.collectAsState()
    val agents by viewModel.agents.collectAsState()
    val selectableModels by viewModel.allSelectableModels.collectAsState()
    val isTermuxOnline by viewModel.isTermuxOnline.collectAsState()
    val bridgePort by viewModel.bridgePort.collectAsState()

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    var activeInstanceId by remember {
        mutableStateOf(
            initialInstanceId
                ?: selectedInstanceId
                ?: "ALL"
        )
    }

    var selectedContactJid by remember { mutableStateOf<String?>(null) }
    var subViewMode by remember { mutableIntStateOf(0) } // 0 = Chat Stream, 1 = Fils WhatsApp
    var expandedInstanceMenu by remember { mutableStateOf(false) }
    var inputMessageText by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("+33 6 98 76 54 32") }
    var customerName by remember { mutableStateOf("Client WhatsApp") }
    var sendAsCustomer by remember { mutableStateOf(true) }
    var showClearConfirmation by remember { mutableStateOf(false) }
    var showBindDialog by remember { mutableStateOf(false) }
    var feedbackToast by remember { mutableStateOf<String?>(null) }
    var isTopExpanded by rememberSaveable { mutableStateOf(true) }
    // 0 = HIDDEN (Monitoring plein écran), 1 = COMPACT (Barre de frappe seule), 2 = FULL (Frappe + Suggestions + Sélecteurs)
    var bottomPanelState by rememberSaveable { mutableIntStateOf(2) }

    val currentInstance = instances.firstOrNull { it.id == activeInstanceId }
    val assignedAgent = currentInstance?.let { inst ->
        agents.firstOrNull { it.assignedInstanceIdsCsv.split(",").map { s -> s.trim() }.contains(inst.id) && it.assignedInstanceIdsCsv != "*" }
            ?: agents.firstOrNull { it.assignedInstanceIdsCsv == "*" }
    } ?: agents.firstOrNull { it.isActive } ?: agents.firstOrNull()

    // Filter messages: "ALL" shows all messages across all instances
    val baseFilteredMessages = remember(messages, activeInstanceId) {
        if (activeInstanceId == "ALL" || activeInstanceId.isBlank()) {
            messages
        } else {
            messages.filter { it.instanceId == activeInstanceId }
        }
    }

    val filteredMessages = remember(baseFilteredMessages, selectedContactJid) {
        if (selectedContactJid == null) {
            baseFilteredMessages
        } else {
            baseFilteredMessages.filter { it.remoteJid == selectedContactJid }
        }
    }

    // Group distinct contacts
    val contactThreads = remember(messages) {
        messages.groupBy { it.remoteJid }.map { (jid, msgList) ->
            val lastMsg = msgList.maxByOrNull { it.timestamp }
            val customerNameFound = msgList.firstOrNull { it.isFromCustomer && it.senderName.isNotBlank() }?.senderName
                ?: jid.substringBefore("@")
            ContactThreadSummary(
                remoteJid = jid,
                contactName = customerNameFound,
                messageCount = msgList.size,
                lastMessage = lastMsg?.content ?: "",
                lastTimestamp = lastMsg?.timestamp ?: 0L,
                lastSender = lastMsg?.senderName ?: ""
            )
        }.sortedByDescending { it.lastTimestamp }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(6.dp))

        // Top Header with fold/unfold button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { isTopExpanded = !isTopExpanded }
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isTermuxOnline) ElegantGreenActive else ElegantPurpleAccent)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isTopExpanded) "CONTRÔLES & INSTANCE" else (if (selectedContactJid != null) "FIL : ${selectedContactJid?.substringBefore("@")}" else "${if (activeInstanceId == "ALL") "Toutes les instances" else (currentInstance?.name ?: "Instance")} • Masqué"),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isTopExpanded) ElegantTextSecondary else ElegantPurpleAccent,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isTopExpanded) ElegantDarkSurfaceVariant else ElegantPurpleAccent.copy(alpha = 0.2f),
                border = BorderStroke(1.dp, if (isTopExpanded) ElegantDarkBorder else ElegantPurpleAccent.copy(alpha = 0.6f)),
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { isTopExpanded = !isTopExpanded }
                    .testTag("toggle_top_panel_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isTopExpanded) "Replier le haut" else "Déplier le haut",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isTopExpanded) ElegantTextPrimary else ElegantPurpleAccent,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (isTopExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isTopExpanded) "Replier le haut" else "Déplier le haut",
                        tint = if (isTopExpanded) ElegantTextPrimary else ElegantPurpleAccent,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = isTopExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Top Row: Instance selector dropdown, Sync button & clear button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExposedDropdownMenuBox(
                        expanded = expandedInstanceMenu,
                        onExpandedChange = { expandedInstanceMenu = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = if (activeInstanceId == "ALL" || currentInstance == null) {
                                "🌐 Toutes les instances (${messages.size} msgs)"
                            } else {
                                "${currentInstance.name} (${currentInstance.phoneNumber})"
                            },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Filtrer l'instance WhatsApp") },
                            shape = RoundedCornerShape(16.dp),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedInstanceMenu) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )

                        ExposedDropdownMenu(
                            expanded = expandedInstanceMenu,
                            onDismissRequest = { expandedInstanceMenu = false },
                            modifier = Modifier.background(ElegantDarkSurface)
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "🌐 Toutes les instances (Tous les messages reçus)",
                                        color = ElegantPurpleAccent,
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                onClick = {
                                    activeInstanceId = "ALL"
                                    expandedInstanceMenu = false
                                }
                            )
                            instances.forEach { inst ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(if (inst.status == "CONNECTED") ElegantGreenActive else ElegantRedAlert)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("${inst.name} - ${inst.phoneNumber} (${inst.status})", color = ElegantTextPrimary)
                                        }
                                    },
                                    onClick = {
                                        activeInstanceId = inst.id
                                        viewModel.selectInstance(inst.id)
                                        expandedInstanceMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // Sync with Termux button
                    IconButton(
                        onClick = {
                            viewModel.syncWithTermux()
                            feedbackToast = "Synchronisation Termux lancée..."
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isTermuxOnline) ElegantGreenActive.copy(alpha = 0.2f) else ElegantDarkSurfaceVariant)
                            .border(1.dp, if (isTermuxOnline) ElegantGreenActive.copy(alpha = 0.6f) else ElegantDarkBorder, RoundedCornerShape(14.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sync Termux",
                            tint = if (isTermuxOnline) ElegantGreenActive else ElegantPurpleAccent
                        )
                    }

                    // Quick Clear all messages button
                    IconButton(
                        onClick = { showClearConfirmation = true },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(ElegantDarkSurfaceVariant)
                            .border(1.dp, ElegantDarkBorder, RoundedCornerShape(14.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Tout effacer",
                            tint = ElegantRedAlert
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Live Termux status banner
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isTermuxOnline) ElegantGreenActive.copy(alpha = 0.12f) else ElegantDarkSurface,
                    border = BorderStroke(1.dp, if (isTermuxOnline) ElegantGreenActive.copy(alpha = 0.5f) else ElegantDarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isTermuxOnline) ElegantGreenActive else Color.Gray)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isTermuxOnline) "Termux Baileys Connecté • ${filteredMessages.size} messages reçus" else "Termux en attente • Port app :$bridgePort",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isTermuxOnline) ElegantGreenActive else ElegantTextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        TextButton(
                            onClick = {
                                viewModel.syncWithTermux()
                                feedbackToast = "Actualisation..."
                            },
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("Actualiser", fontSize = 11.sp, color = if (isTermuxOnline) ElegantGreenActive else EdgeAiCyan, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // AI Agent & Model Banner with direct "Brancher IA" button
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (currentInstance != null) {
                                showBindDialog = true
                            } else if (instances.isNotEmpty()) {
                                activeInstanceId = instances.first().id
                                showBindDialog = true
                            }
                        },
                    shape = RoundedCornerShape(14.dp),
                    color = ElegantDarkSurface,
                    border = BorderStroke(1.dp, ElegantDarkBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = null,
                                tint = ElegantPurpleAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Agent Branché : ${assignedAgent?.name ?: "Conseiller Vente (Général)"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = ElegantTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Modèle Local : ${assignedAgent?.modelId ?: "gemma-2-2b-it-int4"} • ${filteredMessages.size} msgs capturés",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EdgeAiCyan,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ElegantPurpleAccent.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, ElegantPurpleAccent.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "Lier IA",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = ElegantPurpleAccent,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // View Mode Selector Tabs: Discussion vs Fils de Contacts
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (subViewMode == 0) ElegantPurpleAccent else ElegantDarkSurfaceVariant,
                        border = BorderStroke(1.dp, if (subViewMode == 0) ElegantPurpleAccent else ElegantDarkBorder),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { subViewMode = 0 }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = null,
                                tint = if (subViewMode == 0) ElegantPurpleOnAccent else ElegantTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (selectedContactJid != null) "Fil sélectionné" else "Discussion Active",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (subViewMode == 0) ElegantPurpleOnAccent else ElegantTextSecondary,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (subViewMode == 1) ElegantPurpleAccent else ElegantDarkSurfaceVariant,
                        border = BorderStroke(1.dp, if (subViewMode == 1) ElegantPurpleAccent else ElegantDarkBorder),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { subViewMode = 1 }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.QuestionAnswer,
                                contentDescription = null,
                                tint = if (subViewMode == 1) ElegantPurpleOnAccent else ElegantTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Fils WhatsApp (${contactThreads.size})",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (subViewMode == 1) ElegantPurpleOnAccent else ElegantTextSecondary,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                if (selectedContactJid != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ElegantDarkCardDark, RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Filtre contact : $selectedContactJid",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF80D8FF),
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            onClick = { selectedContactJid = null },
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Voir tout", fontSize = 11.sp, color = ElegantPurpleAccent)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Content: SubView 0 = Chat Messages, SubView 1 = Contact Threads List
        if (subViewMode == 1) {
            if (contactThreads.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QuestionAnswer,
                            contentDescription = null,
                            tint = ElegantTextSecondary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Aucun fil de discussion enregistré",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ElegantTextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Les contacts qui vous écrivent sur WhatsApp s'afficheront ici automatiquement.",
                            style = MaterialTheme.typography.bodySmall,
                            color = ElegantTextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(contactThreads, key = { it.remoteJid }) { thread ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = ElegantDarkSurface,
                            border = BorderStroke(1.dp, ElegantDarkBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedContactJid = thread.remoteJid
                                    subViewMode = 0
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(ElegantPurpleAccent.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = thread.contactName.take(2).uppercase(),
                                        color = ElegantPurpleAccent,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = thread.contactName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = ElegantTextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = if (thread.lastTimestamp > 0) timeFormat.format(Date(thread.lastTimestamp)) else "",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = ElegantTextSecondary,
                                            fontSize = 10.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${thread.lastSender}: ${thread.lastMessage}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ElegantTextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = WhatsAppGreen.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "${thread.messageCount}",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = WhatsAppGreen
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            if (filteredMessages.isEmpty()) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
                    border = BorderStroke(1.dp, ElegantDarkBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = ElegantGreenActive.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, ElegantGreenActive.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(ElegantGreenActive)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Passerelle HTTP 127.0.0.1:8080 Active",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ElegantGreenActive,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "En attente de messages WhatsApp...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ElegantTextPrimary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Dès que vous recevez un message sur votre WhatsApp connecté, la passerelle Baileys le transmet ici en temps réel et votre IA répond automatiquement.",
                            style = MaterialTheme.typography.bodySmall,
                            color = ElegantTextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Fast Sync with Termux button
                        Button(
                            onClick = {
                                viewModel.syncWithTermux()
                                feedbackToast = "Synchronisation Termux effectuée !"
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen, contentColor = Color.White),
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Synchroniser les Messages Termux", fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Launch Termux Button
                        OutlinedButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(NodeJsBridgeScript.TERMUX_ONE_LINER))
                                feedbackToast = "Commande copiée ! Lancement de Termux..."
                                TermuxSyncEngine.openTermux(context)
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, WhatsAppGreen.copy(alpha = 0.7f)),
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            Icon(Icons.Default.Terminal, contentDescription = null, tint = WhatsAppGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Lancer Baileys sur Termux", color = WhatsAppGreen, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Simulate test message
                        OutlinedButton(
                            onClick = {
                                val targetInstId = currentInstance?.id ?: instances.firstOrNull()?.id ?: "inst_paris_01"
                                viewModel.simulateCustomerMessage(
                                    instanceId = targetInstId,
                                    senderJid = "+33612345678@s.whatsapp.net",
                                    senderName = "Client Test WhatsApp",
                                    text = "Bonjour, avez-vous des disponibilités pour un rendez-vous ?"
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, ElegantPurpleAccent.copy(alpha = 0.6f)),
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = ElegantPurpleAccent, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Simuler un message client de test", color = ElegantPurpleAccent, fontSize = 12.sp, maxLines = 1, softWrap = false)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    reverseLayout = false
                ) {
                    items(filteredMessages, key = { it.id }) { msg ->
                        ChatBubble(
                            message = msg,
                            onCopy = {
                                clipboardManager.setText(AnnotatedString(msg.content))
                                feedbackToast = "Message copié !"
                            }
                        )
                    }

                    if (isSimulating) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = ElegantPurpleAccent
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "L'Agent Local ${assignedAgent?.name ?: "IA"} génère sa réponse...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ElegantPurpleSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Header with 3-State Fold / Unfold (Complet, Semi-plié frappe seule, Plié monitoring)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            // Cycle between states: 2 (Complet) -> 1 (Semi-plié) -> 0 (Replié) -> 2
                            bottomPanelState = when (bottomPanelState) {
                                2 -> 1
                                1 -> 0
                                else -> 2
                            }
                        }
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = null,
                        tint = if (bottomPanelState > 0) ElegantTextSecondary else ElegantPurpleAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (bottomPanelState) {
                            2 -> "SAISIE & SUGGESTIONS"
                            1 -> "SAISIE COMPACTE (SEMI-PLIÉ)"
                            else -> "MONITORING SEUL (REPLIÉ)"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (bottomPanelState > 0) ElegantTextSecondary else ElegantPurpleAccent,
                        fontSize = 11.sp
                    )
                }

                // 3-state segmented action buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Button 1: Plié complet (Monitoring)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (bottomPanelState == 0) ElegantPurpleAccent.copy(alpha = 0.25f) else ElegantDarkSurfaceVariant,
                        border = BorderStroke(1.dp, if (bottomPanelState == 0) ElegantPurpleAccent else ElegantDarkBorder),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { bottomPanelState = 0 }
                            .testTag("toggle_bottom_hidden_button")
                    ) {
                        Text(
                            text = "Plié",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (bottomPanelState == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (bottomPanelState == 0) ElegantPurpleAccent else ElegantTextSecondary,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                        )
                    }

                    // Button 2: Semi-plié (Frappe seule)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (bottomPanelState == 1) Color(0xFF00B0FF).copy(alpha = 0.25f) else ElegantDarkSurfaceVariant,
                        border = BorderStroke(1.dp, if (bottomPanelState == 1) Color(0xFF00B0FF) else ElegantDarkBorder),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { bottomPanelState = 1 }
                            .testTag("toggle_bottom_compact_button")
                    ) {
                        Text(
                            text = "Semi-plié",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (bottomPanelState == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (bottomPanelState == 1) Color(0xFF00B0FF) else ElegantTextSecondary,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                        )
                    }

                    // Button 3: Déplié complet
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (bottomPanelState == 2) ElegantGreenActive.copy(alpha = 0.25f) else ElegantDarkSurfaceVariant,
                        border = BorderStroke(1.dp, if (bottomPanelState == 2) ElegantGreenActive else ElegantDarkBorder),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { bottomPanelState = 2 }
                            .testTag("toggle_bottom_full_button")
                    ) {
                        Text(
                            text = "Complet",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (bottomPanelState == 2) FontWeight.Bold else FontWeight.Normal,
                            color = if (bottomPanelState == 2) ElegantGreenActive else ElegantTextSecondary,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Quick Prompts Suggestions (Visible only when FULL = 2)
            AnimatedVisibility(
                visible = bottomPanelState == 2,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    val suggestions = listOf(
                        "Prix du pack ?",
                        "Horaires d'ouverture ?",
                        "Prendre RDV",
                        "Suivi commande #9201",
                        "Parler à un humain"
                    )
                    suggestions.forEach { prompt ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = ElegantDarkSurfaceVariant,
                            border = BorderStroke(1.dp, ElegantDarkBorder),
                            modifier = Modifier.clickable {
                                inputMessageText = prompt
                            }
                        ) {
                            Text(
                                text = prompt,
                                style = MaterialTheme.typography.labelSmall,
                                color = ElegantTextPrimary,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Input Bar (Visible in Semi-plié (1) and Complet (2))
            AnimatedVisibility(
                visible = bottomPanelState >= 1,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, ElegantDarkBorder)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                        // In Semi-plié, show ultra-compact selector; in Full, show standard selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (sendAsCustomer) Color(0xFF00B0FF).copy(alpha = 0.2f) else ElegantDarkBg,
                                    border = BorderStroke(1.dp, if (sendAsCustomer) Color(0xFF00B0FF) else ElegantDarkBorder),
                                    modifier = Modifier.clickable { sendAsCustomer = true }
                                ) {
                                    Text(
                                        text = if (bottomPanelState == 1) "Client (IA)" else "Simuler Client (Déclenche IA)",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (sendAsCustomer) FontWeight.Bold else FontWeight.Normal,
                                        color = if (sendAsCustomer) Color(0xFF00B0FF) else ElegantTextSecondary,
                                        fontSize = 10.sp,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (!sendAsCustomer) WhatsAppGreen.copy(alpha = 0.2f) else ElegantDarkBg,
                                    border = BorderStroke(1.dp, if (!sendAsCustomer) WhatsAppGreen else ElegantDarkBorder),
                                    modifier = Modifier.clickable { sendAsCustomer = false }
                                ) {
                                    Text(
                                        text = if (bottomPanelState == 1) "Moi / Manuel" else "Moi / Réponse Manuelle",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (!sendAsCustomer) FontWeight.Bold else FontWeight.Normal,
                                        color = if (!sendAsCustomer) WhatsAppGreen else ElegantTextSecondary,
                                        fontSize = 10.sp,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }

                            if (bottomPanelState == 1) {
                                Text(
                                    text = "Semi-plié",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF00B0FF),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = inputMessageText,
                                onValueChange = { inputMessageText = it },
                                placeholder = {
                                    Text(
                                        text = if (sendAsCustomer) "Écrire un message client..." else "Écrire une réponse manuelle...",
                                        color = ElegantTextSecondary,
                                        fontSize = 13.sp
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("simulator_chat_input"),
                                shape = RoundedCornerShape(16.dp),
                                maxLines = if (bottomPanelState == 1) 2 else 3
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    if (inputMessageText.isNotBlank()) {
                                        val textToSend = inputMessageText
                                        inputMessageText = ""
                                        val targetInstId = currentInstance?.id ?: instances.firstOrNull()?.id ?: "inst_paris_01"
                                        val contactJid = selectedContactJid ?: "$customerPhone@s.whatsapp.net"

                                        if (sendAsCustomer) {
                                            viewModel.simulateCustomerMessage(
                                                instanceId = targetInstId,
                                                senderJid = contactJid,
                                                senderName = customerName,
                                                text = textToSend
                                            )
                                        } else {
                                            viewModel.sendManualReply(
                                                instanceId = targetInstId,
                                                remoteJid = contactJid,
                                                text = textToSend
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(if (sendAsCustomer) ElegantPurpleAccent else WhatsAppGreen)
                                    .testTag("simulator_send_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Envoyer",
                                    tint = ElegantPurpleOnAccent
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearConfirmation = false },
            shape = RoundedCornerShape(18.dp),
            containerColor = ElegantDarkSurface,
            title = { Text("Effacer tous les messages ?", color = ElegantTextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Cette action supprimera l'historique de tous les messages capturés.", color = ElegantTextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllMessages()
                        showClearConfirmation = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElegantRedAlert)
                ) {
                    Text("Effacer", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmation = false }) {
                    Text("Annuler", color = ElegantTextSecondary)
                }
            }
        )
    }

    if (showBindDialog && currentInstance != null) {
        BindAgentAndModelDialog(
            instance = currentInstance,
            agents = agents,
            models = selectableModels,
            onDismiss = { showBindDialog = false },
            onSave = { agentId, modelId ->
                viewModel.bindAgentAndModelToInstance(currentInstance.id, agentId, modelId)
                showBindDialog = false
            }
        )
    }

    feedbackToast?.let { toast ->
        AlertDialog(
            onDismissRequest = { feedbackToast = null },
            shape = RoundedCornerShape(16.dp),
            containerColor = ElegantDarkSurface,
            text = { Text(toast, color = ElegantTextPrimary, fontWeight = FontWeight.Bold) },
            confirmButton = {
                TextButton(onClick = { feedbackToast = null }) {
                    Text("OK", color = ElegantPurpleAccent)
                }
            }
        )
    }
}

@Composable
fun ChatBubble(
    message: WhatsAppMessageEntity,
    onCopy: () -> Unit = {}
) {
    val isCustomer = message.isFromCustomer
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val formattedTime = remember(message.timestamp) {
        if (message.timestamp > 0) timeFormat.format(Date(message.timestamp)) else ""
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isCustomer) Alignment.Start else Alignment.End
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isCustomer) 4.dp else 18.dp,
                bottomEnd = if (isCustomer) 18.dp else 4.dp
            ),
            color = if (isCustomer) ElegantDarkCardDark else ElegantDarkSurfaceVariant,
            border = BorderStroke(1.dp, if (isCustomer) ElegantDarkBorder else ElegantPurpleAccent.copy(alpha = 0.4f)),
            modifier = Modifier.widthIn(min = 140.dp, max = 320.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Header (Sender icon & name & copy button)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = if (isCustomer) Icons.Default.Person else Icons.Default.SmartToy,
                            contentDescription = null,
                            tint = if (isCustomer) Color(0xFF80D8FF) else ElegantPurpleAccent,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = message.senderName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isCustomer) Color(0xFF80D8FF) else ElegantPurpleAccent,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(
                        onClick = onCopy,
                        modifier = Modifier.size(22.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copier",
                            tint = ElegantTextSecondary.copy(alpha = 0.7f),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ElegantTextPrimary
                )

                // Trace info if AI response
                if (!isCustomer) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ElegantDarkBg, RoundedCornerShape(8.dp))
                            .border(1.dp, ElegantDarkBorder, RoundedCornerShape(8.dp))
                            .padding(6.dp)
                    ) {
                        message.routingReason?.let { reason ->
                            Text(
                                text = "🎯 $reason",
                                style = MaterialTheme.typography.labelSmall,
                                color = ElegantPurpleAccent,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        message.toolCallsExecuted?.let { tools ->
                            Text(
                                text = "⚡ MCP: $tools",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFFFD54F),
                                fontSize = 9.sp
                            )
                        }
                        Text(
                            text = "⏱️ Latence Edge: ${message.latencyMs}ms",
                            style = MaterialTheme.typography.labelSmall,
                            color = EdgeAiCyan,
                            fontSize = 9.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Bottom timestamp
                if (formattedTime.isNotBlank()) {
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = ElegantTextSecondary.copy(alpha = 0.7f),
                        fontSize = 9.sp,
                        modifier = Modifier.align(Alignment.End),
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

@Composable
fun McpAndWebhooksTab(
    viewModel: MainViewModel,
    mcpTools: List<McpToolEntity>,
    webhooks: List<WebhookConfigEntity>
) {
    var showAddMcpDialog by remember { mutableStateOf(false) }
    var showAddWebhookDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            // Section: MCP Tools
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Outils MCP (Model Context Protocol)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ElegantTextPrimary
                )
                TextButton(onClick = { showAddMcpDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = ElegantPurpleAccent, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ajouter Outil", color = ElegantPurpleAccent, fontWeight = FontWeight.Bold)
                }
            }
        }

        items(mcpTools, key = { it.id }) { tool ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, ElegantDarkBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(ElegantDarkCardDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Build, contentDescription = null, tint = ElegantPurpleAccent, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = tool.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = ElegantTextPrimary,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = tool.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = ElegantTextSecondary
                            )
                        }
                    }

                    Switch(
                        checked = tool.isEnabled,
                        onCheckedChange = { viewModel.toggleMcpTool(tool.id, it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ElegantPurpleOnAccent,
                            checkedTrackColor = ElegantPurpleAccent
                        )
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(14.dp))
            // Section: Webhooks & WebSockets
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Webhooks & Ponts WebSocket",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ElegantTextPrimary
                )
                TextButton(onClick = { showAddWebhookDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = ElegantPurpleAccent, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Nouveau Webhook", color = ElegantPurpleAccent, fontWeight = FontWeight.Bold)
                }
            }
        }

        items(webhooks, key = { it.id }) { webhook ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, ElegantDarkBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Webhook, contentDescription = null, tint = ElegantGreenActive)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = webhook.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = ElegantTextPrimary
                            )
                        }
                        IconButton(onClick = { viewModel.deleteWebhook(webhook.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = ElegantTextSecondary.copy(alpha = 0.7f))
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = webhook.url,
                        style = MaterialTheme.typography.labelSmall,
                        color = ElegantPurpleSecondary,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ElegantDarkBg,
                            border = BorderStroke(1.dp, ElegantDarkBorder)
                        ) {
                            Text(
                                text = "Événements: ${webhook.eventsCsv}",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = ElegantGreenActive
                            )
                        }
                        Switch(
                            checked = webhook.isEnabled,
                            onCheckedChange = { viewModel.toggleWebhook(webhook.id, it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ElegantPurpleOnAccent,
                                checkedTrackColor = ElegantPurpleAccent
                            )
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    if (showAddMcpDialog) {
        AddMcpToolDialog(
            onDismiss = { showAddMcpDialog = false },
            onAdd = { name, desc, schema ->
                viewModel.addMcpTool(name, desc, schema)
                showAddMcpDialog = false
            }
        )
    }

    if (showAddWebhookDialog) {
        AddWebhookDialog(
            onDismiss = { showAddWebhookDialog = false },
            onAdd = { name, url, events, secret ->
                viewModel.addWebhook(name, url, events, secret)
                showAddWebhookDialog = false
            }
        )
    }
}

@Composable
fun AddMcpToolDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, desc: String, schema: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var schemaJson by remember {
        mutableStateOf("""{"type":"object","properties":{"param1":{"type":"string"}},"required":["param1"]}""")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = ElegantDarkSurface,
        title = { Text("Ajouter un Outil MCP", fontWeight = FontWeight.Bold, color = ElegantTextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom de la fonction (ex: get_stock)") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description pour l'agent IA") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = schemaJson,
                    onValueChange = { schemaJson = it },
                    label = { Text("Schéma JSON des paramètres") },
                    shape = RoundedCornerShape(14.dp),
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onAdd(name, description, schemaJson)
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ElegantPurpleAccent, contentColor = ElegantPurpleOnAccent)
            ) {
                Text("Enregistrer Outil", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler", color = ElegantTextSecondary) }
        }
    )
}

@Composable
fun AddWebhookDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, url: String, events: String, secret: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("https://") }
    var events by remember { mutableStateOf("messages.upsert,connection.update") }
    var secret by remember { mutableStateOf("whsec_12345") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = ElegantDarkSurface,
        title = { Text("Configurer un Webhook", fontWeight = FontWeight.Bold, color = ElegantTextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom du Webhook (ex: CRM Zapier)") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("URL de destination (POST)") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = events,
                    onValueChange = { events = it },
                    label = { Text("Événements abonnés") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = secret,
                    onValueChange = { secret = it },
                    label = { Text("Clé secrète / Signature") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && url.isNotBlank()) {
                        onAdd(name, url, events, secret)
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ElegantPurpleAccent, contentColor = ElegantPurpleOnAccent)
            ) {
                Text("Activer Webhook", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler", color = ElegantTextSecondary) }
        }
    )
}
