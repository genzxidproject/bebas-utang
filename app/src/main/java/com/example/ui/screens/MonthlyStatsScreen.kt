package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CurrencyFormatter
import com.example.data.DebtEntity
import com.example.data.MonthlyExpenseEntity
import com.example.ui.AppScreen
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AppThemeColors
import com.example.ui.theme.CreditCardRoyalBlue
import com.example.ui.theme.HeaderBlueEnd
import com.example.ui.theme.HeaderBlueStart
import com.example.ui.theme.NavyCardDark
import com.example.ui.theme.PlusJakartaSans
import com.example.ui.theme.ProgressGreen
import com.example.ui.theme.WarningAmber
import kotlin.math.roundToInt

data class MonthStatBar(
    val monthKey: String,
    val monthLabel: String,
    val debtExpense: Long,
    val livingExpense: Long
) {
    val total: Long get() = debtExpense + livingExpense
}

@Composable
fun MonthlyStatsScreen(
    debts: List<DebtEntity>,
    expenses: List<MonthlyExpenseEntity>,
    monthlyIncome: Long,
    onAddExpenseClick: () -> Unit,
    onAddDebtClick: () -> Unit,
    onNavigate: (AppScreen) -> Unit,
    onDeleteExpense: (MonthlyExpenseEntity) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val activeMonthlyDebtInstallments = remember(debts) {
        debts.filter { !it.isPaidOff }.sumOf { it.monthlyInstallment }
    }

    val currentMonthExpenses = remember(expenses) {
        expenses.filter { it.monthKey == "2026-10" }
    }

    val totalCurrentMonthOutflow = remember(currentMonthExpenses) {
        currentMonthExpenses.sumOf { it.amount }
    }

    val debtToIncomeRatio = remember(totalCurrentMonthOutflow, monthlyIncome) {
        if (monthlyIncome <= 0L) 0 else ((totalCurrentMonthOutflow.toDouble() / monthlyIncome.toDouble()) * 100).roundToInt()
    }

    val sixMonthsHistory = remember(expenses) {
        val monthOrder = listOf(
            "2026-05" to "Mei",
            "2026-06" to "Jun",
            "2026-07" to "Jul",
            "2026-08" to "Agu",
            "2026-09" to "Sep",
            "2026-10" to "Okt"
        )
        monthOrder.map { (key, label) ->
            val monthItems = expenses.filter { it.monthKey == key }
            val debtPart = monthItems.filter { it.category.equals("Cicilan Utang", true) }.sumOf { it.amount }
            val livingPart = monthItems.filter { !it.category.equals("Cicilan Utang", true) }.sumOf { it.amount }
            MonthStatBar(key, label, debtPart, livingPart)
        }
    }

    val categoryBreakdown = remember(currentMonthExpenses) {
        val grouped = currentMonthExpenses.groupBy { it.category }
            .mapValues { (_, list) -> list.sumOf { it.amount } }
            .toList()
            .sortedByDescending { it.second }
        grouped
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppThemeColors.screenBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(HeaderBlueStart, HeaderBlueEnd)))
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("back_button_laporan")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White
                        )
                    }
                    Column {
                        Text(
                            text = "Dashboard Statistik Bulanan",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 19.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Analisis Arus Kas, Cicilan Utang & Pengeluaran Rutin",
                            fontFamily = PlusJakartaSans,
                            fontSize = 12.5.sp,
                            color = Color(0xFFDCEBFF)
                        )
                    }
                }
            }

            // Quick Feature Jump Bar for effortless navigation
            QuickFeatureJumpBar(
                currentScreen = AppScreen.LAPORAN_BULANAN,
                onNavigate = onNavigate,
                onAddDebtClick = onAddDebtClick
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Summary Cashflow & DSR Health Card
                item {
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = NavyCardDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Total Pengeluaran Oktober 2026",
                                        fontFamily = PlusJakartaSans,
                                        fontSize = 12.5.sp,
                                        color = Color(0xFFA3B8D8)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = CurrencyFormatter.formatRupiah(totalCurrentMonthOutflow),
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 23.sp,
                                        color = Color.White
                                    )
                                }
                                Surface(
                                    color = when {
                                        debtToIncomeRatio <= 35 -> ProgressGreen
                                        debtToIncomeRatio <= 60 -> WarningAmber
                                        else -> AlertRed
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "Rasio Kas $debtToIncomeRatio%",
                                        color = Color.White,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Pendapatan Bulanan",
                                        fontFamily = PlusJakartaSans,
                                        fontSize = 11.5.sp,
                                        color = Color(0xFFA3B8D8)
                                    )
                                    Text(
                                        text = CurrencyFormatter.formatRupiah(monthlyIncome),
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = ProgressGreen
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Total Beban Cicilan Aktif",
                                        fontFamily = PlusJakartaSans,
                                        fontSize = 11.5.sp,
                                        color = Color(0xFFA3B8D8)
                                    )
                                    Text(
                                        text = CurrencyFormatter.formatRupiah(activeMonthlyDebtInstallments),
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF93C5FD)
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. 6-Month Bar Chart Card
                item {
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = AppThemeColors.cardSurface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Insights,
                                    contentDescription = "Tren 6 Bulan",
                                    tint = CreditCardRoyalBlue
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Tren Pengeluaran & Cicilan (6 Bulan)",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        color = AppThemeColors.textPrimary
                                    )
                                    Text(
                                        text = "Penurunan cicilan utang bulan ke bulan (Metode Snowball)",
                                        fontFamily = PlusJakartaSans,
                                        fontSize = 12.sp,
                                        color = AppThemeColors.textSecondary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            SixMonthStackedBarChart(bars = sixMonthsHistory)

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                LegendDot(color = CreditCardRoyalBlue, label = "Cicilan Utang")
                                Spacer(modifier = Modifier.width(20.dp))
                                LegendDot(color = ProgressGreen, label = "Kebutuhan & Rutin")
                            }
                        }
                    }
                }

                // 3. Category Distribution Donut Chart & Breakdown
                item {
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = AppThemeColors.cardSurface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.PieChart,
                                    contentDescription = "Distribusi Kategori",
                                    tint = AppThemeColors.linkBlue
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Komposisi Pengeluaran Bulan Ini",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = AppThemeColors.textPrimary
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            CategoryDonutBreakdown(
                                breakdown = categoryBreakdown,
                                total = totalCurrentMonthOutflow.coerceAtLeast(1L)
                            )
                        }
                    }
                }

                // 4. List of Monthly Expense Items
                item {
                    Text(
                        text = "Rincian Pos Pengeluaran Bulanan (Okt 2026)",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = AppThemeColors.textPrimary
                    )
                }

                items(currentMonthExpenses, key = { it.id }) { expense ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = AppThemeColors.cardSurface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = expense.title,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.5.sp,
                                    color = AppThemeColors.textPrimary
                                )
                                Text(
                                    text = "${expense.category} • ${expense.monthLabel} 2026",
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 12.sp,
                                    color = AppThemeColors.textSecondary
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = CurrencyFormatter.formatRupiah(expense.amount),
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.5.sp,
                                    color = AppThemeColors.linkBlue
                                )
                                IconButton(onClick = { onDeleteExpense(expense) }) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Hapus Pengeluaran",
                                        tint = AlertRed.copy(alpha = 0.7f),
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // FAB to Add New Monthly Expense
        FloatingActionButton(
            onClick = onAddExpenseClick,
            containerColor = CreditCardRoyalBlue,
            contentColor = Color.White,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("add_expense_button")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Add, contentDescription = "Catat Pengeluaran")
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Catat Pengeluaran",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun SixMonthStackedBarChart(bars: List<MonthStatBar>) {
    val maxTotal = (bars.maxOfOrNull { it.total } ?: 15_000_000L).coerceAtLeast(1L)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        bars.forEach { bar ->
            val totalRatio = (bar.total.toFloat() / maxTotal.toFloat()).coerceIn(0.15f, 1f)
            val debtRatio = if (bar.total == 0L) 0.5f else (bar.debtExpense.toFloat() / bar.total.toFloat()).coerceIn(0f, 1f)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = CurrencyFormatter.formatCompactRupiah(bar.total).removePrefix("Rp"),
                    fontFamily = PlusJakartaSans,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppThemeColors.textSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Canvas(
                    modifier = Modifier
                        .width(26.dp)
                        .height(115.dp)
                ) {
                    val fullH = size.height
                    val barH = fullH * totalRatio
                    val debtH = barH * debtRatio
                    val livingH = barH - debtH
                    val topY = fullH - barH

                    drawRoundRect(
                        color = ProgressGreen,
                        topLeft = Offset(0f, topY),
                        size = Size(size.width, livingH + 6.dp.toPx()),
                        cornerRadius = CornerRadius(14f, 14f)
                    )
                    drawRoundRect(
                        color = CreditCardRoyalBlue,
                        topLeft = Offset(0f, topY + livingH),
                        size = Size(size.width, debtH),
                        cornerRadius = CornerRadius(14f, 14f)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = bar.monthLabel,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = AppThemeColors.textPrimary
                )
            }
        }
    }
}

@Composable
private fun CategoryDonutBreakdown(
    breakdown: List<Pair<String, Long>>,
    total: Long
) {
    val palette = listOf(
        CreditCardRoyalBlue,
        ProgressGreen,
        WarningAmber,
        Color(0xFF38BDF8),
        Color(0xFF8B5CF6)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(120.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(104.dp)) {
                var startAngle = -90f
                breakdown.forEachIndexed { index, (_, amount) ->
                    val sweep = (amount.toFloat() / total.toFloat()) * 360f
                    drawArc(
                        color = palette[index % palette.size],
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        style = Stroke(width = 18.dp.toPx(), cap = StrokeCap.Butt)
                    )
                    startAngle += sweep
                }
            }
            Text(
                text = "${breakdown.size} Pos",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp,
                color = AppThemeColors.textPrimary
            )
        }

        Spacer(modifier = Modifier.width(18.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            breakdown.forEachIndexed { index, (cat, amount) ->
                val pct = ((amount.toDouble() / total.toDouble()) * 100).roundToInt()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(palette[index % palette.size])
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = cat,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = AppThemeColors.textPrimary
                        )
                    }
                    Text(
                        text = "$pct% (${CurrencyFormatter.formatCompactRupiah(amount)})",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp,
                        color = AppThemeColors.textSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            color = AppThemeColors.textPrimary
        )
    }
}
