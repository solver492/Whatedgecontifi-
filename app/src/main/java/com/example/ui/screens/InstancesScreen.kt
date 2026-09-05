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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.WhatsAppInstanceEntity
import com.example.domain.baileys.LogType
import com.example.domain.baileys.NodeJsBridgeScript
import com.example.domain.baileys.QrCodeGenerator
import com.example.ui.MainViewModel
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

    val isBridgeRunning by viewModel.bridgeRunning.collectAsState()
    val bridgePort by viewModel.bridgePort.collectAsState()
    val bridgeLogs by viewModel.bridgeLogs.collectAsState()

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
                            Column {
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
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
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(if (isBridgeRunning) ElegantGreenActive else ElegantRedAlert)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isBridgeRunning) "Serveur Local: http://127.0.0.1:$bridgePort" else "Serveur Local: Arrêté",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = ElegantTextPrimary
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (isBridgeRunning) {
                                    OutlinedButton(
                                        onClick = { viewModel.stopBridge() },
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, ElegantRedAlert.copy(alpha = 0.6f))
                                    ) {
                                        Text("Arrêter", color = ElegantRedAlert, fontSize = 11.sp)
                                    }
                                } else {
                                    Button(
                                        onClick = { viewModel.startBridge(8080) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = ElegantPurpleAccent)
                                    ) {
                                        Text("Démarrer", fontSize = 11.sp, color = ElegantPurpleOnAccent)
                                    }
                                }
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
                            InstanceCard(
                                instance = instance,
                                onStart = { viewModel.startInstance(instance) },
                                onDisconnect = { viewModel.disconnectInstance(instance.id) },
                                onDelete = { viewModel.deleteInstance(instance.id) },
                                onShowQr = { activeQrInstance = instance },
                                onShowPairing = { activePairingCodeInstance = instance },
                                onOpenSimulator = { onOpenSimulatorForInstance(instance.id) }
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
    onStartBridge: () -> Unit,
    onCopyText: (String, String) -> Unit
) {
    val termuxOneLiner = NodeJsBridgeScript.TERMUX_ONE_LINER

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
                            text = "Installe Node.js, télécharge bridge.js et lance Baileys",
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
                        text = "pkg update -y && pkg install -y nodejs git && mkdir -p ~/wa-bridge && cd ~/wa-bridge && curl -s http://127.0.0.1:$bridgePort/bridge.js -o bridge.js && npm install @whiskeysockets/baileys pino qrcode-terminal && node bridge.js",
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
                        val cmd = "pkg update -y && pkg install -y nodejs git && mkdir -p ~/wa-bridge && cd ~/wa-bridge && curl -s http://127.0.0.1:$bridgePort/bridge.js -o bridge.js && npm install @whiskeysockets/baileys pino qrcode-terminal && node bridge.js"
                        onCopyText(cmd, "Commande automatique copiée ! Collez-la dans Termux.")
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
                    title = "Installer Node.js et Git sur Termux",
                    description = "Ouvrez Termux et mettez à jour les dépôts de paquets.",
                    command = "pkg update -y && pkg install -y nodejs git",
                    onCopy = { onCopyText(it, "Étape 1 copiée !") }
                )

                // Step 2
                TermuxStepItem(
                    stepNumber = "2",
                    title = "Créer le dossier et télécharger le script du pont",
                    description = "Crée le répertoire de travail et télécharge wa-bridge.js directement depuis cette application.",
                    command = "mkdir -p ~/wa-bridge && cd ~/wa-bridge && curl -s http://127.0.0.1:$bridgePort/bridge.js -o bridge.js",
                    onCopy = { onCopyText(it, "Étape 2 copiée !") }
                )

                // Step 3
                TermuxStepItem(
                    stepNumber = "3",
                    title = "Installer les dépendances Baileys Multi-Device",
                    description = "Installe la bibliothèque WhatsApp Baileys ainsi que le formateur de QR Code.",
                    command = "npm install @whiskeysockets/baileys pino qrcode-terminal",
                    onCopy = { onCopyText(it, "Étape 3 copiée !") }
                )

                // Step 4
                TermuxStepItem(
                    stepNumber = "4",
                    title = "Lancer le pont Baileys WhatsApp",
                    description = "Démarre la connexion WhatsApp. Le QR Code sera imprimé dans le terminal et transmis à l'appli.",
                    command = "node bridge.js",
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
    onStart: () -> Unit,
    onDisconnect: () -> Unit,
    onDelete: () -> Unit,
    onShowQr: () -> Unit,
    onShowPairing: () -> Unit,
    onOpenSimulator: () -> Unit
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
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = instance.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ElegantTextPrimary
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
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Numéro : ${instance.phoneNumber} • Méthode : ${instance.pairingMethod}",
                style = MaterialTheme.typography.bodySmall,
                color = ElegantTextSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))

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
                        Text("Démarrer", fontWeight = FontWeight.Bold)
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
                        Text("Déconnecter", color = ElegantRedAlert)
                    }
                }

                if (isQrReady || (!isConnected && instance.pairingMethod == "QR_CODE")) {
                    OutlinedButton(
                        onClick = onShowQr,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, ElegantPurpleAccent.copy(alpha = 0.6f))
                    ) {
                        Icon(Icons.Default.QrCode, contentDescription = "QR Code", tint = ElegantPurpleAccent, modifier = Modifier.size(18.dp))
                    }
                }

                if (isPairingCode || (!isConnected && instance.pairingMethod == "PAIRING_CODE")) {
                    OutlinedButton(
                        onClick = onShowPairing,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, ElegantPurpleAccent.copy(alpha = 0.6f))
                    ) {
                        Text("Code", color = ElegantPurpleAccent, fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedButton(
                    onClick = onDelete,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, ElegantDarkBorder)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = ElegantTextSecondary, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun CreateInstanceDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, phone: String, pairingMethod: String, bridgeUrl: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var pairingMethod by remember { mutableStateOf("QR_CODE") }
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
                    onValueChange = { phone = it },
                    label = { Text("Numéro WhatsApp") },
                    placeholder = { Text("Ex: +33 7 12 34 56 78") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("instance_phone_input")
                )

                Text("Méthode d'Appairage :", style = MaterialTheme.typography.labelMedium, color = ElegantPurpleAccent, fontWeight = FontWeight.Bold)

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { pairingMethod = "QR_CODE" }) {
                    RadioButton(selected = pairingMethod == "QR_CODE", onClick = { pairingMethod = "QR_CODE" })
                    Text("QR Code (Recommandé)", style = MaterialTheme.typography.bodySmall, color = ElegantTextPrimary)
                }

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { pairingMethod = "PAIRING_CODE" }) {
                    RadioButton(selected = pairingMethod == "PAIRING_CODE", onClick = { pairingMethod = "PAIRING_CODE" })
                    Text("Code à 8 chiffres (Téléphone)", style = MaterialTheme.typography.bodySmall, color = ElegantTextPrimary)
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
                        onCreate(name, phone.ifEmpty { "+33 7 00 00 00 00" }, pairingMethod, bridgeUrl)
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
    onConfirmConnected: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val code = instance.pairingCode.ifBlank { "W8K2-9XP4" }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = ElegantDarkSurface,
        title = {
            Text("Code d'Appairage à 8 Chiffres", fontWeight = FontWeight.Bold, color = ElegantTextPrimary)
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Sur votre smartphone : WhatsApp > Appareils connectés > Connecter un appareil > Lier avec un numéro de téléphone.",
                    style = MaterialTheme.typography.bodySmall,
                    color = ElegantTextSecondary
                )

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = ElegantDarkBg,
                    border = BorderStroke(2.dp, ElegantPurpleAccent)
                ) {
                    Text(
                        text = code,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = ElegantTextPrimary,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp),
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 4.sp
                    )
                }

                Button(
                    onClick = { clipboardManager.setText(AnnotatedString(code)) },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElegantDarkSurfaceVariant)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copier le Code", fontSize = 12.sp, color = ElegantTextPrimary)
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
