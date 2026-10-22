package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CurrencyFormatter
import com.example.data.DebtEntity
import com.example.ui.AppScreen
import com.example.ui.theme.AppThemeColors
import com.example.ui.theme.CreditCardRoyalBlue
import com.example.ui.theme.HeaderBlueEnd
import com.example.ui.theme.HeaderBlueStart
import com.example.ui.theme.NavyCardDark
import com.example.ui.theme.PlusJakartaSans
import com.example.ui.theme.ProgressGreen
import kotlin.math.ceil

@Composable
fun SnowballPriorityScreen(
    mode: AppScreen,
    debts: List<DebtEntity>,
    extraBudget: Long,
    onExtraBudgetChange: (Long) -> Unit,
    onPayDebt: (DebtEntity) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    var strategyType by remember(mode) {
        mutableStateOf(if (mode == AppScreen.PRIORITAS_BAYAR) "AVALANCHE" else "SNOWBALL")
    }

    val activeDebts = remember(debts, strategyType) {
        val list = debts.filter { !it.isPaidOff }
        if (strategyType == "SNOWBALL") {
            // Smallest remaining balance first
            list.sortedWith(compareBy<DebtEntity> { it.remainingAmount }.thenByDescending { it.interestRatePercent })
        } else {
            // Highest interest & nearest due date first
            list.sortedWith(compareByDescending<DebtEntity> { it.interestRatePercent }.thenBy { it.dueDayOfMonth })
        }
    }

    val totalRemaining = remember(activeDebts) { activeDebts.sumOf { it.remainingAmount } }
    val baseMonthlyInstallment = remember(activeDebts) { activeDebts.sumOf { it.monthlyInstallment }.coerceAtLeast(1_000_000L) }
    val estimatedMonthsNormal = remember(totalRemaining, baseMonthlyInstallment) {
        ceil(totalRemaining.toDouble() / (baseMonthlyInstallment.toDouble() * 0.45)).toInt().coerceAtLeast(1)
    }
    val estimatedMonthsAccelerated = remember(totalRemaining, baseMonthlyInstallment, extraBudget) {
        ceil(totalRemaining.toDouble() / ((baseMonthlyInstallment * 0.45) + extraBudget.toDouble())).toInt().coerceAtLeast(1)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppThemeColors.screenBackground)
    ) {
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
                    modifier = Modifier.testTag("back_button_snowball")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = Color.White
                    )
                }
                Column {
                    Text(
                        text = when (mode) {
                            AppScreen.PANDUAN -> "Panduan Strategi Bebas Utang"
                            AppScreen.PRIORITAS_BAYAR -> "Prioritas Bayar & Urutan Target"
                            else -> "Simulator Metode Snowball"
                        },
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 19.sp,
                        color = Color.White
                    )
                    Text(
                        text = "Strategi pelunasan sistematis tanpa gali lubang tutup lubang",
                        fontFamily = PlusJakartaSans,
                        fontSize = 12.5.sp,
                        color = Color(0xFFDCEBFF)
                    )
                }
            }
        }

        if (mode == AppScreen.PANDUAN) {
            PanduanBebasUtangContent()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Strategy Switcher Card
                item {
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = NavyCardDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                FilterChip(
                                    selected = strategyType == "SNOWBALL",
                                    onClick = { strategyType = "SNOWBALL" },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.AcUnit,
                                            contentDescription = "Snowball",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = "Metode Snowball (Saldo Terkecil)",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ProgressGreen,
                                        selectedLabelColor = Color.White,
                                        labelColor = Color.White
                                    )
                                )
                                FilterChip(
                                    selected = strategyType == "AVALANCHE",
                                    onClick = { strategyType = "AVALANCHE" },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.TrackChanges,
                                            contentDescription = "Prioritas Bunga",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = "Prioritas Bunga",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CreditCardRoyalBlue,
                                        selectedLabelColor = Color.White,
                                        labelColor = Color.White
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Simulasi Dana Ekstra Gulungan Snowball: +${CurrencyFormatter.formatRupiah(extraBudget)} / bln",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = Color.White
                            )

                            Slider(
                                value = extraBudget.toFloat(),
                                onValueChange = { onExtraBudgetChange((it / 250_000f).toLong() * 250_000L) },
                                valueRange = 0f..10_000_000f,
                                colors = SliderDefaults.colors(
                                    thumbColor = ProgressGreen,
                                    activeTrackColor = ProgressGreen
                                )
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Estimasi Lunas Normal",
                                        fontFamily = PlusJakartaSans,
                                        fontSize = 11.5.sp,
                                        color = Color(0xFFA3B8D8)
                                    )
                                    Text(
                                        text = "$estimatedMonthsNormal Bulan",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color.White
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Dengan Snowball Ekstra",
                                        fontFamily = PlusJakartaSans,
                                        fontSize = 11.5.sp,
                                        color = Color(0xFFA3B8D8)
                                    )
                                    Text(
                                        text = "$estimatedMonthsAccelerated Bulan (Hemat ${(estimatedMonthsNormal - estimatedMonthsAccelerated).coerceAtLeast(0)} bln!)",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp,
                                        color = ProgressGreen
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = if (strategyType == "SNOWBALL") {
                            "Urutan Eksekusi Snowball (Lunasi dari Sisa Terkecil)"
                        } else {
                            "Urutan Prioritas Bayar (Bunga Tertinggi & Jatuh Tempo Terdekat)"
                        },
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.5.sp,
                        color = AppThemeColors.textPrimary
                    )
                }

                itemsIndexed(activeDebts, key = { _, d -> d.id }) { index, debt ->
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (index == 0) AppThemeColors.highlightedCardSurface else AppThemeColors.cardSurface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (index == 0) ProgressGreen else NavyCardDark),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "#${index + 1}",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = debt.creditorName,
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 15.5.sp,
                                            color = AppThemeColors.textPrimary
                                        )
                                        if (index == 0) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                color = ProgressGreen,
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = "FOKUS #1",
                                                    color = Color.White,
                                                    fontFamily = PlusJakartaSans,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 9.5.sp,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = "Sisa ${CurrencyFormatter.formatRupiah(debt.remainingAmount)} • Bunga ${debt.interestRatePercent}%/bln",
                                        fontFamily = PlusJakartaSans,
                                        fontSize = 12.sp,
                                        color = AppThemeColors.textSecondary
                                    )
                                }
                            }

                            Button(
                                onClick = { onPayDebt(debt) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (index == 0) ProgressGreen else CreditCardRoyalBlue
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Payment,
                                    contentDescription = "Bayar",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Bayar",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PanduanBebasUtangContent() {
    val guides = listOf(
        Triple(
            "1. Hentikan Gali Lubang Tutup Lubang",
            "Jangan pernah mengambil pinjaman baru untuk membayar cicilan lama. Fokuskan seluruh arus kas bersih pada daftar utang yang sudah tercatat di aplikasi BebasUtang.",
            NavyCardDark
        ),
        Triple(
            "2. Eksekusi Metode Debt Snowball",
            "Bayar cicilan minimum pada seluruh utang, lalu kerahkan seluruh dana ekstra untuk melunasi utang dengan sisa saldo terkecil lebih dulu. Saat utang #1 lunas, gulung anggaran cicilannya ke utang #2!",
            CreditCardRoyalBlue
        ),
        Triple(
            "3. Negosiasi Restrukturisasi & Keringanan Pokok",
            "Untuk Pinjol legal OJK atau Kartu Kredit yang berat, ajukan permohonan restrukturisasi penghapusan denda/bunga dan fokus pelunasan pokok satu per satu.",
            ProgressGreen
        ),
        Triple(
            "4. Jaga Rasio Pengeluaran Bulanan < 50%",
            "Gunakan menu Laporan Bulanan untuk memantau rasio kebutuhan pokok vs cicilan utang agar kebutuhan dapur dan keluarga tetap aman selama masa pelunasan.",
            NavyCardDark
        )
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        itemsIndexed(guides) { _, (title, body, badgeColor) ->
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = AppThemeColors.cardSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(badgeColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = title,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = AppThemeColors.textPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = body,
                        fontFamily = PlusJakartaSans,
                        fontSize = 13.5.sp,
                        lineHeight = 20.sp,
                        color = AppThemeColors.textSecondary
                    )
                }
            }
        }
    }
}
