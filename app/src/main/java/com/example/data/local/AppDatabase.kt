package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.AgentDao
import com.example.data.local.dao.CommerceDao
import com.example.data.local.dao.KnowledgeDao
import com.example.data.local.dao.McpDao
import com.example.data.local.dao.SettingsDao
import com.example.data.local.dao.TelegramDao
import com.example.data.local.dao.WebhookDao
import com.example.data.local.dao.WhatsAppDao
import com.example.data.local.dao.WhatsAppMessageDao
import com.example.data.local.entity.AffiliateEntity
import com.example.data.local.entity.AgentEntity
import com.example.data.local.entity.AppSettingsEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.KnowledgeSourceEntity
import com.example.data.local.entity.McpToolEntity
import com.example.data.local.entity.OrderEntity
import com.example.data.local.entity.PriceContactEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.ProductMediaEntity
import com.example.data.local.entity.ShippingAgencyEntity
import com.example.data.local.entity.SupplierEntity
import com.example.data.local.entity.TelegramAccountEntity
import com.example.data.local.entity.TelegramChannelEntity
import com.example.data.local.entity.TelegramLogEntity
import com.example.data.local.entity.TelegramMessageEntity
import com.example.data.local.entity.WebhookConfigEntity
import com.example.data.local.entity.WhatsAppInstanceEntity
import com.example.data.local.entity.WhatsAppMessageEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

@Database(
    entities = [
        WhatsAppInstanceEntity::class,
        AgentEntity::class,
        KnowledgeSourceEntity::class,
        McpToolEntity::class,
        WhatsAppMessageEntity::class,
        WebhookConfigEntity::class,
        TelegramAccountEntity::class,
        TelegramChannelEntity::class,
        TelegramMessageEntity::class,
        TelegramLogEntity::class,
        ProductEntity::class,
        ProductMediaEntity::class,
        CategoryEntity::class,
        SupplierEntity::class,
        PriceContactEntity::class,
        ShippingAgencyEntity::class,
        AffiliateEntity::class,
        OrderEntity::class,
        AppSettingsEntity::class
    ],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun whatsAppDao(): WhatsAppDao
    abstract fun agentDao(): AgentDao
    abstract fun knowledgeDao(): KnowledgeDao
    abstract fun mcpDao(): McpDao
    abstract fun whatsAppMessageDao(): WhatsAppMessageDao
    abstract fun webhookDao(): WebhookDao
    abstract fun telegramDao(): TelegramDao
    abstract fun commerceDao(): CommerceDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ai_edge_whatsapp_db"
                ).fallbackToDestructiveMigration()
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            INSTANCE?.let { seedInitialData(it) }
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun seedInitialData(database: AppDatabase) {
            val waDao = database.whatsAppDao()
            val agentDao = database.agentDao()
            val knowDao = database.knowledgeDao()
            val mcpDao = database.mcpDao()
            val webhookDao = database.webhookDao()
            val msgDao = database.whatsAppMessageDao()

            // 1. Real WhatsApp Instance (User Phone: 33773163772)
            val mainInstance = WhatsAppInstanceEntity(
                id = "inst-wa-main",
                name = "WhatsApp Principal",
                phoneNumber = "33773163772",
                status = "DISCONNECTED",
                pairingMethod = "PAIRING_CODE",
                pairingCode = "",
                qrToken = "",
                bridgeUrl = "http://127.0.0.1:8080",
                localPort = 8080,
                isDefault = true,
                unreadCount = 0,
                messagesCount = 0
            )
            waDao.insertInstance(mainInstance)

            // 2. Initial AI Agents
            val supportAgent = AgentEntity(
                id = "agent-support-01",
                name = "Agent Support Technique & FAQ",
                role = "Support",
                systemPrompt = """Tu es un assistant support client WhatsApp ultra efficace et bienveillant pour notre entreprise.
Réponds de manière concise, polie et directe (format adapté à WhatsApp, avec des émojis professionnels).
Utilise les informations de la base de connaissances fournie pour guider le client.
Si le client pose une question hors champ, propose un transfert vers un humain avec l'outil disponible.""".trimIndent(),
                modelId = "gemma-2-2b-int4",
                isLocal = true,
                isActive = true,
                activationMode = "ALWAYS",
                keywordsCsv = "aide,support,probleme,panne,erreur,bug",
                scheduleStart = "00:00",
                scheduleEnd = "23:59",
                assignedInstanceIdsCsv = "*",
                temperature = 0.5f,
                ragEnabled = true,
                mcpToolsCsv = "check_order_status,transfer_to_human",
                isFallback = true,
                responseCount = 42,
                avgLatencyMs = 210L
            )

            val salesAgent = AgentEntity(
                id = "agent-sales-02",
                name = "Agent Ventes & Devis",
                role = "Commercial",
                systemPrompt = """Tu es un commercial dynamique et persuasif sur WhatsApp.
Tu présentes nos offres (Pack Starter 29€/m, Pack Pro 79€/m, Pack Enterprise sur mesure).
Identifie les besoins du prospect, calcule ou donne les tarifs exacts selon la base de connaissances.
Invite poliment le prospect à réserver un appel ou finaliser sa commande.""".trimIndent(),
                modelId = "llama-3.2-1b-int4",
                isLocal = true,
                isActive = true,
                activationMode = "KEYWORDS",
                keywordsCsv = "prix,tarif,tarifs,devis,offre,offres,acheter,achat,pack,packs,vendre,vente,proposer,propose,catalogue,produit,produits,service,services,reduction,prospect",
                scheduleStart = "08:00",
                scheduleEnd = "20:00",
                assignedInstanceIdsCsv = "inst-support-01,inst-sales-02",
                temperature = 0.7f,
                ragEnabled = true,
                mcpToolsCsv = "get_product_price,book_appointment",
                isFallback = false,
                responseCount = 28,
                avgLatencyMs = 185L
            )

            val nightAgent = AgentEntity(
                id = "agent-night-03",
                name = "Agent Astreinte Nuit (Automatique)",
                role = "Scheduling",
                systemPrompt = """Tu es l'agent de garde nocturne WhatsApp.
Nos bureaux sont actuellement fermés (horaires d'ouverture : 08h30 - 19h00).
Rassure le client, note sa demande et propose de réserver un créneau ou de laisser ses coordonnées pour un rappel dès demain matin.""".trimIndent(),
                modelId = "phi-3.5-mini-int4",
                isLocal = true,
                isActive = true,
                activationMode = "SCHEDULE",
                keywordsCsv = "*",
                scheduleStart = "20:00",
                scheduleEnd = "08:00",
                assignedInstanceIdsCsv = "*",
                temperature = 0.6f,
                ragEnabled = true,
                mcpToolsCsv = "book_appointment",
                isFallback = false,
                responseCount = 15,
                avgLatencyMs = 240L
            )

            agentDao.insertAgent(supportAgent)
            agentDao.insertAgent(salesAgent)
            agentDao.insertAgent(nightAgent)

            // 3. Initial Knowledge Sources (Supabase, Web, PDF, Text)
            knowDao.insertSource(
                KnowledgeSourceEntity(
                    id = "know-supabase-01",
                    agentId = "*",
                    type = "SUPABASE",
                    title = "Supabase DB - Clients & Commandes",
                    targetUrlOrConfig = "https://xyzcompany.supabase.co/rest/v1/clients",
                    contentData = "Table clients(id, nom, email, statut_commande, abonnement). Commandes récentes : #CMD-8491 (Livrée), #CMD-9201 (En transit, livraison estimée demain).",
                    supabaseAnonKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                    supabaseTable = "clients_and_orders",
                    isEnabled = true,
                    chunkCount = 24
                )
            )

            knowDao.insertSource(
                KnowledgeSourceEntity(
                    id = "know-web-02",
                    agentId = "agent-sales-02",
                    type = "WEB_URL",
                    title = "Page Web Tarifs & Services",
                    targetUrlOrConfig = "https://example.com/pricing",
                    contentData = "Offres 2025: Pack Starter à 29€/mois (1 instance WA, 500 réponses IA/jour), Pack Pro à 79€/mois (5 instances WA, modèles Edge Quantizer illimités, RAG Supabase inclus), Entreprise à 249€/mois (Support dédié, NPU Edge optimisé).",
                    isEnabled = true,
                    chunkCount = 12
                )
            )

            knowDao.insertSource(
                KnowledgeSourceEntity(
                    id = "know-pdf-03",
                    agentId = "agent-support-01",
                    type = "PDF_DOC",
                    title = "Guide_SAV_Garanties_2025.pdf",
                    targetUrlOrConfig = "assets/docs/Guide_SAV_Garanties_2025.pdf",
                    contentData = "Garantie satisfait ou remboursé sous 14 jours ouvrés. En cas de dysfonctionnement matériel, échange standard sous 48h. Démarche : renvoyer le numéro de série et la facture.",
                    isEnabled = true,
                    chunkCount = 18
                )
            )

            knowDao.insertSource(
                KnowledgeSourceEntity(
                    id = "know-text-04",
                    agentId = "*",
                    type = "TEXT_SNIPPET",
                    title = "Politique de Livraison & Horaires",
                    targetUrlOrConfig = "Snippet Local",
                    contentData = "Horaires de notre showroom et service client : du lundi au vendredi de 8h30 à 19h00 sans interruption. Livraison offerte dès 60€ d'achats en France métropolitaine.",
                    isEnabled = true,
                    chunkCount = 4
                )
            )

            // 4. Initial MCP Tools
            mcpDao.insertTool(
                McpToolEntity(
                    id = "mcp-order-01",
                    name = "check_order_status",
                    description = "Vérifie le statut et le suivi d'une commande client via son numéro (ex: #CMD-9201)",
                    schemaJson = """{"type":"object","properties":{"order_id":{"type":"string","description":"Le numéro de commande à vérifier"}},"required":["order_id"]}""",
                    isEnabled = true
                )
            )
            mcpDao.insertTool(
                McpToolEntity(
                    id = "mcp-price-02",
                    name = "get_product_price",
                    description = "Consulte le prix actuel et les stocks d'un article ou d'un pack",
                    schemaJson = """{"type":"object","properties":{"item_name":{"type":"string","description":"Nom du pack ou produit"}},"required":["item_name"]}""",
                    isEnabled = true
                )
            )
            mcpDao.insertTool(
                McpToolEntity(
                    id = "mcp-book-03",
                    name = "book_appointment",
                    description = "Planifie un rendez-vous téléphonique avec un conseiller commercial",
                    schemaJson = """{"type":"object","properties":{"client_name":{"type":"string"},"date":{"type":"string"},"time":{"type":"string"}},"required":["client_name","date"]}""",
                    isEnabled = true
                )
            )
            mcpDao.insertTool(
                McpToolEntity(
                    id = "mcp-human-04",
                    name = "transfer_to_human",
                    description = "Transfère la conversation WhatsApp à un agent humain en cas de situation bloquante",
                    schemaJson = """{"type":"object","properties":{"reason":{"type":"string"}},"required":["reason"]}""",
                    isEnabled = true
                )
            )

            // 5. Initial Webhook
            webhookDao.insertWebhook(
                WebhookConfigEntity(
                    id = "webhook-crm-01",
                    name = "Webhook CRM Zapier / N8N",
                    url = "https://hooks.zapier.com/hooks/catch/19482/wa_events",
                    eventsCsv = "messages.upsert,connection.update",
                    secretKey = "whsec_edge_wa_9941",
                    isEnabled = true,
                    lastPingSuccess = true,
                    lastPingTimestamp = System.currentTimeMillis() - 120000
                )
            )

            // 6. Commerce initial data
            val commerceDao = database.commerceDao()

            // Catégories
            commerceDao.insertCategory(
                CategoryEntity(
                    id = "cat-tech",
                    name = "Électronique & High-Tech",
                    slug = "electronique",
                    description = "Smartphones, écouteurs, montres connectées, chargeurs rapides",
                    iconName = "Devices",
                    assignedAgentId = "agent-sales-01",
                    productCount = 12
                )
            )
            commerceDao.insertCategory(
                CategoryEntity(
                    id = "cat-mode",
                    name = "Mode & Chaussures",
                    slug = "mode-chaussures",
                    description = "Sneakers, vêtements tendances, maroquinerie",
                    iconName = "Checkroom",
                    assignedAgentId = "agent-sales-01",
                    productCount = 8
                )
            )
            commerceDao.insertCategory(
                CategoryEntity(
                    id = "cat-beaute",
                    name = "Beauté & Bien-être",
                    slug = "beaute-soin",
                    description = "Soins du visage, parfums, sérums hydratants",
                    iconName = "Spa",
                    assignedAgentId = "agent-support-02",
                    productCount = 5
                )
            )

            // Fournisseurs
            commerceDao.insertSupplier(
                SupplierEntity(
                    id = "sup-canal-01",
                    name = "Grossiste Import Dubai & Chine",
                    telegramUsername = "import_dubai_direct",
                    telegramChannelId = -1001849201934L,
                    phone = "+221 77 842 19 20",
                    address = "Zone Franche Industrielle, Entrepôt B4",
                    reliabilityRating = 4.8f,
                    notes = "Canal Telegram très actif, arrivages hebdomadaires le mardi"
                )
            )
            commerceDao.insertSupplier(
                SupplierEntity(
                    id = "sup-canal-02",
                    name = "Sneakers Factory Dakar",
                    telegramUsername = "dakar_sneakers_hub",
                    telegramChannelId = -1001928374651L,
                    phone = "+221 78 510 33 44",
                    address = "Marché HLM, Boutique 12",
                    reliabilityRating = 4.5f,
                    notes = "Fournisseur local avec stock immédiat et prix dégressifs"
                )
            )

            // Grille de Tarifs & Contacts
            commerceDao.insertPriceContact(
                PriceContactEntity(
                    id = "contact-sup-01",
                    supplierId = "sup-canal-01",
                    supplierName = "Grossiste Import Dubai & Chine",
                    contactPerson = "M. Amadou Diallo (Responsable Expéditions)",
                    contactPhone = "+221 77 842 19 20",
                    negotiatedDiscountPercent = 12.5,
                    paymentTerms = "Acompte 30% commande, solde à réception",
                    minOrderQuantity = 5,
                    specialNotes = "Accepte Wave, Orange Money et virement bancaire"
                )
            )

            // Agences de Livraison
            commerceDao.insertShippingAgency(
                ShippingAgencyEntity(
                    id = "ship-express-01",
                    name = "Colis Express Dakar & Banlieue",
                    coverageZones = "Dakar Centre, Plateau, Almadies, Guédiawaye, Pikine",
                    baseRate = 2000.0,
                    currency = "FCFA",
                    contactPhone = "+221 77 123 45 67",
                    averageDeliveryHours = 12
                )
            )
            commerceDao.insertShippingAgency(
                ShippingAgencyEntity(
                    id = "ship-regions-02",
                    name = "Sahel Logistique Régions",
                    coverageZones = "Thiès, Mbour, Saint-Louis, Touba, Kaolack",
                    baseRate = 3500.0,
                    currency = "FCFA",
                    contactPhone = "+221 76 999 88 77",
                    averageDeliveryHours = 36
                )
            )

            // Partenaires Affiliés
            commerceDao.insertAffiliate(
                AffiliateEntity(
                    id = "aff-fatou-01",
                    fullName = "Fatou Kiné Sène",
                    referralCode = "FATOU10",
                    commissionRatePercent = 8.0,
                    phone = "+221 77 654 32 10",
                    totalEarnings = 48500.0,
                    totalSalesCount = 14
                )
            )
            commerceDao.insertAffiliate(
                AffiliateEntity(
                    id = "aff-moussa-02",
                    fullName = "Moussa Traoré",
                    referralCode = "MOUSSA_VIP",
                    commissionRatePercent = 10.0,
                    phone = "+221 70 812 34 56",
                    totalEarnings = 72000.0,
                    totalSalesCount = 21
                )
            )

            // Produits
            commerceDao.insertProduct(
                ProductEntity(
                    id = "prod-airpods-pro",
                    title = "Écouteurs Sans Fil Pro ANC Bluetooth 5.3",
                    description = "Réduction active du bruit, autonomie 30h avec boîtier MagSafe, son spatial 3D. Idéal pour appels et musique.",
                    categoryId = "cat-tech",
                    supplierId = "sup-canal-01",
                    purchasePrice = 9500.0,
                    sellingPrice = 18500.0,
                    currency = "FCFA",
                    stockQuantity = 45,
                    status = "VALIDATED",
                    isPublishedToWebsite = true
                )
            )
            commerceDao.insertProduct(
                ProductEntity(
                    id = "prod-smartwatch-ultra",
                    title = "Montre Connectée Ultra 49mm AMOLED",
                    description = "Suivi cardiaque, oxymètre SpO2, étanche IP68, 100 modes sport, appels Bluetooth mains-libres.",
                    categoryId = "cat-tech",
                    supplierId = "sup-canal-01",
                    purchasePrice = 14000.0,
                    sellingPrice = 27000.0,
                    currency = "FCFA",
                    stockQuantity = 22,
                    status = "PUBLISHED",
                    isPublishedToWebsite = true
                )
            )
            commerceDao.insertProduct(
                ProductEntity(
                    id = "prod-sneaker-dunk",
                    title = "Sneakers Urban Low Classic Edition",
                    description = "Cuir premium synthétique, semelle anti-dérapante renforcée, disponibles pointures 40 à 45.",
                    categoryId = "cat-mode",
                    supplierId = "sup-canal-02",
                    purchasePrice = 13500.0,
                    sellingPrice = 24900.0,
                    currency = "FCFA",
                    stockQuantity = 16,
                    status = "VALIDATED",
                    isPublishedToWebsite = false
                )
            )

            // Commandes & Clients à appeler
            commerceDao.insertOrder(
                OrderEntity(
                    id = "ord-1001",
                    orderNumber = "CMD-2026-0814",
                    customerName = "Ibrahima Ndiaye",
                    customerPhone = "+221 77 345 88 12",
                    deliveryAddress = "Sacré-Cœur 3, Villa 104 en face de la boulangerie",
                    deliveryZone = "Dakar Centre",
                    productId = "prod-airpods-pro",
                    productName = "Écouteurs Sans Fil Pro ANC Bluetooth 5.3",
                    quantity = 1,
                    totalAmount = 18500.0,
                    currency = "FCFA",
                    status = "PENDING_CONFIRMATION",
                    assignedShippingAgencyId = "ship-express-01",
                    affiliateCode = "FATOU10",
                    customerCallNotes = "Client disponible pour confirmation à partir de 14h.",
                    callAttemptsCount = 0
                )
            )
            commerceDao.insertOrder(
                OrderEntity(
                    id = "ord-1002",
                    orderNumber = "CMD-2026-0815",
                    customerName = "Aïssatou Ba",
                    customerPhone = "+221 78 412 90 33",
                    deliveryAddress = "Mermoz Pyrotechnie, Immeuble Horizon 2ème étage",
                    deliveryZone = "Dakar Centre",
                    productId = "prod-smartwatch-ultra",
                    productName = "Montre Connectée Ultra 49mm AMOLED",
                    quantity = 1,
                    totalAmount = 27000.0,
                    currency = "FCFA",
                    status = "PENDING_CONFIRMATION",
                    assignedShippingAgencyId = "ship-express-01",
                    affiliateCode = "MOUSSA_VIP",
                    customerCallNotes = "Préfère livraison avant 18h ou le weekend.",
                    callAttemptsCount = 1
                )
            )
            commerceDao.insertOrder(
                OrderEntity(
                    id = "ord-1003",
                    orderNumber = "CMD-2026-0816",
                    customerName = "Cheikh Oumar Tall",
                    customerPhone = "+221 76 555 41 89",
                    deliveryAddress = "Cité Keur Gorgui, Immeuble Sonatel",
                    deliveryZone = "Dakar Centre",
                    productId = "prod-sneaker-dunk",
                    productName = "Sneakers Urban Low Classic Edition (Taille 43)",
                    quantity = 2,
                    totalAmount = 49800.0,
                    currency = "FCFA",
                    status = "CONFIRMED_CALL",
                    assignedShippingAgencyId = "ship-express-01",
                    affiliateCode = null,
                    customerCallNotes = "Client appelé et validé. Confirme paiement à la livraison.",
                    callAttemptsCount = 1
                )
            )

            // 7. Paramètres Généraux de l'Application (Maroc + MAD par défaut)
            val settingsDao = database.settingsDao()
            settingsDao.saveSettings(
                AppSettingsEntity(
                    id = "global_settings",
                    currency = "MAD",
                    currencySymbol = "DH",
                    defaultCountryCode = "+212",
                    countryName = "Maroc",
                    timeZone = "Africa/Casablanca",
                    defaultProfitMarginPercent = 40.0,
                    lowStockThreshold = 5
                )
            )
        }
    }
}
