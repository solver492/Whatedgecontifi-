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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.AgentEntity
import com.example.data.local.entity.WhatsAppInstanceEntity
import com.example.domain.engine.InferenceResult
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
import kotlinx.coroutines.launch

@Composable
fun AgentsScreen(
    viewModel: MainViewModel,
    agents: List<AgentEntity>,
    instances: List<WhatsAppInstanceEntity> = emptyList()
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var editingAgent by remember { mutableStateOf<AgentEntity?>(null) }
    var testingAgent by remember { mutableStateOf<AgentEntity?>(null) }

    Box(modifier = Modifier.fillMaxSize().background(ElegantDarkBg)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                // Banner header - Elegant Dark style
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
                                    imageVector = Icons.Default.SmartToy,
                                    contentDescription = null,
                                    tint = ElegantPurpleAccent
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Orchestrateur Multi-Agents IA",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ElegantTextPrimary
                                )
                                Text(
                                    text = "Routage par mot-clé, plage horaire & canal WhatsApp dédié",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ElegantPurpleSecondary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        val activeCount = agents.count { it.isActive }
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
                                        .background(if (activeCount > 0) ElegantGreenActive else ElegantPurpleSecondary)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "$activeCount agent(s) actif(s) sur ${agents.size} configurés • ${instances.size} instance(s) WhatsApp",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = ElegantTextPrimary
                                )
                            }
                        }
                    }
                }
            }

            items(agents, key = { it.id }) { agent ->
                AgentCard(
                    agent = agent,
                    instances = instances,
                    onToggleActive = { active -> viewModel.toggleAgent(agent.id, active) },
                    onEdit = { editingAgent = agent },
                    onDelete = { viewModel.deleteAgent(agent.id) },
                    onTest = { testingAgent = agent },
                    onAssignInstance = { instanceId ->
                        viewModel.assignAgentToInstances(agent.id, instanceId)
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(84.dp))
            }
        }

        // FAB to add agent
        FloatingActionButton(
            onClick = { showCreateDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("create_agent_fab"),
            containerColor = ElegantPurpleAccent,
            contentColor = ElegantPurpleOnAccent,
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Nouvel Agent")
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Créer un Agent", fontWeight = FontWeight.Bold)
            }
        }
    }

    // Create Agent Dialog
    if (showCreateDialog) {
        AgentEditDialog(
            agent = null,
            instances = instances,
            onDismiss = { showCreateDialog = false },
            onSave = { name, role, prompt, model, activation, keywords, start, end, selectedInstances, andTest ->
                viewModel.saveAgent(
                    id = null,
                    name = name,
                    role = role,
                    systemPrompt = prompt,
                    modelId = model,
                    activationMode = activation,
                    keywordsCsv = keywords,
                    scheduleStart = start,
                    scheduleEnd = end,
                    assignedInstanceIdsCsv = selectedInstances,
                    onSaved = { savedAgent ->
                        if (andTest) {
                            testingAgent = savedAgent
                        }
                    }
                )
                showCreateDialog = false
            }
        )
    }

    // Edit Agent Dialog
    editingAgent?.let { agent ->
        AgentEditDialog(
            agent = agent,
            instances = instances,
            onDismiss = { editingAgent = null },
            onSave = { name, role, prompt, model, activation, keywords, start, end, selectedInstances, andTest ->
                viewModel.saveAgent(
                    id = agent.id,
                    name = name,
                    role = role,
                    systemPrompt = prompt,
                    modelId = model,
                    activationMode = activation,
                    keywordsCsv = keywords,
                    scheduleStart = start,
                    scheduleEnd = end,
                    assignedInstanceIdsCsv = selectedInstances,
                    onSaved = { savedAgent ->
                        if (andTest) {
                            testingAgent = savedAgent
                        }
                    }
                )
                editingAgent = null
            }
        )
    }

    // Agent Playground / Tester Dialog
    testingAgent?.let { agent ->
        AgentPlaygroundDialog(
            agent = agent,
            instances = instances,
            onDismiss = { testingAgent = null },
            onExecuteTest = { query ->
                viewModel.testAgentDirectly(agent, query)
            },
            onAssignInstance = { instanceId ->
                viewModel.assignAgentToInstances(agent.id, instanceId)
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AgentCard(
    agent: AgentEntity,
    instances: List<WhatsAppInstanceEntity>,
    onToggleActive: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTest: () -> Unit,
    onAssignInstance: (String) -> Unit
) {
    var showAssignDropdown by remember { mutableStateOf(false) }

    // Resolve assigned instance label
    val assignedLabel = remember(agent.assignedInstanceIdsCsv, instances) {
        when {
            agent.assignedInstanceIdsCsv == "*" -> "Toutes les instances (*)"
            else -> {
                val matched = instances.firstOrNull { it.id == agent.assignedInstanceIdsCsv }
                matched?.let { "${it.name} (${it.phoneNumber})" } ?: agent.assignedInstanceIdsCsv
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("agent_card_${agent.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (agent.isActive) ElegantDarkCardElevated else ElegantDarkSurface
        ),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, if (agent.isActive) ElegantPurpleAccent.copy(alpha = 0.35f) else ElegantDarkBorder)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header: Name, Switch Active, Edit, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                if (agent.isActive) ElegantPurpleAccent
                                else ElegantDarkSurfaceVariant
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = null,
                            tint = if (agent.isActive) ElegantPurpleOnAccent else ElegantTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = agent.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ElegantTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${agent.role} • ${agent.modelId}",
                            style = MaterialTheme.typography.bodySmall,
                            color = ElegantPurpleSecondary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = agent.isActive,
                        onCheckedChange = onToggleActive,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ElegantPurpleOnAccent,
                            checkedTrackColor = ElegantPurpleAccent,
                            uncheckedThumbColor = ElegantTextMuted,
                            uncheckedTrackColor = ElegantDarkBg
                        )
                    )
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Modifier", tint = ElegantTextSecondary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = ElegantTextSecondary.copy(alpha = 0.6f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Assigned WhatsApp instance badge + Quick Dropdown Menu
            Box {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ElegantDarkBg,
                    border = BorderStroke(1.dp, ElegantPurpleAccent.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAssignDropdown = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = Icons.Default.Smartphone,
                                contentDescription = null,
                                tint = WhatsAppGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Instance : ",
                                style = MaterialTheme.typography.labelSmall,
                                color = ElegantTextSecondary
                            )
                            Text(
                                text = assignedLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = ElegantTextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Changer d'instance",
                            tint = ElegantPurpleAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Dropdown menu to assign instance
                DropdownMenu(
                    expanded = showAssignDropdown,
                    onDismissRequest = { showAssignDropdown = false },
                    modifier = Modifier.background(ElegantDarkSurface)
                ) {
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Hub, contentDescription = null, tint = ElegantPurpleAccent, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Toutes les instances (*)", color = ElegantTextPrimary, fontWeight = FontWeight.Bold)
                            }
                        },
                        onClick = {
                            onAssignInstance("*")
                            showAssignDropdown = false
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
                                            .background(if (inst.status == "CONNECTED") ElegantGreenActive else ElegantTextSecondary)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(inst.name, color = ElegantTextPrimary, fontWeight = FontWeight.SemiBold)
                                        Text(inst.phoneNumber, color = ElegantTextSecondary, fontSize = 11.sp)
                                    }
                                }
                            },
                            onClick = {
                                onAssignInstance(inst.id)
                                showAssignDropdown = false
                            }
                        )
                    }
                    if (instances.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("Aucune instance créée (créez-en une dans Instances)", color = ElegantTextSecondary) },
                            onClick = { showAssignDropdown = false }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // System prompt preview
            Text(
                text = "\"${agent.systemPrompt}\"",
                style = MaterialTheme.typography.bodySmall,
                color = ElegantTextPrimary.copy(alpha = 0.85f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ElegantDarkBg, RoundedCornerShape(12.dp))
                    .border(BorderStroke(1.dp, ElegantDarkBorder), RoundedCornerShape(12.dp))
                    .padding(10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Activation criteria chips
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (agent.activationMode) {
                    "ALWAYS" -> {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = ElegantDarkBg,
                            border = BorderStroke(1.dp, ElegantDarkBorder)
                        ) {
                            Text(
                                text = "Toujours Actif (Défaut)",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = ElegantGreenActive
                            )
                        }
                    }
                    "KEYWORDS" -> {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = ElegantDarkBg,
                            border = BorderStroke(1.dp, ElegantDarkBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Tag, contentDescription = null, modifier = Modifier.size(12.dp), tint = ElegantPurpleAccent)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Mots-clés: ${agent.keywordsCsv}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ElegantPurpleAccent
                                )
                            }
                        }
                    }
                    "SCHEDULE" -> {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = ElegantDarkBg,
                            border = BorderStroke(1.dp, ElegantDarkBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(12.dp), tint = ElegantPinkTertiary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Plage: ${agent.scheduleStart} - ${agent.scheduleEnd}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ElegantPinkTertiary
                                )
                            }
                        }
                    }
                    else -> {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = ElegantDarkBg,
                            border = BorderStroke(1.dp, ElegantDarkBorder)
                        ) {
                            Text(
                                text = "Mode Manuel",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = ElegantTextSecondary
                            )
                        }
                    }
                }

                // Stats chip
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = ElegantDarkBg,
                    border = BorderStroke(1.dp, ElegantDarkBorder)
                ) {
                    Text(
                        text = "${agent.responseCount} réponses • ~${agent.avgLatencyMs}ms",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = ElegantPurpleSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Row with prominent "Tester l'Agent" and "Assigner Instance"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onTest,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("test_agent_btn_${agent.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElegantPurpleAccent,
                        contentColor = ElegantPurpleOnAccent
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Science,
                        contentDescription = "Tester",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tester l'Agent", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { showAssignDropdown = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, ElegantDarkBorder),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ElegantTextPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Hub,
                        contentDescription = "Assigner",
                        modifier = Modifier.size(16.dp),
                        tint = WhatsAppGreen
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Assigner Instance", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentEditDialog(
    agent: AgentEntity?,
    instances: List<WhatsAppInstanceEntity>,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        role: String,
        prompt: String,
        model: String,
        activationMode: String,
        keywords: String,
        scheduleStart: String,
        scheduleEnd: String,
        instances: String,
        andTest: Boolean
    ) -> Unit
) {
    var name by remember { mutableStateOf(agent?.name ?: "") }
    var role by remember { mutableStateOf(agent?.role ?: "Support") }
    var prompt by remember {
        mutableStateOf(
            agent?.systemPrompt
                ?: "Tu es un agent d'assistance WhatsApp spécialisé. Réponds avec précision, clarté et bienveillance en t'appuyant sur la base de connaissances."
        )
    }
    var modelId by remember { mutableStateOf(agent?.modelId ?: "gemma-2-2b-int4") }
    var activationMode by remember { mutableStateOf(agent?.activationMode ?: "KEYWORDS") }
    var keywords by remember { mutableStateOf(agent?.keywordsCsv ?: "prix,devis,tarifs,aide") }
    var scheduleStart by remember { mutableStateOf(agent?.scheduleStart ?: "08:00") }
    var scheduleEnd by remember { mutableStateOf(agent?.scheduleEnd ?: "20:00") }
    var selectedInstanceId by remember { mutableStateOf(agent?.assignedInstanceIdsCsv ?: "*") }
    var isInstanceDropdownExpanded by remember { mutableStateOf(false) }

    // Human-readable selected instance title
    val currentSelectedName = remember(selectedInstanceId, instances) {
        if (selectedInstanceId == "*") {
            "📡 Toutes les instances WhatsApp (*)"
        } else {
            val matched = instances.firstOrNull { it.id == selectedInstanceId }
            matched?.let { "📱 ${it.name} (${it.phoneNumber})" } ?: selectedInstanceId
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = ElegantDarkSurface,
        title = {
            Text(
                if (agent == null) "Créer un Agent IA Local" else "Modifier l'Agent",
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
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nom de l'Agent") },
                        placeholder = { Text("Ex: Agent Ventes & Devis") },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().testTag("agent_name_input")
                    )
                }

                item {
                    OutlinedTextField(
                        value = role,
                        onValueChange = { role = it },
                        label = { Text("Rôle / Spécialité") },
                        placeholder = { Text("Support, Commercial, SAV, Nuit") },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().testTag("agent_role_input")
                    )
                }

                // Instance assignment with Dropdown menu
                item {
                    Text(
                        text = "Assignation Instance WhatsApp :",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = ElegantPurpleAccent
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    ExposedDropdownMenuBox(
                        expanded = isInstanceDropdownExpanded,
                        onExpandedChange = { isInstanceDropdownExpanded = !isInstanceDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = currentSelectedName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Instance assignée") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isInstanceDropdownExpanded) },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                                .testTag("instance_dropdown_field"),
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )

                        ExposedDropdownMenu(
                            expanded = isInstanceDropdownExpanded,
                            onDismissRequest = { isInstanceDropdownExpanded = false },
                            modifier = Modifier.background(ElegantDarkSurface)
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Hub, contentDescription = null, tint = ElegantPurpleAccent, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text("Toutes les instances (*)", color = ElegantTextPrimary, fontWeight = FontWeight.Bold)
                                            Text("L'agent répond sur tous les canaux", color = ElegantTextSecondary, fontSize = 11.sp)
                                        }
                                    }
                                },
                                onClick = {
                                    selectedInstanceId = "*"
                                    isInstanceDropdownExpanded = false
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
                                                    .background(if (inst.status == "CONNECTED") ElegantGreenActive else ElegantTextSecondary)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(inst.name, color = ElegantTextPrimary, fontWeight = FontWeight.SemiBold)
                                                Text("${inst.phoneNumber} • ${inst.status}", color = ElegantTextSecondary, fontSize = 11.sp)
                                            }
                                        }
                                    },
                                    onClick = {
                                        selectedInstanceId = inst.id
                                        isInstanceDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Modèle Local AI Edge :",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = ElegantPurpleAccent
                    )
                    val models = listOf(
                        "gemma-2-2b-int4" to "Gemma-2 2B (INT4 Blockwise - Local NPU)",
                        "llama-3.2-1b-int4" to "Llama-3.2 1B (INT4 GPTQ - Local CPU)",
                        "phi-3.5-mini-int4" to "Phi-3.5-mini (INT4/8 Mixed - Local GPU)",
                        "gemini-3.5-flash" to "Gemini 3.5 Flash (Cloud Edge Fallback)"
                    )
                    Column {
                        models.forEach { (id, label) ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { modelId = id }
                            ) {
                                RadioButton(
                                    selected = modelId == id,
                                    onClick = { modelId = id }
                                )
                                Text(label, style = MaterialTheme.typography.bodySmall, color = ElegantTextPrimary)
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = prompt,
                        onValueChange = { prompt = it },
                        label = { Text("System Prompt (Instructions)") },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().testTag("agent_prompt_input"),
                        minLines = 3,
                        maxLines = 5
                    )
                }

                item {
                    Text(
                        text = "Règle de déclenchement / Routage :",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = ElegantPurpleAccent
                    )
                    val modes = listOf(
                        "KEYWORDS" to "Par Mots-clés (Message client contient)",
                        "SCHEDULE" to "Par Plage Horaire (Programmable)",
                        "ALWAYS" to "Toujours Actif (Par défaut)",
                        "MANUAL" to "Manuel uniquement"
                    )
                    Column {
                        modes.forEach { (mode, label) ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { activationMode = mode }
                            ) {
                                RadioButton(
                                    selected = activationMode == mode,
                                    onClick = { activationMode = mode }
                                )
                                Text(label, style = MaterialTheme.typography.bodySmall, color = ElegantTextPrimary)
                            }
                        }
                    }
                }

                if (activationMode == "KEYWORDS") {
                    item {
                        OutlinedTextField(
                            value = keywords,
                            onValueChange = { keywords = it },
                            label = { Text("Mots-clés déclencheurs (séparés par des virgules)") },
                            placeholder = { Text("prix,tarif,devis,reduction,acheter") },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                if (activationMode == "SCHEDULE") {
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = scheduleStart,
                                onValueChange = { scheduleStart = it },
                                label = { Text("Début (HH:mm)") },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = scheduleEnd,
                                onValueChange = { scheduleEnd = it },
                                label = { Text("Fin (HH:mm)") },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Secondary action: Save only
                TextButton(
                    onClick = {
                        if (name.isNotBlank()) {
                            onSave(name, role, prompt, modelId, activationMode, keywords, scheduleStart, scheduleEnd, selectedInstanceId, false)
                        }
                    }
                ) {
                    Text("Enregistrer", color = ElegantPurpleSecondary)
                }

                // Primary action: Save & Test immediately
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            onSave(name, role, prompt, modelId, activationMode, keywords, scheduleStart, scheduleEnd, selectedInstanceId, true)
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElegantPurpleAccent, contentColor = ElegantPurpleOnAccent),
                    modifier = Modifier.testTag("save_and_test_btn")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Enregistrer & Tester", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", color = ElegantTextSecondary)
            }
        }
    )
}

/**
 * Interactive Local AI Agent Playground / Tester Dialog
 * Allows live validation of agent responses, RAG retrieval & MCP tools execution.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AgentPlaygroundDialog(
    agent: AgentEntity,
    instances: List<WhatsAppInstanceEntity>,
    onDismiss: () -> Unit,
    onExecuteTest: suspend (query: String) -> InferenceResult,
    onAssignInstance: (instanceId: String) -> Unit
) {
    val scope = rememberCoroutineScope()
    var testQuery by remember { mutableStateOf("Bonjour ! Quel est le prix de votre formule pro et que contient-elle ?") }
    var isRunningInference by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<InferenceResult?>(null) }
    var selectedInstanceId by remember { mutableStateOf(agent.assignedInstanceIdsCsv) }
    var isInstanceDropdownExpanded by remember { mutableStateOf(false) }

    val instanceDisplayName = remember(selectedInstanceId, instances) {
        if (selectedInstanceId == "*") {
            "Toutes les instances (*)"
        } else {
            val inst = instances.firstOrNull { it.id == selectedInstanceId }
            inst?.let { "${it.name} (${it.phoneNumber})" } ?: selectedInstanceId
        }
    }

    val sampleQueries = listOf(
        "Combien coûte le pack pro ?",
        "Statut de ma commande #CMD-9201",
        "Avez-vous des disponibilités ?",
        "Je voudrais parler à un humain"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = ElegantDarkSurface,
        title = {
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
                            .background(ElegantPurpleAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = null,
                            tint = ElegantPurpleOnAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Testeur d'Agent IA Local",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ElegantTextPrimary
                        )
                        Text(
                            text = "${agent.name} • ${agent.modelId}",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElegantPurpleAccent
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Fermer", tint = ElegantTextSecondary)
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Live Instance Assignment Switcher right inside the tester
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = ElegantDarkBg,
                        border = BorderStroke(1.dp, ElegantDarkBorder)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Canal WhatsApp assigné :",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = ElegantPurpleSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            ExposedDropdownMenuBox(
                                expanded = isInstanceDropdownExpanded,
                                onExpandedChange = { isInstanceDropdownExpanded = !isInstanceDropdownExpanded }
                            ) {
                                OutlinedTextField(
                                    value = instanceDisplayName,
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isInstanceDropdownExpanded) },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                        .fillMaxWidth(),
                                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                                )

                                ExposedDropdownMenu(
                                    expanded = isInstanceDropdownExpanded,
                                    onDismissRequest = { isInstanceDropdownExpanded = false },
                                    modifier = Modifier.background(ElegantDarkSurface)
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Hub, contentDescription = null, tint = ElegantPurpleAccent, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Toutes les instances (*)", color = ElegantTextPrimary, fontWeight = FontWeight.Bold)
                                            }
                                        },
                                        onClick = {
                                            selectedInstanceId = "*"
                                            onAssignInstance("*")
                                            isInstanceDropdownExpanded = false
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
                                                            .background(if (inst.status == "CONNECTED") ElegantGreenActive else ElegantTextSecondary)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Column {
                                                        Text(inst.name, color = ElegantTextPrimary, fontWeight = FontWeight.SemiBold)
                                                        Text(inst.phoneNumber, color = ElegantTextSecondary, fontSize = 11.sp)
                                                    }
                                                }
                                            },
                                            onClick = {
                                                selectedInstanceId = inst.id
                                                onAssignInstance(inst.id)
                                                isInstanceDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Sample prompt pills
                item {
                    Text(
                        text = "Questions types (cliquer pour tester) :",
                        style = MaterialTheme.typography.labelSmall,
                        color = ElegantTextSecondary
                    )
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
                                modifier = Modifier.clickable {
                                    testQuery = query
                                }
                            ) {
                                Text(
                                    text = query,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ElegantPurpleAccent
                                )
                            }
                        }
                    }
                }

                // Input prompt
                item {
                    OutlinedTextField(
                        value = testQuery,
                        onValueChange = { testQuery = it },
                        label = { Text("Message de test client") },
                        placeholder = { Text("Ex: Quel est le prix... ?") },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().testTag("test_query_input"),
                        minLines = 2,
                        maxLines = 4
                    )
                }

                // Run inference button
                item {
                    Button(
                        onClick = {
                            if (testQuery.isNotBlank() && !isRunningInference) {
                                scope.launch {
                                    isRunningInference = true
                                    try {
                                        testResult = onExecuteTest(testQuery)
                                    } finally {
                                        isRunningInference = false
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("execute_test_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElegantPurpleAccent,
                            contentColor = ElegantPurpleOnAccent
                        ),
                        enabled = !isRunningInference && testQuery.isNotBlank()
                    ) {
                        if (isRunningInference) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = ElegantPurpleOnAccent,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Inférence NPU locale en cours...", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.Bolt, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Exécuter Inférence Locale", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Display Test Output Result
                testResult?.let { result ->
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = ElegantDarkCardDark),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, ElegantPurpleAccent.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.SmartToy,
                                            contentDescription = null,
                                            tint = ElegantPurpleAccent,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Réponse de ${agent.name} :",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = ElegantPurpleAccent
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = ElegantDarkBg,
                                        border = BorderStroke(1.dp, ElegantDarkBorder)
                                    ) {
                                        Text(
                                            text = "⚡ ${result.latencyMs} ms",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = ElegantGreenActive,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // AI Response Text Bubble
                                Text(
                                    text = result.replyText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = ElegantTextPrimary,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(ElegantDarkBg, RoundedCornerShape(10.dp))
                                        .padding(12.dp)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Metadata bar (RAG snippets, MCP tools)
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    if (result.ragSnippetsApplied.isNotEmpty()) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Layers, contentDescription = null, tint = EdgeAiCyan, modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "RAG : ${result.ragSnippetsApplied.size} chunk(s) injecté(s) (${result.ragSnippetsApplied.firstOrNull()?.take(40)}...)",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = EdgeAiCyan,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    if (result.toolCalls.isNotEmpty()) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = ElegantPinkTertiary, modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "MCP Outils : ${result.toolCalls.joinToString(" • ")}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = ElegantPinkTertiary
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Memory, contentDescription = null, tint = ElegantTextSecondary, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Modèle : ${result.modelUsed} (~${result.tokensGenerated} tokens)",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = ElegantTextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElegantPurpleAccent,
                    contentColor = ElegantPurpleOnAccent
                )
            ) {
                Text("Terminer", fontWeight = FontWeight.Bold)
            }
        }
    )
}
