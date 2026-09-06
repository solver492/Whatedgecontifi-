package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object PriceFormatter {

    /**
     * Formate un montant numérique selon la devise et le séparateur configurés.
     * Exemple : 25000.0, "MAD" -> "25 000 MAD"
     */
    fun format(
        amount: Double?,
        currency: String = "MAD",
        thousandsSeparator: String = " "
    ): String {
        if (amount == null) return "Non fixé"

        val symbols = DecimalFormatSymbols(Locale.FRENCH).apply {
            groupingSeparator = if (thousandsSeparator.isNotEmpty()) thousandsSeparator[0] else ' '
            decimalSeparator = ','
        }

        val pattern = if (amount % 1.0 == 0.0) "#,##0" else "#,##0.00"
        val df = DecimalFormat(pattern, symbols)
        val formattedNumber = df.format(amount)

        return "$formattedNumber $currency"
    }

    /**
     * Formate un prix d'achat avec libellé clair
     */
    fun formatPurchase(
        amount: Double?,
        currency: String = "MAD",
        thousandsSeparator: String = " "
    ): String {
        if (amount == null) return "Achat: Inconnu"
        return "Achat: " + format(amount, currency, thousandsSeparator)
    }
}
