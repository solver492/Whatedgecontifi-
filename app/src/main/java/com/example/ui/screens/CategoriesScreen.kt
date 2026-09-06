package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import com.example.data.local.entity.CategoryEntity
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val categories by viewModel.commerceCategories.collectAsState()
    val agents by viewModel.agents.collectAsState()
    val products by viewModel.commerceProducts.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var categoryToEdit by remember { mutableStateOf<CategoryEntity?>(null) }
    var expandedAgentMenuCatId by remember { mutableStateOf<String?>(null) }

    Box(modifier = modifier.fillMaxSize().background(ElegantDarkBg)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Catégories & Routage WhatsApp",
                            color = ElegantTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            "Chaque catégorie redirige les demandes clients vers son Agent IA dédié",
                            color = ElegantTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            if (categories.isEmpty()) {
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
                            Icon(Icons.Default.Category, contentDescription = null, tint = ElegantTextSecondary, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Aucune catégorie enregistrée", color = ElegantTextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                "Créez votre première catégorie pour classifier vos articles et assigner un agent commercial.",
                                color = ElegantTextSecondary,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            } else {
                items(categories, key = { it.id }) { cat ->
                    val assignedAgent = agents.find { it.id == cat.assignedAgentId }
                    val currentCount = products.count { it.categoryId == cat.id }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
                        border = BorderStroke(1.dp, ElegantDarkBorder),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("category_card_${cat.id}")
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
                                        color = ElegantPurpleAccent.copy(alpha = 0.15f),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            val icon = when (cat.iconName) {
                                                "Devices" -> Icons.Default.Devices
                                                "Checkroom" -> Icons.Default.Checkroom
                                                "Spa" -> Icons.Default.Spa
                                                else -> Icons.Default.Category
                                            }
                                            Icon(icon, contentDescription = null, tint = ElegantPurpleAccent, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            cat.name,
                                            color = ElegantTextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            "${currentCount.coerceAtLeast(cat.productCount)} articles",
                                            color = ElegantTextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Row {
                                    IconButton(onClick = { categoryToEdit = cat }, modifier = Modifier.size(28.dp)) {
                                        Icon(Icons.Default.Edit, contentDescription = "Modifier", tint = ElegantTextSecondary, modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(onClick = { viewModel.deleteCategory(cat) }, modifier = Modifier.size(28.dp)) {
                                        Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = Color(0xFFEF5350), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }

                            if (cat.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    cat.description,
                                    color = ElegantTextSecondary,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }

                            // Aperçu des produits associés s'ils existent (chips compacts)
                            val catProducts = products.filter { it.categoryId == cat.id }
                            if (catProducts.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    catProducts.take(2).forEach { prod ->
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = ElegantDarkSurfaceVariant.copy(alpha = 0.5f),
                                            border = BorderStroke(1.dp, ElegantDarkBorder)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = prod.title.take(18) + if (prod.title.length > 18) "…" else "",
                                                    fontSize = 10.sp,
                                                    color = ElegantTextPrimary,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                    if (catProducts.size > 2) {
                                        Text(
                                            text = "+${catProducts.size - 2} autres",
                                            fontSize = 10.sp,
                                            color = ElegantPurpleAccent,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Badge Agent Routage WhatsApp (Cliquable avec DropdownMenu)
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = ElegantDarkBg,
                                    border = BorderStroke(1.dp, if (assignedAgent != null) WhatsAppGreen.copy(alpha = 0.5f) else ElegantDarkBorder),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { expandedAgentMenuCatId = cat.id }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f, fill = false)
                                        ) {
                                            Icon(Icons.Default.SmartToy, contentDescription = null, tint = WhatsAppGreen, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                "Agent : ",
                                                color = ElegantTextSecondary,
                                                fontSize = 11.sp
                                            )
                                            Text(
                                                assignedAgent?.name ?: "Routage Automatique",
                                                color = if (assignedAgent != null) ElegantTextPrimary else ElegantTextSecondary.copy(alpha = 0.7f),
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 11.sp,
                                                maxLines = 1
                                            )
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = WhatsAppGreen.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                "Modifier ▾",
                                                color = WhatsAppGreen,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                DropdownMenu(
                                    expanded = expandedAgentMenuCatId == cat.id,
                                    onDismissRequest = { expandedAgentMenuCatId = null },
                                    modifier = Modifier.background(ElegantDarkSurface)
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Routage Automatique (Par défaut)", color = ElegantTextSecondary, fontSize = 12.sp) },
                                        leadingIcon = { Icon(Icons.Default.Autorenew, contentDescription = null, tint = ElegantTextSecondary, modifier = Modifier.size(16.dp)) },
                                        onClick = {
                                            viewModel.updateCategoryAgent(cat.id, null)
                                            expandedAgentMenuCatId = null
                                        }
                                    )
                                    agents.forEach { ag ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(ag.name, color = ElegantTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                                    Text(ag.role, color = ElegantTextSecondary, fontSize = 10.sp)
                                                }
                                            },
                                            leadingIcon = { Icon(Icons.Default.SmartToy, contentDescription = null, tint = WhatsAppGreen, modifier = Modifier.size(16.dp)) },
                                            onClick = {
                                                viewModel.updateCategoryAgent(cat.id, ag.id)
                                                expandedAgentMenuCatId = null
                                            }
                                        )
                                    }
                                }
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
                .testTag("fab_add_category")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Nouvelle Catégorie")
        }
    }

    if (showAddDialog || categoryToEdit != null) {
        val initialCat = categoryToEdit
        var name by remember { mutableStateOf(initialCat?.name ?: "") }
        var description by remember { mutableStateOf(initialCat?.description ?: "") }
        var selectedAgentId by remember { mutableStateOf(initialCat?.assignedAgentId ?: agents.firstOrNull()?.id) }

        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                categoryToEdit = null
            },
            containerColor = ElegantDarkSurface,
            title = {
                Text(
                    if (initialCat == null) "Nouvelle Catégorie" else "Modifier la Catégorie",
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
                        label = { Text("Nom de la catégorie *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description & mots-clés") },
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Agent IA WhatsApp pour cette catégorie :", color = ElegantTextSecondary, fontSize = 11.sp)

                    // Option automatique
                    val isAuto = selectedAgentId == null
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isAuto) ElegantDarkSurfaceVariant else ElegantDarkBg,
                        border = BorderStroke(1.dp, if (isAuto) ElegantPurpleAccent else ElegantDarkBorder),
                        modifier = Modifier.fillMaxWidth().clickable { selectedAgentId = null }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Autorenew, contentDescription = null, tint = if (isAuto) ElegantPurpleAccent else ElegantTextSecondary, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Routage Automatique (Par défaut)", color = if (isAuto) ElegantTextPrimary else ElegantTextSecondary, fontSize = 12.sp)
                        }
                    }

                    // Choix Agent
                    agents.forEach { agent ->
                        val isChosen = selectedAgentId == agent.id
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isChosen) WhatsAppGreen.copy(alpha = 0.15f) else ElegantDarkBg,
                            border = BorderStroke(1.dp, if (isChosen) WhatsAppGreen else ElegantDarkBorder),
                            modifier = Modifier.fillMaxWidth().clickable { selectedAgentId = agent.id }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.SmartToy, contentDescription = null, tint = if (isChosen) WhatsAppGreen else ElegantTextSecondary, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(agent.name, color = if (isChosen) ElegantTextPrimary else ElegantTextSecondary, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Bouton Enregistrer situé en bas des paramètres
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                val cat = (initialCat ?: CategoryEntity(
                                    id = "cat-${UUID.randomUUID().toString().take(8)}",
                                    name = name,
                                    slug = name.lowercase().replace(" ", "-")
                                )).copy(
                                    name = name,
                                    description = description,
                                    assignedAgentId = selectedAgentId
                                )
                                viewModel.saveCategory(cat)
                                showAddDialog = false
                                categoryToEdit = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElegantPurpleAccent),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("save_category_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Enregistrer la catégorie", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    TextButton(
                        onClick = {
                            showAddDialog = false
                            categoryToEdit = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Annuler", color = ElegantTextSecondary)
                    }
                }
            },
            confirmButton = {}
        )
    }
}
