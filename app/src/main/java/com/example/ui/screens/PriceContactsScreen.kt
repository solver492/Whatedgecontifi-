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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.RequestQuote
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.PriceContactEntity
import com.example.ui.MainViewModel
import com.example.ui.theme.ElegantDarkBg
import com.example.ui.theme.ElegantDarkBorder
import com.example.ui.theme.ElegantDarkSurface
import com.example.ui.theme.ElegantDarkSurfaceVariant
import com.example.ui.theme.ElegantPurpleAccent
import com.example.ui.theme.ElegantTextPrimary
import com.example.ui.theme.ElegantTextSecondary
import com.example.ui.theme.WhatsAppGreen
import java.util.UUID

@Composable
fun PriceContactsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val contacts by viewModel.commercePriceContacts.collectAsState()
    val suppliers by viewModel.commerceSuppliers.collectAsState()
    val appSettings by viewModel.appSettings.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var contactToEdit by remember { mutableStateOf<PriceContactEntity?>(null) }

    Box(modifier = modifier.fillMaxSize().background(ElegantDarkBg)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column {
                    Text(
                        "Contacts & Grille de Tarifs",
                        color = ElegantTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        "Négociations fournisseurs, remises dégressives et conditions de paiement",
                        color = ElegantTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            if (contacts.isEmpty()) {
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
                            Icon(Icons.Default.RequestQuote, contentDescription = null, tint = ElegantTextSecondary, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Aucune grille tarifaire enregistrée", color = ElegantTextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                "Enregistrez les interlocuteurs privilégiés et remises négociées auprès de chaque fournisseur.",
                                color = ElegantTextSecondary,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            } else {
                items(contacts, key = { it.id }) { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
                        border = BorderStroke(1.dp, ElegantDarkBorder),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("price_contact_${item.id}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        item.supplierName,
                                        color = ElegantTextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Person, contentDescription = null, tint = ElegantTextSecondary, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(item.contactPerson, color = ElegantTextSecondary, fontSize = 12.sp)
                                    }
                                }

                                Row {
                                    IconButton(onClick = { contactToEdit = item }, modifier = Modifier.size(28.dp)) {
                                        Icon(Icons.Default.Edit, contentDescription = "Modifier", tint = ElegantTextSecondary, modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(onClick = { viewModel.deletePriceContact(item) }, modifier = Modifier.size(28.dp)) {
                                        Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = Color(0xFFEF5350), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = WhatsAppGreen.copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, WhatsAppGreen.copy(alpha = 0.3f)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text("Remise Négociée", color = WhatsAppGreen, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                                        Text("${item.negotiatedDiscountPercent}%", color = WhatsAppGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = ElegantDarkBg,
                                    border = BorderStroke(1.dp, ElegantDarkBorder),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text("Minimum Commande", color = ElegantTextSecondary, fontSize = 10.sp)
                                        Text("${item.minOrderQuantity} pièces", color = ElegantTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Phone, contentDescription = null, tint = ElegantTextSecondary, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(item.contactPhone, color = ElegantTextSecondary, fontSize = 11.sp)
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Payment, contentDescription = null, tint = ElegantTextSecondary, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(item.paymentTerms, color = ElegantTextSecondary, fontSize = 11.sp)
                            }

                            if (item.specialNotes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    item.specialNotes,
                                    color = ElegantTextSecondary.copy(alpha = 0.8f),
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
                .testTag("fab_add_price_contact")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Ajouter Contact / Tarif")
        }
    }

    if (showAddDialog || contactToEdit != null) {
        val initial = contactToEdit
        var supplierName by remember { mutableStateOf(initial?.supplierName ?: suppliers.firstOrNull()?.name ?: "") }
        var contactPerson by remember { mutableStateOf(initial?.contactPerson ?: "") }
        var contactPhone by remember { mutableStateOf(initial?.contactPhone ?: "${appSettings.defaultCountryCode} ") }
        var discountStr by remember { mutableStateOf(initial?.negotiatedDiscountPercent?.toString() ?: "10.0") }
        var minQtyStr by remember { mutableStateOf(initial?.minOrderQuantity?.toString() ?: "1") }
        var paymentTerms by remember { mutableStateOf(initial?.paymentTerms ?: "Comptant à la livraison") }
        var notes by remember { mutableStateOf(initial?.specialNotes ?: "") }

        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                contactToEdit = null
            },
            containerColor = ElegantDarkSurface,
            title = {
                Text(
                    if (initial == null) "Nouveau Contact & Tarif" else "Modifier Tarif Fournisseur",
                    color = ElegantTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = supplierName,
                        onValueChange = { supplierName = it },
                        label = { Text("Nom du Fournisseur *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = contactPerson,
                        onValueChange = { contactPerson = it },
                        label = { Text("Personne de Contact *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = contactPhone,
                        onValueChange = { contactPhone = it },
                        label = { Text("Téléphone Direct / WhatsApp") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = discountStr,
                            onValueChange = { discountStr = it },
                            label = { Text("Remise %") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = minQtyStr,
                            onValueChange = { minQtyStr = it },
                            label = { Text("Qté Min.") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = paymentTerms,
                        onValueChange = { paymentTerms = it },
                        label = { Text("Modalités de Paiement") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (supplierName.isNotBlank() && contactPerson.isNotBlank()) {
                            val item = (initial ?: PriceContactEntity(
                                id = "pct-${UUID.randomUUID().toString().take(8)}",
                                supplierId = "sup-custom",
                                supplierName = supplierName,
                                contactPerson = contactPerson,
                                contactPhone = contactPhone
                            )).copy(
                                supplierName = supplierName,
                                contactPerson = contactPerson,
                                contactPhone = contactPhone,
                                negotiatedDiscountPercent = discountStr.toDoubleOrNull() ?: 0.0,
                                minOrderQuantity = minQtyStr.toIntOrNull() ?: 1,
                                paymentTerms = paymentTerms,
                                specialNotes = notes
                            )
                            viewModel.savePriceContact(item)
                            showAddDialog = false
                            contactToEdit = null
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
                    contactToEdit = null
                }) {
                    Text("Annuler", color = ElegantTextSecondary)
                }
            }
        )
    }
}
