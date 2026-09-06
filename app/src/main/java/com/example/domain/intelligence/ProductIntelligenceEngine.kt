package com.example.domain.intelligence

import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.TelegramMessageEntity
import java.util.Locale
import java.util.UUID
import java.util.regex.Pattern

data class ExtractedProductData(
    val title: String,
    val description: String,
    val purchasePrice: Double?,
    val currency: String,
    val suggestedSellingPrice: Double?,
    val variantsOrSizes: String?,
    val contactOrOrderLink: String?,
    val isLotOrPackPrice: Boolean = false,
    val lotQuantity: Int? = null,
    val lotTotalPrice: Double? = null,
    val lotUnitPriceEstimate: Double? = null,
    val lotLabel: String? = null,
    val needsPriceReview: Boolean = false
)

/**
 * Moteur d'extraction et d'intelligence produit (Product Intelligence)
 * Analyse les messages Telegram des grossistes/fournisseurs (multilingue : Français, Arabe, Anglais)
 * Extrait automatiquement le nom, la description, les prix d'achat, les variantes (pointures/tailles)
 * et les coordonnées de commande.
 */
object ProductIntelligenceEngine {

    /**
     * Convertit un message Telegram exploré en entité ProductEntity prête à être enregistrée en DRAFT.
     * Rapproche toutes les URLs de médias (photos/vidéos) captées.
     */
    fun extractFromTelegramMessage(
        message: TelegramMessageEntity,
        defaultCurrency: String = "MAD"
    ): Pair<ProductEntity, List<String>> {
        val extracted = parseProductText(
            rawText = message.text,
            channelTitle = message.channelTitle,
            defaultCurrency = defaultCurrency
        )
        val mediaUrls = message.getMediaUrls()
        val primaryImage = mediaUrls.firstOrNull() ?: message.mediaUrl

        val product = ProductEntity(
            id = "prod-${UUID.randomUUID().toString().take(8)}",
            sourceTelegramMessageId = message.messageId,
            sourceChannelTitle = message.channelTitle,
            title = extracted.title,
            description = extracted.description,
            purchasePrice = extracted.purchasePrice,
            sellingPrice = extracted.suggestedSellingPrice,
            currency = extracted.currency,
            stockQuantity = 10,
            primaryImageUrl = primaryImage,
            status = if (extracted.needsPriceReview) "NEEDS_PRICE_REVIEW" else "DRAFT",
            isPublishedToWebsite = false,
            isLotOrPackPrice = extracted.isLotOrPackPrice,
            lotQuantity = extracted.lotQuantity,
            lotTotalPrice = extracted.lotTotalPrice,
            lotUnitPriceEstimate = extracted.lotUnitPriceEstimate,
            lotLabel = extracted.lotLabel,
            needsPriceReview = extracted.needsPriceReview,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        return Pair(product, mediaUrls)
    }

    /**
     * Analyse en profondeur le texte d'un message grossiste en arabe, français ou anglais.
     */
    fun parseProductText(
        rawText: String,
        channelTitle: String = "Fournisseur",
        defaultCurrency: String = "MAD"
    ): ExtractedProductData {
        if (rawText.isBlank()) {
            return ExtractedProductData(
                title = "Arrivage $channelTitle",
                description = "",
                purchasePrice = null,
                currency = defaultCurrency,
                suggestedSellingPrice = null,
                variantsOrSizes = null,
                contactOrOrderLink = null
            )
        }

        // Normalisation des chiffres arabes orientaux (٠١٢٣٤٥٦٧٨٩ -> 0123456789)
        val normalizedText = normalizeArabicDigits(rawText)

        // Extraction Prix et Devise
        val priceResult = extractPriceAndCurrency(normalizedText, defaultCurrency)

        // Extraction Variantes / Tailles / Pointures
        val variants = extractVariantsOrSizes(normalizedText)

        // Extraction Contact / Lien de commande
        val contactLink = extractContactOrLink(normalizedText)

        // Extraction Titre propre
        val title = extractCleanTitle(normalizedText, channelTitle)

        // Prix de vente suggéré (marge raisonnable calculée automatiquement)
        val suggestedSelling = priceResult.price?.let { p ->
            val markup = when {
                p <= 50.0 -> 1.50   // +50% pour petits articles
                p <= 200.0 -> 1.35  // +35% standard
                p <= 500.0 -> 1.30  // +30%
                else -> 1.25        // +25% gros articles
            }
            val rawCalculated = p * markup
            // Arrondi propre (ex: 121.5 -> 125, 89.2 -> 90)
            if (rawCalculated >= 20) Math.ceil(rawCalculated / 5.0) * 5.0 else Math.ceil(rawCalculated)
        }

        // Nettoyage de la description
        val description = buildCleanDescription(normalizedText, variants, contactLink)

        return ExtractedProductData(
            title = title,
            description = description,
            purchasePrice = priceResult.price,
            currency = priceResult.currency,
            suggestedSellingPrice = suggestedSelling,
            variantsOrSizes = variants,
            contactOrOrderLink = contactLink,
            isLotOrPackPrice = priceResult.isLot,
            lotQuantity = priceResult.lotQuantity,
            lotTotalPrice = priceResult.lotTotalPrice,
            lotUnitPriceEstimate = priceResult.lotUnitPriceEstimate,
            lotLabel = priceResult.lotLabel,
            needsPriceReview = priceResult.needsPriceReview
        )
    }

    /**
     * Remplace les chiffres arabes orientaux par des chiffres latins pour parsing fiable
     */
    private fun normalizeArabicDigits(text: String): String {
        var res = text
        val arabicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
        for (i in arabicDigits.indices) {
            res = res.replace(arabicDigits[i], ('0'.code + i).toChar())
        }
        return res
    }

    data class PriceDetectionResult(
        val price: Double?,
        val currency: String,
        val isLot: Boolean = false,
        val lotQuantity: Int? = null,
        val lotTotalPrice: Double? = null,
        val lotUnitPriceEstimate: Double? = null,
        val lotLabel: String? = null,
        val needsPriceReview: Boolean = false
    )

    /**
     * Extrait le prix fournisseur et la devise détectée (avec support arabe poussé et gestion avancée des prix au lot/colis)
     * Gère notamment les formats comme "💰150 :الثمن dh colis 24pcs", "كولي 24 حبة ب 150 درهم", "prix 150 dh carton 24pcs"
     */
    private fun extractPriceAndCurrency(text: String, defaultCurrency: String): PriceDetectionResult {
        // 1. Détection préalable d'information de lot / colis / carton (avant ou après le prix)
        val lotInfo = detectLotInfo(text)

        // 2. Extraction du montant numérique du prix
        // Patterns multilingues avec support de l'ordre inversé (ex: "150 :الثمن" ou "الثمن: 150")
        val pricePatterns = listOf(
            // Format inversé ou standard avec préfixe/suffixe arabe : "150 :الثمن dh", "💰150 :الثمن", "الثمن: 150 dh"
            Pattern.compile("""([0-9]+(?:[\.,][0-9]+)?)\s*[:：=\-]?\s*(?:الثمن|السعر|ثمن|سعر)\s*(?:درهم|دراهم|د\.م|دم|dh|DH|MAD)?""", Pattern.CASE_INSENSITIVE),
            Pattern.compile("""(?:الثمن|السعر|ثمن|سعر)\s*[:：=\-]?\s*([0-9]+(?:[\.,][0-9]+)?)\s*(?:درهم|دراهم|د\.م|دم|dh|DH|MAD)?""", Pattern.CASE_INSENSITIVE),
            Pattern.compile("""(?:بـ|فقط بـ|ب)\s*([0-9]+(?:[\.,][0-9]+)?)\s*(?:درهم|دراهم|د\.م|دم|dh|DH|MAD)""", Pattern.CASE_INSENSITIVE),
            Pattern.compile("""(?:prix|tarif|coût|cout|price)\s*[:：=\-]?\s*([0-9]+(?:[\s\.][0-9]{3})*(?:,[0-9]+)?)\s*(?:€|eur|fcfa|cfa|f\s*cfa|dh|mad|\$|usd)?""", Pattern.CASE_INSENSITIVE),
            Pattern.compile("""([0-9]+(?:[\.,][0-9]+)?)\s*(?:درهم|دراهم|د\.م|دم|dh|DH|MAD)""", Pattern.CASE_INSENSITIVE),
            Pattern.compile("""([0-9]+(?:[\s\.][0-9]{3})*(?:,[0-9]+)?)\s*(?:€|eur|fcfa|cfa|f\s*cfa|dh|mad|\$|usd)""", Pattern.CASE_INSENSITIVE)
        )

        var detectedPrice: Double? = null
        var detectedCurrency = defaultCurrency

        for (pattern in pricePatterns) {
            val matcher = pattern.matcher(text)
            if (matcher.find()) {
                val numRaw = matcher.group(1)?.replace(" ", "")?.replace(",", ".")?.trim()
                val parsed = numRaw?.toDoubleOrNull()
                if (parsed != null && parsed > 0.0) {
                    detectedPrice = parsed
                    val matchedSegment = matcher.group(0)?.lowercase(Locale.getDefault()) ?: ""
                    detectedCurrency = when {
                        matchedSegment.contains("درهم") || matchedSegment.contains("د.م") || matchedSegment.contains("دم") ||
                                matchedSegment.contains("dh") || matchedSegment.contains("mad") -> "MAD"
                        matchedSegment.contains("fcfa") || matchedSegment.contains("cfa") -> "FCFA"
                        matchedSegment.contains("€") || matchedSegment.contains("eur") -> "EUR"
                        matchedSegment.contains("$") || matchedSegment.contains("usd") -> "USD"
                        else -> defaultCurrency
                    }
                    break
                }
            }
        }

        // 3. Si un lot / colis est identifié
        if (lotInfo != null && detectedPrice != null) {
            val qty = lotInfo.quantity
            val unitPrice = if (qty != null && qty > 0) {
                Math.round((detectedPrice / qty) * 100.0) / 100.0
            } else null

            val label = if (qty != null) {
                "${lotInfo.keyword} de $qty pièces (Total: $detectedPrice $detectedCurrency, ~${unitPrice ?: "?"} $detectedCurrency/pc)"
            } else {
                "Prix au ${lotInfo.keyword.lowercase(Locale.getDefault())} (Total: $detectedPrice $detectedCurrency)"
            }

            return PriceDetectionResult(
                price = unitPrice ?: detectedPrice,
                currency = detectedCurrency,
                isLot = true,
                lotQuantity = qty,
                lotTotalPrice = detectedPrice,
                lotUnitPriceEstimate = unitPrice,
                lotLabel = label,
                needsPriceReview = (qty == null) // A vérifier si la quantité par colis n'a pas pu être extraite
            )
        }

        // 4. Si prix standard détecté sans lot
        if (detectedPrice != null) {
            return PriceDetectionResult(
                price = detectedPrice,
                currency = detectedCurrency,
                isLot = false,
                needsPriceReview = false
            )
        }

        // 5. Pas de prix formellement détecté (marquage "Prix à vérifier")
        val mentionsPriceQuery = text.contains("prix", ignoreCase = true) ||
                text.contains("tarif", ignoreCase = true) ||
                text.contains("الثمن") ||
                text.contains("السعر") ||
                text.contains("كم الثمن") ||
                text.contains("بشحال")

        return PriceDetectionResult(
            price = null,
            currency = defaultCurrency,
            isLot = false,
            needsPriceReview = mentionsPriceQuery
        )
    }

    private data class LotExtraction(val quantity: Int?, val keyword: String)

    /**
     * Recherche la présence de mots-clés de lot/colis/carton et extrait la quantité associée
     */
    private fun detectLotInfo(text: String): LotExtraction? {
        val lotPatterns = listOf(
            // colis 24pcs / carton 24 pcs / pack 12 pcs / lot de 10 pièces
            Pattern.compile("""(?:colis|carton|pack|lot|كولي|كرطونة|كرتونة|كولية|باك|لوط)\s*(?:de)?\s*([0-9]+)\s*(?:pcs|pc|pièces|pieces|u|unités|بياسة|حبة|قطعة|بياسات)?""", Pattern.CASE_INSENSITIVE),
            // 24pcs colis / 24 pcs carton / 24 حبة كولي
            Pattern.compile("""([0-9]+)\s*(?:pcs|pc|pièces|pieces|u|unités|بياسة|حبة|قطعة|بياسات)\s*(?:de)?\s*(?:colis|carton|pack|lot|كولي|كرطونة|كرتونة|كولية|باك|لوط)""", Pattern.CASE_INSENSITIVE),
            // 24pcs / 24 pcs (si colis ou carton mentionné dans le message)
            Pattern.compile("""([0-9]+)\s*(?:pcs|pièces|pieces|بياسة|حبة|قطعة)""", Pattern.CASE_INSENSITIVE)
        )

        val hasKeyword = text.contains("colis", ignoreCase = true) ||
                text.contains("carton", ignoreCase = true) ||
                text.contains("pack", ignoreCase = true) ||
                text.contains("lot", ignoreCase = true) ||
                text.contains("كولي") ||
                text.contains("كرطونة") ||
                text.contains("كرتونة") ||
                text.contains("كولية") ||
                text.contains("باك") ||
                text.contains("لوط")

        for (pattern in lotPatterns) {
            val matcher = pattern.matcher(text)
            if (matcher.find()) {
                val qty = matcher.group(1)?.toIntOrNull()
                if (qty != null && qty > 1) {
                    val kw = when {
                        text.contains("كولي") || text.contains("colis", ignoreCase = true) -> "Colis"
                        text.contains("كرطونة") || text.contains("كرتونة") || text.contains("carton", ignoreCase = true) -> "Carton"
                        text.contains("pack", ignoreCase = true) || text.contains("باك") -> "Pack"
                        else -> "Lot"
                    }
                    return LotExtraction(quantity = qty, keyword = kw)
                }
            }
        }

        if (hasKeyword) {
            val kw = when {
                text.contains("كولي") || text.contains("colis", ignoreCase = true) -> "Colis"
                text.contains("كرطونة") || text.contains("كرتونة") || text.contains("carton", ignoreCase = true) -> "Carton"
                text.contains("pack", ignoreCase = true) || text.contains("باك") -> "Pack"
                else -> "Lot"
            }
            return LotExtraction(quantity = null, keyword = kw)
        }

        return null
    }

    /**
     * Extrait les variantes (tailles, pointures, coloris)
     */
    private fun extractVariantsOrSizes(text: String): String? {
        val lines = text.lines()
        val variantKeywords = listOf(
            "المقاسات", "الأحجام", "القياسات", "المقاس", "الاحجام", "القياس",
            "tailles", "pointures", "taille", "pointure", "sizes", "size", "colors", "couleurs"
        )

        for (line in lines) {
            val trimmed = line.trim()
            val lower = trimmed.lowercase(Locale.getDefault())
            for (kw in variantKeywords) {
                if (lower.contains(kw)) {
                    // Nettoyer la ligne pour isoler la valeur
                    val cleaned = trimmed
                        .replace(Pattern.compile("""^[\s\-*•👉⚡✅🔹]+""").toRegex(), "")
                        .trim()
                    if (cleaned.length >= 3) {
                        return cleaned
                    }
                }
            }
        }

        // Recherche par pattern regex inline (ex: "36 à 41", "38-44", "S M L XL")
        val sizeRangePattern = Pattern.compile("""(?:من\s*)?([0-9]{2})\s*(?:إلى|a|à|-)\s*([0-9]{2})""", Pattern.CASE_INSENSITIVE)
        val matcher = sizeRangePattern.matcher(text)
        if (matcher.find()) {
            return "Pointures ${matcher.group(1)} à ${matcher.group(2)}"
        }

        return null
    }

    /**
     * Extrait un numéro de téléphone ou lien de commande
     */
    private fun extractContactOrLink(text: String): String? {
        // 1. Lien WhatsApp wa.me ou http
        val linkPattern = Pattern.compile("""(https?://[^\s]+|wa\.me/[0-9+]+)""", Pattern.CASE_INSENSITIVE)
        val linkMatcher = linkPattern.matcher(text)
        if (linkMatcher.find()) {
            return linkMatcher.group(1)
        }

        // 2. Numéro de téléphone marocain ou international
        val phonePattern = Pattern.compile("""(?:(?:\+|00)[0-9]{1,3}[-. ]?)?(?:0[5-7][0-9]{8}|[0-9]{2}[-. ]?[0-9]{2}[-. ]?[0-9]{2}[-. ]?[0-9]{2})""")
        val phoneMatcher = phonePattern.matcher(text)
        if (phoneMatcher.find()) {
            return phoneMatcher.group(0)?.trim()
        }

        return null
    }

    /**
     * Isole un titre descriptif et concis à partir du message
     */
    private fun extractCleanTitle(text: String, channelTitle: String): String {
        val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() }
        val promoBadges = listOf(
            "nouvel arrivage", "arrivage", "nouveaute", "nouveauté", "promo", "offre spéciale",
            "promotion", "bon plan", "جديد", "عرض خاص", "تخفيضات", "سلعة جديدة", "عرض حصري",
            "حصريا", "السلام عليكم", "bonjour", "salut"
        )

        for (line in lines) {
            val clean = line
                .replace(Pattern.compile("""^[\s\-*•👉⚡🔥💥✅🔹✨🛍️👟👕👗👖📦]+""").toRegex(), "")
                .replace(Pattern.compile("""[\s\-*•👉⚡🔥💥✅🔹✨🛍️👟👕👗👖📦]+$""").toRegex(), "")
                .trim()

            val lower = clean.lowercase(Locale.getDefault())

            // Ignorer les lignes de salutation ou de pur badge promo
            val isPureBadge = promoBadges.any { lower == it || lower == "$it :" || lower == "$it:" }
            val isPricingLine = lower.contains("الثمن") || lower.contains("السعر") || lower.startsWith("prix") || lower.startsWith("tarif")

            if (clean.length in 4..70 && !isPureBadge && !isPricingLine && !clean.startsWith("http") && !clean.startsWith("wa.me")) {
                return clean
            }
        }

        // Si la première ligne était un peu longue, découper la première phrase
        if (lines.isNotEmpty()) {
            val firstLine = lines.first()
                .replace(Pattern.compile("""^[\s\-*•👉⚡🔥💥✅🔹✨🛍️📦]+""").toRegex(), "")
                .trim()
            val cut = firstLine.split(Pattern.compile("""[,;.\-—|]""")).firstOrNull()?.trim() ?: ""
            if (cut.length in 4..60) {
                return cut
            }
            return firstLine.take(50).trim()
        }

        return "Arrivage $channelTitle"
    }

    /**
     * Reconstitue une description propre pour la fiche produit
     */
    private fun buildCleanDescription(rawText: String, variants: String?, contactLink: String?): String {
        val builder = StringBuilder()
        val lines = rawText.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }

        builder.append(lines.joinToString("\n"))

        if (!variants.isNullOrBlank() && !rawText.contains(variants)) {
            builder.append("\n\n• Variantes / Tailles : ").append(variants)
        }
        if (!contactLink.isNullOrBlank() && !rawText.contains(contactLink)) {
            builder.append("\n• Contact fournisseur : ").append(contactLink)
        }

        return builder.toString().trim()
    }
}
