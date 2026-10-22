package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CurrencyFormatter
import com.example.data.DebtEntity
import com.example.data.NotificationChannelMethod
import com.example.ui.theme.AppThemeColors
import com.example.ui.theme.CreditCardRoyalBlue
import com.example.ui.theme.NavyCardDark
import com.example.ui.theme.PlusJakartaSans
import com.example.ui.theme.ProgressGreen

@Composable
fun QuickPayInstallmentDialog(
    debt: DebtEntity,
    onDismiss: () -> Unit,
    onConfirmPayment: (Long, String) -> Unit
) {
    var nominalText by remember(debt) {
        mutableStateOf(debt.monthlyInstallment.coerceAtMost(debt.remainingAmount).toString())
    }
    var noteText by remember { mutableStateOf("Cicilan rutin via Mobile Banking") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = AppThemeColors.cardSurface,
        title = {
            Column {
                Text(
                    text = "Bayar Cicilan: ${debt.creditorName}",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = AppThemeColors.textPrimary
                )
                Text(
                    text = "Sisa Utang Saat Ini: ${CurrencyFormatter.formatRupiah(debt.remainingAmount)}",
                    fontFamily = PlusJakartaSans,
                    fontSize = 13.sp,
                    color = AppThemeColors.textSecondary
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { nominalText = debt.monthlyInstallment.coerceAtMost(debt.remainingAmount).toString() },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "1x Cicilan",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    OutlinedButton(
                        onClick = { nominalText = debt.remainingAmount.toString() },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("pay_full_chip")
                    ) {
                        Text(
                            text = "Lunasi Penuh",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = ProgressGreen
                        )
                    }
                }

                OutlinedTextField(
                    value = nominalText,
                    onValueChange = { nominalText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Nominal Pembayaran (Rp)") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("payment_amount_input")
                )

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Catatan Bukti Bayar (Dienkripsi E2EE)") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = nominalText.toLongOrNull() ?: debt.monthlyInstallment
                    onConfirmPayment(amount, noteText)
                },
                colors = ButtonDefaults.buttonColors(containerColor = ProgressGreen),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.testTag("confirm_payment_button")
            ) {
                Text(
                    text = "Konfirmasi Bayar",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Batal",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    color = AppThemeColors.textSecondary
                )
            }
        }
    )
}

@Composable
fun AddDebtModalDialog(
    onDismiss: () -> Unit,
    onOpenAddExpenseInstead: () -> Unit,
    onConfirmAddDebt: (
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
    ) -> Unit
) {
    var creditorName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Pinjol") }
    var totalAmountText by remember { mutableStateOf("3500000") }
    var installmentText by remember { mutableStateOf("650000") }
    var interestText by remember { mutableStateOf("2.5") }
    var dueDayText by remember { mutableStateOf("15") }
    var noteText by remember { mutableStateOf("Kontrak baru terenkripsi AES-256-GCM") }
    var selectedTimings by remember { mutableStateOf(setOf(1, 3, 7)) }
    var selectedMethod by remember { mutableStateOf(NotificationChannelMethod.PUSH_AND_EMAIL) }

    val categories = listOf("Pinjol", "Kartu Kredit", "Paylater", "Pinjaman Pribadi")

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = AppThemeColors.cardSurface,
        title = {
            Column {
                Text(
                    text = "Tambah Catatan Utang Baru",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = AppThemeColors.textPrimary
                )
                Text(
                    text = "Atur Waktu Pengingatan (H-1/H-3/H-7) & Metode (Push/Email)",
                    fontFamily = PlusJakartaSans,
                    fontSize = 12.sp,
                    color = AppThemeColors.textSecondary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenAddExpenseInstead,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Ingin Catat Pengeluaran Bulanan? Klik di sini",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp,
                        color = CreditCardRoyalBlue
                    )
                }

                OutlinedTextField(
                    value = creditorName,
                    onValueChange = { creditorName = it },
                    label = { Text("Nama Kreditor / Aplikasi (Mis: Kredivo, BCA)") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_debt_creditor_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.take(3).forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = {
                                Text(
                                    text = cat,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NavyCardDark,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = totalAmountText,
                    onValueChange = { totalAmountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Total Pokok Utang (Rp)") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_debt_total_input")
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = installmentText,
                        onValueChange = { installmentText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Cicilan/Bln (Rp)") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = dueDayText,
                        onValueChange = { dueDayText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Tgl Jatuh Tempo") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(0.75f)
                    )
                }

                Text(
                    text = "Waktu Pengingatan (Bisa Pilih Banyak):",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = AppThemeColors.textPrimary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(1, 3, 7).forEach { d ->
                        val active = selectedTimings.contains(d)
                        FilterChip(
                            selected = active,
                            onClick = {
                                selectedTimings = if (active && selectedTimings.size > 1) {
                                    selectedTimings - d
                                } else {
                                    selectedTimings + d
                                }
                            },
                            label = {
                                Text(
                                    text = "H-$d",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ProgressGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Text(
                    text = "Metode Notifikasi:",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = AppThemeColors.textPrimary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    NotificationChannelMethod.entries.forEach { method ->
                        FilterChip(
                            selected = selectedMethod == method,
                            onClick = { selectedMethod = method },
                            label = {
                                Text(
                                    text = method.label,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.5.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CreditCardRoyalBlue,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Catatan Rahasia (Dienkripsi E2EE)") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleanName = creditorName.ifBlank { "$category Baru" }
                    val total = (totalAmountText.toLongOrNull() ?: 3_500_000L).coerceAtLeast(50_000L)
                    val inst = (installmentText.toLongOrNull() ?: 650_000L).coerceAtLeast(10_000L)
                    val rate = interestText.toDoubleOrNull() ?: 2.5
                    val day = (dueDayText.toIntOrNull() ?: 15).coerceIn(1, 31)
                    onConfirmAddDebt(
                        cleanName,
                        category,
                        total,
                        0L,
                        inst,
                        rate,
                        day,
                        noteText,
                        true,
                        selectedTimings,
                        selectedMethod.code
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = NavyCardDark),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.testTag("confirm_add_debt_button")
            ) {
                Text(
                    text = "Simpan & Jadwalkan",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Batal",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    color = AppThemeColors.textSecondary
                )
            }
        }
    )
}

@Composable
fun AddMonthlyExpenseDialog(
    onDismiss: () -> Unit,
    onConfirmAddExpense: (title: String, category: String, amount: Long) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Kebutuhan Pokok") }
    var amountText by remember { mutableStateOf("450000") }

    val categories = listOf("Kebutuhan Pokok", "Utilitas & Tagihan", "Transportasi", "Cicilan Utang", "Lainnya")

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = AppThemeColors.cardSurface,
        title = {
            Text(
                text = "Catat Pengeluaran Bulanan",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = AppThemeColors.textPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nama Pengeluaran (Mis: Listrik & Internet)") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expense_title_input")
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Nominal Pengeluaran (Rp)") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expense_amount_input")
                )

                Spacer(modifier = Modifier.height(4.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    categories.chunked(2).forEach { rowCats ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            rowCats.forEach { cat ->
                                FilterChip(
                                    selected = category == cat,
                                    onClick = { category = cat },
                                    label = { Text(cat, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleanTitle = title.ifBlank { "Pengeluaran $category" }
                    val amt = (amountText.toLongOrNull() ?: 450_000L).coerceAtLeast(1_000L)
                    onConfirmAddExpense(cleanTitle, category, amt)
                },
                colors = ButtonDefaults.buttonColors(containerColor = CreditCardRoyalBlue),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.testTag("confirm_add_expense_button")
            ) {
                Text(
                    text = "Simpan ke Statistik",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, color = AppThemeColors.textSecondary)
            }
        }
    )
}
