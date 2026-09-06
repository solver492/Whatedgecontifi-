package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entité de contrôle fin par conversation WhatsApp.
 * Permet d'activer/désactiver les agents IA pour une discussion spécifique (remoteJid),
 * ou de forcer un agent IA en particulier (ou désactiver tout agent pour passer en manuel).
 */
@Entity(tableName = "conversation_agent_overrides")
data class ConversationAgentOverrideEntity(
    @PrimaryKey val remoteJid: String, // ex: "33712345678@s.whatsapp.net"
    val contactName: String = "",
    val isAiEnabled: Boolean = true, // Si faux: aucun agent IA ne répondra automatiquement à ce contact
    val forcedAgentId: String? = null, // ID spécifique d'un agent pour cette conversation (null = routage auto)
    val updatedAt: Long = System.currentTimeMillis()
)
