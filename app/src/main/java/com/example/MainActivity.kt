package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AppScreen
import com.example.ui.BebasUtangViewModel
import com.example.ui.screens.AddDebtModalDialog
import com.example.ui.screens.AddMonthlyExpenseDialog
import com.example.ui.screens.CloudSyncSecurityScreen
import com.example.ui.screens.DebtListScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MonthlyStatsScreen
import com.example.ui.screens.QuickActionHubDialog
import com.example.ui.screens.QuickPayInstallmentDialog
import com.example.ui.screens.ScheduleReminderScreen
import com.example.ui.screens.SnowballPriorityScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BebasUtangApp()
        }
    }
}

@Composable
fun BebasUtangApp(
    viewModel: BebasUtangViewModel = viewModel()
) {
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val monthlyIncome by viewModel.monthlyIncome.collectAsStateWithLifecycle()
    val e2ePassphrase by viewModel.e2ePassphrase.collectAsStateWithLifecycle()
    val notificationPrefs by viewModel.notificationPrefs.collectAsStateWithLifecycle()
    val dispatchLogs by viewModel.dispatchLogs.collectAsStateWithLifecycle()
    val snowballExtraBudget by viewModel.snowballExtraBudget.collectAsStateWithLifecycle()
    val summary by viewModel.dashboardSummary.collectAsStateWithLifecycle()
    val cloudSyncState by viewModel.cloudSyncState.collectAsStateWithLifecycle()
    val debts by viewModel.debts.collectAsStateWithLifecycle()
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategoryFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedDebtForPayment by viewModel.selectedDebtForPayment.collectAsStateWithLifecycle()
    val showAddDebtDialog by viewModel.showAddDebtDialog.collectAsStateWithLifecycle()
    val showAddExpenseDialog by viewModel.showAddExpenseDialog.collectAsStateWithLifecycle()
    val showQuickActionHub by viewModel.showQuickActionHub.collectAsStateWithLifecycle()

    MyApplicationTheme(darkTheme = isDarkMode) {
        when (currentScreen) {
            AppScreen.HOME -> {
                HomeScreen(
                    userName = userName,
                    summary = summary,
                    cloudSyncState = cloudSyncState,
                    isDarkMode = isDarkMode,
                    onToggleDarkMode = { viewModel.toggleDarkMode() },
                    onNavigate = { screen, cat -> viewModel.navigateTo(screen, cat) },
                    onLanjutBayarClick = { viewModel.openQuickPayDialog() },
                    onSegarkanClick = { viewModel.refreshEncryptedCloudSnapshot(showBanner = true) },
                    onCenterAddClick = { viewModel.setShowQuickActionHub(true) }
                )
            }

            AppScreen.RINCIAN_UTANG, AppScreen.SEARCH -> {
                DebtListScreen(
                    debts = debts,
                    selectedCategory = selectedCategory,
                    searchQuery = searchQuery,
                    autoFocusSearch = currentScreen == AppScreen.SEARCH,
                    onCategoryChange = { viewModel.setCategoryFilter(it) },
                    onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                    onPayClick = { viewModel.openQuickPayDialog(it) },
                    onDeleteClick = { viewModel.deleteDebt(it) },
                    onAddDebtClick = { viewModel.setShowAddDebtDialog(true) },
                    onNavigate = { screen -> viewModel.navigateTo(screen) },
                    decryptNote = { viewModel.decryptNote(it) },
                    onBack = { viewModel.navigateBackToHome() }
                )
            }

            AppScreen.JADWAL_CICILAN -> {
                ScheduleReminderScreen(
                    debts = debts,
                    notificationPrefs = notificationPrefs,
                    dispatchLogs = dispatchLogs,
                    onUpdateGlobalPrefs = { master, push, email, recipient, timings, hour, applyAll ->
                        viewModel.updateNotificationPreferences(
                            masterEnabled = master,
                            pushEnabled = push,
                            emailEnabled = email,
                            recipientEmail = recipient,
                            activeTimingsDays = timings,
                            reminderHour = hour,
                            applyToAllActiveDebts = applyAll
                        )
                    },
                    onUpdateSingleDebtReminder = { debt, enabled, timings, methodCode ->
                        viewModel.updateSingleDebtReminder(debt, enabled, timings, methodCode)
                    },
                    onTriggerTestPush = { viewModel.triggerImmediateTestPushNotification() },
                    onTriggerEmailReminder = { launchExternal ->
                        viewModel.triggerEmailReminderDispatch(launchExternal)
                    },
                    onPayDebt = { viewModel.openQuickPayDialog(it) },
                    onNavigate = { screen -> viewModel.navigateTo(screen) },
                    onAddDebtClick = { viewModel.setShowAddDebtDialog(true) },
                    onBack = { viewModel.navigateBackToHome() }
                )
            }

            AppScreen.LAPORAN_BULANAN -> {
                MonthlyStatsScreen(
                    debts = debts,
                    expenses = expenses,
                    monthlyIncome = monthlyIncome,
                    onAddExpenseClick = { viewModel.setShowAddExpenseDialog(true) },
                    onAddDebtClick = { viewModel.setShowAddDebtDialog(true) },
                    onNavigate = { screen -> viewModel.navigateTo(screen) },
                    onDeleteExpense = { viewModel.deleteMonthlyExpense(it) },
                    onBack = { viewModel.navigateBackToHome() }
                )
            }

            AppScreen.PANDUAN, AppScreen.PRIORITAS_BAYAR, AppScreen.METODE_SNOWBALL -> {
                SnowballPriorityScreen(
                    mode = currentScreen,
                    debts = debts,
                    extraBudget = snowballExtraBudget,
                    onExtraBudgetChange = { viewModel.updateSnowballExtraBudget(it) },
                    onPayDebt = { viewModel.openQuickPayDialog(it) },
                    onBack = { viewModel.navigateBackToHome() }
                )
            }

            AppScreen.PENGATURAN_CLOUD -> {
                CloudSyncSecurityScreen(
                    userName = userName,
                    monthlyIncome = monthlyIncome,
                    e2ePassphrase = e2ePassphrase,
                    keyFingerprint = viewModel.keyFingerprint(),
                    cloudSyncState = cloudSyncState,
                    isDarkMode = isDarkMode,
                    onSetDarkMode = { viewModel.setDarkMode(it) },
                    onNavigate = { screen -> viewModel.navigateTo(screen) },
                    onAddDebtClick = { viewModel.setShowAddDebtDialog(true) },
                    onSaveProfileAndKey = { name, inc, key ->
                        viewModel.updateUserProfile(name, inc, key)
                    },
                    onSyncNow = { viewModel.refreshEncryptedCloudSnapshot(showBanner = true) },
                    onImportSyncPacket = { packet, key, cb ->
                        viewModel.importCloudSyncPacket(packet, key, cb)
                    },
                    onResetDemoData = { viewModel.resetToRickyDemoData() },
                    onClearAllData = { viewModel.clearAllUserData() },
                    onBack = { viewModel.navigateBackToHome() }
                )
            }
        }

        // Active Modals
        if (showQuickActionHub) {
            QuickActionHubDialog(
                isDarkMode = isDarkMode,
                onToggleDarkMode = { viewModel.toggleDarkMode() },
                onDismiss = { viewModel.setShowQuickActionHub(false) },
                onAddDebt = { viewModel.setShowAddDebtDialog(true) },
                onAddExpense = { viewModel.setShowAddExpenseDialog(true) },
                onPayPriorityDebt = { viewModel.openQuickPayDialog() },
                onOpenSchedule = { viewModel.navigateTo(AppScreen.JADWAL_CICILAN) },
                onOpenStats = { viewModel.navigateTo(AppScreen.LAPORAN_BULANAN) }
            )
        }

        if (selectedDebtForPayment != null) {
            QuickPayInstallmentDialog(
                debt = selectedDebtForPayment!!,
                onDismiss = { viewModel.dismissQuickPayDialog() },
                onConfirmPayment = { amount, note ->
                    viewModel.submitInstallmentPayment(selectedDebtForPayment!!, amount, note)
                }
            )
        }

        if (showAddDebtDialog) {
            AddDebtModalDialog(
                onDismiss = { viewModel.setShowAddDebtDialog(false) },
                onOpenAddExpenseInstead = {
                    viewModel.setShowAddDebtDialog(false)
                    viewModel.setShowAddExpenseDialog(true)
                },
                onConfirmAddDebt = { name, cat, total, paid, inst, rate, dueDay, note, remEnabled, timings, methodCode ->
                    viewModel.addNewDebt(name, cat, total, paid, inst, rate, dueDay, note, remEnabled, timings, methodCode)
                }
            )
        }

        if (showAddExpenseDialog) {
            AddMonthlyExpenseDialog(
                onDismiss = { viewModel.setShowAddExpenseDialog(false) },
                onConfirmAddExpense = { title, cat, amount ->
                    viewModel.addMonthlyExpense(title, cat, amount)
                }
            )
        }
    }
}
