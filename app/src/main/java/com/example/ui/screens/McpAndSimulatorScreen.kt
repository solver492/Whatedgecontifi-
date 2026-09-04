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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Sync
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.McpToolEntity
import com.example.data.local.entity.WebhookConfigEntity
import com.example.data.local.entity.WhatsAppInstanceEntity
import com.example.data.local.entity.WhatsAppMessageEntity
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
                        "Simulateur Chat WhatsApp",
                        fontWeight = if (selectedSubTab == 0) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedSubTab == 0) ElegantPurpleAccent else ElegantTextSecondary
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
                        color = if (selectedSubTab == 1) ElegantPurpleAccent else ElegantTextSecondary
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

    var activeInstanceId by remember {
        mutableStateOf(
            initialInstanceId
                ?: selectedInstanceId
                ?: instances.firstOrNull()?.id
                ?: ""
        )
    }

    var expandedInstanceMenu by remember { mutableStateOf(false) }
    var inputMessageText by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("+33 6 98 76 54 32") }
    var customerName by remember { mutableStateOf("Client WhatsApp") }

    val currentInstance = instances.firstOrNull { it.id == activeInstanceId } ?: instances.firstOrNull()
    val filteredMessages = messages.filter { it.instanceId == (currentInstance?.id ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Top instance picker selector
        ExposedDropdownMenuBox(
            expanded = expandedInstanceMenu,
            onExpandedChange = { expandedInstanceMenu = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = currentInstance?.let { "${it.name} (${it.phoneNumber})" } ?: "Aucune instance",
                onValueChange = {},
                readOnly = true,
                label = { Text("Canal / Instance WhatsApp de test") },
                shape = RoundedCornerShape(16.dp),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedInstanceMenu) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
            )

            ExposedDropdownMenu(
                expanded = expandedInstanceMenu,
                onDismissRequest = { expandedInstanceMenu = false }
            ) {
                instances.forEach { inst ->
                    DropdownMenuItem(
                        text = { Text("${inst.name} - ${inst.phoneNumber} (${inst.status})", color = ElegantTextPrimary) },
                        onClick = {
                            activeInstanceId = inst.id
                            viewModel.selectInstance(inst.id)
                            expandedInstanceMenu = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick prompts suggestions
        Text("Suggestions de messages clients :", style = MaterialTheme.typography.labelSmall, color = ElegantPurpleSecondary)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(vertical = 6.dp)
        ) {
            val suggestions = listOf(
                "Prix du pack Pro ?",
                "Suivi commande #CMD-9201",
                "Parler à un humain",
                "Horaires d'ouverture ?",
                "Prendre rendez-vous"
            )
            suggestions.forEach { prompt ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
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
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Messages list
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            reverseLayout = false
        ) {
            items(filteredMessages, key = { it.id }) { msg ->
                ChatBubble(message = msg)
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
                            text = "L'Agent Local AI Edge réfléchit et génère sa réponse...",
                            style = MaterialTheme.typography.bodySmall,
                            color = ElegantPurpleSecondary
                        )
                    }
                }
            }
        }

        // Input bar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, ElegantDarkBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputMessageText,
                    onValueChange = { inputMessageText = it },
                    placeholder = { Text("Écrire un message en tant que client...", color = ElegantTextSecondary) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("simulator_chat_input"),
                    shape = RoundedCornerShape(18.dp),
                    maxLines = 3
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (inputMessageText.isNotBlank() && currentInstance != null) {
                            val textToSend = inputMessageText
                            inputMessageText = ""
                            viewModel.simulateCustomerMessage(
                                instanceId = currentInstance.id,
                                senderJid = "$customerPhone@s.whatsapp.net",
                                senderName = customerName,
                                text = textToSend
                            )
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(ElegantPurpleAccent)
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

@Composable
fun ChatBubble(message: WhatsAppMessageEntity) {
    val isCustomer = message.isFromCustomer

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
            modifier = Modifier.widthIn(max = 310.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Header (Sender)
                Row(verticalAlignment = Alignment.CenterVertically) {
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
                        color = if (isCustomer) Color(0xFF80D8FF) else ElegantPurpleAccent
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ElegantTextPrimary
                )

                // Trace info if AI response
                if (!isCustomer) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ElegantDarkBg, RoundedCornerShape(10.dp))
                            .border(1.dp, ElegantDarkBorder, RoundedCornerShape(10.dp))
                            .padding(8.dp)
                    ) {
                        message.routingReason?.let { reason ->
                            Text(
                                text = "🎯 $reason",
                                style = MaterialTheme.typography.labelSmall,
                                color = ElegantPurpleAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        message.toolCallsExecuted?.let { tools ->
                            Text(
                                text = "⚡ MCP: $tools",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFFFD54F),
                                fontSize = 10.sp
                            )
                        }
                        Text(
                            text = "⚡ Latence Edge: ${message.latencyMs}ms",
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
