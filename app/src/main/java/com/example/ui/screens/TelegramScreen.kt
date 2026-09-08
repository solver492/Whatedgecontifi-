package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import java.io.File
import androidx.compose.ui.layout.ContentScale
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Link
import com.example.util.ProductMediaManager
import com.example.util.EditableMediaItem
import com.example.data.local.entity.ParsedMediaItem
import kotlinx.coroutines.launch
import java.util.UUID
import com.example.data.local.entity.AppSettingsEntity
import com.example.data.local.entity.CategoryEntity
import com.example.domain.intelligence.ProductIntelligenceEngine
import com.example.ui.components.MediaCarousel
import com.example.ui.components.ProductPromptButton
import com.example.ui.components.ProductPromptEnhancementDialog
import com.example.ui.components.PromptTargetMedia
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.TelegramAccountEntity
import com.example.data.local.entity.TelegramChannelEntity
import com.example.data.local.entity.TelegramLogEntity
import com.example.data.local.entity.TelegramMessageEntity
import com.example.domain.telegram.TelegramBridgeScript
import com.example.ui.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Design Palette
private val ElegantDarkBg = Color(0xFF0F0E17)
private val ElegantDarkSurface = Color(0xFF1B192E)
private val ElegantDarkCard = Color(0xFF22203A)
private val ElegantDarkBorder = Color(0xFF2D2A4A)
val TelegramBlue = Color(0xFF2AABEE)
val TelegramBlueLight = Color(0xFF229ED9)
private val ElegantGreenActive = Color(0xFF10B981)
private val ElegantTextPrimary = Color(0xFFF3F4F6)
private val ElegantTextSecondary = Color(0xFF9CA3AF)
private val ElegantOrangeNotice = Color(0xFFF59E0B)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TelegramScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val accounts by viewModel.telegramAccounts.collectAsState()
    val channels by viewModel.telegramChannels.collectAsState()
    val messages by viewModel.telegramMessages.collectAsState()
    val logs by viewModel.telegramLogs.collectAsState()
    val isBridgeOnline by viewModel.isTelegramBridgeOnline.collectAsState()
    val telegramStatus by viewModel.telegramStatus.collectAsState()
    val isLoading by viewModel.isTelegramLoading.collectAsState()
    val appSettings by viewModel.appSettings.collectAsState()
    val categories by viewModel.commerceCategories.collectAsState()
    val hiddenChannelIds by viewModel.hiddenChannelIds.collectAsState()

    var selectedSection by remember { mutableStateOf(0) }
    var showTermuxDialog by remember { mutableStateOf(false) }
    var showAddChannelDialog by remember { mutableStateOf(false) }
    var selectedMessageForProductCreation by remember { mutableStateOf<TelegramMessageEntity?>(null) }
    var toastMessage by remember { mutableStateOf<String?>(null) }

    // Channel exploration state
    var exploringChannel by remember { mutableStateOf<TelegramChannelEntity?>(null) }
    var exploringMessages by remember { mutableStateOf<List<TelegramMessageEntity>>(emptyList()) }
    var isExploringLoading by remember { mutableStateOf(false) }

    // Channel filter: 0 = Surveillés, 1 = Tous, 2 = Masqués
    var channelFilterIndex by remember { mutableStateOf(0) }

    // Form states (prefilled from saved settings if available)
    var apiIdInput by remember(appSettings) { mutableStateOf(appSettings.telegramApiId) }
    var apiHashInput by remember(appSettings) { mutableStateOf(appSettings.telegramApiHash) }
    var phoneInput by remember(appSettings) { mutableStateOf(appSettings.defaultCountryCode) }
    var codeInput by remember { mutableStateOf("") }
    var password2FAInput by remember { mutableStateOf("") }
    var step by remember { mutableStateOf(1) } // 1: Info/Phone, 2: Code verification
    var requires2FA by remember { mutableStateOf(false) }

    val activeAccount = accounts.firstOrNull { it.status == "CONNECTED" }

    LaunchedEffect(Unit) {
        viewModel.refreshTelegramStatus()
    }

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            toastMessage = null
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ElegantDarkBg)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. HEADER & BRIDGE STATUS ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("telegram_header_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
                border = BorderStroke(1.dp, TelegramBlue.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = TelegramBlue.copy(alpha = 0.15f),
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Send,
                                        contentDescription = "Telegram MTProto",
                                        tint = TelegramBlue,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Passerelle Telegram MTProto",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ElegantTextPrimary
                                )
                                Text(
                                    text = "Écoute Telethon • Canal Fournisseurs",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ElegantTextSecondary
                                )
                            }
                        }

                        // Refresh status button
                        IconButton(
                            onClick = { viewModel.refreshTelegramStatus() },
                            modifier = Modifier.testTag("refresh_telegram_status_button")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Rafraîchir", tint = ElegantTextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Status Pills
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Bridge state pill
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isBridgeOnline) ElegantGreenActive.copy(alpha = 0.12f) else ElegantDarkCard,
                            border = BorderStroke(1.dp, if (isBridgeOnline) ElegantGreenActive.copy(alpha = 0.5f) else ElegantDarkBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (isBridgeOnline) ElegantGreenActive else ElegantOrangeNotice)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isBridgeOnline) "● Bridge Python Actif (:8088)" else "○ Bridge Hors-Ligne (Port 8088)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isBridgeOnline) ElegantGreenActive else ElegantOrangeNotice
                                )
                            }
                        }

                        // Account state pill
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (activeAccount != null) TelegramBlue.copy(alpha = 0.12f) else ElegantDarkCard,
                            border = BorderStroke(1.dp, if (activeAccount != null) TelegramBlue.copy(alpha = 0.5f) else ElegantDarkBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (activeAccount != null) TelegramBlue else Color.Gray)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (activeAccount != null) "● CONNECTÉ" else "DÉCONNECTÉ",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (activeAccount != null) TelegramBlue else ElegantTextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action buttons: Launch Termux / Setup Guide
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val cleanCmd = TelegramBridgeScript.INSTALL_COMMAND.trim()
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Telegram Termux", cleanCmd))
                                android.util.Log.d("TelegramScreen", "Copied to clipboard: '$cleanCmd'")
                                toastMessage = "Commande d'installation copiée ! Ouverture de Termux..."
                                viewModel.openTermuxForTelegram(context)
                            },
                            modifier = Modifier.weight(1f).testTag("launch_termux_telegram_button"),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue, contentColor = Color.White)
                        ) {
                            Icon(Icons.Default.Terminal, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Lancer Termux", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                        }

                        OutlinedButton(
                            onClick = { showTermuxDialog = true },
                            modifier = Modifier.weight(1f).testTag("termux_guide_button"),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                            border = BorderStroke(1.dp, ElegantDarkBorder),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = ElegantDarkCard)
                        ) {
                            Icon(Icons.Default.HelpOutline, contentDescription = null, tint = ElegantTextSecondary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Guide Telethon", fontSize = 11.sp, color = ElegantTextPrimary, fontWeight = FontWeight.SemiBold, maxLines = 1, softWrap = false)
                        }
                    }
                }
            }
        }

        // --- 1.5 NAVIGATION TABS ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // 3-way Navigation TabRow
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ElegantDarkSurface,
                    border = BorderStroke(1.dp, ElegantDarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TabRow(
                        selectedTabIndex = selectedSection,
                        containerColor = Color.Transparent,
                        contentColor = ElegantTextPrimary,
                        indicator = { tabPositions ->
                            if (selectedSection < tabPositions.size) {
                                TabRowDefaults.SecondaryIndicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedSection]),
                                    color = TelegramBlue
                                )
                            }
                        }
                    ) {
                        Tab(
                            selected = selectedSection == 0,
                            onClick = { selectedSection = 0 },
                            text = { Text("📡 Canaux (${channels.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            modifier = Modifier.testTag("tab_channels")
                        )
                        Tab(
                            selected = selectedSection == 1,
                            onClick = { selectedSection = 1 },
                            text = { Text("💬 Messages (${messages.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            modifier = Modifier.testTag("tab_messages")
                        )
                        Tab(
                            selected = selectedSection == 2,
                            onClick = { selectedSection = 2 },
                            text = { Text("⚡ Logs (${logs.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            modifier = Modifier.testTag("tab_logs")
                        )
                    }
                }
            }
        }

        // =====================================================================
        // SECTION 0 : CANAUX & COMPTE TELEGRAM
        // =====================================================================
        if (selectedSection == 0) {
            // --- 2. AUTHENTICATION / CONNECTED ACCOUNT CARD ---
            item {
                if (activeAccount == null) {
                    // Connection Form
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("telegram_login_form_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
                        border = BorderStroke(1.dp, ElegantDarkBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = if (step == 1) "1. Connexion Compte Utilisateur" else "2. Validation du Code de Sécurité",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ElegantTextPrimary
                            )
                            Text(
                                text = if (step == 1)
                                    "Utilisez vos identifiants my.telegram.org et votre numéro pour connecter la session Telethon."
                                else
                                    "Saisissez le code à 5 chiffres reçu dans votre application Telegram officielle.",
                                style = MaterialTheme.typography.bodySmall,
                                color = ElegantTextSecondary,
                                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                            )

                            if (step == 1) {
                                OutlinedTextField(
                                    value = apiIdInput,
                                    onValueChange = { apiIdInput = it },
                                    label = { Text("App API ID") },
                                    placeholder = { Text("ex: 2040... (my.telegram.org)") },
                                    leadingIcon = { Icon(Icons.Default.VpnKey, contentDescription = null, tint = TelegramBlue) },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("telegram_api_id_input"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = TelegramBlue,
                                        unfocusedBorderColor = ElegantDarkBorder
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = apiHashInput,
                                    onValueChange = { apiHashInput = it },
                                    label = { Text("App API HASH") },
                                    placeholder = { Text("ex: b083b7c55c...") },
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TelegramBlue) },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("telegram_api_hash_input"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = TelegramBlue,
                                        unfocusedBorderColor = ElegantDarkBorder
                                    ),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = phoneInput,
                                    onValueChange = { phoneInput = it },
                                    label = { Text("Numéro Téléphone International") },
                                    placeholder = { Text("+33612345678 ou +221...") },
                                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = TelegramBlue) },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("telegram_phone_input"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = TelegramBlue,
                                        unfocusedBorderColor = ElegantDarkBorder
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = {
                                        viewModel.sendTelegramCode(apiIdInput, apiHashInput, phoneInput) { result ->
                                            toastMessage = result.message
                                            if (result.success) {
                                                step = 2
                                            }
                                        }
                                    },
                                    enabled = !isLoading && phoneInput.isNotBlank() && apiIdInput.isNotBlank(),
                                    modifier = Modifier.fillMaxWidth().testTag("telegram_send_code_button"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue)
                                ) {
                                    if (isLoading) {
                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                    Text("Envoyer le Code de Vérification", fontWeight = FontWeight.Bold)
                                }
                            } else {
                                // Step 2: Code verification
                                OutlinedTextField(
                                    value = codeInput,
                                    onValueChange = { codeInput = it },
                                    label = { Text("Code de Confirmation Telegram") },
                                    placeholder = { Text("12345") },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("telegram_code_input"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = TelegramBlue,
                                        unfocusedBorderColor = ElegantDarkBorder
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true
                                )

                                if (requires2FA) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    OutlinedTextField(
                                        value = password2FAInput,
                                        onValueChange = { password2FAInput = it },
                                        label = { Text("Mot de passe 2FA (Double Authentification)") },
                                        shape = RoundedCornerShape(12.dp),
                                        visualTransformation = PasswordVisualTransformation(),
                                        modifier = Modifier.fillMaxWidth().testTag("telegram_2fa_input"),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = TelegramBlue,
                                            unfocusedBorderColor = ElegantDarkBorder
                                        ),
                                        singleLine = true
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { step = 1 },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, ElegantDarkBorder)
                                    ) {
                                        Text("Retour", color = ElegantTextSecondary)
                                    }

                                    Button(
                                        onClick = {
                                            viewModel.verifyTelegramCode(phoneInput, codeInput, password2FAInput.takeIf { it.isNotBlank() }) { result ->
                                                toastMessage = result.message
                                                if (result.requiresPassword) {
                                                    requires2FA = true
                                                } else if (result.success) {
                                                    step = 1
                                                    codeInput = ""
                                                    password2FAInput = ""
                                                }
                                            }
                                        },
                                        enabled = !isLoading && codeInput.isNotBlank(),
                                        modifier = Modifier.weight(2f).testTag("telegram_verify_button"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue)
                                    ) {
                                        if (isLoading) {
                                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                            Spacer(modifier = Modifier.width(8.dp))
                                        }
                                        Text("Valider & Connecter", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Connected Account Card
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("telegram_connected_account_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
                        border = BorderStroke(1.dp, TelegramBlue.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = TelegramBlue,
                                    modifier = Modifier.size(52.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = (activeAccount.firstName.take(1) + activeAccount.lastName.take(1)).ifBlank { "TG" },
                                            color = Color.White,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 18.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${activeAccount.firstName} ${activeAccount.lastName}".trim().ifBlank { "Compte Telegram" },
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = ElegantTextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = if (activeAccount.username.isNotBlank()) "@${activeAccount.username}" else activeAccount.phoneNumber,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TelegramBlueLight
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(ElegantGreenActive))
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = "Session MTProto Active",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = ElegantGreenActive,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { viewModel.disconnectTelegramAccount(activeAccount.id) },
                                    modifier = Modifier.testTag("disconnect_telegram_button")
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Déconnecter", tint = Color(0xFFEF4444))
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.syncTelegramChannels(activeAccount.id)
                                        toastMessage = "Canaux actualisés depuis Telegram !"
                                    },
                                    modifier = Modifier.weight(1f).testTag("sync_channels_button"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue.copy(alpha = 0.2f), contentColor = TelegramBlue)
                                ) {
                                    Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Synchroniser Canaux", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // --- 3. CANAUX & GROUPES SURVEILLÉS ---
            item {
                val monitoredCount = channels.count { it.isMonitored && !hiddenChannelIds.contains(it.channelId) }
                val totalVisibleCount = channels.count { !hiddenChannelIds.contains(it.channelId) }
                val hiddenCount = channels.count { hiddenChannelIds.contains(it.channelId) }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Canaux Fournisseurs & Alertes",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ElegantTextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "$monitoredCount canal/groupes surveillés pour l'ingestion",
                                style = MaterialTheme.typography.bodySmall,
                                color = ElegantTextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        TextButton(
                            onClick = { showAddChannelDialog = true },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("add_channel_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = TelegramBlue, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ajouter", color = TelegramBlue, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                        }
                    }

                    // Filtres de visibilité des canaux
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "Surveillés ($monitoredCount)",
                            "Tous ($totalVisibleCount)",
                            "Masqués ($hiddenCount)"
                        ).forEachIndexed { idx, label ->
                            val isSelected = channelFilterIndex == idx
                            Button(
                                onClick = { channelFilterIndex = idx },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) TelegramBlue else ElegantDarkCard,
                                    contentColor = if (isSelected) Color.White else ElegantTextSecondary
                                ),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            }
                        }
                    }
                }
            }

            val displayedChannels = when (channelFilterIndex) {
                0 -> channels.filter { it.isMonitored && !hiddenChannelIds.contains(it.channelId) }
                1 -> channels.filter { !hiddenChannelIds.contains(it.channelId) }
                2 -> channels.filter { hiddenChannelIds.contains(it.channelId) }
                else -> channels
            }

            if (displayedChannels.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
                        border = BorderStroke(1.dp, ElegantDarkBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, tint = ElegantTextSecondary, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = when (channelFilterIndex) {
                                    2 -> "Aucun canal masqué"
                                    0 -> "Aucun canal surveillé"
                                    else -> "Aucun canal Telegram configuré"
                                },
                                color = ElegantTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Connectez votre compte ou synchronisez les canaux pour démarrer la surveillance des catalogues e-commerce.",
                                color = ElegantTextSecondary,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            } else {
                items(displayedChannels, key = { it.id }) { channel ->
                    val isHidden = hiddenChannelIds.contains(channel.channelId)
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("telegram_channel_item_${channel.id}"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
                        border = BorderStroke(1.dp, if (channel.isMonitored && !isHidden) TelegramBlue.copy(alpha = 0.35f) else ElegantDarkBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (channel.isMonitored) TelegramBlue.copy(alpha = 0.15f) else ElegantDarkCard,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = if (channel.isChannel) "📢" else "👥",
                                            fontSize = 18.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = channel.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = ElegantTextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = if (channel.username.isNotBlank()) "@${channel.username}" else "ID: ${channel.channelId}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ElegantTextSecondary
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.toggleChannelVisibility(channel.channelId) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (isHidden) "Afficher le canal" else "Masquer le canal",
                                        tint = if (isHidden) Color(0xFFEF4444) else ElegantTextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                Switch(
                                    checked = channel.isMonitored,
                                    onCheckedChange = { viewModel.toggleChannelMonitoring(channel.id, it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = TelegramBlue,
                                        uncheckedThumbColor = Color.LightGray,
                                        uncheckedTrackColor = ElegantDarkCard
                                    ),
                                    modifier = Modifier.testTag("switch_monitor_${channel.id}")
                                )
                            }

                            if (channel.lastMessageText.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = ElegantDarkCard,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Dernier message : ${channel.lastMessageText}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ElegantTextSecondary,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${channel.memberCount} abonnés",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ElegantTextSecondary
                                )

                                Button(
                                    onClick = {
                                        exploringChannel = channel
                                        isExploringLoading = true
                                        exploringMessages = emptyList()
                                        viewModel.fetchChannelRecentMessages(channel.channelId, channel.title) { msgs ->
                                            exploringMessages = msgs
                                            isExploringLoading = false
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = TelegramBlue.copy(alpha = 0.15f),
                                        contentColor = TelegramBlueLight
                                    ),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Explorer messages", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // =====================================================================
        // SECTION 1 : MESSAGES EN DIRECT (ÉCOUTEUR TELETHON / ROOM)
        // =====================================================================
        if (selectedSection == 1) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Flux des Messages Fournisseurs",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ElegantTextPrimary
                        )
                        Text(
                            text = "${messages.size} message(s) capturé(s) par Telethon",
                            style = MaterialTheme.typography.bodySmall,
                            color = ElegantTextSecondary
                        )
                    }

                    if (messages.isNotEmpty()) {
                        TextButton(
                            onClick = { viewModel.clearTelegramMessages() },
                            modifier = Modifier.testTag("clear_telegram_messages_button")
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Effacer", color = Color(0xFFEF4444), fontSize = 12.sp)
                        }
                    }
                }
            }

            if (messages.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
                        border = BorderStroke(1.dp, ElegantDarkBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = null, tint = TelegramBlueLight, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Aucun message capturé pour l'instant", color = ElegantTextPrimary, fontWeight = FontWeight.Bold)
                            Text(
                                "Le listener Telethon (NewMessage) transmet automatiquement chaque publication des canaux fournisseurs vers l'appli.",
                                color = ElegantTextSecondary,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { showTermuxDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue)
                            ) {
                                Icon(Icons.Default.Terminal, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Voir Guide Bridge Termux", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            } else {
                items(messages, key = { it.id }) { msg ->
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("telegram_message_item_${msg.id}"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
                        border = BorderStroke(1.dp, TelegramBlue.copy(alpha = 0.25f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Message header: Channel title & timestamp
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = TelegramBlue.copy(alpha = 0.2f),
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Send, contentDescription = null, tint = TelegramBlue, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = msg.channelTitle,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = ElegantTextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (msg.channelUsername.isNotBlank()) {
                                            Text(
                                                text = "@${msg.channelUsername}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TelegramBlueLight
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(msg.timestamp)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ElegantTextSecondary
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Carrousel Multimédia plein format (Section 1 : Photos & Vidéos avec swipe direct)
                            val mediaItems = remember(msg.id, msg.rawJson, msg.mediaUrl, msg.localMediaPath) {
                                msg.getMediaItems()
                            }

                            if (mediaItems.isNotEmpty()) {
                                MediaCarousel(
                                    mediaItems = mediaItems,
                                    height = 240.dp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 10.dp)
                                )
                            } else if (msg.mediaType != "none") {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = TelegramBlue.copy(alpha = 0.15f),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (msg.mediaType == "photo") Icons.Default.Image else Icons.Default.Chat,
                                            contentDescription = null,
                                            tint = TelegramBlueLight,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Média: ${msg.mediaType.uppercase()}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TelegramBlueLight
                                        )
                                    }
                                }
                            }

                            // Message text content
                            Text(
                                text = msg.text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = ElegantTextPrimary,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Actions row: Convert to Product & Delete
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = {
                                        selectedMessageForProductCreation = msg
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ElegantGreenActive.copy(alpha = 0.2f),
                                        contentColor = ElegantGreenActive
                                    ),
                                    border = BorderStroke(1.dp, ElegantGreenActive.copy(alpha = 0.4f)),
                                    modifier = Modifier.testTag("convert_msg_to_product_${msg.id}")
                                ) {
                                    Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Créer Fiche Produit", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                IconButton(
                                    onClick = { viewModel.deleteTelegramMessage(msg.id) },
                                    modifier = Modifier.size(32.dp).testTag("delete_telegram_msg_${msg.id}")
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = ElegantTextSecondary, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // =====================================================================
        // SECTION 2 : CONSOLE DE LOGS TELETHON EN TEMPS RÉEL
        // =====================================================================
        if (selectedSection == 2) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Console Telethon & Bridge",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ElegantTextPrimary
                        )
                        Text(
                            text = "Flux d'événements et requêtes en temps réel",
                            style = MaterialTheme.typography.bodySmall,
                            color = ElegantTextSecondary
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TextButton(
                            onClick = {
                                val allLogsText = logs.joinToString("\n") { "[${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(it.timestamp))}] [${it.level}] ${it.message}" }
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Telethon Logs", allLogsText))
                                toastMessage = "Logs copiés dans le presse-papier !"
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("copy_telegram_logs_button")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = TelegramBlue, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copier", color = TelegramBlue, fontSize = 11.sp)
                        }

                        TextButton(
                            onClick = { viewModel.clearTelegramLogs() },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("clear_telegram_logs_button")
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Effacer", color = Color(0xFFEF4444), fontSize = 11.sp)
                        }
                    }
                }
            }

            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF090812),
                    border = BorderStroke(1.dp, ElegantDarkBorder),
                    modifier = Modifier.fillMaxWidth().testTag("telethon_console_card")
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(if (isBridgeOnline) ElegantGreenActive else ElegantOrangeNotice))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isBridgeOnline) "Session Telethon active • En écoute sur le port 8088" else "En attente du démarrage du bridge Python Termux...",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = if (isBridgeOnline) ElegantGreenActive else ElegantOrangeNotice
                            )
                        }

                        if (logs.isEmpty()) {
                            Text(
                                text = "Aucun log reçu. Les logs d'écoute, requêtes et arrivages s'afficheront ici en direct.",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = ElegantTextSecondary,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        } else {
                            logs.forEach { logItem ->
                                val levelColor = when (logItem.level.uppercase()) {
                                    "INCOMING" -> ElegantGreenActive
                                    "ERROR" -> Color(0xFFEF4444)
                                    "WARN" -> ElegantOrangeNotice
                                    else -> TelegramBlueLight
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(logItem.timestamp)),
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = ElegantTextSecondary,
                                        modifier = Modifier.width(55.dp)
                                    )
                                    Text(
                                        text = "[${logItem.level}]",
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = levelColor,
                                        modifier = Modifier.width(72.dp)
                                    )
                                    Text(
                                        text = logItem.message,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = ElegantTextPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- DIALOG GUIDE TERMUX / TELETHON ---
    if (showTermuxDialog) {
        AlertDialog(
            onDismissRequest = { showTermuxDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Terminal, contentDescription = null, tint = TelegramBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Configuration Telethon Termux", fontWeight = FontWeight.Bold, color = ElegantTextPrimary)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Pour écouter les canaux en arrière-plan sans bloquer Android, exécutez le pont Python Telethon dans Termux :",
                        style = MaterialTheme.typography.bodySmall,
                        color = ElegantTextSecondary
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Installation + Lancement (1 clic)", color = TelegramBlueLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                IconButton(
                                    onClick = {
                                        val cleanCmd = TelegramBridgeScript.COMPLETE_TERMUX_COMMAND.trim()
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Telegram Full Install", cleanCmd))
                                        Toast.makeText(context, "Commande complète copiée !", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copier", tint = TelegramBlueLight, modifier = Modifier.size(16.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(TelegramBridgeScript.COMPLETE_TERMUX_COMMAND, color = Color.Green, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Démarrage Rapide (si déjà installé)", color = ElegantOrangeNotice, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                IconButton(
                                    onClick = {
                                        val cleanCmd = TelegramBridgeScript.FAST_START_COMMAND.trim()
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Telegram Fast Start", cleanCmd))
                                        Toast.makeText(context, "Commande rapide copiée !", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copier", tint = ElegantOrangeNotice, modifier = Modifier.size(16.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(TelegramBridgeScript.FAST_START_COMMAND, color = Color(0xFFFFB74D), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Port d'écoute : 8088 (Isolé de WhatsApp sur 8081)", color = TelegramBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("Le bridge Python tourne en local et transmet les catalogues au RAG sans aucun serveur externe.", color = Color.LightGray, fontSize = 10.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cleanCmd = TelegramBridgeScript.COMPLETE_TERMUX_COMMAND.trim()
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Telegram Install", cleanCmd))
                        Toast.makeText(context, "Commande complète copiée !", Toast.LENGTH_SHORT).show()
                        showTermuxDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copier Tout", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTermuxDialog = false }) {
                    Text("Fermer", color = ElegantTextSecondary)
                }
            },
            containerColor = ElegantDarkSurface
        )
    }

    // --- DIALOG EXPLORATION DU CANAL ---
    exploringChannel?.let { ch ->
        AlertDialog(
            onDismissRequest = { exploringChannel = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (ch.isChannel) "📢 " else "👥 ", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = ch.title,
                            fontWeight = FontWeight.Bold,
                            color = ElegantTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (ch.username.isNotBlank()) "@${ch.username}" else "ID: ${ch.channelId}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TelegramBlueLight
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().height(360.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Messages récents (Telethon)",
                            style = MaterialTheme.typography.labelMedium,
                            color = ElegantTextSecondary
                        )
                        IconButton(
                            onClick = {
                                isExploringLoading = true
                                viewModel.fetchChannelRecentMessages(ch.channelId, ch.title) { msgs ->
                                    exploringMessages = msgs
                                    isExploringLoading = false
                                }
                            }
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Actualiser", tint = TelegramBlue, modifier = Modifier.size(18.dp))
                        }
                    }

                    if (isExploringLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = TelegramBlue, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Récupération des messages Telethon...", fontSize = 12.sp, color = ElegantTextSecondary)
                            }
                        }
                    } else if (exploringMessages.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                                Icon(Icons.Default.Chat, contentDescription = null, tint = ElegantTextSecondary, modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Aucun message récupéré.", color = ElegantTextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(
                                    "Vérifiez que le bridge Termux Telethon est démarré et que le compte est connecté.",
                                    color = ElegantTextSecondary,
                                    fontSize = 11.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(exploringMessages, key = { it.id }) { msg ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = ElegantDarkCard,
                                    border = BorderStroke(1.dp, ElegantDarkBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = msg.senderName.ifBlank { "Auteur" },
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = TelegramBlueLight
                                            )
                                            Text(
                                                text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(msg.timestamp)),
                                                fontSize = 10.sp,
                                                color = ElegantTextSecondary
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = msg.text,
                                            fontSize = 12.sp,
                                            color = ElegantTextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { exploringChannel = null },
                    colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue)
                ) {
                    Text("Fermer")
                }
            },
            containerColor = ElegantDarkSurface
        )
    }

    // --- DIALOG AJOUT CANAL MANUEL ---
    if (showAddChannelDialog) {
        var newChannelTitle by remember { mutableStateOf("") }
        var newChannelUsername by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddChannelDialog = false },
            title = {
                Text("Ajouter un Canal Fournisseur", fontWeight = FontWeight.Bold, color = ElegantTextPrimary)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newChannelTitle,
                        onValueChange = { newChannelTitle = it },
                        label = { Text("Titre ou description du canal") },
                        placeholder = { Text("ex: Grossiste Chaussures VIP") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newChannelUsername,
                        onValueChange = { newChannelUsername = it },
                        label = { Text("Nom d'utilisateur public Telegram") },
                        placeholder = { Text("ex: grossiste_france_direct") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            if (newChannelTitle.isNotBlank()) {
                                val accountId = activeAccount?.id ?: "default_account"
                                val cleanUser = newChannelUsername.removePrefix("@").trim()
                                viewModel.addManualTelegramChannel(
                                    title = newChannelTitle,
                                    username = cleanUser,
                                    accountId = accountId
                                )
                                showAddChannelDialog = false
                                toastMessage = "Canal '$newChannelTitle' ajouté à la surveillance !"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(46.dp)
                    ) {
                        Text("Enregistrer le Canal", fontWeight = FontWeight.Bold)
                    }

                    TextButton(
                        onClick = { showAddChannelDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Annuler", color = ElegantTextSecondary)
                    }
                }
            },
            confirmButton = {},
            containerColor = ElegantDarkSurface
        )
    }

    // Modal de création de produit avec choix explicite de catégorie et gestion complète des médias
    selectedMessageForProductCreation?.let { msgToConvert ->
        CreateProductFromTelegramDialog(
            message = msgToConvert,
            categories = categories,
            appSettings = appSettings,
            onDismiss = { selectedMessageForProductCreation = null },
            onConfirm = { catId, customTitle, pPrice, sPrice, customMedia, primaryImg ->
                viewModel.createProductFromTelegram(
                    message = msgToConvert,
                    categoryId = catId,
                    customTitle = customTitle,
                    purchasePrice = pPrice,
                    sellingPrice = sPrice,
                    currency = appSettings.currency.ifBlank { "MAD" },
                    customMediaItems = customMedia,
                    customPrimaryImageUrl = primaryImg
                ) { createdProd ->
                    toastMessage = "Produit '${createdProd.title}' créé avec succès !"
                }
                selectedMessageForProductCreation = null
            }
        )
    }
}

/**
 * Dialog de création de produit à partir d'un message Telegram capturé.
 * Permet d'assigner une catégorie réelle ou de laisser explicite "Non catégorisé",
 * et applique la devise configurée dans Paramètres.
 * Permet de modifier, télécharger, supprimer et uploader des images et vidéos.
 */
@Composable
fun CreateProductFromTelegramDialog(
    message: TelegramMessageEntity,
    categories: List<CategoryEntity>,
    appSettings: AppSettingsEntity,
    onDismiss: () -> Unit,
    onConfirm: (
        categoryId: String?,
        title: String,
        purchasePrice: Double?,
        sellingPrice: Double?,
        mediaItems: List<ParsedMediaItem>,
        primaryImageUrl: String?
    ) -> Unit
) {
    val currency = appSettings.currency.ifBlank { "MAD" }
    val extracted = remember(message.id) {
        ProductIntelligenceEngine.extractFromTelegramMessage(message, currency)
    }

    val rawExtracted = remember(message.id) {
        ProductIntelligenceEngine.parseProductText(message.text, message.channelTitle, currency)
    }

    var title by remember { mutableStateOf(extracted.first.title) }
    var purchasePriceInput by remember {
        mutableStateOf(extracted.first.purchasePrice?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "")
    }
    var sellingPriceInput by remember {
        mutableStateOf(extracted.first.sellingPrice?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "")
    }
    var selectedCategoryId by remember { mutableStateOf<String?>(null) } // "Non catégorisé" par défaut

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Liste éditable des médias (images & vidéos)
    val mediaList = remember {
        mutableStateListOf<EditableMediaItem>().apply {
            message.getMediaItems().forEach { item ->
                val usableLocal = item.localPath?.takeIf { File(it).canRead() && File(it).length() > 0 }
                val targetPath = usableLocal ?: item.url ?: run {
                    val p = item.localPath ?: ""
                    if (p.contains("telegram_media/")) {
                        val sub = p.substringAfter("telegram_media/").trimStart('/')
                        val parts = sub.split("/")
                        if (parts.size >= 3) {
                            val ch = parts[0]
                            val mid = parts[1]
                            val fn = parts.drop(2).joinToString("/")
                            "http://127.0.0.1:8088/media/$ch/$mid/$fn"
                        } else p
                    } else p
                }
                if (targetPath.isNotBlank()) {
                    add(EditableMediaItem(urlOrPath = targetPath, isVideo = item.isVideo))
                }
            }
        }
    }
    var primaryMediaId by remember {
        mutableStateOf(mediaList.firstOrNull { !it.isVideo }?.id ?: mediaList.firstOrNull()?.id)
    }

    // Pré-téléchargement et mise en cache locale transparente
    LaunchedEffect(Unit) {
        mediaList.forEachIndexed { index, item ->
            if (item.urlOrPath.startsWith("http://") || item.urlOrPath.contains("telegram_media/")) {
                val cached = ProductMediaManager.cacheMediaLocally(context, item.urlOrPath, "tg_draft_")
                if (cached != null && File(cached).exists() && File(cached).length() > 0) {
                    item.urlOrPath = cached
                }
            }
        }
    }

    var isProcessingMedia by remember { mutableStateOf(false) }
    var showAddUrlInput by remember { mutableStateOf(false) }
    var manualUrlText by remember { mutableStateOf("") }
    var showPromptDialog by remember { mutableStateOf(false) }

    // Sélecteur Android officiel : sélectionne photos et vidéos de la galerie
    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris ->
        if (uris.isNotEmpty()) {
            coroutineScope.launch {
                isProcessingMedia = true
                var addedCount = 0
                uris.forEach { uri ->
                    val mime = try { context.contentResolver.getType(uri) ?: "" } catch (e: Exception) { "" }
                    val isVid = mime.startsWith("video") || ProductMediaManager.isVideoUrlOrPath(uri.toString())
                    val savedPath = ProductMediaManager.saveUriToInternalStorage(context, uri, isVid)
                    if (savedPath != null) {
                        val newItem = EditableMediaItem(
                            id = UUID.randomUUID().toString(),
                            urlOrPath = savedPath,
                            isVideo = isVid
                        )
                        mediaList.add(newItem)
                        if (primaryMediaId == null) {
                            primaryMediaId = newItem.id
                        }
                        addedCount++
                    }
                }
                isProcessingMedia = false
                if (addedCount > 0) {
                    Toast.makeText(context, "$addedCount média(s) ajouté(s) avec succès", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Erreur lors de l'enregistrement des médias", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = ElegantGreenActive, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Créer Fiche Produit E-commerce", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = ElegantTextPrimary)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Badge si prix au lot / colis détecté
                if (rawExtracted.isLotOrPackPrice) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF59E0B).copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    rawExtracted.lotLabel ?: "Prix au colis / lot détecté",
                                    color = Color(0xFFF59E0B),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Prix unitaire estimé : ~${extracted.first.purchasePrice ?: "?"} $currency (Total lot : ${rawExtracted.lotTotalPrice ?: "?"} $currency pour ${rawExtracted.lotQuantity ?: "?"} pièces)",
                                color = ElegantTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                } else if (rawExtracted.needsPriceReview || extracted.first.purchasePrice == null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFEF5350).copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Color(0xFFEF5350).copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF5350), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Prix à vérifier : Veuillez renseigner le prix d'achat manuellement.",
                                color = Color(0xFFEF5350),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Section Médias : Modification, Téléchargement, Suppression, Ajout (Photos & Vidéos)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = ElegantDarkCard,
                    border = BorderStroke(1.dp, ElegantDarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Photos & Vidéos du produit (${mediaList.size})",
                                    color = ElegantTextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Touchez pour définir la miniature principale ★",
                                    color = ElegantTextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                            if (isProcessingMedia) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = ElegantGreenActive)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (mediaList.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                mediaList.forEach { item ->
                                    val isPrimary = (primaryMediaId == item.id) || (primaryMediaId == null && mediaList.firstOrNull()?.id == item.id)
                                    val displayModel = remember(item.urlOrPath) {
                                        ProductMediaManager.resolveMediaDisplayModel(context, item.urlOrPath)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(86.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF1E293B))
                                            .border(
                                                width = if (isPrimary) 2.dp else 1.dp,
                                                color = if (isPrimary) Color(0xFFF59E0B) else ElegantDarkBorder,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable { primaryMediaId = item.id }
                                    ) {
                                        SubcomposeAsyncImage(
                                            model = ImageRequest.Builder(context)
                                                .data(displayModel)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = "Média produit",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize(),
                                            loading = {
                                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(20.dp),
                                                        strokeWidth = 2.dp,
                                                        color = ElegantGreenActive
                                                    )
                                                }
                                            },
                                            error = {
                                                Box(
                                                    modifier = Modifier.fillMaxSize().background(Color(0xFF1E293B)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Icon(
                                                            imageVector = if (item.isVideo) Icons.Default.Videocam else Icons.Default.PhotoLibrary,
                                                            contentDescription = null,
                                                            tint = ElegantTextSecondary.copy(alpha = 0.6f),
                                                            modifier = Modifier.size(22.dp)
                                                        )
                                                        Text(
                                                            if (item.isVideo) "Vidéo" else "Image",
                                                            fontSize = 9.sp,
                                                            color = ElegantTextSecondary.copy(alpha = 0.6f)
                                                        )
                                                    }
                                                }
                                            }
                                        )

                                        // Badge Miniature principale (Étoile)
                                        if (isPrimary) {
                                            Surface(
                                                shape = RoundedCornerShape(bottomEnd = 6.dp),
                                                color = Color(0xFFF59E0B),
                                                modifier = Modifier.align(Alignment.TopStart)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color.Black, modifier = Modifier.size(10.dp))
                                                    Spacer(modifier = Modifier.width(2.dp))
                                                    Text("Miniature", color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }

                                        // Badge Vidéo
                                        if (item.isVideo) {
                                            Surface(
                                                shape = RoundedCornerShape(topStart = 6.dp),
                                                color = Color(0xFFE53935).copy(alpha = 0.9f),
                                                modifier = Modifier.align(Alignment.BottomStart)
                                            ) {
                                                Icon(Icons.Default.Videocam, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp).padding(2.dp))
                                            }
                                        }

                                        // Actions flottantes sur le média : Télécharger et Supprimer
                                        Row(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(2.dp),
                                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            // Télécharger vers galerie / vidéos
                                            Box(
                                                modifier = Modifier
                                                    .size(22.dp)
                                                    .background(Color.Black.copy(alpha = 0.75f), CircleShape)
                                                    .clickable {
                                                        coroutineScope.launch {
                                                            ProductMediaManager.downloadMediaToDevice(context, item.urlOrPath, title.ifBlank { "produit" })
                                                        }
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.Download, contentDescription = "Télécharger", tint = Color.White, modifier = Modifier.size(12.dp))
                                            }

                                            // Supprimer média
                                            Box(
                                                modifier = Modifier
                                                    .size(22.dp)
                                                    .background(Color(0xFFEF4444).copy(alpha = 0.85f), CircleShape)
                                                    .clickable {
                                                        mediaList.remove(item)
                                                        if (primaryMediaId == item.id) {
                                                            primaryMediaId = mediaList.firstOrNull()?.id
                                                        }
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.Close, contentDescription = "Supprimer", tint = Color.White, modifier = Modifier.size(12.dp))
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // Boutons d'action pour les médias (Galerie, Nouveau Bouton "P" Studio WhatsApp, et URL)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    mediaPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ElegantGreenActive.copy(alpha = 0.25f)),
                                border = BorderStroke(1.dp, ElegantGreenActive),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = ElegantGreenActive, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Ajouter Photos/Vidéos", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }

                            // Bouton "P" personnalisé
                            ProductPromptButton(
                                onClick = {
                                    if (mediaList.isEmpty()) {
                                        Toast.makeText(context, "Ajoutez d'abord une photo pour appliquer les commandes de retouche IA", Toast.LENGTH_SHORT).show()
                                    } else {
                                        showPromptDialog = true
                                    }
                                }
                            )

                            OutlinedButton(
                                onClick = { showAddUrlInput = !showAddUrlInput },
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, ElegantDarkBorder),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ElegantTextSecondary)
                            ) {
                                Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("URL", fontSize = 11.sp)
                            }
                        }

                        if (showAddUrlInput) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                OutlinedTextField(
                                    value = manualUrlText,
                                    onValueChange = { manualUrlText = it },
                                    label = { Text("https://...") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                Button(
                                    onClick = {
                                        if (manualUrlText.isNotBlank()) {
                                            val newItem = EditableMediaItem(
                                                urlOrPath = manualUrlText.trim(),
                                                isVideo = ProductMediaManager.isVideoUrlOrPath(manualUrlText)
                                            )
                                            mediaList.add(newItem)
                                            if (primaryMediaId == null) primaryMediaId = newItem.id
                                            manualUrlText = ""
                                            showAddUrlInput = false
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ElegantGreenActive)
                                ) {
                                    Text("OK", fontSize = 11.sp)
                                }
                            }
                        }

                        // Option de tout télécharger d'un coup
                        if (mediaList.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedButton(
                                onClick = {
                                    coroutineScope.launch {
                                        mediaList.forEach { m ->
                                            ProductMediaManager.downloadMediaToDevice(context, m.urlOrPath, title.ifBlank { "produit" })
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, ElegantDarkBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp), tint = ElegantTextSecondary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Télécharger tous les médias (${mediaList.size}) dans la galerie", fontSize = 11.sp, color = ElegantTextSecondary)
                            }
                        }
                    }
                }

                // Titre
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titre de la fiche *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Prix en devise configurée (MAD)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = purchasePriceInput,
                        onValueChange = { purchasePriceInput = it },
                        label = { Text("Prix Achat ($currency)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = sellingPriceInput,
                        onValueChange = { sellingPriceInput = it },
                        label = { Text("Prix Vente ($currency)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Sélection de Catégorie explicite (Section 5 : Jamais de faux "Général")
                Text(
                    "Catégorie assignée (Routage IA) :",
                    color = ElegantTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                // Option "Non catégorisé" + catégories existantes
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val isNoneSelected = selectedCategoryId == null
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isNoneSelected) ElegantOrangeNotice.copy(alpha = 0.2f) else ElegantDarkCard,
                        border = BorderStroke(1.dp, if (isNoneSelected) ElegantOrangeNotice else ElegantDarkBorder),
                        modifier = Modifier.clickable { selectedCategoryId = null }
                    ) {
                        Text(
                            "Non catégorisé",
                            color = if (isNoneSelected) ElegantOrangeNotice else ElegantTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isNoneSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }

                    categories.forEach { cat ->
                        val isSelected = selectedCategoryId == cat.id
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) TelegramBlue.copy(alpha = 0.2f) else ElegantDarkCard,
                            border = BorderStroke(1.dp, if (isSelected) TelegramBlue else ElegantDarkBorder),
                            modifier = Modifier.clickable { selectedCategoryId = cat.id }
                        ) {
                            Text(
                                cat.name,
                                color = if (isSelected) TelegramBlue else ElegantTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            val pPrice = purchasePriceInput.toDoubleOrNull()
                            val sPrice = sellingPriceInput.toDoubleOrNull()
                            val finalParsedMedia = mediaList.map { item ->
                                val cleanPath = if (item.urlOrPath.startsWith("file://")) item.urlOrPath.removePrefix("file://") else item.urlOrPath
                                val isLocal = cleanPath.startsWith("/")
                                val isRealReadableFile = isLocal && File(cleanPath).let { it.canRead() && it.length() > 0 }
                                val usableUrl = if (item.urlOrPath.startsWith("http://") || item.urlOrPath.startsWith("https://")) {
                                    item.urlOrPath
                                } else if (!isRealReadableFile && item.urlOrPath.contains("telegram_media/")) {
                                    val sub = item.urlOrPath.substringAfter("telegram_media/").trimStart('/')
                                    val parts = sub.split("/")
                                    if (parts.size >= 3) {
                                        val ch = parts[0]
                                        val mid = parts[1]
                                        val fn = parts.drop(2).joinToString("/")
                                        "http://127.0.0.1:8088/media/$ch/$mid/$fn"
                                    } else item.urlOrPath
                                } else if (!isRealReadableFile && !isLocal) item.urlOrPath else null

                                ParsedMediaItem(
                                    url = usableUrl,
                                    localPath = if (isRealReadableFile) cleanPath else if (usableUrl == null) cleanPath else null,
                                    isVideo = item.isVideo
                                )
                            }
                            val selectedPrimary = mediaList.find { it.id == primaryMediaId } ?: mediaList.firstOrNull()
                            val primaryUrl = selectedPrimary?.let { sel ->
                                if (sel.urlOrPath.startsWith("file://")) sel.urlOrPath.removePrefix("file://") else sel.urlOrPath
                            }
                            onConfirm(selectedCategoryId, title, pPrice, sPrice, finalParsedMedia, primaryUrl)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElegantGreenActive),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Créer la Fiche Produit", color = Color.White, fontWeight = FontWeight.Bold)
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Annuler", color = ElegantTextSecondary)
                }
            }
        },
        confirmButton = {},
        containerColor = ElegantDarkSurface
    )

    if (showPromptDialog) {
        ProductPromptEnhancementDialog(
            mediaList = mediaList.map { PromptTargetMedia(id = it.id, urlOrPath = it.urlOrPath, isVideo = it.isVideo) },
            primaryMediaId = mediaList.firstOrNull()?.id,
            productTitle = title,
            initialProductDescription = message.text,
            onDismiss = { showPromptDialog = false },
            onMediaReplaced = { targetId, newPath ->
                val index = mediaList.indexOfFirst { it.id == targetId }
                if (index != -1) {
                    val oldItem = mediaList[index]
                    mediaList[index] = oldItem.copy(urlOrPath = newPath)
                }
            }
        )
    }
}
