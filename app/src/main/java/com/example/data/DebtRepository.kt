package com.example.data

import android.content.Context
import com.example.notification.ReminderScheduler
import com.example.security.CryptoManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.Calendar

class DebtRepository(
    private val context: Context,
    private val dao: DebtDao
) {
    val allDebts: Flow<List<DebtEntity>> = dao.observeAllDebts()
    val allExpenses: Flow<List<MonthlyExpenseEntity>> = dao.observeAllExpenses()
    val paymentHistory: Flow<List<PaymentHistoryEntity>> = dao.observePaymentHistory()

    suspend fun ensureSeeded(passphrase: String) {
        if (dao.getDebtCount() == 0) {
            seedRickyPortfolio(passphrase)
        }
    }

    suspend fun resetToDemoData(passphrase: String) {
        dao.clearAllDebts()
        dao.clearAllExpenses()
        dao.clearAllPaymentHistory()
        seedRickyPortfolio(passphrase)
    }

    private suspend fun seedRickyPortfolio(passphrase: String) {
        val now = System.currentTimeMillis()
        val seededDebts = mutableListOf<DebtEntity>()

        // 1. 5 Paid-Off Debts ("5 dari 45 Utang Lunas")
        // Total paid across these 5 = Rp34.337.390
        val paidOffTemplates = listOf(
            Triple("Kredivo Prioritas #1", 9_500_000L, "Lunas lebih awal - Kontrak KRD-8821"),
            Triple("Akulaku Cicilan #1", 8_200_000L, "Lunas tepat waktu - Kontrak AKL-3319"),
            Triple("Shopee SPinjam #1", 6_400_000L, "Lunas pokok & bunga - Kontrak SPJ-1029"),
            Triple("AdaKami Reguler #1", 5_800_000L, "Lunas bebas denda - Kontrak ADK-5540"),
            Triple("Julo Cicilan Pintar #1", 4_437_390L, "Lunas penutupan akun - Kontrak JLO-9012")
        )
        paidOffTemplates.forEachIndexed { idx, (name, amount, rawNote) ->
            seededDebts.add(
                DebtEntity(
                    creditorName = name,
                    category = "Pinjol",
                    totalAmount = amount,
                    paidAmount = amount,
                    monthlyInstallment = (amount / 6).coerceAtLeast(500_000L),
                    interestRatePercent = 2.4,
                    dueDayOfMonth = (idx * 4 + 5).coerceIn(1, 28),
                    nextDueDateMillis = computeNextDueMillis((idx * 4 + 5).coerceIn(1, 28)),
                    isPaidOff = true,
                    encryptedNotes = CryptoManager.encryptText(rawNote, passphrase),
                    priorityOrder = 40 + idx,
                    reminderEnabled = false,
                    reminderDaysBefore = 3,
                    reminderTimingsCsv = "1,3,7",
                    reminderMethodCode = "PUSH,EMAIL",
                    updatedAt = now - (idx + 1) * 86_400_000L
                )
            )
        }

        // 2. 40 Active Pinjol Debts ("*40 Utang aktif")
        // Remaining balance across all 40 active debts = exact Rp146.735.070
        val pinjolProviders = listOf(
            "Shopee SPinjam", "Kredivo", "Akulaku", "AdaKami", "Easycash",
            "Julo Kredit", "Indodana", "GopayPinjam", "Kredit Pintar", "Tunaiku Amar",
            "Home Credit", "Rupiah Cepat", "Uatas Pinjol", "BantuSaku", "Kredito",
            "Maucash Astra", "PinjamYuk", "Finmas", "Cairin", "Singa Fintech"
        )

        for (i in 0 until 40) {
            val providerName = pinjolProviders[i % pinjolProviders.size]
            val batchNum = (i / pinjolProviders.size) + 1
            val creditorTitle = "$providerName #$batchNum"
            val remaining = if (i == 0) 4_385_070L else 3_650_000L
            val alreadyPaid = if (i < 12) 1_000_000L else 0L
            val total = remaining + alreadyPaid
            val installment = when {
                i == 0 -> 1_150_000L
                i % 3 == 0 -> 950_000L
                i % 2 == 0 -> 750_000L
                else -> 610_000L
            }
            val dueDay = ((i * 2 + 24) % 28) + 1
            val interest = 1.8 + (i % 6) * 0.45
            val notePlain = "Kontrak E2EE #${1040 + i} • Pokok Rp${remaining / 1000}rb • Tenor sisa ${(remaining / installment).coerceAtLeast(1)} bln"
            val defaultTimings = when {
                i < 5 -> "1,3,7"
                i % 2 == 0 -> "1,3"
                else -> "3,7"
            }
            val defaultMethod = when {
                i % 4 == 0 -> "PUSH,EMAIL"
                i % 4 == 1 -> "PUSH"
                i % 4 == 2 -> "PUSH,EMAIL"
                else -> "EMAIL"
            }

            seededDebts.add(
                DebtEntity(
                    creditorName = creditorTitle,
                    category = "Pinjol",
                    totalAmount = total,
                    paidAmount = alreadyPaid,
                    monthlyInstallment = installment,
                    interestRatePercent = (Math.round(interest * 10.0) / 10.0),
                    dueDayOfMonth = dueDay,
                    nextDueDateMillis = computeNextDueMillis(dueDay),
                    isPaidOff = false,
                    encryptedNotes = CryptoManager.encryptText(notePlain, passphrase),
                    priorityOrder = i + 1,
                    reminderEnabled = true,
                    reminderDaysBefore = if (i < 5) 1 else 3,
                    reminderTimingsCsv = defaultTimings,
                    reminderMethodCode = defaultMethod,
                    updatedAt = now - i * 3_600_000L
                )
            )
        }

        dao.insertDebts(seededDebts)

        // 3. Seed 6 months of Monthly Expense Statistics for the Laporan Dashboard
        val monthlyExpenses = listOf(
            MonthlyExpenseEntity(title = "Alokasi Cicilan Utang Pinjol", category = "Cicilan Utang", amount = 8_450_000L, monthKey = "2026-10", monthLabel = "Okt", isAutoSyncedFromDebt = true),
            MonthlyExpenseEntity(title = "Belanja Dapur & Kebutuhan Pokok", category = "Kebutuhan Pokok", amount = 3_200_000L, monthKey = "2026-10", monthLabel = "Okt"),
            MonthlyExpenseEntity(title = "Listrik PLN, Air PDAM & Wi-Fi", category = "Utilitas & Tagihan", amount = 1_150_000L, monthKey = "2026-10", monthLabel = "Okt"),
            MonthlyExpenseEntity(title = "BBM, KRL & Transportasi Kerja", category = "Transportasi", amount = 850_000L, monthKey = "2026-10", monthLabel = "Okt"),
            MonthlyExpenseEntity(title = "Kesehatan & Dana Darurat", category = "Lainnya", amount = 600_000L, monthKey = "2026-10", monthLabel = "Okt"),
            MonthlyExpenseEntity(title = "Cicilan Utang Bulanan", category = "Cicilan Utang", amount = 8_900_000L, monthKey = "2026-09", monthLabel = "Sep", isAutoSyncedFromDebt = true),
            MonthlyExpenseEntity(title = "Kebutuhan Pokok & Rumah Tangga", category = "Kebutuhan Pokok", amount = 3_100_000L, monthKey = "2026-09", monthLabel = "Sep"),
            MonthlyExpenseEntity(title = "Listrik, Air & Kuota", category = "Utilitas & Tagihan", amount = 1_100_000L, monthKey = "2026-09", monthLabel = "Sep"),
            MonthlyExpenseEntity(title = "Transportasi Harian", category = "Transportasi", amount = 900_000L, monthKey = "2026-09", monthLabel = "Sep"),
            MonthlyExpenseEntity(title = "Cicilan Utang Bulanan", category = "Cicilan Utang", amount = 9_400_000L, monthKey = "2026-08", monthLabel = "Agu", isAutoSyncedFromDebt = true),
            MonthlyExpenseEntity(title = "Kebutuhan Pokok & Rumah Tangga", category = "Kebutuhan Pokok", amount = 3_350_000L, monthKey = "2026-08", monthLabel = "Agu"),
            MonthlyExpenseEntity(title = "Utilitas & Internet", category = "Utilitas & Tagihan", amount = 1_200_000L, monthKey = "2026-08", monthLabel = "Agu"),
            MonthlyExpenseEntity(title = "Cicilan Utang Bulanan", category = "Cicilan Utang", amount = 9_800_000L, monthKey = "2026-07", monthLabel = "Jul", isAutoSyncedFromDebt = true),
            MonthlyExpenseEntity(title = "Kebutuhan Pokok & Makan", category = "Kebutuhan Pokok", amount = 3_400_000L, monthKey = "2026-07", monthLabel = "Jul"),
            MonthlyExpenseEntity(title = "Tagihan Rumah & Transport", category = "Utilitas & Tagihan", amount = 1_850_000L, monthKey = "2026-07", monthLabel = "Jul"),
            MonthlyExpenseEntity(title = "Cicilan Utang Bulanan", category = "Cicilan Utang", amount = 10_200_000L, monthKey = "2026-06", monthLabel = "Jun", isAutoSyncedFromDebt = true),
            MonthlyExpenseEntity(title = "Pengeluaran Rumah Tangga", category = "Kebutuhan Pokok", amount = 3_600_000L, monthKey = "2026-06", monthLabel = "Jun"),
            MonthlyExpenseEntity(title = "Cicilan Utang Bulanan", category = "Cicilan Utang", amount = 10_750_000L, monthKey = "2026-05", monthLabel = "Mei", isAutoSyncedFromDebt = true),
            MonthlyExpenseEntity(title = "Pengeluaran Rumah Tangga", category = "Kebutuhan Pokok", amount = 3_800_000L, monthKey = "2026-05", monthLabel = "Mei")
        )
        dao.insertExpenses(monthlyExpenses)

        dao.insertPaymentHistory(
            PaymentHistoryEntity(
                debtId = 1,
                creditorName = "Julo Cicilan Pintar #1",
                category = "Pinjol",
                amountPaid = 4_437_390L,
                paidAtMillis = now - 86_400_000L * 2,
                encryptedReceiptNote = CryptoManager.encryptText("Pelunasan penuh via Virtual Account BCA", passphrase)
            )
        )

        ReminderScheduler.scheduleRemindersForDebts(context, seededDebts)
    }

    suspend fun addDebt(
        creditorName: String,
        category: String,
        totalAmount: Long,
        paidAmount: Long,
        monthlyInstallment: Long,
        interestRatePercent: Double,
        dueDayOfMonth: Int,
        rawNote: String,
        reminderEnabled: Boolean,
        reminderTimings: Set<Int>,
        reminderMethodCode: String,
        passphrase: String
    ) {
        val safeDueDay = dueDayOfMonth.coerceIn(1, 31)
        val encryptedNote = CryptoManager.encryptText(rawNote, passphrase)
        val isPaid = paidAmount >= totalAmount && totalAmount > 0L
        val currentCount = dao.getDebtCount()
        val sortedTimings = reminderTimings.sorted().ifEmpty { listOf(1, 3, 7) }
        val entity = DebtEntity(
            creditorName = creditorName.trim(),
            category = category,
            totalAmount = totalAmount,
            paidAmount = paidAmount.coerceAtMost(totalAmount),
            monthlyInstallment = monthlyInstallment,
            interestRatePercent = interestRatePercent,
            dueDayOfMonth = safeDueDay,
            nextDueDateMillis = computeNextDueMillis(safeDueDay),
            isPaidOff = isPaid,
            encryptedNotes = encryptedNote,
            priorityOrder = currentCount + 1,
            reminderEnabled = reminderEnabled,
            reminderDaysBefore = sortedTimings.first(),
            reminderTimingsCsv = sortedTimings.joinToString(","),
            reminderMethodCode = reminderMethodCode.ifBlank { "PUSH,EMAIL" }
        )
        dao.insertDebt(entity)
        ReminderScheduler.scheduleRemindersForDebts(context, dao.getAllDebtsOnce())
    }

    suspend fun makeInstallmentPayment(
        debt: DebtEntity,
        paymentAmount: Long,
        note: String,
        passphrase: String
    ) {
        val actualPayment = paymentAmount.coerceAtLeast(1L)
        val updatedPaid = (debt.paidAmount + actualPayment).coerceAtMost(debt.totalAmount)
        val nowPaidOff = updatedPaid >= debt.totalAmount
        val nextDue = if (nowPaidOff) debt.nextDueDateMillis else debt.nextDueDateMillis + 30L * 86_400_000L

        val updatedDebt = debt.copy(
            paidAmount = updatedPaid,
            isPaidOff = nowPaidOff,
            nextDueDateMillis = nextDue,
            updatedAt = System.currentTimeMillis()
        )
        dao.updateDebt(updatedDebt)

        dao.insertPaymentHistory(
            PaymentHistoryEntity(
                debtId = debt.id,
                creditorName = debt.creditorName,
                category = debt.category,
                amountPaid = actualPayment,
                paidAtMillis = System.currentTimeMillis(),
                encryptedReceiptNote = CryptoManager.encryptText(
                    note.ifBlank { "Pembayaran cicilan ${debt.creditorName}" },
                    passphrase
                )
            )
        )
        ReminderScheduler.scheduleRemindersForDebts(context, dao.getAllDebtsOnce())
    }

    suspend fun updateDebtReminderConfig(
        debt: DebtEntity,
        enabled: Boolean,
        timings: Set<Int>,
        methodCode: String,
        reminderHour: Int,
        recipientEmail: String
    ) {
        val sorted = timings.sorted().ifEmpty { listOf(1, 3, 7) }
        dao.updateDebt(
            debt.copy(
                reminderEnabled = enabled,
                reminderDaysBefore = sorted.first(),
                reminderTimingsCsv = sorted.joinToString(","),
                reminderMethodCode = methodCode,
                updatedAt = System.currentTimeMillis()
            )
        )
        ReminderScheduler.scheduleRemindersForDebts(
            context = context,
            debts = dao.getAllDebtsOnce(),
            reminderHour = reminderHour,
            recipientEmail = recipientEmail
        )
    }

    suspend fun applyGlobalReminderToAllDebts(
        enabled: Boolean,
        timings: Set<Int>,
        methodCode: String,
        reminderHour: Int,
        recipientEmail: String
    ) {
        val sorted = timings.sorted().ifEmpty { listOf(1, 3, 7) }
        val csv = sorted.joinToString(",")
        val current = dao.getAllDebtsOnce()
        val updated = current.map { debt ->
            if (debt.isPaidOff) debt else debt.copy(
                reminderEnabled = enabled,
                reminderDaysBefore = sorted.first(),
                reminderTimingsCsv = csv,
                reminderMethodCode = methodCode,
                updatedAt = System.currentTimeMillis()
            )
        }
        dao.insertDebts(updated)
        ReminderScheduler.scheduleRemindersForDebts(
            context = context,
            debts = updated,
            reminderHour = reminderHour,
            recipientEmail = recipientEmail
        )
    }

    suspend fun deleteDebt(debt: DebtEntity) {
        dao.deleteDebt(debt)
    }

    suspend fun addMonthlyExpense(
        title: String,
        category: String,
        amount: Long,
        monthKey: String = "2026-10",
        monthLabel: String = "Okt"
    ) {
        dao.insertExpense(
            MonthlyExpenseEntity(
                title = title.trim(),
                category = category,
                amount = amount,
                monthKey = monthKey,
                monthLabel = monthLabel
            )
        )
    }

    suspend fun deleteMonthlyExpense(expense: MonthlyExpenseEntity) {
        dao.deleteExpense(expense)
    }

    suspend fun exportEncryptedCloudSyncPacket(passphrase: String, deviceName: String): String {
        val debts = dao.getAllDebtsOnce()
        val expenses = dao.getAllExpensesOnce()

        val root = JSONObject()
        root.put("version", 2)
        root.put("device", deviceName)
        root.put("syncedAt", System.currentTimeMillis())

        val debtsArray = JSONArray()
        debts.forEach { d ->
            val obj = JSONObject().apply {
                put("creditorName", d.creditorName)
                put("category", d.category)
                put("totalAmount", d.totalAmount)
                put("paidAmount", d.paidAmount)
                put("monthlyInstallment", d.monthlyInstallment)
                put("interestRatePercent", d.interestRatePercent)
                put("dueDayOfMonth", d.dueDayOfMonth)
                put("nextDueDateMillis", d.nextDueDateMillis)
                put("isPaidOff", d.isPaidOff)
                put("plainNote", CryptoManager.decryptText(d.encryptedNotes, passphrase))
                put("priorityOrder", d.priorityOrder)
                put("reminderEnabled", d.reminderEnabled)
                put("reminderDaysBefore", d.reminderDaysBefore)
                put("reminderTimingsCsv", d.reminderTimingsCsv)
                put("reminderMethodCode", d.reminderMethodCode)
            }
            debtsArray.put(obj)
        }
        root.put("debts", debtsArray)

        val expensesArray = JSONArray()
        expenses.forEach { e ->
            val obj = JSONObject().apply {
                put("title", e.title)
                put("category", e.category)
                put("amount", e.amount)
                put("monthKey", e.monthKey)
                put("monthLabel", e.monthLabel)
                put("isAutoSyncedFromDebt", e.isAutoSyncedFromDebt)
            }
            expensesArray.put(obj)
        }
        root.put("expenses", expensesArray)

        return CryptoManager.encryptText(root.toString(), passphrase)
    }

    suspend fun importEncryptedCloudSyncPacket(cipherPacket: String, passphrase: String): Result<Int> {
        return try {
            val decryptedJson = CryptoManager.decryptText(cipherPacket.trim(), passphrase)
            if (decryptedJson.startsWith("🔒") || !decryptedJson.trim().startsWith("{")) {
                return Result.failure(IllegalArgumentException("Kunci Enkripsi E2EE tidak cocok atau format paket tidak valid."))
            }
            val root = JSONObject(decryptedJson)
            val debtsArray = root.getJSONArray("debts")
            val importedDebts = mutableListOf<DebtEntity>()
            for (i in 0 until debtsArray.length()) {
                val obj = debtsArray.getJSONObject(i)
                val plainNote = obj.optString("plainNote", "")
                importedDebts.add(
                    DebtEntity(
                        creditorName = obj.getString("creditorName"),
                        category = obj.getString("category"),
                        totalAmount = obj.getLong("totalAmount"),
                        paidAmount = obj.getLong("paidAmount"),
                        monthlyInstallment = obj.getLong("monthlyInstallment"),
                        interestRatePercent = obj.getDouble("interestRatePercent"),
                        dueDayOfMonth = obj.getInt("dueDayOfMonth"),
                        nextDueDateMillis = obj.getLong("nextDueDateMillis"),
                        isPaidOff = obj.getBoolean("isPaidOff"),
                        encryptedNotes = CryptoManager.encryptText(plainNote, passphrase),
                        priorityOrder = obj.optInt("priorityOrder", i + 1),
                        reminderEnabled = obj.optBoolean("reminderEnabled", true),
                        reminderDaysBefore = obj.optInt("reminderDaysBefore", 3),
                        reminderTimingsCsv = obj.optString("reminderTimingsCsv", "1,3,7"),
                        reminderMethodCode = obj.optString("reminderMethodCode", "PUSH,EMAIL")
                    )
                )
            }

            val expensesArray = root.optJSONArray("expenses")
            val importedExpenses = mutableListOf<MonthlyExpenseEntity>()
            if (expensesArray != null) {
                for (i in 0 until expensesArray.length()) {
                    val obj = expensesArray.getJSONObject(i)
                    importedExpenses.add(
                        MonthlyExpenseEntity(
                            title = obj.getString("title"),
                            category = obj.getString("category"),
                            amount = obj.getLong("amount"),
                            monthKey = obj.getString("monthKey"),
                            monthLabel = obj.getString("monthLabel"),
                            isAutoSyncedFromDebt = obj.optBoolean("isAutoSyncedFromDebt", false)
                        )
                    )
                }
            }

            dao.clearAllDebts()
            dao.insertDebts(importedDebts)
            if (importedExpenses.isNotEmpty()) {
                dao.clearAllExpenses()
                dao.insertExpenses(importedExpenses)
            }
            ReminderScheduler.scheduleRemindersForDebts(context, importedDebts)
            Result.success(importedDebts.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearAllData() {
        dao.clearAllDebts()
        dao.clearAllExpenses()
        dao.clearAllPaymentHistory()
    }

    suspend fun pushToRemoteCloudServer(
        serverUrl: String,
        syncChannel: String,
        cipherPacket: String
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val endpoint = if (serverUrl.endsWith("/")) "${serverUrl}sync" else "${serverUrl}/sync"
            val url = URL(endpoint)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 10000
                readTimeout = 15000
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                setRequestProperty("X-Sync-Channel", syncChannel)
            }
            val body = JSONObject().apply {
                put("channel", syncChannel)
                put("payload", cipherPacket)
                put("timestamp", System.currentTimeMillis())
            }
            OutputStreamWriter(conn.outputStream, "UTF-8").use { it.write(body.toString()) }
            val code = conn.responseCode
            if (code in 200..299) {
                Result.success(true)
            } else {
                Result.failure(Exception("Server HTTP error: $code"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchFromRemoteCloudServer(
        serverUrl: String,
        syncChannel: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val endpoint = if (serverUrl.endsWith("/")) "${serverUrl}sync?channel=${syncChannel}" else "${serverUrl}/sync?channel=${syncChannel}"
            val url = URL(endpoint)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10000
                readTimeout = 15000
                setRequestProperty("Accept", "application/json")
            }
            val code = conn.responseCode
            if (code in 200..299) {
                val response = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8")).use { it.readText() }
                val json = JSONObject(response)
                val payload = json.optString("payload", "")
                if (payload.isNotBlank()) {
                    Result.success(payload)
                } else {
                    Result.failure(Exception("Payload kosong dari server"))
                }
            } else {
                Result.failure(Exception("Server HTTP error: $code"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        fun computeNextDueMillis(dueDayOfMonth: Int): Long {
            val cal = Calendar.getInstance()
            val todayDay = cal.get(Calendar.DAY_OF_MONTH)
            val maxDayInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            cal.set(Calendar.DAY_OF_MONTH, dueDayOfMonth.coerceAtMost(maxDayInMonth))
            if (dueDayOfMonth < todayDay) {
                cal.add(Calendar.MONTH, 1)
                val nextMax = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                cal.set(Calendar.DAY_OF_MONTH, dueDayOfMonth.coerceAtMost(nextMax))
            }
            cal.set(Calendar.HOUR_OF_DAY, 9)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            return cal.timeInMillis
        }
    }
}
