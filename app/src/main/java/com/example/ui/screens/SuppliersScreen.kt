package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.SupplierEntity
import com.example.ui.MainViewModel
import com.example.ui.theme.ElegantDarkBg
import com.example.ui.theme.ElegantDarkBorder
import com.example.ui.theme.ElegantDarkSurface
import com.example.ui.theme.ElegantPurpleAccent
import com.example.ui.theme.ElegantTextPrimary
import com.example.ui.theme.ElegantTextSecondary
import java.util.UUID

@Composable
fun SuppliersScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val suppliers by viewModel.commerceSuppliers.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var supplierToEdit by remember { mutableStateOf<SupplierEntity?>(null) }

    Box(modifier = modifier.fillMaxSize().background(ElegantDarkBg)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column {
                    Text(
                        "Fournisseurs & Canaux Sources",
                        color = ElegantTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        "Origines d'approvisionnement (Canaux Telegram, Grossistes Chine/Dubaï, Marché local)",
                        color = ElegantTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            if (suppliers.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
                        border = BorderStroke(1.dp, ElegantDarkBorder),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Store, contentDescription = null, tint = ElegantTextSecondary, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Aucun fournisseur référencé", color = ElegantTextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                "Enregistrez les fournisseurs avec leurs canaux Telegram ou contacts téléphoniques.",
                                color = ElegantTextSecondary,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            } else {
                items(suppliers, key = { it.id }) { supplier ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
                        border = BorderStroke(1.dp, ElegantDarkBorder),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("supplier_card_${supplier.id}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF2AABEE).copy(alpha = 0.15f),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Store, contentDescription = null, tint = Color(0xFF2AABEE), modifier = Modifier.size(20.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            supplier.name,
                                            color = ElegantTextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                "${supplier.reliabilityRating} / 5.0",
                                                color = Color(0xFFFFB300),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Row {
                                    IconButton(onClick = { supplierToEdit = supplier }, modifier = Modifier.size(28.dp)) {
                                        Icon(Icons.Default.Edit, contentDescription = "Modifier", tint = ElegantTextSecondary, modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(onClick = { viewModel.deleteSupplier(supplier) }, modifier = Modifier.size(28.dp)) {
                                        Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = Color(0xFFEF5350), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }

                            if (supplier.telegramUsername != null || supplier.phone.isNotBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    if (supplier.telegramUsername != null) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color(0xFF2AABEE), modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("@${supplier.telegramUsername}", color = Color(0xFF2AABEE), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                        }
                                    }
                                    if (supplier.phone.isNotBlank()) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Phone, contentDescription = null, tint = ElegantTextSecondary, modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(supplier.phone, color = ElegantTextSecondary, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }

                            if (supplier.address.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Place, contentDescription = null, tint = ElegantTextSecondary.copy(alpha = 0.7f), modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(supplier.address, color = ElegantTextSecondary.copy(alpha = 0.8f), fontSize = 11.sp)
                                }
                            }

                            if (supplier.notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    supplier.notes,
                                    color = ElegantTextSecondary,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = ElegantPurpleAccent,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("fab_add_supplier")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Nouveau Fournisseur")
        }
    }

    if (showAddDialog || supplierToEdit != null) {
        val initial = supplierToEdit
        var name by remember { mutableStateOf(initial?.name ?: "") }
        var tgUsername by remember { mutableStateOf(initial?.telegramUsername ?: "") }
        var phone by remember { mutableStateOf(initial?.phone ?: "") }
        var address by remember { mutableStateOf(initial?.address ?: "") }
        var notes by remember { mutableStateOf(initial?.notes ?: "") }

        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                supplierToEdit = null
            },
            containerColor = ElegantDarkSurface,
            title = {
                Text(
                    if (initial == null) "Nouveau Fournisseur" else "Modifier Fournisseur",
                    color = ElegantTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nom du fournisseur *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = tgUsername,
                        onValueChange = { tgUsername = it },
                        label = { Text("Nom d'utilisateur Telegram (sans @)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Téléphone WhatsApp") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Adresse / Localisation de l'entrepôt") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes (conditions, délais, etc.)") },
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            val sup = (initial ?: SupplierEntity(
                                id = "sup-${UUID.randomUUID().toString().take(8)}",
                                name = name
                            )).copy(
                                name = name,
                                telegramUsername = tgUsername.ifBlank { null },
                                phone = phone,
                                address = address,
                                notes = notes
                            )
                            viewModel.saveSupplier(sup)
                            showAddDialog = false
                            supplierToEdit = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElegantPurpleAccent)
                ) {
                    Text("Enregistrer", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddDialog = false
                    supplierToEdit = null
                }) {
                    Text("Annuler", color = ElegantTextSecondary)
                }
            }
        )
    }
}
