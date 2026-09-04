package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Sync
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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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

@Composable
fun InstancesScreen(
    viewModel: MainViewModel,
    instances: List<WhatsAppInstanceEntity>,
    onOpenSimulatorForInstance: (String) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var activeQrInstance by remember { mutableStateOf<WhatsAppInstanceEntity?>(null) }
    var activePairingCodeInstance by remember { mutableStateOf<WhatsAppInstanceEntity?>(null) }

    Box(modifier = Modifier.fillMaxSize().background(ElegantDarkBg)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                // Elegant Dark Hero Banner (bg-[#4A4458] rounded-3xl)
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
                                    imageVector = Icons.Default.Hub,
                                    contentDescription = null,
                                    tint = ElegantPurpleAccent
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "WhatsApp Services",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ElegantTextPrimary
                                )
                                Text(
                                    text = "BAILEYS INSTANCES & CLOUD BRIDGE",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 1.sp,
                                    color = ElegantPurpleAccent,
                                    fontSize = 10.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val connectedCount = instances.count { it.status == "CONNECTED" }
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
                                            .background(if (connectedCount > 0) ElegantGreenActive else ElegantRedAlert)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "$connectedCount / ${instances.size} En ligne",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = ElegantTextPrimary
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = ElegantDarkBg.copy(alpha = 0.6f),
                                border = BorderStroke(1.dp, ElegantDarkBorder)
                            ) {
                                Text(
                                    text = "WS Protocol: 127.0.0.1",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ElegantPurpleSecondary,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }

            items(instances, key = { it.id }) { instance ->
                InstanceCard(
                    instance = instance,
                    onStart = { viewModel.startInstance(instance) },
                    onDisconnect = { viewModel.disconnectInstance(instance.id) },
                    onShowQr = { activeQrInstance = instance },
                    onShowPairingCode = { activePairingCodeInstance = instance },
                    onDelete = { viewModel.deleteInstance(instance.id) },
                    onTestChat = { onOpenSimulatorForInstance(instance.id) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(84.dp))
            }
        }

        // Floating Action Button to create instance with Elegant Dark styling
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
                Icon(imageVector = Icons.Default.Add, contentDescription = "Nouvelle instance")
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Nouvelle Instance", fontWeight = FontWeight.Bold)
            }
        }
    }

    // Dialog: Create Instance
    if (showCreateDialog) {
        CreateInstanceDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, phone, pairingMethod, bridgeUrl ->
                viewModel.createInstance(name, phone, pairingMethod, bridgeUrl)
                showCreateDialog = false
            }
        )
    }

    // Dialog: QR Code Viewer
    activeQrInstance?.let { instance ->
        QrCodeDialog(
            instance = instance,
            onDismiss = { activeQrInstance = null },
            onConfirmConnected = {
                viewModel.confirmConnection(instance.id)
                activeQrInstance = null
            }
        )
    }

    // Dialog: Pairing Code
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
}

@Composable
fun InstanceCard(
    instance: WhatsAppInstanceEntity,
    onStart: () -> Unit,
    onDisconnect: () -> Unit,
    onShowQr: () -> Unit,
    onShowPairingCode: () -> Unit,
    onDelete: () -> Unit,
    onTestChat: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("instance_card_${instance.id}"),
        colors = CardDefaults.cardColors(
            containerColor = ElegantDarkSurface
        ),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, ElegantDarkBorder)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header: Name, Status Badge, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val (statusColor, statusText) = when (instance.status) {
                        "CONNECTED" -> Pair(ElegantGreenActive, "Connecté")
                        "QR_READY" -> Pair(ElegantPurpleAccent, "Scannez QR")
                        "PAIRING_CODE" -> Pair(ElegantPurpleAccent, "Code Prêt")
                        "CONNECTING" -> Pair(Color(0xFFFFB300), "Connexion...")
                        else -> Pair(ElegantRedAlert, "Déconnecté")
                    }

                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = instance.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ElegantTextPrimary
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Supprimer",
                        tint = ElegantTextSecondary.copy(alpha = 0.6f)
                    )
                }
            }

            Text(
                text = instance.phoneNumber,
                style = MaterialTheme.typography.bodyMedium,
                color = ElegantTextSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Details row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = ElegantDarkBg,
                    border = BorderStroke(1.dp, ElegantDarkBorder)
                ) {
                    Text(
                        text = "Port: ${instance.localPort}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = ElegantPurpleSecondary,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = ElegantDarkBg,
                    border = BorderStroke(1.dp, ElegantDarkBorder)
                ) {
                    Text(
                        text = "Baileys WS",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = ElegantTextSecondary
                    )
                }
                if (instance.status == "CONNECTED") {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = ElegantDarkCardElevated,
                        border = BorderStroke(1.dp, ElegantPurpleAccent.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = "Agents IA Actifs",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = ElegantPurpleAccent,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (instance.status) {
                    "CONNECTED" -> {
                        OutlinedButton(
                            onClick = onDisconnect,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, ElegantDarkBorder),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ElegantRedAlert)
                        ) {
                            Icon(Icons.Default.LinkOff, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Déconnecter", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    "QR_READY" -> {
                        Button(
                            onClick = onShowQr,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ElegantPurpleAccent, contentColor = ElegantPurpleOnAccent)
                        ) {
                            Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Afficher QR", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                    "PAIRING_CODE" -> {
                        Button(
                            onClick = onShowPairingCode,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ElegantPurpleAccent, contentColor = ElegantPurpleOnAccent)
                        ) {
                            Text("Code : ${instance.pairingCode.ifEmpty { "Voir Code" }}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                    else -> {
                        Button(
                            onClick = onStart,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ElegantPurpleAccent, contentColor = ElegantPurpleOnAccent)
                        ) {
                            Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Lancer Connexion", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Simulator Chat Button
                OutlinedButton(
                    onClick = onTestChat,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, ElegantDarkBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ElegantTextPrimary)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp), tint = ElegantPurpleAccent)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tester Chat", style = MaterialTheme.typography.labelMedium)
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
    var bridgeUrl by remember { mutableStateOf("ws://127.0.0.1:8080/baileys") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = ElegantDarkSurface,
        title = {
            Text(
                "Créer une Instance WhatsApp",
                fontWeight = FontWeight.Bold,
                color = ElegantTextPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom de l'instance") },
                    placeholder = { Text("Ex: Support VIP, Commercial") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Numéro WhatsApp") },
                    placeholder = { Text("+33 6 12 34 56 78") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Méthode d'appairage :",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = ElegantPurpleAccent
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = pairingMethod == "QR_CODE",
                        onClick = { pairingMethod = "QR_CODE" }
                    )
                    Text("QR Code (WhatsApp Web)", style = MaterialTheme.typography.bodySmall, color = ElegantTextPrimary)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = pairingMethod == "PAIRING_CODE",
                        onClick = { pairingMethod = "PAIRING_CODE" }
                    )
                    Text("Code à 8 chiffres (Téléphone)", style = MaterialTheme.typography.bodySmall, color = ElegantTextPrimary)
                }

                OutlinedTextField(
                    value = bridgeUrl,
                    onValueChange = { bridgeUrl = it },
                    label = { Text("URL Bridge Baileys / WebSocket") },
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
                Text("Démarrer Baileys", fontWeight = FontWeight.Bold)
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
fun QrCodeDialog(
    instance: WhatsAppInstanceEntity,
    onDismiss: () -> Unit,
    onConfirmConnected: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = ElegantDarkSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.QrCode, contentDescription = null, tint = ElegantPurpleAccent)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Scanner le QR Code", fontWeight = FontWeight.Bold, color = ElegantTextPrimary)
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Ouvrez WhatsApp sur votre smartphone > Appareils connectés > Connecter un appareil.",
                    style = MaterialTheme.typography.bodySmall,
                    color = ElegantTextSecondary
                )

                // Render aesthetic mock WhatsApp QR matrix
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .border(2.dp, ElegantPurpleAccent, RoundedCornerShape(16.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val cellSize = size.width / 16f
                        // Corner finder patterns
                        drawRect(Color.Black, Offset(0f, 0f), Size(cellSize * 5, cellSize * 5))
                        drawRect(Color.White, Offset(cellSize, cellSize), Size(cellSize * 3, cellSize * 3))
                        drawRect(Color.Black, Offset(cellSize * 1.5f, cellSize * 1.5f), Size(cellSize * 2, cellSize * 2))

                        drawRect(Color.Black, Offset(size.width - cellSize * 5, 0f), Size(cellSize * 5, cellSize * 5))
                        drawRect(Color.White, Offset(size.width - cellSize * 4, cellSize), Size(cellSize * 3, cellSize * 3))
                        drawRect(Color.Black, Offset(size.width - cellSize * 3.5f, cellSize * 1.5f), Size(cellSize * 2, cellSize * 2))

                        drawRect(Color.Black, Offset(0f, size.height - cellSize * 5), Size(cellSize * 5, cellSize * 5))
                        drawRect(Color.White, Offset(cellSize, size.height - cellSize * 4), Size(cellSize * 3, cellSize * 3))
                        drawRect(Color.Black, Offset(cellSize * 1.5f, size.height - cellSize * 3.5f), Size(cellSize * 2, cellSize * 2))

                        // Center dots
                        for (i in 4..11) {
                            for (j in 4..11) {
                                if ((i + j) % 2 == 0 || (i * j) % 3 == 0) {
                                    drawRect(Color.Black, Offset(i * cellSize, j * cellSize), Size(cellSize * 0.9f, cellSize * 0.9f))
                                }
                            }
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ElegantDarkBg,
                    border = BorderStroke(1.dp, ElegantDarkBorder)
                ) {
                    Text(
                        text = "Expire dans 45s • Instance: ${instance.name}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
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
                Text("Simuler Scan Réussi", fontWeight = FontWeight.Bold)
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
fun PairingCodeDialog(
    instance: WhatsAppInstanceEntity,
    onDismiss: () -> Unit,
    onConfirmConnected: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val code = instance.pairingCode.ifEmpty { "AEQ4-9872" }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = ElegantDarkSurface,
        title = {
            Text("Code d'appairage WhatsApp", fontWeight = FontWeight.Bold, color = ElegantTextPrimary)
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Sur votre smartphone, dans WhatsApp > Appareils connectés > Connecter avec le numéro de téléphone, saisissez ce code :",
                    style = MaterialTheme.typography.bodySmall,
                    color = ElegantTextSecondary
                )

                Surface(
                    color = ElegantDarkBg,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, ElegantDarkBorder),
                    modifier = Modifier.clickable {
                        clipboardManager.setText(AnnotatedString(code))
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = code,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            color = ElegantPurpleAccent
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copier",
                            tint = ElegantPurpleSecondary
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
                Text("Valider la Connexion", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer", color = ElegantTextSecondary)
            }
        }
    )
}
