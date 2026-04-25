package com.example.mymobileproject.core.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyUtils {
    private val thaiFormat = NumberFormat.getNumberInstance(Locale("th", "TH")).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 2
    }

    fun formatBaht(amount: Double): String {
        return "฿${thaiFormat.format(amount)}"
    }

    fun formatBahtSigned(amount: Double): String {
        val sign = if (amount >= 0) "+" else ""
        return "$sign฿${thaiFormat.format(amount)}"
    }

    fun formatCompact(amount: Double): String {
        return when {
            amount >= 1_000_000 -> "฿${String.format("%.1fM", amount / 1_000_000)}"
            amount >= 1_000 -> "฿${String.format("%.1fK", amount / 1_000)}"
            else -> formatBaht(amount)
        }
    }
}
