package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.theme.ElegantDarkBg
import com.example.ui.theme.ElegantDarkBorder
import com.example.ui.theme.ElegantDarkSurface
import com.example.ui.theme.ElegantDarkSurfaceVariant
import com.example.ui.theme.ElegantPurpleAccent
import com.example.ui.theme.ElegantTextPrimary
import com.example.ui.theme.ElegantTextSecondary
import com.example.ui.theme.TelegramBlue
import com.example.ui.theme.WhatsAppGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.appSettings.collectAsState()
    val context = LocalContext.current

    // Local form states
    var currency by remember(settings.currency) { mutableStateOf(settings.currency) }
    var currencySymbol by remember(settings.currencySymbol) { mutableStateOf(settings.currencySymbol) }
    var defaultCountryCode by remember(settings.defaultCountryCode) { mutableStateOf(settings.defaultCountryCode) }
    var countryName by remember(settings.countryName) { mutableStateOf(settings.countryName) }
    var profitMargin by remember(settings.defaultProfitMarginPercent) { mutableStateOf(settings.defaultProfitMarginPercent.toString()) }
    var lowStockThreshold by remember(settings.lowStockThreshold) { mutableStateOf(settings.lowStockThreshold.toString()) }

    var geminiKey by remember(settings.geminiApiKey) { mutableStateOf(settings.geminiApiKey) }
    var showGeminiKey by remember { mutableStateOf(false) }
    var supabaseUrl by remember(settings.supabaseUrl) { mutableStateOf(settings.supabaseUrl) }
    var supabaseKey by remember(settings.supabaseAnonKey) { mutableStateOf(settings.supabaseAnonKey) }
    var showSupabaseKey by remember { mutableStateOf(false) }
    var telegramApiId by remember(settings.telegramApiId) { mutableStateOf(settings.telegramApiId) }
    var telegramApiHash by remember(settings.telegramApiHash) { mutableStateOf(settings.telegramApiHash) }

    var notifyOrders by remember(settings.notifyNewOrders) { mutableStateOf(settings.notifyNewOrders) }
    var notifyProducts by remember(settings.notifyTelegramProducts) { mutableStateOf(settings.notifyTelegramProducts) }
    var notifyStock by remember(settings.notifyLowStock) { mutableStateOf(settings.notifyLowStock) }

    var showExportDialog by remember { mutableStateOf(false) }
    var exportedJsonText by remember { mutableStateOf("") }
    var currencyMenuExpanded by remember { mutableStateOf(false) }
    var countryMenuExpanded by remember { mutableStateOf(false) }

    val currencyOptions = listOf(
        Triple("MAD", "DH", "Dirham marocain (Maroc)"),
        Triple("FCFA", "FCFA", "Franc CFA (Afrique de l'Ouest)"),
        Triple("EUR", "€", "Euro (Union Européenne)"),
        Triple("USD", "$", "Dollar américain (USA)")
    )

    val countryOptions = listOf(
        Pair("+212", "Maroc"),
        Pair("+221", "Sénégal"),
        Pair("+33", "France"),
        Pair("+225", "Côte d'Ivoire"),
        Pair("+213", "Algérie"),
        Pair("+216", "Tunisie"),
        Pair("+1", "États-Unis / Canada")
    )

    Box(modifier = modifier.fillMaxSize().background(ElegantDarkBg)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Paramètres & Configuration",
                            color = ElegantTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Devise, pays par défaut, clés d'API et règles de calcul",
                            color = ElegantTextSecondary,
                            fontSize = 13.sp
                        )
                    }

                    Button(
                        onClick = {
                            val marginParsed = profitMargin.toDoubleOrNull() ?: 40.0
                            val thresholdParsed = lowStockThreshold.toIntOrNull() ?: 5
                            val updated = settings.copy(
                                currency = currency,
                                currencySymbol = currencySymbol,
                                defaultCountryCode = defaultCountryCode,
                                countryName = countryName,
                                defaultProfitMarginPercent = marginParsed,
                                lowStockThreshold = thresholdParsed,
                                geminiApiKey = geminiKey,
                                supabaseUrl = supabaseUrl,
                                supabaseAnonKey = supabaseKey,
                                telegramApiId = telegramApiId,
                                telegramApiHash = telegramApiHash,
                                notifyNewOrders = notifyOrders,
                                notifyTelegramProducts = notifyProducts,
                                notifyLowStock = notifyStock,
                                updatedAt = System.currentTimeMillis()
                            )
                            viewModel.saveAppSettings(updated)
                            Toast.makeText(context, "Paramètres enregistrés avec succès !", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElegantPurpleAccent),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("save_settings_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Enregistrer les Paramètres", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            // --- SECTION 1 : DEVISE & LOCALISATION (B1 & B2) ---
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
                    border = BorderStroke(1.dp, ElegantDarkBorder),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("settings_currency_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = WhatsAppGreen.copy(alpha = 0.15f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = WhatsAppGreen, modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    "Devise & Localisation Régionale",
                                    color = ElegantTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    "Format monétaire centralisé et indicatif par défaut",
                                    color = ElegantTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // B1: Devise (Dropdown)
                        ExposedDropdownMenuBox(
                            expanded = currencyMenuExpanded,
                            onExpandedChange = { currencyMenuExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = "$currency ($currencySymbol)",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Devise de l'application (B1)") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyMenuExpanded) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ElegantPurpleAccent,
                                    unfocusedBorderColor = ElegantDarkBorder,
                                    focusedTextColor = ElegantTextPrimary,
                                    unfocusedTextColor = ElegantTextPrimary
                                ),
                                modifier = Modifier.fillMaxWidth().menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = currencyMenuExpanded,
                                onDismissRequest = { currencyMenuExpanded = false }
                            ) {
                                currencyOptions.forEach { opt ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("${opt.first} - ${opt.third}")
                                                if (currency == opt.first) {
                                                    Icon(Icons.Default.Check, contentDescription = null, tint = WhatsAppGreen, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        },
                                        onClick = {
                                            currency = opt.first
                                            currencySymbol = opt.second
                                            currencyMenuExpanded = false
                                            viewModel.updateCurrency(opt.first, opt.second)
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // B2: Pays / Indicatif Téléphonique par défaut (Dropdown)
                        ExposedDropdownMenuBox(
                            expanded = countryMenuExpanded,
                            onExpandedChange = { countryMenuExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = "$countryName ($defaultCountryCode)",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Pays & Indicatif par défaut (B2)") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = countryMenuExpanded) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ElegantPurpleAccent,
                                    unfocusedBorderColor = ElegantDarkBorder,
                                    focusedTextColor = ElegantTextPrimary,
                                    unfocusedTextColor = ElegantTextPrimary
                                ),
                                modifier = Modifier.fillMaxWidth().menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = countryMenuExpanded,
                                onDismissRequest = { countryMenuExpanded = false }
                            ) {
                                countryOptions.forEach { opt ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("${opt.second} (${opt.first})")
                                                if (defaultCountryCode == opt.first) {
                                                    Icon(Icons.Default.Check, contentDescription = null, tint = WhatsAppGreen, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        },
                                        onClick = {
                                            defaultCountryCode = opt.first
                                            countryName = opt.second
                                            countryMenuExpanded = false
                                            viewModel.updateDefaultCountryCode(opt.first, opt.second)
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "💡 Tous les nouveaux formulaires (Fournisseurs, Agences, Affiliés, Clients) seront automatiquement pré-remplis avec cet indicatif.",
                            color = ElegantTextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            // --- SECTION 2 : RÈGLES E-COMMERCE & IA ---
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
                    border = BorderStroke(1.dp, ElegantDarkBorder),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = ElegantPurpleAccent.copy(alpha = 0.15f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = ElegantPurpleAccent, modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    "Règles Commerciales & Marges",
                                    color = ElegantTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    "Marge automatique pour l'extraction IA et alertes d'inventaire",
                                    color = ElegantTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = profitMargin,
                                onValueChange = { profitMargin = it },
                                label = { Text("Marge IA (%)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ElegantPurpleAccent,
                                    unfocusedBorderColor = ElegantDarkBorder,
                                    focusedTextColor = ElegantTextPrimary,
                                    unfocusedTextColor = ElegantTextPrimary
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = lowStockThreshold,
                                onValueChange = { lowStockThreshold = it },
                                label = { Text("Seuil Stock Bas") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ElegantPurpleAccent,
                                    unfocusedBorderColor = ElegantDarkBorder,
                                    focusedTextColor = ElegantTextPrimary,
                                    unfocusedTextColor = ElegantTextPrimary
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // --- SECTION 3 : SÉCURITÉ & CLÉS D'API (B3) ---
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
                    border = BorderStroke(1.dp, ElegantDarkBorder),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFFA000).copy(alpha = 0.15f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFFFFA000), modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    "Sécurité & Fournisseurs Cloud / IA",
                                    color = ElegantTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    "Clés API sécurisées pour Gemini, Supabase et Telethon",
                                    color = ElegantTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Clé Gemini API
                        OutlinedTextField(
                            value = geminiKey,
                            onValueChange = { geminiKey = it },
                            label = { Text("Clé API Gemini (Google AI Studio)") },
                            visualTransformation = if (showGeminiKey) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showGeminiKey = !showGeminiKey }) {
                                    Icon(
                                        imageVector = if (showGeminiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Afficher/Masquer",
                                        tint = ElegantTextSecondary
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElegantPurpleAccent,
                                unfocusedBorderColor = ElegantDarkBorder,
                                focusedTextColor = ElegantTextPrimary,
                                unfocusedTextColor = ElegantTextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Supabase URL & Anon Key
                        OutlinedTextField(
                            value = supabaseUrl,
                            onValueChange = { supabaseUrl = it },
                            label = { Text("URL Projet Supabase (ex: https://xxx.supabase.co)") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElegantPurpleAccent,
                                unfocusedBorderColor = ElegantDarkBorder,
                                focusedTextColor = ElegantTextPrimary,
                                unfocusedTextColor = ElegantTextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = supabaseKey,
                            onValueChange = { supabaseKey = it },
                            label = { Text("Clé Anonyme Supabase (anon_key)") },
                            visualTransformation = if (showSupabaseKey) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showSupabaseKey = !showSupabaseKey }) {
                                    Icon(
                                        imageVector = if (showSupabaseKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Afficher/Masquer",
                                        tint = ElegantTextSecondary
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElegantPurpleAccent,
                                unfocusedBorderColor = ElegantDarkBorder,
                                focusedTextColor = ElegantTextPrimary,
                                unfocusedTextColor = ElegantTextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Telegram Telethon API
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = telegramApiId,
                                onValueChange = { telegramApiId = it },
                                label = { Text("Telegram API_ID") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = TelegramBlue,
                                    unfocusedBorderColor = ElegantDarkBorder,
                                    focusedTextColor = ElegantTextPrimary,
                                    unfocusedTextColor = ElegantTextPrimary
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = telegramApiHash,
                                onValueChange = { telegramApiHash = it },
                                label = { Text("Telegram API_HASH") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = TelegramBlue,
                                    unfocusedBorderColor = ElegantDarkBorder,
                                    focusedTextColor = ElegantTextPrimary,
                                    unfocusedTextColor = ElegantTextPrimary
                                ),
                                modifier = Modifier.weight(1.5f)
                            )
                        }
                    }
                }
            }

            // --- SECTION 4 : NOTIFICATIONS & PASSERELLES ---
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
                    border = BorderStroke(1.dp, ElegantDarkBorder),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = TelegramBlue.copy(alpha = 0.15f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Notifications, contentDescription = null, tint = TelegramBlue, modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    "Notifications & Alertes",
                                    color = ElegantTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    "Alertes instantanées WhatsApp et détection d'opportunités",
                                    color = ElegantTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Alerte nouvelle commande WhatsApp", color = ElegantTextPrimary, fontSize = 13.sp)
                            Switch(
                                checked = notifyOrders,
                                onCheckedChange = { notifyOrders = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = WhatsAppGreen, checkedTrackColor = WhatsAppGreen.copy(alpha = 0.4f))
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Alerte nouveaux catalogues Telegram", color = ElegantTextPrimary, fontSize = 13.sp)
                            Switch(
                                checked = notifyProducts,
                                onCheckedChange = { notifyProducts = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = TelegramBlue, checkedTrackColor = TelegramBlue.copy(alpha = 0.4f))
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Alerte rupture de stock", color = ElegantTextPrimary, fontSize = 13.sp)
                            Switch(
                                checked = notifyStock,
                                onCheckedChange = { notifyStock = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = ElegantPurpleAccent, checkedTrackColor = ElegantPurpleAccent.copy(alpha = 0.4f))
                            )
                        }
                    }
                }
            }

            // --- SECTION 5 : SAUVEGARDE & EXPORT DES DONNÉES (B3) ---
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
                    border = BorderStroke(1.dp, ElegantDarkBorder),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = ElegantDarkSurfaceVariant,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Download, contentDescription = null, tint = ElegantTextPrimary, modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    "Sauvegarde & Export Base Room",
                                    color = ElegantTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    "Exporter l'intégralité du catalogue, des commandes et contacts",
                                    color = ElegantTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedButton(
                            onClick = {
                                val json = viewModel.exportDatabaseToJson()
                                exportedJsonText = json
                                showExportDialog = true
                            },
                            border = BorderStroke(1.dp, ElegantDarkBorder),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("export_json_button")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = ElegantTextPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Générer l'Export JSON de la base", color = ElegantTextPrimary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }

    // Dialogue d'aperçu d'export JSON
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = {
                Text("Export JSON de la Base de Données", color = ElegantTextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        "Données exportées avec succès (produits, commandes, catégories, réglages) :",
                        color = ElegantTextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ElegantDarkBg,
                        border = BorderStroke(1.dp, ElegantDarkBorder),
                        modifier = Modifier.fillMaxWidth().height(200.dp)
                    ) {
                        Box(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = exportedJsonText,
                                color = WhatsAppGreen,
                                fontSize = 11.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Export DB JSON", exportedJsonText))
                        Toast.makeText(context, "JSON copié dans le presse-papier !", Toast.LENGTH_SHORT).show()
                        showExportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElegantPurpleAccent)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copier JSON", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Fermer", color = ElegantTextSecondary)
                }
            },
            containerColor = ElegantDarkSurface
        )
    }
}
