package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entité Produit e-commerce extrait ou saisi manuellement
 */
@Entity(tableName = "ecommerce_products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val sourceTelegramMessageId: Long? = null,
    val sourceChannelTitle: String? = null,
    val title: String,
    val description: String = "",
    val categoryId: String? = null,
    val supplierId: String? = null,
    val purchasePrice: Double? = null,       // Prix fournisseur / achat (NULL si non détecté)
    val sellingPrice: Double? = null,        // Prix de vente conseillé
    val currency: String = "FCFA",
    val stockQuantity: Int = 0,
    val primaryImageUrl: String? = null,
    val status: String = "DRAFT",            // DRAFT, VALIDATED, PUBLISHED, ARCHIVED
    val isPublishedToWebsite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Entité Média rattachée à un produit e-commerce (images, vidéos, albums captés)
 */
@Entity(tableName = "ecommerce_product_media")
data class ProductMediaEntity(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val productId: String,
    val mediaUrl: String,
    val localPath: String? = null,
    val mediaType: String = "photo", // photo, video, document
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Entité Catégorie de produits
 */
@Entity(tableName = "ecommerce_categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val slug: String,
    val description: String = "",
    val iconName: String = "Category",
    val assignedAgentId: String? = null,     // Agent IA assigné au routage WhatsApp pour cette catégorie
    val productCount: Int = 0,
    val isActive: Boolean = true
)

/**
 * Entité Fournisseur
 */
@Entity(tableName = "ecommerce_suppliers")
data class SupplierEntity(
    @PrimaryKey val id: String,
    val name: String,
    val telegramUsername: String? = null,
    val telegramChannelId: Long? = null,
    val phone: String = "",
    val address: String = "",
    val reliabilityRating: Float = 4.5f,
    val notes: String = "",
    val isActive: Boolean = true
)

/**
 * Entité Grille de Tarifs & Contacts Fournisseurs
 */
@Entity(tableName = "ecommerce_price_contacts")
data class PriceContactEntity(
    @PrimaryKey val id: String,
    val supplierId: String,
    val supplierName: String,
    val contactPerson: String,
    val contactPhone: String,
    val negotiatedDiscountPercent: Double = 0.0,
    val paymentTerms: String = "Comptant à la livraison",
    val minOrderQuantity: Int = 1,
    val specialNotes: String = ""
)

/**
 * Entité Agence de Livraison
 */
@Entity(tableName = "ecommerce_shipping_agencies")
data class ShippingAgencyEntity(
    @PrimaryKey val id: String,
    val name: String,
    val coverageZones: String,               // ex: "Dakar, Thiès, Mbour" ou "Abidjan Sud"
    val baseRate: Double = 1500.0,
    val currency: String = "FCFA",
    val contactPhone: String = "",
    val averageDeliveryHours: Int = 24,
    val isActive: Boolean = true
)

/**
 * Entité Partenaire Affilié
 */
@Entity(tableName = "ecommerce_affiliates")
data class AffiliateEntity(
    @PrimaryKey val id: String,
    val fullName: String,
    val referralCode: String,
    val commissionRatePercent: Double = 10.0,
    val phone: String = "",
    val totalEarnings: Double = 0.0,
    val totalSalesCount: Int = 0,
    val currency: String = "FCFA",
    val isActive: Boolean = true
)

/**
 * Entité Commande & Clients à appeler
 */
@Entity(tableName = "ecommerce_orders")
data class OrderEntity(
    @PrimaryKey val id: String,
    val orderNumber: String,
    val customerName: String,
    val customerPhone: String,
    val deliveryAddress: String,
    val deliveryZone: String = "Zone Standard",
    val productId: String? = null,
    val productName: String,
    val quantity: Int = 1,
    val totalAmount: Double,
    val currency: String = "FCFA",
    val status: String = "PENDING_CONFIRMATION", // PENDING_CONFIRMATION, CONFIRMED_CALL, IN_DELIVERY, DELIVERED, CANCELLED
    val assignedShippingAgencyId: String? = null,
    val affiliateCode: String? = null,
    val customerCallNotes: String = "",
    val callAttemptsCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
