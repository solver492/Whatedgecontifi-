package com.example.domain.baileys

import com.example.data.local.entity.ConversationAgentOverrideEntity

/**
 * Utilitaire de normalisation et de comparaison des numéros de téléphone WhatsApp / JID.
 * Garantit qu'une désactivation ou assignation pour un client donné n'impacte JAMAIS
 * d'autres numéros clients ni l'instance entière.
 */
object PhoneNumberUtils {

    /**
     * Extrait les chiffres stricts d'un numéro ou JID WhatsApp.
     * Exemples :
     *   "+33 7 73 16 37 72" -> "33773163772"
     *   "33773163772@s.whatsapp.net" -> "33773163772"
     *   "0612345678" -> "0612345678"
     */
    fun extractCleanDigits(input: String?): String {
        if (input.isNullOrBlank()) return ""
        val beforeAt = input.substringBefore("@")
        return beforeAt.replace(Regex("[^0-9]"), "").trim()
    }

    /**
     * Convertit un numéro brut ou JID en JID WhatsApp canonique.
     * Exemple : "+33 7 73 16 37 72" -> "33773163772@s.whatsapp.net"
     */
    fun toCanonicalJid(input: String?): String {
        if (input.isNullOrBlank()) return ""
        val digits = extractCleanDigits(input)
        return if (digits.length >= 6) {
            "$digits@s.whatsapp.net"
        } else {
            input.trim()
        }
    }

    /**
     * Compare deux identifiants de contact (numéros ou JID) avec robustesse.
     * Deux numéros ne correspondent QUE s'ils représentent le même contact physique.
     */
    fun areMatching(contactA: String?, contactB: String?): Boolean {
        if (contactA.isNullOrBlank() || contactB.isNullOrBlank()) return false
        val cleanA = contactA.trim()
        val cleanB = contactB.trim()

        if (cleanA.equals(cleanB, ignoreCase = true)) return true

        val digitsA = extractCleanDigits(cleanA)
        val digitsB = extractCleanDigits(cleanB)

        // Si l'un des deux n'a pas de chiffres significatifs (ex: identifiant symbolique court), comparaison stricte
        if (digitsA.length < 6 || digitsB.length < 6) {
            return false
        }

        // Correspondance exacte des chiffres
        if (digitsA == digitsB) return true

        // Gestion de l'indicatif international avec ou sans '0' initial national
        // Ex: 33612345678 vs 0612345678
        val nationalA = if (digitsA.startsWith("0")) digitsA.substring(1) else ""
        val nationalB = if (digitsB.startsWith("0")) digitsB.substring(1) else ""

        if (nationalA.isNotEmpty() && digitsB.endsWith(nationalA) && digitsB.length > nationalA.length) return true
        if (nationalB.isNotEmpty() && digitsA.endsWith(nationalB) && digitsA.length > nationalB.length) return true

        return false
    }

    /**
     * Recherche l'override spécifique (mode humain, agent forcé) pour un contact précis.
     * Ne produit aucun faux-positif sur les autres contacts.
     */
    fun findOverride(
        overrides: List<ConversationAgentOverrideEntity>,
        contactJid: String?
    ): ConversationAgentOverrideEntity? {
        if (contactJid.isNullOrBlank() || overrides.isEmpty()) return null
        val targetDigits = extractCleanDigits(contactJid)
        val targetCanonical = toCanonicalJid(contactJid)

        // 1. Recherche par correspondance exacte JID
        overrides.firstOrNull { it.remoteJid.equals(targetCanonical, ignoreCase = true) }?.let { return it }
        overrides.firstOrNull { it.remoteJid.equals(contactJid.trim(), ignoreCase = true) }?.let { return it }

        // 2. Recherche par chiffres stricts (longueur >= 6)
        if (targetDigits.length >= 6) {
            overrides.firstOrNull { override ->
                val overrideDigits = extractCleanDigits(override.remoteJid)
                overrideDigits.length >= 6 && areMatching(targetDigits, overrideDigits)
            }?.let { return it }
        }

        return null
    }
}
