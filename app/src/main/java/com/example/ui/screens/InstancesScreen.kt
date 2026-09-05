package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.AgentEntity
import com.example.data.local.entity.WhatsAppInstanceEntity
import com.example.domain.baileys.LogType
import com.example.domain.baileys.NodeJsBridgeScript
import com.example.domain.baileys.QrCodeGenerator
import com.example.domain.baileys.TermuxSyncEngine
import com.example.domain.engine.EdgeModelCatalogItem
import com.example.ui.MainViewModel
import com.example.ui.SelectableModelOption
import com.example.ui.theme.EdgeAiCyan
import com.example.ui.theme.ElegantDarkBg
import com.example.ui.theme.ElegantDarkBorder
import com.example.ui.theme.ElegantDarkCardDark
import com.example.ui.theme.ElegantDarkCardElevated
import com.example.ui.theme.ElegantDarkSurface
import com.example.ui.theme.ElegantDarkSurfaceVariant
import com.example.ui.theme.ElegantGreenActive
import com.example.ui.theme.ElegantPurpleAccent
import com.example.ui.theme.ElegantPurpleOnAccent
import com.example.ui.theme.ElegantPurpleSecondary
import com.example.ui.theme.ElegantRedAlert
import com.example.ui.theme.ElegantTextMuted
import com.example.ui.theme.ElegantTextPrimary
import com.example.ui.theme.ElegantTextSecondary
import com.example.ui.theme.WhatsAppGreen

@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun InstancesScreen(
    viewModel: MainViewModel,
    instances: List<WhatsAppInstanceEntity>,
    onOpenSimulatorForInstance: (String) -> Unit
) {
    var selectedScreenTab by remember { mutableIntStateOf(0) }
    val screenTabs = listOf("Instances WhatsApp", "Guide Termux & Node.js", "Logs Passerelle")

    var showCreateDialog by remember { mutableStateOf(false) }
    var activeQrInstance by remember { mutableStateOf<WhatsAppInstanceEntity?>(null) }
    var activePairingCodeInstance by remember { mutableStateOf<WhatsAppInstanceEntity?>(null) }
    var bindingInstance by remember { mutableStateOf<WhatsAppInstanceEntity?>(null) }

    val agents by viewModel.agents.collectAsState()
    val selectableModels by viewModel.allSelectableModels.collectAsState()

    val isBridgeRunning by viewModel.bridgeRunning.collectAsState()
    val bridgePort by viewModel.bridgePort.collectAsState()
    val bridgeLogs by viewModel.bridgeLogs.collectAsState()
    val isTermuxOnline by viewModel.isTermuxOnline.collectAsState()
    val termuxPort by viewModel.termuxPort.collectAsState()

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var copiedNotice by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize().background(ElegantDarkBg)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                // Hero Banner
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
                                Icon(
                                    imageVector = Icons.Default.Hub,
                                    contentDescription = null,
                                    tint = ElegantPurpleAccent
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Passerelle WhatsApp & Baileys",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ElegantTextPrimary
                                )
                                Text(
                                    text = "Multi-Instances • Pont Node.js Termux • Webhook Local",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ElegantPurpleSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Status Badges (Server and Termux)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Android App Server Status
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                color = ElegantDarkBg.copy(alpha = 0.7f),
                                border = BorderStroke(1.dp, if (isBridgeRunning) ElegantGreenActive.copy(alpha = 0.4f) else ElegantDarkBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(if (isBridgeRunning) ElegantGreenActive else ElegantRedAlert)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isBridgeRunning) "Serveur App : :$bridgePort" else "Serveur : Off",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = ElegantTextPrimary,
                                        maxLines = 1
                                    )
                                }
                            }

                            // Termux Node.js Status
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                color = ElegantDarkBg.copy(alpha = 0.7f),
                                border = BorderStroke(1.dp, if (isTermuxOnline) ElegantGreenActive.copy(alpha = 0.5f) else ElegantDarkBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(if (isTermuxOnline) ElegantGreenActive else Color.Gray)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isTermuxOnline) "Termux : En Ligne" else "Termux : En attente",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isTermuxOnline) ElegantGreenActive else ElegantTextSecondary,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Controls Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Launch Termux Button
                            Button(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(NodeJsBridgeScript.TERMUX_ONE_LINER))
                                    copiedNotice = "Commande copiée ! Ouverture de Termux..."
                                    TermuxSyncEngine.openTermux(context)
                                },
                                modifier = Modifier.weight(1.3f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen, contentColor = Color.White)
                            ) {
                                Icon(Icons.Default.Terminal, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Lancer Termux", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                            }

                            // Server Start / Stop Button
                            if (isBridgeRunning) {
                                OutlinedButton(
                                    onClick = { viewModel.stopBridge() },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, ElegantRedAlert.copy(alpha = 0.8f))
                                ) {
                                    Text("Arrêter", color = ElegantRedAlert, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.startBridge(8081) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ElegantPurpleAccent)
                                ) {
                                    Text("Démarrer", fontSize = 11.sp, color = ElegantPurpleOnAccent, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                                }
                            }

                            // Sync Button
                            IconButton(
                                onClick = {
                                    viewModel.syncWithTermux()
                                    copiedNotice = "Synchronisation Termux effectuée"
                                },
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ElegantDarkBg)
                                    .border(1.dp, ElegantDarkBorder, RoundedCornerShape(10.dp))
                            ) {
                                Icon(Icons.Default.Sync, contentDescription = "Sync", tint = ElegantPurpleAccent, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            // Sub Navigation Tabs
            item {
                PrimaryTabRow(
                    selectedTabIndex = selectedScreenTab,
                    containerColor = ElegantDarkSurface,
                    contentColor = ElegantPurpleAccent,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, ElegantDarkBorder, RoundedCornerShape(16.dp))
                ) {
                    screenTabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedScreenTab == index,
                            onClick = { selectedScreenTab = index },
                            text = {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (selectedScreenTab == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedScreenTab == index) ElegantPurpleAccent else ElegantTextSecondary,
                                    maxLines = 1
                                )
                            }
                        )
                    }
                }
            }

            when (selectedScreenTab) {
                // TAB 0: WhatsApp Instances list
                0 -> {
                    if (instances.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(1.dp, ElegantDarkBorder)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Hub,
                                        contentDescription = null,
                                        tint = ElegantPurpleAccent,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Aucune instance WhatsApp configurée",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = ElegantTextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Créez une instance pour générer un QR Code ou un code d'appairage et la relier à vos agents IA.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ElegantTextSecondary,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = { showCreateDialog = true },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = ElegantPurpleAccent, contentColor = ElegantPurpleOnAccent)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Créer la Première Instance", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    } else {
                        items(instances, key = { it.id }) { instance ->
                            val assignedAgent = agents.firstOrNull { it.assignedInstanceIdsCsv.split(",").map { s -> s.trim() }.contains(instance.id) && it.assignedInstanceIdsCsv != "*" }
                                ?: agents.firstOrNull { it.assignedInstanceIdsCsv == "*" }
                            InstanceCard(
                                instance = instance,
                                assignedAgent = assignedAgent,
                                onStart = {
                                    viewModel.startInstance(instance)
                                    val cleanPhone = instance.phoneNumber.replace(Regex("[^0-9]"), "")
                                    val cmd = NodeJsBridgeScript.buildPairingCommand(cleanPhone)
                                    clipboardManager.setText(AnnotatedString(cmd))
                                    copiedNotice = "Code 8 chiffres activé ! Commande copiée pour Termux."
                                    activePairingCodeInstance = instance
                                },
                                onDisconnect = { viewModel.disconnectInstance(instance.id) },
                                onDelete = { viewModel.deleteInstance(instance.id) },
                                onShowQr = { activeQrInstance = instance },
                                onShowPairing = { activePairingCodeInstance = instance },
                                onOpenSimulator = { onOpenSimulatorForInstance(instance.id) },
                                onOpenBindDialog = { bindingInstance = instance },
                                onForceConnected = {
                                    viewModel.forceInstanceConnected(instance.id)
                                    copiedNotice = "Statut validé : CONNECTÉ !"
                                },
                                onLaunchTermux = {
                                    clipboardManager.setText(AnnotatedString(NodeJsBridgeScript.TERMUX_ONE_LINER))
                                    copiedNotice = "Commande copiée ! Lancement de Termux..."
                                    TermuxSyncEngine.openTermux(context)
                                },
                                onUpdateServerScript = {
                                    clipboardManager.setText(AnnotatedString(NodeJsBridgeScript.FAST_UPDATE_COMMAND))
                                    copiedNotice = "Commande de mise à jour copiée ! Lancement de Termux..."
                                    TermuxSyncEngine.openTermux(context)
                                }
                            )
                        }
                    }
                }

                // TAB 1: Termux & Node.js Guide
                // TAB 1: Complete Termux & Node.js Guide
                1 -> {
                    item {
                        TermuxGuideSection(
                            isBridgeRunning = isBridgeRunning,
                            bridgePort = bridgePort,
                            phoneNumber = instances.firstOrNull()?.phoneNumber ?: "33773163772",
                            onStartBridge = { viewModel.startBridge(8080) },
                            onCopyText = { text, notice ->
                                clipboardManager.setText(AnnotatedString(text))
                                copiedNotice = notice
                            }
                        )
                    }
                }

                // TAB 2: Live Bridge Logs
                2 -> {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, ElegantDarkBorder)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Logs de la Passerelle Locale",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = ElegantTextPrimary
                                    )
                                    Text(
                                        text = "${bridgeLogs.size} événements enregistrés",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ElegantPurpleSecondary
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedButton(
                                        onClick = { viewModel.clearBridgeLogs() },
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, ElegantDarkBorder)
                                    ) {
                                        Text("Effacer", fontSize = 11.sp, color = ElegantTextSecondary)
                                    }
                                }
                            }
                        }
                    }

                    if (bridgeLogs.isEmpty()) {
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                color = ElegantDarkSurface,
                                border = BorderStroke(1.dp, ElegantDarkBorder)
                            ) {
                                Text(
                                    text = "Aucun log reçu pour le moment. Lancez le script Node.js sur Termux pour voir les événements en temps réel.",
                                    modifier = Modifier.padding(24.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ElegantTextSecondary
                                )
                            }
                        }
                    } else {
                        items(bridgeLogs, key = { it.id }) { logEntry ->
                            BridgeLogItem(logEntry)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(84.dp))
            }
        }

        // FAB to add instance
        if (selectedScreenTab == 0) {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
                    .testTag("create_instance_fab"),
                containerColor = ElegantPurpleAccent,
                contentColor = ElegantPurpleOnAccent,
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Nouvelle Instance")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Nouvelle Instance", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Create Instance Dialog
    if (showCreateDialog) {
        CreateInstanceDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, phone, pairingMethod, bridgeUrl ->
                viewModel.createInstance(name, phone, pairingMethod, bridgeUrl)
                showCreateDialog = false
            }
        )
    }

    // Real QR Code Dialog with ZXing
    activeQrInstance?.let { instance ->
        RealQrCodeDialog(
            instance = instance,
            onDismiss = { activeQrInstance = null },
            onConfirmConnected = {
                viewModel.confirmConnection(instance.id)
                activeQrInstance = null
            }
        )
    }

    // Pairing Code Dialog
    activePairingCodeInstance?.let { instance ->
        PairingCodeDialog(
            instance = instance,
            onDismiss = { activePairingCodeInstance = null },
            onConfirmConnected = {
                viewModel.confirmConnection(instance.id)
                activePairingCodeInstance = null
            },
            onSavePhone = { newPhone ->
                viewModel.updateInstancePhone(instance.id, newPhone)
            }
        )
    }

    // Bind Agent & Model Dialog
    bindingInstance?.let { instance ->
        BindAgentAndModelDialog(
            instance = instance,
            agents = agents,
            models = selectableModels,
            onDismiss = { bindingInstance = null },
            onSave = { agentId, modelId ->
                viewModel.bindAgentAndModelToInstance(instance.id, agentId, modelId)
                bindingInstance = null
            }
        )
    }

    // Copied feedback snackbar
    copiedNotice?.let { notice ->
        AlertDialog(
            onDismissRequest = { copiedNotice = null },
            shape = RoundedCornerShape(16.dp),
            containerColor = ElegantDarkSurface,
            title = { Text("Copié !", color = ElegantGreenActive, fontWeight = FontWeight.Bold) },
            text = { Text(notice, color = ElegantTextPrimary) },
            confirmButton = {
                Button(
                    onClick = { copiedNotice = null },
                    colors = ButtonDefaults.buttonColors(containerColor = ElegantPurpleAccent)
                ) {
                    Text("OK", color = ElegantPurpleOnAccent)
                }
            }
        )
    }
}

@Composable
fun TermuxGuideSection(
    isBridgeRunning: Boolean,
    bridgePort: Int,
    phoneNumber: String = "33773163772",
    onStartBridge: () -> Unit,
    onCopyText: (String, String) -> Unit
) {
    val cleanPhone = phoneNumber.replace(Regex("[^0-9]"), "").ifBlank { "33773163772" }
    val termuxOneLiner = "killall node 2>/dev/null ; pkg update -y && pkg install -y nodejs curl && mkdir -p ~/wa-bridge && cd ~/wa-bridge && rm -rf auth_info_baileys phone.txt auth_* && (curl -s http://127.0.0.1:$bridgePort/server.js > server.js 2>/dev/null || curl -s http://127.0.0.1:8080/server.js > server.js) && npm install --no-audit @whiskeysockets/baileys pino qrcode-terminal && node server.js $cleanPhone"

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
            shape = RoundedCornerShape(22.dp),
            border = BorderStroke(1.dp, if (isBridgeRunning) ElegantGreenActive.copy(alpha = 0.5f) else ElegantRedAlert.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(if (isBridgeRunning) ElegantGreenActive else ElegantRedAlert)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isBridgeRunning) "Passerelle HTTP Prête sur le Téléphone" else "Passerelle HTTP Inactive",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ElegantTextPrimary
                            )
                            Text(
                                text = "Point d'accès local : http://127.0.0.1:$bridgePort",
                                style = MaterialTheme.typography.bodySmall,
                                color = ElegantPurpleSecondary
                            )
                        }
                    }

                    if (!isBridgeRunning) {
                        Button(
                            onClick = onStartBridge,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ElegantPurpleAccent)
                        ) {
                            Text("Démarrer", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Method 1: All-in-One 1-Click Command
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
            shape = RoundedCornerShape(22.dp),
            border = BorderStroke(1.dp, ElegantPurpleAccent.copy(alpha = 0.6f))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(38.dp).clip(CircleShape).background(ElegantDarkBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Terminal, contentDescription = null, tint = ElegantPurpleAccent)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Méthode 1 : Commande Automatique en 1-Clic",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ElegantTextPrimary
                        )
                        Text(
                            text = "Pour le numéro $cleanPhone (code à 8 chiffres sans aucun signe +)",
                            style = MaterialTheme.typography.bodySmall,
                            color = ElegantPurpleSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = ElegantDarkBg,
                    border = BorderStroke(1.dp, ElegantDarkBorder)
                ) {
                    Text(
                        text = termuxOneLiner,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = ElegantGreenActive,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        onCopyText(termuxOneLiner, "Commande copiée ! Collez-la dans Termux.")
                    },
                    modifier = Modifier.fillMaxWidth().testTag("copy_termux_oneliner"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElegantPurpleAccent, contentColor = ElegantPurpleOnAccent)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Copier la Commande Complète Termux", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Method 2: Detailed Step-by-Step
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
            shape = RoundedCornerShape(22.dp),
            border = BorderStroke(1.dp, ElegantDarkBorder)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Méthode 2 : Installation Pas-à-Pas (Manuel)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ElegantTextPrimary
                )

                // Step 1
                TermuxStepItem(
                    stepNumber = "1",
                    title = "Installer Node.js et curl sur Termux",
                    description = "Ouvrez Termux et mettez à jour les dépôts de paquets.",
                    command = "pkg update -y && pkg install -y nodejs curl",
                    onCopy = { onCopyText(it, "Étape 1 copiée !") }
                )

                // Step 2
                TermuxStepItem(
                    stepNumber = "2",
                    title = "Nettoyer et télécharger le script serveur",
                    description = "Prépare le dossier ~/wa-bridge et télécharge la dernière version de server.js.",
                    command = "killall node 2>/dev/null ; mkdir -p ~/wa-bridge && cd ~/wa-bridge && rm -rf auth_info_baileys phone.txt auth_* && (curl -s http://127.0.0.1:$bridgePort/server.js > server.js 2>/dev/null || curl -s http://127.0.0.1:8080/server.js > server.js)",
                    onCopy = { onCopyText(it, "Étape 2 copiée !") }
                )

                // Step 3
                TermuxStepItem(
                    stepNumber = "3",
                    title = "Installer les dépendances Baileys Multi-Device",
                    description = "Installe Baileys et pino.",
                    command = "npm install --no-audit @whiskeysockets/baileys pino qrcode-terminal",
                    onCopy = { onCopyText(it, "Étape 3 copiée !") }
                )

                // Step 4
                TermuxStepItem(
                    stepNumber = "4",
                    title = "Lancer Baileys pour votre numéro",
                    description = "Génère le code d'appairage à 8 chiffres pour votre WhatsApp.",
                    command = "node server.js $cleanPhone",
                    onCopy = { onCopyText(it, "Étape 4 copiée !") }
                )
            }
        }

        // Production Stability & Background Execution (Wake-lock)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = ElegantDarkSurfaceVariant),
            shape = RoundedCornerShape(22.dp),
            border = BorderStroke(1.dp, ElegantDarkBorder)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Sync, contentDescription = null, tint = ElegantPurpleAccent)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Stabilité & Arrière-Plan 24h/24 (Anti-Kill Android)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ElegantTextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Android coupe les applications en arrière-plan lorsque l'écran s'éteint. Pour que votre bot WhatsApp réponde 24h/24 en continu, activez le verrouillage de veille Termux :",
                    style = MaterialTheme.typography.bodySmall,
                    color = ElegantTextSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = ElegantDarkBg,
                    border = BorderStroke(1.dp, ElegantDarkBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "termux-wake-lock",
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = FontFamily.Monospace,
                            color = ElegantGreenActive
                        )
                        IconButton(onClick = { onCopyText("termux-wake-lock", "Commande wake-lock copiée !") }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = ElegantPurpleAccent, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "💡 Test rapide de connectivité :",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = ElegantTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = ElegantDarkBg,
                    border = BorderStroke(1.dp, ElegantDarkBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "curl http://127.0.0.1:$bridgePort/api/health",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = EdgeAiCyan
                        )
                        IconButton(onClick = { onCopyText("curl http://127.0.0.1:$bridgePort/api/health", "Test santé copié !") }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = ElegantPurpleAccent, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TermuxStepItem(
    stepNumber: String,
    title: String,
    description: String,
    command: String,
    onCopy: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(ElegantPurpleAccent),
                contentAlignment = Alignment.Center
            ) {
                Text(stepNumber, color = ElegantPurpleOnAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = ElegantTextPrimary)
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(description, style = MaterialTheme.typography.bodySmall, color = ElegantTextSecondary, modifier = Modifier.padding(start = 34.dp))
        Spacer(modifier = Modifier.height(6.dp))

        Surface(
            modifier = Modifier.fillMaxWidth().padding(start = 34.dp),
            shape = RoundedCornerShape(10.dp),
            color = ElegantDarkBg,
            border = BorderStroke(1.dp, ElegantDarkBorder)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = command,
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = ElegantGreenActive,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { onCopy(command) }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = ElegantPurpleAccent, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun BridgeLogItem(log: com.example.domain.baileys.BridgeLogEntry) {
    val (bgColor, tintColor) = when (log.type) {
        LogType.SUCCESS -> ElegantGreenActive.copy(alpha = 0.1f) to ElegantGreenActive
        LogType.INCOMING -> ElegantPurpleAccent.copy(alpha = 0.1f) to ElegantPurpleAccent
        LogType.OUTGOING -> EdgeAiCyan.copy(alpha = 0.1f) to EdgeAiCyan
        LogType.ERROR -> ElegantRedAlert.copy(alpha = 0.1f) to ElegantRedAlert
        LogType.INFO -> ElegantDarkBg to ElegantTextSecondary
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        border = BorderStroke(1.dp, tintColor.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = log.timestamp,
                style = MaterialTheme.typography.labelSmall,
                color = tintColor,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = log.message,
                style = MaterialTheme.typography.bodySmall,
                color = ElegantTextPrimary,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun RealQrCodeDialog(
    instance: WhatsAppInstanceEntity,
    onDismiss: () -> Unit,
    onConfirmConnected: () -> Unit
) {
    val qrPayload = instance.qrToken.ifBlank { "2@mock-wa-pairing-token-ready" }
    val qrBitmap = remember(qrPayload) {
        QrCodeGenerator.generateQrCodeBitmap(qrPayload, width = 512, height = 512)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = ElegantDarkSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.QrCode, contentDescription = null, tint = ElegantPurpleAccent)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Scanner le QR Code WhatsApp", fontWeight = FontWeight.Bold, color = ElegantTextPrimary)
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Ouvrez WhatsApp sur votre téléphone > Appareils connectés > Connecter un appareil.",
                    style = MaterialTheme.typography.bodySmall,
                    color = ElegantTextSecondary
                )

                // Real ZXing Generated QR Code
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .border(2.dp, ElegantPurpleAccent, RoundedCornerShape(16.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (qrBitmap != null) {
                        Image(
                            bitmap = qrBitmap,
                            contentDescription = "QR Code WhatsApp",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text("Génération QR...", color = Color.Black)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ElegantDarkBg,
                    border = BorderStroke(1.dp, ElegantDarkBorder)
                ) {
                    Text(
                        text = "Instance: ${instance.name} • Port: ${instance.localPort}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = ElegantPurpleSecondary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmConnected,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ElegantPurpleAccent, contentColor = ElegantPurpleOnAccent)
            ) {
                Text("J'ai Scanné / Confirmer Connexion", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer", color = ElegantTextSecondary)
            }
        }
    )
}

@Composable
fun InstanceCard(
    instance: WhatsAppInstanceEntity,
    assignedAgent: AgentEntity?,
    onStart: () -> Unit,
    onDisconnect: () -> Unit,
    onDelete: () -> Unit,
    onShowQr: () -> Unit,
    onShowPairing: () -> Unit,
    onOpenSimulator: () -> Unit,
    onOpenBindDialog: () -> Unit,
    onForceConnected: () -> Unit,
    onLaunchTermux: () -> Unit,
    onUpdateServerScript: () -> Unit
) {
    val isConnected = instance.status == "CONNECTED"
    val isQrReady = instance.status == "QR_READY"
    val isPairingCode = instance.status == "PAIRING_CODE"

    val statusColor = when (instance.status) {
        "CONNECTED" -> ElegantGreenActive
        "CONNECTING" -> ElegantPurpleAccent
        "QR_READY" -> EdgeAiCyan
        "PAIRING_CODE" -> EdgeAiCyan
        else -> ElegantRedAlert
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("instance_card_${instance.id}"),
        colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, ElegantDarkBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Status dot + name and Status pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = instance.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ElegantTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = instance.status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Numéro : ${instance.phoneNumber} • Méthode : ${instance.pairingMethod} • Port : ${instance.localPort}",
                style = MaterialTheme.typography.bodySmall,
                color = ElegantTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Force Connected Helper if not connected yet
            if (!isConnected) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onForceConnected() },
                    shape = RoundedCornerShape(12.dp),
                    color = ElegantGreenActive.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, ElegantGreenActive.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ElegantGreenActive, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "WhatsApp déjà connecté sur Termux ?",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ElegantTextPrimary
                                )
                                Text(
                                    text = "Touchez pour passer en statut CONNECTÉ",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ElegantGreenActive
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = ElegantGreenActive,
                            contentColor = Color.Black
                        ) {
                            Text(
                                text = "Valider",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            } else {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = ElegantGreenActive.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, ElegantGreenActive.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ElegantGreenActive, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Connecté à Baileys • Interception active",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElegantGreenActive,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // AI Model & Agent Binding Banner
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenBindDialog() },
                shape = RoundedCornerShape(14.dp),
                color = ElegantDarkBg,
                border = BorderStroke(1.dp, if (assignedAgent != null) ElegantPurpleAccent.copy(alpha = 0.5f) else ElegantDarkBorder)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (assignedAgent != null) ElegantPurpleAccent.copy(alpha = 0.2f) else ElegantDarkSurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = null,
                                tint = if (assignedAgent != null) ElegantPurpleAccent else ElegantTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (assignedAgent != null) "Agent IA : ${assignedAgent.name}" else "Aucun agent IA branché",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = if (assignedAgent != null) ElegantTextPrimary else ElegantTextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (assignedAgent != null) "Modèle : ${assignedAgent.modelId}" else "Touchez pour brancher un modèle IA",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (assignedAgent != null) EdgeAiCyan else ElegantPurpleSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ElegantPurpleAccent.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, ElegantPurpleAccent.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = if (assignedAgent != null) "Modifier" else "Brancher",
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

            Spacer(modifier = Modifier.height(12.dp))

            // Action Row 1: Primary actions (Start/Disconnect and Open Threads)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!isConnected) {
                    Button(
                        onClick = onStart,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ElegantPurpleAccent, contentColor = ElegantPurpleOnAccent)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Démarrer", fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                    }
                } else {
                    OutlinedButton(
                        onClick = onDisconnect,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, ElegantRedAlert.copy(alpha = 0.7f))
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null, tint = ElegantRedAlert, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Déconnecter", color = ElegantRedAlert, maxLines = 1, softWrap = false)
                    }
                }

                Button(
                    onClick = onOpenSimulator,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen, contentColor = Color.White)
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Threads WhatsApp", fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1, softWrap = false)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Row 2: Secondary actions (Brancher IA, Termux Helper, QR/Code, Delete)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenBindDialog,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, ElegantPurpleAccent.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.Link, contentDescription = null, tint = ElegantPurpleAccent, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Brancher IA", color = ElegantPurpleAccent, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, softWrap = false)
                }

                // Launch Termux Button
                OutlinedButton(
                    onClick = onLaunchTermux,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, WhatsAppGreen.copy(alpha = 0.6f))
                ) {
                    Icon(Icons.Default.Terminal, contentDescription = "Termux", tint = WhatsAppGreen, modifier = Modifier.size(16.dp))
                }

                // Update server.js Button
                OutlinedButton(
                    onClick = onUpdateServerScript,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, EdgeAiCyan.copy(alpha = 0.6f))
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Maj Script", tint = EdgeAiCyan, modifier = Modifier.size(16.dp))
                }

                // Code 8 Chiffres is the primary connection dialog
                OutlinedButton(
                    onClick = onShowPairing,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, ElegantPurpleAccent.copy(alpha = 0.8f)),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = ElegantPurpleAccent.copy(alpha = 0.12f))
                ) {
                    Icon(Icons.Default.VpnKey, contentDescription = "Code", tint = ElegantPurpleAccent, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Code 8 Chiffres", color = ElegantPurpleAccent, fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1, softWrap = false)
                }

                if (isQrReady || (!isConnected && instance.pairingMethod == "QR_CODE")) {
                    OutlinedButton(
                        onClick = onShowQr,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, ElegantDarkBorder)
                    ) {
                        Icon(Icons.Default.QrCode, contentDescription = "QR Code", tint = ElegantTextSecondary, modifier = Modifier.size(16.dp))
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
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BindAgentAndModelDialog(
    instance: WhatsAppInstanceEntity,
    agents: List<AgentEntity>,
    models: List<SelectableModelOption>,
    onDismiss: () -> Unit,
    onSave: (agentId: String, modelId: String) -> Unit
) {
    var selectedAgentId by remember {
        mutableStateOf(
            agents.firstOrNull { it.assignedInstanceIdsCsv == instance.id || it.assignedInstanceIdsCsv == "*" }?.id
                ?: agents.firstOrNull()?.id
                ?: ""
        )
    }
    val currentAgent = agents.firstOrNull { it.id == selectedAgentId }
    var selectedModelId by remember {
        mutableStateOf(
            currentAgent?.modelId ?: models.firstOrNull()?.id ?: "gemma-2-2b-it-int4"
        )
    }

    var expandedAgentDropdown by remember { mutableStateOf(false) }
    var expandedModelDropdown by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = ElegantDarkSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.SmartToy, contentDescription = null, tint = ElegantPurpleAccent)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Brancher IA sur ${instance.name}", fontWeight = FontWeight.Bold, color = ElegantTextPrimary, fontSize = 18.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Choisissez quel agent et quel modèle d'IA exécuter automatiquement lorsqu'un message WhatsApp arrive sur cette instance.",
                    style = MaterialTheme.typography.bodySmall,
                    color = ElegantTextSecondary
                )

                // Agent selector
                Text("1. Sélectionner l'Agent IA :", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = ElegantTextPrimary)
                ExposedDropdownMenuBox(
                    expanded = expandedAgentDropdown,
                    onExpandedChange = { expandedAgentDropdown = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = agents.firstOrNull { it.id == selectedAgentId }?.name ?: "Sélectionner un agent",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAgentDropdown) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedAgentDropdown,
                        onDismissRequest = { expandedAgentDropdown = false },
                        modifier = Modifier.background(ElegantDarkSurface)
                    ) {
                        agents.forEach { agent ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(agent.name, fontWeight = FontWeight.Bold, color = ElegantTextPrimary)
                                        Text("${agent.role} • ${agent.modelId}", fontSize = 11.sp, color = ElegantPurpleSecondary)
                                    }
                                },
                                onClick = {
                                    selectedAgentId = agent.id
                                    selectedModelId = agent.modelId
                                    expandedAgentDropdown = false
                                }
                            )
                        }
                    }
                }

                // Model selector
                Text("2. Sélectionner le Modèle Local :", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = ElegantTextPrimary)
                ExposedDropdownMenuBox(
                    expanded = expandedModelDropdown,
                    onExpandedChange = { expandedModelDropdown = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val currentModelName = models.firstOrNull { it.id == selectedModelId }?.name ?: selectedModelId
                    OutlinedTextField(
                        value = currentModelName,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedModelDropdown) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedModelDropdown,
                        onDismissRequest = { expandedModelDropdown = false },
                        modifier = Modifier.background(ElegantDarkSurface)
                    ) {
                        models.forEach { model ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(model.name, fontWeight = FontWeight.Bold, color = ElegantTextPrimary)
                                        Text(model.details, fontSize = 11.sp, color = EdgeAiCyan)
                                    }
                                },
                                onClick = {
                                    selectedModelId = model.id
                                    expandedModelDropdown = false
                                }
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = ElegantDarkBg,
                    border = BorderStroke(1.dp, ElegantDarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "⚡ Exécution Locale :",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = ElegantGreenActive
                        )
                        Text(
                            text = "Dès qu'un client vous écrit sur WhatsApp, Baileys transmet le message à l'application. L'agent répondra en quelques millisecondes avec le modèle sélectionné.",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElegantTextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedAgentId.isNotBlank()) {
                        onSave(selectedAgentId, selectedModelId)
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ElegantPurpleAccent, contentColor = ElegantPurpleOnAccent)
            ) {
                Text("Enregistrer la Liaison", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", color = ElegantTextSecondary)
            }
        }
    )
}

@Composable
fun CreateInstanceDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, phone: String, pairingMethod: String, bridgeUrl: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var pairingMethod by remember { mutableStateOf("PAIRING_CODE") }
    var bridgeUrl by remember { mutableStateOf("http://127.0.0.1:8080") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = ElegantDarkSurface,
        title = {
            Text("Créer une Instance WhatsApp", fontWeight = FontWeight.Bold, color = ElegantTextPrimary)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom de l'Instance") },
                    placeholder = { Text("Ex: WhatsApp Principal") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("instance_name_input")
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it.replace(Regex("[^0-9]"), "") },
                    label = { Text("Numéro WhatsApp (chiffres sans +)") },
                    placeholder = { Text("Ex: 33773163772") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("instance_phone_input")
                )

                Text("Méthode d'Appairage :", style = MaterialTheme.typography.labelMedium, color = ElegantPurpleAccent, fontWeight = FontWeight.Bold)

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { pairingMethod = "PAIRING_CODE" }) {
                    RadioButton(selected = pairingMethod == "PAIRING_CODE", onClick = { pairingMethod = "PAIRING_CODE" })
                    Text("Code à 8 chiffres (Recommandé, sans QR)", style = MaterialTheme.typography.bodySmall, color = ElegantTextPrimary)
                }

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { pairingMethod = "QR_CODE" }) {
                    RadioButton(selected = pairingMethod == "QR_CODE", onClick = { pairingMethod = "QR_CODE" })
                    Text("QR Code (Scanner)", style = MaterialTheme.typography.bodySmall, color = ElegantTextPrimary)
                }

                OutlinedTextField(
                    value = bridgeUrl,
                    onValueChange = { bridgeUrl = it },
                    label = { Text("URL Passerelle Locale") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val cleanPhone = phone.replace(Regex("[^0-9]"), "").ifBlank { "33773163772" }
                        onCreate(name, cleanPhone, pairingMethod, bridgeUrl)
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ElegantPurpleAccent, contentColor = ElegantPurpleOnAccent)
            ) {
                Text("Créer l'Instance", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", color = ElegantTextSecondary)
            }
        }
    )
}

@Composable
fun PairingCodeDialog(
    instance: WhatsAppInstanceEntity,
    onDismiss: () -> Unit,
    onConfirmConnected: () -> Unit,
    onSavePhone: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var phoneNumber by remember(instance.phoneNumber) { mutableStateOf(instance.phoneNumber) }
    val displayCode = instance.pairingCode.ifBlank { "" }
    val cleanPhone = phoneNumber.replace(Regex("[^0-9]"), "")
    val pairingCmd = NodeJsBridgeScript.buildPairingCommand(cleanPhone)

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = ElegantDarkSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(ElegantPurpleAccent.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VpnKey,
                        contentDescription = null,
                        tint = ElegantPurpleAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Code d'Appairage (8 Chiffres)", fontWeight = FontWeight.Bold, color = ElegantTextPrimary, fontSize = 17.sp)
                    Text("Multi-Device WhatsApp sans QR Code", style = MaterialTheme.typography.labelSmall, color = ElegantGreenActive)
                }
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Phone number input field
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = {
                        val clean = it.replace(Regex("[^0-9]"), "")
                        phoneNumber = clean
                        onSavePhone?.invoke(clean)
                    },
                    label = { Text("Numéro WhatsApp (chiffres sans +)") },
                    placeholder = { Text("Ex: 33773163772") },
                    leadingIcon = {
                        Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = ElegantPurpleAccent)
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // 8-Digit Code Display or Waiting banner
                if (displayCode.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = ElegantDarkBg,
                        border = BorderStroke(2.dp, ElegantPurpleAccent)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = displayCode,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = ElegantGreenActive,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 4.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Code généré par le pont WhatsApp",
                                style = MaterialTheme.typography.labelSmall,
                                color = ElegantTextSecondary
                            )
                        }
                    }

                    Button(
                        onClick = { clipboardManager.setText(AnnotatedString(displayCode)) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ElegantDarkSurfaceVariant)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copier le Code à 8 Chiffres", fontSize = 12.sp, color = ElegantTextPrimary)
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = ElegantDarkBg,
                        border = BorderStroke(1.dp, ElegantPurpleAccent.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🔑 QR Code masqué dans Termux",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = ElegantPurpleAccent
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Le QR code est désactivé. Lancez la commande ci-dessous dans Termux pour afficher le code d'appairage à 8 chiffres.",
                                style = MaterialTheme.typography.bodySmall,
                                color = ElegantTextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Launch Termux Button
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(pairingCmd))
                        TermuxSyncEngine.openTermux(context)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen, contentColor = Color.White)
                ) {
                    Icon(Icons.Default.Terminal, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Lancer Termux (Code 8 Chiffres)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = { clipboardManager.setText(AnnotatedString(pairingCmd)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, ElegantDarkBorder)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = ElegantTextSecondary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copier la Commande Termux", fontSize = 12.sp, color = ElegantTextSecondary)
                }

                // Step by step guide
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ElegantDarkBg,
                    border = BorderStroke(1.dp, ElegantDarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "📲 Comment lier sur WhatsApp :",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = ElegantTextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "1. Touchez 'Lancer Termux' (le code s'affiche en gros à l'écran)\n2. Ouvrez WhatsApp > ⋮ > Appareils connectés\n3. Touchez 'Connecter un appareil'\n4. En bas du scanner, touchez 'Lier avec un numéro de téléphone'\n5. Entrez le code à 8 chiffres",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElegantTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmConnected,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ElegantPurpleAccent, contentColor = ElegantPurpleOnAccent)
            ) {
                Text("J'ai Saisi le Code", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer", color = ElegantTextSecondary)
            }
        }
    )
}
