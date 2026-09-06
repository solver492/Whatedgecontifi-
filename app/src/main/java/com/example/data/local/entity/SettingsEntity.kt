package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val id: String = "global_settings",

    // B1: Devise (MAD par défaut pour l'usage réel)
    val currency: String = "MAD",
    val currencySymbol: String = "DH",

    // B2: Localisation & Indicatif par défaut (Maroc +212 par défaut)
    val defaultCountryCode: String = "+212",
    val countryName: String = "Maroc",
    val timeZone: String = "Africa/Casablanca",

    // B3: Paramètres E-Commerce & IA
    val defaultProfitMarginPercent: Double = 40.0,
    val lowStockThreshold: Int = 5,
    val numberFormatThousandsSeparator: String = " ",
    val numberFormatDecimalSeparator: String = ",",

    // B3: Sécurité & Clés d'API (masquables dans l'UI)
    val geminiApiKey: String = "",
    val geminiModel: String = "gemini-2.5-flash",
    val supabaseUrl: String = "",
    val supabaseAnonKey: String = "",
    val huggingFaceToken: String = "",
    val telegramApiId: String = "",
    val telegramApiHash: String = "",

    // B3: Passerelles & Ports réseau
    val baileysPort: Int = 8080,
    val telethonPort: Int = 8088,

    // Notifications
    val notifyNewOrders: Boolean = true,
    val notifyTelegramProducts: Boolean = true,
    val notifyLowStock: Boolean = true,

    val updatedAt: Long = System.currentTimeMillis()
)
