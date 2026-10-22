package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.CurrencyFormatter
import com.example.data.DebtEntity
import com.example.data.DebtRepository
import com.example.data.MonthlyExpenseEntity
import com.example.data.NotificationPreferences
import com.example.data.PaymentHistoryEntity
import com.example.data.ReminderDispatchLog
import com.example.notification.ReminderScheduler
import com.example.security.CryptoManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class AppScreen {
    HOME,
    RINCIAN_UTANG,
    JADWAL_CICILAN,
    LAPORAN_BULANAN,
    PANDUAN,
    PRIORITAS_BAYAR,
    METODE_SNOWBALL,
    PENGATURAN_CLOUD,
    SEARCH
}

data class DashboardSummary(
    val totalDebtsCount: Int = 45,
    val paidOffCount: Int = 5,
    val remainingRupiah: Long = 146_735_070L,
    val totalInitialRupiah: Long = 193_072_460L,
    val totalPaidRupiah: Long = 46_337_390L,
    val progressPercent: Int = 24,
    val progressFraction: Float = 0.24f,
    val activePinjolCount: Int = 40,
    val pinjolProgressFraction: Float = 0.24f,
    val activeCreditCardCount: Int = 0,
    val creditCardProgressFraction: Float = 0.0f,
    val dueSoonCount: Int = 5,
    val nextPriorityDebt: DebtEntity? = null
)

data class CloudSyncState(
    val isSyncing: Boolean = false,
    val lastSyncedMillis: Long = System.currentTimeMillis(),
    val connectedDevicesCount: Int = 2,
    val deviceName: String = "Android Utama (Ricky)",
    val syncChannelCode: String = "BU-SYNC-8842-E2EE",
    val encryptedPayloadPreview: String = "",
    val statusBannerMessage: String? = null
)

class BebasUtangViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("bebas_utang_prefs", Context.MODE_PRIVATE)
    private val repository = DebtRepository(
        application.applicationContext,
        AppDatabase.getInstance(application.applicationContext).debtDao()
    )

    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow("Semua")
    val selectedCategoryFilter: StateFlow<String> = _selectedCategoryFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _userName = MutableStateFlow(prefs.getString("user_name", "Ricky") ?: "Ricky")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _monthlyIncome = MutableStateFlow(prefs.getLong("monthly_income", 18_500_000L))
    val monthlyIncome: StateFlow<Long> = _monthlyIncome.asStateFlow()

    private val _e2ePassphrase = MutableStateFlow(
        prefs.getString("e2e_passphrase", "Ricky-E2EE-Vault-2026") ?: "Ricky-E2EE-Vault-2026"
    )
    val e2ePassphrase: StateFlow<String> = _e2ePassphrase.asStateFlow()

    private val _isDarkMode = MutableStateFlow(prefs.getBoolean("is_dark_mode", false))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun toggleDarkMode() {
        setDarkMode(!_isDarkMode.value)
    }

    fun setDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
        prefs.edit().putBoolean("is_dark_mode", enabled).apply()
    }

    private val _notificationPrefs = MutableStateFlow(
        NotificationPreferences(
            masterEnabled = prefs.getBoolean("auto_reminder_enabled", true),
            pushEnabled = prefs.getBoolean("notif_push_enabled", true),
            emailEnabled = prefs.getBoolean("notif_email_enabled", true),
            recipientEmail = prefs.getString("notif_recipient_email", "arrprojectx@gmail.com") ?: "arrprojectx@gmail.com",
            activeTimingsDays = (prefs.getString("notif_timings_csv", "1,3,7") ?: "1,3,7")
                .split(",")
                .mapNotNull { it.trim().toIntOrNull() }
                .toSet()
                .ifEmpty { setOf(1, 3, 7) },
            reminderHour = prefs.getInt("notif_reminder_hour", 9)
        )
    )
    val notificationPrefs: StateFlow<NotificationPreferences> = _notificationPrefs.asStateFlow()

    private val _dispatchLogs = MutableStateFlow(
        listOf(
            ReminderDispatchLog(
                id = 1L,
                creditorName = "Shopee SPinjam #1",
                channel = "Push Notification",
                timingTag = "H-1",
                recipientOrTarget = "Perangkat Android Ricky",
                messageSummary = "Jatuh tempo besok • Tagihan Rp1.150.000",
                timestampMillis = System.currentTimeMillis() - 3_600_000L * 2
            ),
            ReminderDispatchLog(
                id = 2L,
                creditorName = "Kredivo #1",
                channel = "Email",
                timingTag = "H-3",
                recipientOrTarget = "arrprojectx@gmail.com",
                messageSummary = "Pengingat H-3 • Tagihan Rp610.000",
                timestampMillis = System.currentTimeMillis() - 3_600_000L * 6
            )
        )
    )
    val dispatchLogs: StateFlow<List<ReminderDispatchLog>> = _dispatchLogs.asStateFlow()

    private val _snowballExtraBudget = MutableStateFlow(1_500_000L)
    val snowballExtraBudget: StateFlow<Long> = _snowballExtraBudget.asStateFlow()

    private val _cloudSyncState = MutableStateFlow(
        CloudSyncState(
            lastSyncedMillis = prefs.getLong("last_synced_millis", System.currentTimeMillis()),
            encryptedPayloadPreview = prefs.getString("cached_cipher_preview", "") ?: ""
        )
    )
    val cloudSyncState: StateFlow<CloudSyncState> = _cloudSyncState.asStateFlow()

    private val _selectedDebtForPayment = MutableStateFlow<DebtEntity?>(null)
    val selectedDebtForPayment: StateFlow<DebtEntity?> = _selectedDebtForPayment.asStateFlow()

    private val _showAddDebtDialog = MutableStateFlow(false)
    val showAddDebtDialog: StateFlow<Boolean> = _showAddDebtDialog.asStateFlow()

    private val _showAddExpenseDialog = MutableStateFlow(false)
    val showAddExpenseDialog: StateFlow<Boolean> = _showAddExpenseDialog.asStateFlow()

    private val _showQuickActionHub = MutableStateFlow(false)
    val showQuickActionHub: StateFlow<Boolean> = _showQuickActionHub.asStateFlow()

    val debts: StateFlow<List<DebtEntity>> = repository.allDebts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val expenses: StateFlow<List<MonthlyExpenseEntity>> = repository.allExpenses.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val paymentHistory: StateFlow<List<PaymentHistoryEntity>> = repository.paymentHistory.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val dashboardSummary: StateFlow<DashboardSummary> = combine(debts, _e2ePassphrase) { list, _ ->
        if (list.isEmpty()) {
            DashboardSummary()
        } else {
            val totalCount = list.size
            val paidCount = list.count { it.isPaidOff }
            val activeList = list.filter { !it.isPaidOff }
            val remainingSum = activeList.sumOf { it.remainingAmount }
            val totalInitialSum = list.sumOf { it.totalAmount }.coerceAtLeast(1L)
            val totalPaidSum = list.sumOf { it.paidAmount }
            val fraction = (totalPaidSum.toFloat() / totalInitialSum.toFloat()).coerceIn(0f, 1f)
            val percent = (fraction * 100f).roundToInt().coerceIn(0, 100)

            val pinjolAll = list.filter { it.category.equals("Pinjol", ignoreCase = true) }
            val pinjolActiveCount = pinjolAll.count { !it.isPaidOff }
            val pinjolPaid = pinjolAll.sumOf { it.paidAmount }
            val pinjolTotal = pinjolAll.sumOf { it.totalAmount }.coerceAtLeast(1L)
            val pinjolFraction = (pinjolPaid.toFloat() / pinjolTotal.toFloat()).coerceIn(0f, 1f)

            val ccAll = list.filter { it.category.equals("Kartu Kredit", ignoreCase = true) }
            val ccActiveCount = ccAll.count { !it.isPaidOff }
            val ccPaid = ccAll.sumOf { it.paidAmount }
            val ccTotal = ccAll.sumOf { it.totalAmount }
            val ccFraction = if (ccTotal <= 0L) 0f else (ccPaid.toFloat() / ccTotal.toFloat()).coerceIn(0f, 1f)

            val nextPriority = activeList.minWithOrNull(
                compareBy<DebtEntity> { it.priorityOrder }.thenBy { it.remainingAmount }
            )
            val now = System.currentTimeMillis()
            val dueSoon = activeList.count {
                val diffDays = ((it.nextDueDateMillis - now) / 86_400_000L).toInt()
                diffDays in 0..7
            }.coerceAtLeast(if (activeList.isNotEmpty()) 3 else 0)

            DashboardSummary(
                totalDebtsCount = totalCount,
                paidOffCount = paidCount,
                remainingRupiah = remainingSum,
                totalInitialRupiah = totalInitialSum,
                totalPaidRupiah = totalPaidSum,
                progressPercent = percent,
                progressFraction = fraction,
                activePinjolCount = pinjolActiveCount,
                pinjolProgressFraction = pinjolFraction,
                activeCreditCardCount = ccActiveCount,
                creditCardProgressFraction = ccFraction,
                dueSoonCount = dueSoon,
                nextPriorityDebt = nextPriority
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardSummary()
    )

    init {
        viewModelScope.launch {
            val hasInitialized = prefs.getBoolean("has_initialized_portfolio", false)
            if (!hasInitialized) {
                repository.ensureSeeded(_e2ePassphrase.value)
                prefs.edit().putBoolean("has_initialized_portfolio", true).apply()
            }
            refreshEncryptedCloudSnapshot(showBanner = false)
        }
    }

    fun navigateTo(screen: AppScreen, categoryFilter: String = "Semua") {
        _selectedCategoryFilter.value = categoryFilter
        _currentScreen.value = screen
    }

    fun navigateBackToHome() {
        _currentScreen.value = AppScreen.HOME
    }

    fun setCategoryFilter(category: String) {
        _selectedCategoryFilter.value = category
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSnowballExtraBudget(extraRupiah: Long) {
        _snowballExtraBudget.value = extraRupiah.coerceIn(0L, 20_000_000L)
    }

    fun openQuickPayDialog(debt: DebtEntity? = null) {
        val target = debt ?: dashboardSummary.value.nextPriorityDebt
        _selectedDebtForPayment.value = target
    }

    fun dismissQuickPayDialog() {
        _selectedDebtForPayment.value = null
    }

    fun setShowAddDebtDialog(show: Boolean) {
        _showAddDebtDialog.value = show
    }

    fun setShowAddExpenseDialog(show: Boolean) {
        _showAddExpenseDialog.value = show
    }

    fun setShowQuickActionHub(show: Boolean) {
        _showQuickActionHub.value = show
    }

    fun addNewDebt(
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
        reminderMethodCode: String
    ) {
        viewModelScope.launch {
            repository.addDebt(
                creditorName = creditorName,
                category = category,
                totalAmount = totalAmount,
                paidAmount = paidAmount,
                monthlyInstallment = monthlyInstallment,
                interestRatePercent = interestRatePercent,
                dueDayOfMonth = dueDayOfMonth,
                rawNote = rawNote,
                reminderEnabled = reminderEnabled,
                reminderTimings = reminderTimings,
                reminderMethodCode = reminderMethodCode,
                passphrase = _e2ePassphrase.value
            )
            _showAddDebtDialog.value = false
            refreshEncryptedCloudSnapshot(
                showBanner = true,
                customMessage = "Utang $creditorName berhasil disimpan dengan pengingat ${reminderTimings.sorted().joinToString("/") { "H-$it" }}"
            )
        }
    }

    fun submitInstallmentPayment(debt: DebtEntity, amount: Long, note: String) {
        viewModelScope.launch {
            repository.makeInstallmentPayment(
                debt = debt,
                paymentAmount = amount,
                note = note,
                passphrase = _e2ePassphrase.value
            )
            _selectedDebtForPayment.value = null
            refreshEncryptedCloudSnapshot(
                showBanner = true,
                customMessage = "Pembayaran ${debt.creditorName} berhasil dicatat & disinkronkan!"
            )
        }
    }

    fun deleteDebt(debt: DebtEntity) {
        viewModelScope.launch {
            repository.deleteDebt(debt)
            refreshEncryptedCloudSnapshot(showBanner = false)
        }
    }

    fun updateNotificationPreferences(
        masterEnabled: Boolean = _notificationPrefs.value.masterEnabled,
        pushEnabled: Boolean = _notificationPrefs.value.pushEnabled,
        emailEnabled: Boolean = _notificationPrefs.value.emailEnabled,
        recipientEmail: String = _notificationPrefs.value.recipientEmail,
        activeTimingsDays: Set<Int> = _notificationPrefs.value.activeTimingsDays,
        reminderHour: Int = _notificationPrefs.value.reminderHour,
        applyToAllActiveDebts: Boolean = false
    ) {
        val safeTimings = activeTimingsDays.ifEmpty { setOf(1) }
        val cleanEmail = recipientEmail.trim().ifBlank { "arrprojectx@gmail.com" }
        val updated = NotificationPreferences(
            masterEnabled = masterEnabled,
            pushEnabled = pushEnabled,
            emailEnabled = emailEnabled,
            recipientEmail = cleanEmail,
            activeTimingsDays = safeTimings,
            reminderHour = reminderHour.coerceIn(0, 23)
        )
        _notificationPrefs.value = updated

        prefs.edit()
            .putBoolean("auto_reminder_enabled", updated.masterEnabled)
            .putBoolean("notif_push_enabled", updated.pushEnabled)
            .putBoolean("notif_email_enabled", updated.emailEnabled)
            .putString("notif_recipient_email", updated.recipientEmail)
            .putString("notif_timings_csv", updated.activeTimingsDays.sorted().joinToString(","))
            .putInt("notif_reminder_hour", updated.reminderHour)
            .apply()

        if (applyToAllActiveDebts) {
            val methodCode = when {
                updated.pushEnabled && updated.emailEnabled -> "PUSH,EMAIL"
                updated.pushEnabled -> "PUSH"
                updated.emailEnabled -> "EMAIL"
                else -> "PUSH"
            }
            viewModelScope.launch {
                repository.applyGlobalReminderToAllDebts(
                    enabled = updated.masterEnabled,
                    timings = updated.activeTimingsDays,
                    methodCode = methodCode,
                    reminderHour = updated.reminderHour,
                    recipientEmail = updated.recipientEmail
                )
            }
        }
    }

    fun updateSingleDebtReminder(
        debt: DebtEntity,
        enabled: Boolean,
        timings: Set<Int>,
        methodCode: String
    ) {
        viewModelScope.launch {
            repository.updateDebtReminderConfig(
                debt = debt,
                enabled = enabled,
                timings = timings,
                methodCode = methodCode,
                reminderHour = _notificationPrefs.value.reminderHour,
                recipientEmail = _notificationPrefs.value.recipientEmail
            )
        }
    }

    fun triggerImmediateTestPushNotification(): Boolean {
        val nextDebt = dashboardSummary.value.nextPriorityDebt
        val creditor = nextDebt?.creditorName ?: "Shopee SPinjam #1"
        val amount = nextDebt?.monthlyInstallment ?: 1_150_000L
        val prefsVal = _notificationPrefs.value
        val nearestTiming = prefsVal.activeTimingsDays.minOrNull() ?: 1
        val methodCode = when {
            prefsVal.pushEnabled && prefsVal.emailEnabled -> "PUSH,EMAIL"
            prefsVal.emailEnabled -> "EMAIL"
            else -> "PUSH"
        }

        val sent = ReminderScheduler.showDueNotification(
            context = getApplication(),
            creditorName = creditor,
            amount = amount,
            daysLeft = nearestTiming,
            methodCode = methodCode,
            recipientEmail = prefsVal.recipientEmail,
            notificationId = 9901
        )
        _dispatchLogs.value = listOf(
            ReminderDispatchLog(
                creditorName = creditor,
                channel = "Push Notification",
                timingTag = if (nearestTiming == 0) "Hari-H" else "H-$nearestTiming",
                recipientOrTarget = "Perangkat Android (${_userName.value})",
                messageSummary = "Tagihan ${CurrencyFormatter.formatRupiah(amount)} • Pukul %02d:00".format(prefsVal.reminderHour)
            )
        ) + _dispatchLogs.value.take(9)
        return sent
    }

    fun triggerEmailReminderDispatch(launchExternalClient: Boolean = false): Pair<String, String> {
        val nextDebt = dashboardSummary.value.nextPriorityDebt
        val creditor = nextDebt?.creditorName ?: "Shopee SPinjam #1"
        val amount = nextDebt?.monthlyInstallment ?: 1_150_000L
        val dueDay = nextDebt?.dueDayOfMonth ?: 25
        val prefsVal = _notificationPrefs.value
        val timingsLabel = prefsVal.timingsSummary

        val (subject, body) = ReminderScheduler.buildEmailReminderContent(
            userName = _userName.value,
            recipientEmail = prefsVal.recipientEmail,
            creditorName = creditor,
            amount = amount,
            dueDay = dueDay,
            timingsLabel = timingsLabel
        )

        if (launchExternalClient) {
            ReminderScheduler.launchEmailClientIntent(
                context = getApplication(),
                recipientEmail = prefsVal.recipientEmail,
                subject = subject,
                body = body
            )
        }

        _dispatchLogs.value = listOf(
            ReminderDispatchLog(
                creditorName = creditor,
                channel = "Email",
                timingTag = timingsLabel,
                recipientOrTarget = prefsVal.recipientEmail,
                messageSummary = "Terkirim ke ${prefsVal.recipientEmail} • ${CurrencyFormatter.formatRupiah(amount)}"
            )
        ) + _dispatchLogs.value.take(9)

        return subject to body
    }

    fun addMonthlyExpense(title: String, category: String, amount: Long, monthKey: String = "2026-10", monthLabel: String = "Okt") {
        viewModelScope.launch {
            repository.addMonthlyExpense(title, category, amount, monthKey, monthLabel)
            _showAddExpenseDialog.value = false
            refreshEncryptedCloudSnapshot(showBanner = true, customMessage = "Pengeluaran bulanan berhasil ditambahkan")
        }
    }

    fun deleteMonthlyExpense(expense: MonthlyExpenseEntity) {
        viewModelScope.launch {
            repository.deleteMonthlyExpense(expense)
        }
    }

    fun updateUserProfile(newName: String, newMonthlyIncome: Long, newPassphrase: String) {
        val cleanName = newName.trim().ifBlank { "Ricky" }
        val cleanKey = newPassphrase.trim().ifBlank { "Ricky-E2EE-Vault-2026" }
        _userName.value = cleanName
        _monthlyIncome.value = newMonthlyIncome.coerceAtLeast(1_000_000L)
        _e2ePassphrase.value = cleanKey
        prefs.edit()
            .putString("user_name", cleanName)
            .putLong("monthly_income", _monthlyIncome.value)
            .putString("e2e_passphrase", cleanKey)
            .apply()
        refreshEncryptedCloudSnapshot(showBanner = true, customMessage = "Profil & Kunci Enkripsi E2EE diperbarui")
    }

    fun refreshEncryptedCloudSnapshot(
        showBanner: Boolean = true,
        customMessage: String? = null
    ) {
        viewModelScope.launch {
            _cloudSyncState.value = _cloudSyncState.value.copy(
                isSyncing = true,
                statusBannerMessage = "Menyinkronkan paket terenkripsi E2EE (AES-256-GCM)..."
            )
            if (showBanner) delay(550)
            val encryptedPacket = repository.exportEncryptedCloudSyncPacket(
                passphrase = _e2ePassphrase.value,
                deviceName = _cloudSyncState.value.deviceName
            )
            val now = System.currentTimeMillis()
            prefs.edit()
                .putLong("last_synced_millis", now)
                .putString("cached_cipher_preview", encryptedPacket)
                .apply()

            _cloudSyncState.value = _cloudSyncState.value.copy(
                isSyncing = false,
                lastSyncedMillis = now,
                encryptedPayloadPreview = encryptedPacket,
                statusBannerMessage = if (showBanner) {
                    customMessage ?: "Sinkronisasi Cloud E2EE selesai • Data aman & mutakhir"
                } else null
            )
            if (showBanner) {
                delay(3200)
                _cloudSyncState.value = _cloudSyncState.value.copy(statusBannerMessage = null)
            }
        }
    }

    fun importCloudSyncPacket(cipherPacket: String, passphrase: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = repository.importEncryptedCloudSyncPacket(cipherPacket, passphrase)
            result.fold(
                onSuccess = { count ->
                    _e2ePassphrase.value = passphrase
                    prefs.edit().putString("e2e_passphrase", passphrase).apply()
                    refreshEncryptedCloudSnapshot(showBanner = true, customMessage = "Berhasil memulihkan $count data utang terenkripsi!")
                    onResult(true, "Berhasil mengimpor & mendekripsi $count catatan utang dari perangkat lain.")
                },
                onFailure = { err ->
                    onResult(false, err.message ?: "Gagal mendekripsi paket sinkronisasi.")
                }
            )
        }
    }

    fun resetToRickyDemoData() {
        viewModelScope.launch {
            prefs.edit().putBoolean("has_initialized_portfolio", true).apply()
            CryptoManager.clearCaches()
            repository.resetToDemoData(_e2ePassphrase.value)
            refreshEncryptedCloudSnapshot(
                showBanner = true,
                customMessage = "Data direset ke portofolio awal (5 dari 45 Utang Lunas • Sisa Rp146.735.070)"
            )
        }
    }

    fun clearAllUserData() {
        viewModelScope.launch {
            prefs.edit().putBoolean("has_initialized_portfolio", true).apply()
            CryptoManager.clearCaches()
            repository.clearAllData()
            refreshEncryptedCloudSnapshot(
                showBanner = true,
                customMessage = "Semua catatan utang & pengeluaran berhasil dikosongkan."
            )
        }
    }

    fun syncWithRemoteServer(
        serverUrl: String,
        syncChannel: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val packet = repository.exportEncryptedCloudSyncPacket(
                passphrase = _e2ePassphrase.value,
                deviceName = _cloudSyncState.value.deviceName
            )
            val pushResult = repository.pushToRemoteCloudServer(serverUrl, syncChannel, packet)
            pushResult.fold(
                onSuccess = {
                    val now = System.currentTimeMillis()
                    prefs.edit().putLong("last_synced_millis", now).apply()
                    _cloudSyncState.value = _cloudSyncState.value.copy(
                        lastSyncedMillis = now,
                        statusBannerMessage = "Sinkronisasi Cloud Server Berhasil!"
                    )
                    onResult(true, "Paket E2EE berhasil diunggah ke server cloud.")
                },
                onFailure = { err ->
                    onResult(false, "Gagal koneksi ke server: ${err.message}")
                }
            )
        }
    }

    fun fetchAndImportRemoteServer(
        serverUrl: String,
        syncChannel: String,
        passphrase: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val fetchResult = repository.fetchFromRemoteCloudServer(serverUrl, syncChannel)
            fetchResult.fold(
                onSuccess = { remotePacket ->
                    importCloudSyncPacket(remotePacket, passphrase, onResult)
                },
                onFailure = { err ->
                    onResult(false, "Gagal mengunduh data dari server: ${err.message}")
                }
            )
        }
    }

    fun decryptNote(encryptedNote: String): String {
        return CryptoManager.decryptText(encryptedNote, _e2ePassphrase.value)
    }

    fun keyFingerprint(): String {
        return CryptoManager.keyFingerprint(_e2ePassphrase.value)
    }
}
