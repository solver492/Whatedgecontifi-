package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.verticalScroll
import com.example.data.local.entity.ParsedMediaItem
import com.example.ui.components.MediaCarousel
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ProductEntity
import com.example.ui.MainViewModel
import com.example.util.PriceFormatter
import com.example.ui.theme.ElegantDarkBg
import com.example.ui.theme.ElegantDarkBorder
import com.example.ui.theme.ElegantDarkSurface
import com.example.ui.theme.ElegantDarkSurfaceVariant
import com.example.ui.theme.ElegantPurpleAccent
import com.example.ui.theme.ElegantTextPrimary
import com.example.ui.theme.ElegantTextSecondary
import com.example.ui.theme.WhatsAppGreen
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProductsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val products by viewModel.commerceProducts.collectAsState()
    val categories by viewModel.commerceCategories.collectAsState()
    val suppliers by viewModel.commerceSuppliers.collectAsState()
    val appSettings by viewModel.appSettings.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterCategory by remember { mutableStateOf<String?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<ProductEntity?>(null) }
    var productForDetail by remember { mutableStateOf<ProductEntity?>(null) }

    val filteredProducts = products.filter { prod ->
        val matchesSearch = searchQuery.isBlank() ||
                prod.title.contains(searchQuery, ignoreCase = true) ||
                prod.description.contains(searchQuery, ignoreCase = true)
        val matchesCategory = when (selectedFilterCategory) {
            null -> true
            "__uncategorized__" -> prod.categoryId == null
            else -> prod.categoryId == selectedFilterCategory
        }
        matchesSearch && matchesCategory
    }

    Box(modifier = modifier.fillMaxSize().background(ElegantDarkBg)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Stats & Search
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Catalogue Produits",
                            color = ElegantTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            "${products.size} produits référencés • ${products.count { it.isPublishedToWebsite }} en ligne",
                            color = ElegantTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ElegantPurpleAccent.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, ElegantPurpleAccent.copy(alpha = 0.4f))
                    ) {
                        Text(
                            "Sync RAG Auto",
                            color = ElegantPurpleAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Champ Recherche
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().testTag("products_search_input"),
                    placeholder = { Text("Rechercher un article, modèle...", color = ElegantTextSecondary, fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = ElegantTextSecondary, modifier = Modifier.size(18.dp)) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = ElegantDarkSurface,
                        unfocusedContainerColor = ElegantDarkSurface,
                        focusedBorderColor = ElegantPurpleAccent,
                        unfocusedBorderColor = ElegantDarkBorder,
                        focusedTextColor = ElegantTextPrimary,
                        unfocusedTextColor = ElegantTextPrimary
                    )
                )

                // Filtres Catégories Horizontaux
                if (categories.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val isAllSelected = selectedFilterCategory == null
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isAllSelected) ElegantPurpleAccent.copy(alpha = 0.2f) else ElegantDarkSurface,
                            border = BorderStroke(1.dp, if (isAllSelected) ElegantPurpleAccent else ElegantDarkBorder),
                            modifier = Modifier.clickable { selectedFilterCategory = null }
                        ) {
                            Text(
                                "Toutes (${products.size})",
                                color = if (isAllSelected) ElegantPurpleAccent else ElegantTextSecondary,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }

                        categories.forEach { cat ->
                            val isSelected = selectedFilterCategory == cat.id
                            val count = products.count { it.categoryId == cat.id }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) ElegantPurpleAccent.copy(alpha = 0.2f) else ElegantDarkSurface,
                                border = BorderStroke(1.dp, if (isSelected) ElegantPurpleAccent else ElegantDarkBorder),
                                modifier = Modifier.clickable { selectedFilterCategory = cat.id }
                            ) {
                                Text(
                                    "${cat.name} ($count)",
                                    color = if (isSelected) ElegantPurpleAccent else ElegantTextSecondary,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }

                        // Filtre explicite pour les produits non catégorisés (Section 5)
                        val uncategorizedCount = products.count { it.categoryId == null }
                        if (uncategorizedCount > 0) {
                            val isUncategorizedSelected = selectedFilterCategory == "__uncategorized__"
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isUncategorizedSelected) Color(0xFFF59E0B).copy(alpha = 0.2f) else ElegantDarkSurface,
                                border = BorderStroke(1.dp, if (isUncategorizedSelected) Color(0xFFF59E0B) else ElegantDarkBorder),
                                modifier = Modifier.clickable { selectedFilterCategory = "__uncategorized__" }
                            ) {
                                Text(
                                    "Non catégorisés ($uncategorizedCount)",
                                    color = if (isUncategorizedSelected) Color(0xFFF59E0B) else ElegantTextSecondary,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (filteredProducts.isEmpty()) {
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
                            Icon(Icons.Default.Inventory2, contentDescription = null, tint = ElegantTextSecondary, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Aucun produit trouvé", color = ElegantTextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                "Ajoutez un produit manuellement ou activez la surveillance d'un canal Telegram pour l'extraction IA automatique.",
                                color = ElegantTextSecondary,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            } else {
                items(filteredProducts, key = { it.id }) { product ->
                    ProductCardItem(
                        product = product,
                        categoryName = categories.find { it.id == product.categoryId }?.name ?: "Non catégorisé",
                        supplierName = suppliers.find { it.id == product.supplierId }?.name ?: "Fournisseur Direct",
                        appCurrency = appSettings.currency.ifBlank { "MAD" },
                        onViewDetail = { productForDetail = product },
                        onTogglePublish = { viewModel.toggleProductPublish(product) },
                        onEdit = { productToEdit = product },
                        onDelete = { viewModel.deleteProduct(product) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }

        // Floating Action Button pour ajouter un produit
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = ElegantPurpleAccent,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("fab_add_product")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Ajouter Produit")
        }
    }

    // Dialogue Détail Produit avec Galerie Médias swipeable et Vidéo lisible (Section 2)
    productForDetail?.let { detailProd ->
        ProductDetailDialog(
            product = detailProd,
            categoryName = categories.find { it.id == detailProd.categoryId }?.name ?: "Non catégorisé",
            supplierName = suppliers.find { it.id == detailProd.supplierId }?.name ?: "Fournisseur Direct",
            appCurrency = appSettings.currency.ifBlank { "MAD" },
            viewModel = viewModel,
            onDismiss = { productForDetail = null },
            onEdit = {
                val target = detailProd
                productForDetail = null
                productToEdit = target
            },
            onTogglePublish = { viewModel.toggleProductPublish(detailProd) }
        )
    }

    // Dialogue Ajout / Édition
    if (showAddDialog || productToEdit != null) {
        val target = productToEdit
        val defaultCurrency = appSettings.currency.ifBlank { "MAD" }
        ProductEditDialog(
            initialProduct = target,
            categories = categories,
            suppliers = suppliers,
            defaultCurrency = defaultCurrency,
            onDismiss = {
                showAddDialog = false
                productToEdit = null
            },
            onSave = { savedProd ->
                viewModel.saveProduct(savedProd)
                showAddDialog = false
                productToEdit = null
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProductCardItem(
    product: ProductEntity,
    categoryName: String,
    supplierName: String,
    appCurrency: String,
    onViewDetail: () -> Unit,
    onTogglePublish: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val displayCurrency = if (product.currency.isNotBlank() && product.currency != "FCFA") product.currency else appCurrency
    val isUncategorized = categoryName == "Non catégorisé"

    Card(
        colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
        border = BorderStroke(1.dp, if (isUncategorized) Color(0xFFF59E0B).copy(alpha = 0.3f) else ElegantDarkBorder),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewDetail() }
            .testTag("product_card_${product.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Catégorie Badge (Section 5 : non catégorisé bien visible)
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isUncategorized) Color(0xFFF59E0B).copy(alpha = 0.2f) else ElegantDarkSurfaceVariant,
                    border = BorderStroke(1.dp, if (isUncategorized) Color(0xFFF59E0B) else ElegantDarkBorder)
                ) {
                    Text(
                        categoryName,
                        color = if (isUncategorized) Color(0xFFF59E0B) else ElegantTextSecondary,
                        fontSize = 10.sp,
                        fontWeight = if (isUncategorized) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // Statut Publication
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (product.isPublishedToWebsite) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = WhatsAppGreen.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, WhatsAppGreen.copy(alpha = 0.4f))
                        ) {
                            Text(
                                "● Site Publié",
                                color = WhatsAppGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFFFB300).copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, Color(0xFFFFB300).copy(alpha = 0.4f))
                        ) {
                            Text(
                                "Brouillon Local",
                                color = Color(0xFFFFB300),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(onClick = onEdit, modifier = Modifier.size(26.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Modifier", tint = ElegantTextSecondary, modifier = Modifier.size(15.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(26.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = Color(0xFFEF5350), modifier = Modifier.size(15.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                if (!product.primaryImageUrl.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(ElegantDarkBg)
                            .clickable { onViewDetail() }
                    ) {
                        AsyncImage(
                            model = product.primaryImageUrl,
                            contentDescription = product.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        product.title,
                        color = ElegantTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    if (product.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            product.description,
                            color = ElegantTextSecondary,
                            fontSize = 11.sp,
                            maxLines = 2,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Grille Prix & Stock (Section 4 : Devise dynamique MAD)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    val sellPriceStr = PriceFormatter.format(product.sellingPrice, displayCurrency)
                    val buyPriceStr = PriceFormatter.formatPurchase(product.purchasePrice, displayCurrency)

                    Text(
                        sellPriceStr,
                        color = WhatsAppGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        buyPriceStr,
                        color = ElegantTextSecondary.copy(alpha = 0.8f),
                        fontSize = 10.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = ElegantDarkBg,
                        border = BorderStroke(1.dp, ElegantDarkBorder)
                    ) {
                        Text(
                            "Stock: ${product.stockQuantity}",
                            color = ElegantTextPrimary,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Button(
                        onClick = onTogglePublish,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (product.isPublishedToWebsite) ElegantDarkSurfaceVariant else ElegantPurpleAccent
                        ),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(
                            Icons.Default.Public,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            if (product.isPublishedToWebsite) "Retirer" else "Publier",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // Provenance / Fournisseur
            if (product.sourceChannelTitle != null || supplierName.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Source: ${product.sourceChannelTitle ?: supplierName}",
                        color = ElegantTextSecondary.copy(alpha = 0.6f),
                        fontSize = 9.sp
                    )
                }
            }
        }
    }
}

/**
 * Dialogue de détail de la fiche produit avec galerie multimédia complète (photos swipeables + vidéos lisibles)
 * Conforme à la Section 2 du document d'erreurs.
 */
@Composable
private fun ProductDetailDialog(
    product: ProductEntity,
    categoryName: String,
    supplierName: String,
    appCurrency: String,
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onTogglePublish: () -> Unit
) {
    val mediaEntities by viewModel.getProductMedia(product.id).collectAsState(initial = emptyList())
    val parsedMedia = remember(mediaEntities, product.primaryImageUrl) {
        if (mediaEntities.isNotEmpty()) {
            mediaEntities.map { entity ->
                ParsedMediaItem(
                    url = entity.mediaUrl,
                    localPath = if (entity.mediaUrl.startsWith("/")) entity.mediaUrl else null,
                    isVideo = (entity.mediaType == "video")
                )
            }
        } else if (!product.primaryImageUrl.isNullOrBlank()) {
            listOf(
                ParsedMediaItem(
                    url = product.primaryImageUrl,
                    localPath = if (product.primaryImageUrl.startsWith("/")) product.primaryImageUrl else null,
                    isVideo = product.primaryImageUrl.endsWith(".mp4") || product.primaryImageUrl.endsWith(".mov")
                )
            )
        } else {
            emptyList()
        }
    }

    val displayCurrency = if (product.currency.isNotBlank() && product.currency != "FCFA") product.currency else appCurrency

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    product.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = ElegantTextPrimary,
                    modifier = Modifier.weight(1f),
                    maxLines = 2
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Fermer", tint = ElegantTextSecondary)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Galerie Médias Swipeable / Vidéo (Section 2)
                if (parsedMedia.isNotEmpty()) {
                    MediaCarousel(
                        mediaItems = parsedMedia,
                        height = 230.dp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Badges Catégorie et Statut
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isUncategorized = categoryName == "Non catégorisé"
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isUncategorized) Color(0xFFF59E0B).copy(alpha = 0.2f) else ElegantDarkSurfaceVariant,
                        border = BorderStroke(1.dp, if (isUncategorized) Color(0xFFF59E0B) else ElegantDarkBorder)
                    ) {
                        Text(
                            categoryName,
                            color = if (isUncategorized) Color(0xFFF59E0B) else ElegantTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    if (product.isPublishedToWebsite) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = WhatsAppGreen.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, WhatsAppGreen.copy(alpha = 0.4f))
                        ) {
                            Text(
                                "● En ligne sur le site",
                                color = WhatsAppGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFFFB300).copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, Color(0xFFFFB300).copy(alpha = 0.4f))
                        ) {
                            Text(
                                "Brouillon Local",
                                color = Color(0xFFFFB300),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Bloc Prix, Coût et Marge
                Card(
                    colors = CardDefaults.cardColors(containerColor = ElegantDarkBg),
                    border = BorderStroke(1.dp, ElegantDarkBorder),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Prix de vente client :", color = ElegantTextSecondary, fontSize = 12.sp)
                            Text(
                                PriceFormatter.format(product.sellingPrice, displayCurrency),
                                color = WhatsAppGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        if (product.purchasePrice != null && product.purchasePrice > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Prix d'achat fournisseur :", color = ElegantTextSecondary, fontSize = 12.sp)
                                Text(
                                    PriceFormatter.format(product.purchasePrice, displayCurrency),
                                    color = ElegantTextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                            val margin = (product.sellingPrice ?: 0.0) - (product.purchasePrice ?: 0.0)
                            val sellingP = product.sellingPrice ?: 1.0
                            val marginPercent = if (sellingP > 0) (margin / sellingP) * 100 else 0.0
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Marge brute estimée :", color = ElegantTextSecondary, fontSize = 12.sp)
                                Text(
                                    "${PriceFormatter.format(margin, displayCurrency)} (+${marginPercent.toInt()}%)",
                                    color = if (margin >= 0) Color(0xFF10B981) else Color(0xFFEF4444),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Stock disponible :", color = ElegantTextSecondary, fontSize = 12.sp)
                            Text("${product.stockQuantity} unités", color = ElegantTextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        }
                        if (!product.sourceChannelTitle.isNullOrBlank()) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Source Telegram :", color = ElegantTextSecondary, fontSize = 12.sp)
                                Text(product.sourceChannelTitle, color = TelegramBlue, fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Description
                if (product.description.isNotBlank()) {
                    Text("Description :", color = ElegantTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        product.description,
                        color = ElegantTextPrimary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Boutons d'action
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onTogglePublish,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(if (product.isPublishedToWebsite) "Dépublier" else "Publier en ligne", fontSize = 12.sp)
                    }
                    Button(
                        onClick = onEdit,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = ElegantPurpleAccent),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Modifier", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {},
        containerColor = ElegantDarkSurface
    )
}

@Composable
private fun ProductEditDialog(
    initialProduct: ProductEntity?,
    categories: List<com.example.data.local.entity.CategoryEntity>,
    suppliers: List<com.example.data.local.entity.SupplierEntity>,
    defaultCurrency: String,
    onDismiss: () -> Unit,
    onSave: (ProductEntity) -> Unit
) {
    var title by remember { mutableStateOf(initialProduct?.title ?: "") }
    var description by remember { mutableStateOf(initialProduct?.description ?: "") }
    var sellPrice by remember { mutableStateOf(initialProduct?.sellingPrice?.toString() ?: "") }
    var buyPrice by remember { mutableStateOf(initialProduct?.purchasePrice?.toString() ?: "") }
    var stock by remember { mutableStateOf(initialProduct?.stockQuantity?.toString() ?: "10") }
    var selectedCatId by remember { mutableStateOf(initialProduct?.categoryId) }
    var selectedSupId by remember { mutableStateOf(initialProduct?.supplierId ?: suppliers.firstOrNull()?.id) }
    var imageUrl by remember { mutableStateOf(initialProduct?.primaryImageUrl ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ElegantDarkSurface,
        title = {
            Text(
                if (initialProduct == null) "Nouveau Produit" else "Modifier le Produit",
                color = ElegantTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titre du produit *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = sellPrice,
                        onValueChange = { sellPrice = it },
                        label = { Text("Prix Vente ($defaultCurrency)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = buyPrice,
                        onValueChange = { buyPrice = it },
                        label = { Text("Prix Achat ($defaultCurrency)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = stock,
                    onValueChange = { stock = it },
                    label = { Text("Quantité en stock") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("URL Photo / Image (Optionnel)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (categories.isNotEmpty()) {
                    Text("Catégorie & Routage IA :", color = ElegantTextSecondary, fontSize = 11.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val isNone = selectedCatId == null
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isNone) Color(0xFFF59E0B).copy(alpha = 0.2f) else ElegantDarkBg,
                            border = BorderStroke(1.dp, if (isNone) Color(0xFFF59E0B) else ElegantDarkBorder),
                            modifier = Modifier.clickable { selectedCatId = null }
                        ) {
                            Text(
                                "Non catégorisé",
                                color = if (isNone) Color(0xFFF59E0B) else ElegantTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isNone) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }

                        categories.forEach { cat ->
                            val isSelected = selectedCatId == cat.id
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) ElegantPurpleAccent.copy(alpha = 0.2f) else ElegantDarkBg,
                                border = BorderStroke(1.dp, if (isSelected) ElegantPurpleAccent else ElegantDarkBorder),
                                modifier = Modifier.clickable { selectedCatId = cat.id }
                            ) {
                                Text(
                                    cat.name,
                                    color = if (isSelected) ElegantPurpleAccent else ElegantTextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bouton Enregistrer situé en bas des paramètres
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            val product = (initialProduct ?: ProductEntity(
                                id = "prod-${UUID.randomUUID().toString().take(8)}",
                                title = title,
                                currency = defaultCurrency
                            )).copy(
                                title = title,
                                description = description,
                                sellingPrice = sellPrice.toDoubleOrNull(),
                                purchasePrice = buyPrice.toDoubleOrNull(),
                                stockQuantity = stock.toIntOrNull() ?: 0,
                                categoryId = selectedCatId,
                                supplierId = selectedSupId,
                                primaryImageUrl = imageUrl.ifBlank { null },
                                currency = if (initialProduct?.currency.isNullOrBlank() || initialProduct?.currency == "FCFA") defaultCurrency else initialProduct!!.currency,
                                updatedAt = System.currentTimeMillis()
                            )
                            onSave(product)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElegantPurpleAccent),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_product_button")
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Enregistrer le Produit", color = Color.White, fontWeight = FontWeight.Bold)
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Annuler", color = ElegantTextSecondary)
                }
            }
        },
        confirmButton = {}
    )
}
