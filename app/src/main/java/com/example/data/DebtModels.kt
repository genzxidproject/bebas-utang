package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.NumberFormat
import java.util.Locale

enum class NotificationChannelMethod(val code: String, val label: String) {
    PUSH_ONLY("PUSH", "Push Notification"),
    EMAIL_ONLY("EMAIL", "Email"),
    PUSH_AND_EMAIL("PUSH,EMAIL", "Push & Email");

    val includesPush: Boolean get() = code.contains("PUSH")
    val includesEmail: Boolean get() = code.contains("EMAIL")

    companion object {
        fun fromCode(code: String): NotificationChannelMethod {
            return entries.find { it.code.equals(code, ignoreCase = true) } ?: PUSH_AND_EMAIL
        }
    }
}

data class NotificationPreferences(
    val masterEnabled: Boolean = true,
    val pushEnabled: Boolean = true,
    val emailEnabled: Boolean = true,
    val recipientEmail: String = "arrprojectx@gmail.com",
    val activeTimingsDays: Set<Int> = setOf(1, 3, 7), // H-1, H-3, H-7
    val reminderHour: Int = 9,
    val reminderMinute: Int = 0
) {
    val methodLabel: String
        get() = when {
            pushEnabled && emailEnabled -> "Push Notification & Email"
            pushEnabled -> "Push Notification Saja"
            emailEnabled -> "Email Saja"
            else -> "Nonaktif"
        }

    val timingsSummary: String
        get() = if (activeTimingsDays.isEmpty()) {
            "Tidak ada"
        } else {
            activeTimingsDays.sorted().joinToString(", ") { if (it == 0) "Hari-H" else "H-$it" }
        }
}

data class ReminderDispatchLog(
    val id: Long = System.currentTimeMillis(),
    val creditorName: String,
    val channel: String, // "Push Notification" or "Email"
    val timingTag: String, // "H-1", "H-3", "H-7"
    val recipientOrTarget: String,
    val messageSummary: String,
    val timestampMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "debts")
data class DebtEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val creditorName: String,
    val category: String, // "Pinjol", "Kartu Kredit", "Paylater", "KPR & Kendaraan", "Pinjaman Pribadi"
    val totalAmount: Long,
    val paidAmount: Long,
    val monthlyInstallment: Long,
    val interestRatePercent: Double,
    val dueDayOfMonth: Int,
    val nextDueDateMillis: Long,
    val isPaidOff: Boolean,
    val encryptedNotes: String,
    val priorityOrder: Int,
    val reminderEnabled: Boolean = true,
    val reminderDaysBefore: Int = 3,
    val reminderTimingsCsv: String = "1,3,7", // Supports multiple H-1, H-3, H-7
    val reminderMethodCode: String = "PUSH,EMAIL", // "PUSH", "EMAIL", "PUSH,EMAIL"
    val updatedAt: Long = System.currentTimeMillis()
) {
    val remainingAmount: Long
        get() = (totalAmount - paidAmount).coerceAtLeast(0L)

    val progressFraction: Float
        get() = if (totalAmount <= 0L) 1f else (paidAmount.toFloat() / totalAmount.toFloat()).coerceIn(0f, 1f)

    val parsedTimings: Set<Int>
        get() = reminderTimingsCsv.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .toSet()
            .ifEmpty { setOf(reminderDaysBefore) }

    val channelMethod: NotificationChannelMethod
        get() = NotificationChannelMethod.fromCode(reminderMethodCode)
}

@Entity(tableName = "monthly_expenses")
data class MonthlyExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String, // "Cicilan Utang", "Kebutuhan Pokok", "Utilitas & Tagihan", "Transportasi", "Lainnya"
    val amount: Long,
    val monthKey: String, // e.g. "2026-10"
    val monthLabel: String, // e.g. "Okt 2026"
    val isAutoSyncedFromDebt: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "payment_history")
data class PaymentHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val debtId: Long,
    val creditorName: String,
    val category: String,
    val amountPaid: Long,
    val paidAtMillis: Long = System.currentTimeMillis(),
    val encryptedReceiptNote: String = ""
)

object CurrencyFormatter {
    private val indonesiaLocale = Locale("id", "ID")

    fun formatRupiah(amount: Long): String {
        val formatter = NumberFormat.getNumberInstance(indonesiaLocale)
        return "Rp${formatter.format(amount)}"
    }

    fun formatCompactRupiah(amount: Long): String {
        return when {
            amount >= 1_000_000_000L -> String.format(indonesiaLocale, "Rp%.1f M", amount / 1_000_000_000.0)
            amount >= 1_000_000L -> String.format(indonesiaLocale, "Rp%.1f Jt", amount / 1_000_000.0)
            amount >= 1_000L -> String.format(indonesiaLocale, "Rp%d Rb", amount / 1_000L)
            else -> formatRupiah(amount)
        }
    }
}
