package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppScreen
import com.example.ui.theme.AppThemeColors
import com.example.ui.theme.CreditCardRoyalBlue
import com.example.ui.theme.NavyCardDark
import com.example.ui.theme.PlusJakartaSans
import com.example.ui.theme.ProgressGreen

@Composable
fun QuickFeatureJumpBar(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit,
    onAddDebtClick: () -> Unit
) {
    Surface(
        color = AppThemeColors.cardSurface,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            QuickJumpPill(
                label = "Beranda",
                icon = Icons.Default.Home,
                selected = currentScreen == AppScreen.HOME,
                onClick = { onNavigate(AppScreen.HOME) },
                testTag = "quick_nav_home"
            )
            QuickJumpPill(
                label = "Daftar Utang",
                icon = Icons.Default.ReceiptLong,
                selected = currentScreen == AppScreen.RINCIAN_UTANG || currentScreen == AppScreen.SEARCH,
                onClick = { onNavigate(AppScreen.RINCIAN_UTANG) },
                testTag = "quick_nav_debts"
            )
            QuickJumpPill(
                label = "Jatuh Tempo",
                icon = Icons.Default.CalendarMonth,
                selected = currentScreen == AppScreen.JADWAL_CICILAN,
                onClick = { onNavigate(AppScreen.JADWAL_CICILAN) },
                testTag = "quick_nav_schedule"
            )
            QuickJumpPill(
                label = "Statistik",
                icon = Icons.Default.Insights,
                selected = currentScreen == AppScreen.LAPORAN_BULANAN,
                onClick = { onNavigate(AppScreen.LAPORAN_BULANAN) },
                testTag = "quick_nav_stats"
            )
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onAddDebtClick() }
                    .testTag("quick_nav_add_debt"),
                color = ProgressGreen,
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = "Tambah Utang",
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "+ Utang Baru",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickJumpPill(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .testTag(testTag),
        color = if (selected) CreditCardRoyalBlue else AppThemeColors.menuTileBackground,
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) Color.White else AppThemeColors.textPrimary,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = label,
                fontFamily = PlusJakartaSans,
                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
                fontSize = 12.sp,
                color = if (selected) Color.White else AppThemeColors.textPrimary
            )
        }
    }
}

@Composable
fun QuickActionHubDialog(
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    onDismiss: () -> Unit,
    onAddDebt: () -> Unit,
    onAddExpense: () -> Unit,
    onPayPriorityDebt: () -> Unit,
    onOpenSchedule: () -> Unit,
    onOpenStats: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(26.dp),
        containerColor = AppThemeColors.cardSurface,
        title = {
            Column {
                Text(
                    text = "Menu Aksi Cepat BebasUtang",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.5.sp,
                    color = AppThemeColors.textPrimary
                )
                Text(
                    text = "Pilih tindakan atau navigasi langsung ke fitur utama",
                    fontFamily = PlusJakartaSans,
                    fontSize = 12.5.sp,
                    color = AppThemeColors.textSecondary
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                QuickActionRowCard(
                    title = "Tambah Catatan Utang Baru",
                    subtitle = "Atur pokok, cicilan & pengingat H-1/H-3/H-7 (Push/Email)",
                    icon = Icons.Default.AddCircle,
                    accentColor = NavyCardDark,
                    onClick = {
                        onDismiss()
                        onAddDebt()
                    },
                    testTag = "hub_add_debt"
                )
                QuickActionRowCard(
                    title = "Bayar Cicilan Prioritas Sekarang",
                    subtitle = "Catat pembayaran angsuran & perbarui progres bebas utang",
                    icon = Icons.Default.Payment,
                    accentColor = ProgressGreen,
                    onClick = {
                        onDismiss()
                        onPayPriorityDebt()
                    },
                    testTag = "hub_pay_priority"
                )
                QuickActionRowCard(
                    title = "Daftar Jatuh Tempo & Notifikasi",
                    subtitle = "Kelola jadwal H-1, H-3, H-7 via Push Notification & Email",
                    icon = Icons.Default.CalendarMonth,
                    accentColor = CreditCardRoyalBlue,
                    onClick = {
                        onDismiss()
                        onOpenSchedule()
                    },
                    testTag = "hub_open_schedule"
                )
                QuickActionRowCard(
                    title = "Statistik & Catat Pengeluaran Bulanan",
                    subtitle = "Lihat grafik arus kas 6 bulan atau tambah pengeluaran baru",
                    icon = Icons.Default.Insights,
                    accentColor = Color(0xFF0284C7),
                    onClick = {
                        onDismiss()
                        onOpenStats()
                    },
                    testTag = "hub_open_stats"
                )
                QuickActionRowCard(
                    title = if (isDarkMode) "Beralih ke Mode Terang (Light Mode)" else "Beralih ke Mode Gelap (Dark Mode)",
                    subtitle = "Nyaman di mata saat digunakan pada kondisi cahaya rendah",
                    icon = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                    accentColor = Color(0xFF6366F1),
                    onClick = {
                        onToggleDarkMode()
                    },
                    testTag = "hub_toggle_theme"
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onDismiss()
                onAddExpense()
            }) {
                Text(
                    text = "+ Catat Pengeluaran",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.ExtraBold,
                    color = AppThemeColors.linkBlue
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Tutup",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    color = AppThemeColors.textSecondary
                )
            }
        }
    )
}

@Composable
private fun QuickActionRowCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppThemeColors.subtleSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(accentColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.5.sp,
                    color = AppThemeColors.textPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontFamily = PlusJakartaSans,
                    fontSize = 11.5.sp,
                    lineHeight = 15.sp,
                    color = AppThemeColors.textSecondary
                )
            }
        }
    }
}
