package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.CurrencyFormatter
import com.example.ui.AppScreen
import com.example.ui.CloudSyncState
import com.example.ui.DashboardSummary
import com.example.ui.theme.ActionBlueEnd
import com.example.ui.theme.ActionBlueStart
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AppThemeColors
import com.example.ui.theme.BottomNavInactive
import com.example.ui.theme.CreditCardRoyalBlue
import com.example.ui.theme.CreditCardTrackBlue
import com.example.ui.theme.HeaderBlueEnd
import com.example.ui.theme.HeaderBlueMid
import com.example.ui.theme.HeaderBlueStart
import com.example.ui.theme.MenuTileIconBlue
import com.example.ui.theme.MenuTileIconLightBlue
import com.example.ui.theme.NavyCardDark
import com.example.ui.theme.NavyCardDeep
import com.example.ui.theme.NavyTrackSlate
import com.example.ui.theme.PlusJakartaSans
import com.example.ui.theme.ProgressGreen
import com.example.ui.theme.RingTrackLight
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun HomeScreen(
    userName: String,
    summary: DashboardSummary,
    cloudSyncState: CloudSyncState,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    onNavigate: (AppScreen, String) -> Unit,
    onLanjutBayarClick: () -> Unit,
    onSegarkanClick: () -> Unit,
    onCenterAddClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppThemeColors.screenBackground)
    ) {
        // Curved/Slanted Royal Blue Header Background matching the screenshot
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(310.dp)
        ) {
            val w = size.width
            val h = size.height
            val path = Path().apply {
                moveTo(0f, 0f)
                lineTo(w, 0f)
                lineTo(w, h * 0.78f)
                cubicTo(
                    w * 0.72f, h * 0.96f,
                    w * 0.32f, h * 0.62f,
                    0f, h * 0.56f
                )
                close()
            }
            val headerColors = if (isDarkMode) {
                listOf(Color(0xFF0E254D), Color(0xFF143C7D), Color(0xFF1A4F9E))
            } else {
                listOf(HeaderBlueStart, HeaderBlueMid, HeaderBlueEnd)
            }
            drawPath(
                path = path,
                brush = Brush.linearGradient(
                    colors = headerColors,
                    start = Offset(0f, 0f),
                    end = Offset(w, h)
                )
            )
        }

        // Main Scrollable Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .verticalScroll(scrollState)
                .padding(bottom = 120.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // 1. Top Greeting, Dark/Light Theme Toggle & Notification Bell Header
            TopGreetingHeader(
                userName = userName,
                dueSoonCount = summary.dueSoonCount,
                isDarkMode = isDarkMode,
                onToggleDarkMode = onToggleDarkMode,
                onProfileClick = { onNavigate(AppScreen.PENGATURAN_CLOUD, "Semua") },
                onNotificationClick = { onNavigate(AppScreen.JADWAL_CICILAN, "Semua") }
            )

            // Real-time Cloud & E2EE Sync Status Pill Banner
            AnimatedVisibility(
                visible = cloudSyncState.statusBannerMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF0F294A).copy(alpha = 0.92f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = "Status Enkripsi E2E",
                            tint = ProgressGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = cloudSyncState.statusBannerMessage ?: "",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = PlusJakartaSans
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 2. Hero Card: "Progres Bebas Utang" with Cutout "Lanjut Bayar ->" Button
            ProgresBebasUtangHeroCard(
                summary = summary,
                onLanjutBayarClick = onLanjutBayarClick,
                onCardClick = { onNavigate(AppScreen.RINCIAN_UTANG, "Semua") }
            )

            Spacer(modifier = Modifier.height(22.dp))

            // 3. Section: "Menu Utama" (8-item 2x4 grid)
            Text(
                text = "Menu Utama",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 19.sp,
                color = AppThemeColors.textPrimary,
                modifier = Modifier.padding(horizontal = 22.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            MenuUtamaGrid(
                onRincianUtang = { onNavigate(AppScreen.RINCIAN_UTANG, "Semua") },
                onJadwalCicilan = { onNavigate(AppScreen.JADWAL_CICILAN, "Semua") },
                onLaporan = { onNavigate(AppScreen.LAPORAN_BULANAN, "Semua") },
                onPanduan = { onNavigate(AppScreen.PANDUAN, "Semua") },
                onPrioritasBayar = { onNavigate(AppScreen.PRIORITAS_BAYAR, "Semua") },
                onMetodeSnowball = { onNavigate(AppScreen.METODE_SNOWBALL, "Semua") },
                onSegarkan = onSegarkanClick,
                onPengaturan = { onNavigate(AppScreen.PENGATURAN_CLOUD, "Semua") }
            )

            Spacer(modifier = Modifier.height(26.dp))

            // 4. Section: "Kategori Utang" + "Lihat Semua"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Kategori Utang",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 19.sp,
                    color = AppThemeColors.textPrimary
                )
                Text(
                    text = "Lihat Semua",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.5.sp,
                    color = AppThemeColors.linkBlue,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onNavigate(AppScreen.RINCIAN_UTANG, "Semua") }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                        .testTag("lihat_semua_kategori_button")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Side-by-side Pinjol & Kartu Kredit Cards matching the screenshot
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                KategoriUtangCard(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("kategori_pinjol_card"),
                    title = "Pinjol",
                    activeCount = summary.activePinjolCount,
                    progressFraction = summary.pinjolProgressFraction.coerceAtLeast(0.24f),
                    backgroundColor = NavyCardDark,
                    trackColor = NavyTrackSlate,
                    progressColor = ProgressGreen,
                    onClick = { onNavigate(AppScreen.RINCIAN_UTANG, "Pinjol") }
                )

                KategoriUtangCard(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("kategori_kartu_kredit_card"),
                    title = "Kartu Kredit",
                    activeCount = summary.activeCreditCardCount,
                    progressFraction = summary.creditCardProgressFraction,
                    backgroundColor = CreditCardRoyalBlue,
                    trackColor = CreditCardTrackBlue,
                    progressColor = ProgressGreen,
                    onClick = { onNavigate(AppScreen.RINCIAN_UTANG, "Kartu Kredit") }
                )
            }
        }

        // 5. Bottom Navigation Bar with Raised Center Glossy "T" Logo Button
        BottomNavBarWithCenterEmblem(
            modifier = Modifier.align(Alignment.BottomCenter),
            currentScreen = AppScreen.HOME,
            onHomeClick = { /* Already on Home */ },
            onSearchClick = { onNavigate(AppScreen.SEARCH, "Semua") },
            onCenterLogoClick = onCenterAddClick,
            onCalendarClick = { onNavigate(AppScreen.JADWAL_CICILAN, "Semua") },
            onProfileClick = { onNavigate(AppScreen.PENGATURAN_CLOUD, "Semua") }
        )
    }
}

@Composable
private fun TopGreetingHeader(
    userName: String,
    dueSoonCount: Int,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    onProfileClick: () -> Unit,
    onNotificationClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(32.dp))
                .clickable { onProfileClick() }
                .padding(end = 6.dp)
                .testTag("header_profile_row")
        ) {
            Image(
                painter = painterResource(id = R.drawable.avatar_ricky),
                contentDescription = "Foto Profil $userName",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color(0xFF7CC2FF), CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Halo $userName,",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Langkah menuju bebas hutang",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp,
                    color = Color(0xFFE2EEFF)
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Quick Dark Mode / Light Mode Toggle Button
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.22f))
                    .clickable { onToggleDarkMode() }
                    .testTag("theme_toggle_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = if (isDarkMode) "Aktifkan Mode Terang" else "Aktifkan Mode Gelap",
                    tint = if (isDarkMode) Color(0xFFFDE047) else Color.White,
                    modifier = Modifier.size(23.dp)
                )
            }

            // Glass Notification Bell Button with Red Badge Dot
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.22f))
                    .clickable { onNotificationClick() }
                    .testTag("notification_bell_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Pengingat Jatuh Tempo ($dueSoonCount aktif)",
                    tint = Color.White,
                    modifier = Modifier.size(25.dp)
                )
                if (dueSoonCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-10).dp, y = 10.dp)
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(AlertRed)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgresBebasUtangHeroCard(
    summary: DashboardSummary,
    onLanjutBayarClick: () -> Unit,
    onCardClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        // Main Dark Navy Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(196.dp)
                .clip(RoundedCornerShape(26.dp))
                .clickable { onCardClick() }
                .testTag("progres_bebas_utang_card"),
            color = NavyCardDark,
            shape = RoundedCornerShape(26.dp),
            shadowElevation = 6.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 22.dp, end = 20.dp, top = 20.dp, bottom = 36.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular 24% Progress Ring
                Box(
                    modifier = Modifier.size(104.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(96.dp)) {
                        val stroke = 11.dp.toPx()
                        drawArc(
                            color = RingTrackLight,
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                        val sweep = (summary.progressFraction * 360f).coerceIn(18f, 360f)
                        drawArc(
                            color = ProgressGreen,
                            startAngle = -90f,
                            sweepAngle = sweep,
                            useCenter = false,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                    }
                    Text(
                        text = "${summary.progressPercent}%",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                // Right Column Details
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Progres Bebas Utang",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.5.sp,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                SpanStyle(
                                    color = ProgressGreen,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            ) {
                                append("${summary.paidOffCount} ")
                            }
                            withStyle(
                                SpanStyle(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                append("dari ${summary.totalDebtsCount} Utang ")
                            }
                            withStyle(
                                SpanStyle(
                                    color = Color(0xFF94A3B8),
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                append("Lunas")
                            }
                        },
                        fontFamily = PlusJakartaSans,
                        fontSize = 15.5.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Horizontal Progress Bar inside Hero Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .height(9.dp)
                            .clip(CircleShape)
                            .background(NavyTrackSlate)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(summary.progressFraction.coerceIn(0.08f, 1f))
                                .height(9.dp)
                                .clip(CircleShape)
                                .background(ProgressGreen)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                SpanStyle(
                                    color = Color(0xFFA3B8D8),
                                    fontWeight = FontWeight.Medium
                                )
                            ) {
                                append("Sisa ")
                            }
                            withStyle(
                                SpanStyle(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                append(CurrencyFormatter.formatRupiah(summary.remainingRupiah))
                            }
                        },
                        fontFamily = PlusJakartaSans,
                        fontSize = 13.5.sp
                    )
                }
            }
        }

        // Bottom-Right Cutout Notch + "Lanjut Bayar ->" Pill Button
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(y = 6.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 26.dp,
                        bottomStart = 26.dp,
                        topEnd = 0.dp,
                        bottomEnd = 24.dp
                    )
                )
                .background(AppThemeColors.cutoutNotchBg)
                .padding(start = 10.dp, top = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .height(50.dp)
                    .shadow(6.dp, RoundedCornerShape(25.dp))
                    .clip(RoundedCornerShape(25.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(ActionBlueStart, ActionBlueEnd)
                        )
                    )
                    .clickable { onLanjutBayarClick() }
                    .padding(start = 20.dp, end = 7.dp)
                    .testTag("lanjut_bayar_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Lanjut Bayar",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.5.sp,
                        color = Color.White
                    )
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(NavyCardDeep),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Lanjut Bayar",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuUtamaGrid(
    onRincianUtang: () -> Unit,
    onJadwalCicilan: () -> Unit,
    onLaporan: () -> Unit,
    onPanduan: () -> Unit,
    onPrioritasBayar: () -> Unit,
    onMetodeSnowball: () -> Unit,
    onSegarkan: () -> Unit,
    onPengaturan: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Row 1
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            MenuTileCard(
                modifier = Modifier
                    .weight(1f)
                    .testTag("menu_rincian_utang"),
                label = "Rincian Utang",
                isPrimaryDark = true,
                onClick = onRincianUtang
            ) {
                InvoiceDollarVectorIcon()
            }

            MenuTileCard(
                modifier = Modifier
                    .weight(1f)
                    .testTag("menu_jadwal_cicilan"),
                label = "Jadwal Cicilan",
                isPrimaryDark = false,
                onClick = onJadwalCicilan
            ) {
                CalendarCheckVectorIcon()
            }

            MenuTileCard(
                modifier = Modifier
                    .weight(1f)
                    .testTag("menu_laporan"),
                label = "Laporan",
                isPrimaryDark = false,
                onClick = onLaporan
            ) {
                PieChartVectorIcon()
            }

            MenuTileCard(
                modifier = Modifier
                    .weight(1f)
                    .testTag("menu_panduan"),
                label = "Panduan",
                isPrimaryDark = false,
                onClick = onPanduan
            ) {
                OpenBookVectorIcon()
            }
        }

        // Row 2
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            MenuTileCard(
                modifier = Modifier
                    .weight(1f)
                    .testTag("menu_prioritas_bayar"),
                label = "Prioritas\nBayar",
                isPrimaryDark = false,
                onClick = onPrioritasBayar
            ) {
                TargetBullseyeVectorIcon()
            }

            MenuTileCard(
                modifier = Modifier
                    .weight(1f)
                    .testTag("menu_metode_snowball"),
                label = "Metode\nSnowball",
                isPrimaryDark = false,
                onClick = onMetodeSnowball
            ) {
                SnowflakeVectorIcon()
            }

            MenuTileCard(
                modifier = Modifier
                    .weight(1f)
                    .testTag("menu_segarkan"),
                label = "Segarkan",
                isPrimaryDark = false,
                onClick = onSegarkan
            ) {
                RefreshSyncVectorIcon()
            }

            MenuTileCard(
                modifier = Modifier
                    .weight(1f)
                    .testTag("menu_pengaturan"),
                label = "Pengaturan",
                isPrimaryDark = false,
                onClick = onPengaturan
            ) {
                GearSettingsVectorIcon()
            }
        }
    }
}

@Composable
private fun MenuTileCard(
    modifier: Modifier = Modifier,
    label: String,
    isPrimaryDark: Boolean,
    onClick: () -> Unit,
    iconContent: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .height(98.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPrimaryDark) AppThemeColors.menuTilePrimaryBackground else AppThemeColors.menuTileBackground
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isPrimaryDark) 4.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                iconContent()
            }
            Text(
                text = label,
                fontFamily = PlusJakartaSans,
                fontWeight = if (isPrimaryDark) FontWeight.Bold else FontWeight.SemiBold,
                fontSize = 11.5.sp,
                lineHeight = 14.sp,
                color = if (isPrimaryDark) Color.White else AppThemeColors.textPrimary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun KategoriUtangCard(
    modifier: Modifier = Modifier,
    title: String,
    activeCount: Int,
    progressFraction: Float,
    backgroundColor: Color,
    trackColor: Color,
    progressColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(132.dp)
            .clip(RoundedCornerShape(22.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = title,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "*$activeCount Utang aktif",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.5.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }

            // Bottom Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(trackColor)
            ) {
                if (activeCount > 0 && progressFraction > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progressFraction.coerceIn(0.15f, 1f))
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(progressColor)
                    )
                }
            }
        }
    }
}

@Composable
fun BottomNavBarWithCenterEmblem(
    modifier: Modifier = Modifier,
    currentScreen: AppScreen,
    onHomeClick: () -> Unit,
    onSearchClick: () -> Unit,
    onCenterLogoClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Rounded Bottom Bar Surface
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(16.dp, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)),
            color = AppThemeColors.bottomBarSurface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .height(74.dp)
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable { onHomeClick() }
                        .testTag("bottom_nav_home"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Beranda",
                        tint = if (currentScreen == AppScreen.HOME) AppThemeColors.bottomNavActive else BottomNavInactive,
                        modifier = Modifier.size(30.dp)
                    )
                }

                // Search Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable { onSearchClick() }
                        .testTag("bottom_nav_search"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Cari Utang",
                        tint = if (currentScreen == AppScreen.SEARCH) AppThemeColors.bottomNavActive else BottomNavInactive,
                        modifier = Modifier.size(29.dp)
                    )
                }

                // Center Spacer for Floating "T" Emblem
                Spacer(modifier = Modifier.width(76.dp))

                // Calendar Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable { onCalendarClick() }
                        .testTag("bottom_nav_calendar"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Jadwal & Pengingat",
                        tint = if (currentScreen == AppScreen.JADWAL_CICILAN) AppThemeColors.bottomNavActive else BottomNavInactive,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Profile Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable { onProfileClick() }
                        .testTag("bottom_nav_profile"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profil & Enkripsi",
                        tint = if (currentScreen == AppScreen.PENGATURAN_CLOUD) AppThemeColors.bottomNavActive else BottomNavInactive,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        }

        // Raised Center Glossy Blue "T" Button matching the uploaded photo
        Box(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.navigationBars)
                .offset(y = (-14).dp)
                .size(84.dp)
                .shadow(10.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(AppThemeColors.centerButtonRing)
                .clickable { onCenterLogoClick() }
                .padding(5.dp)
                .testTag("center_t_action_button"),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_center_t_logo),
                contentDescription = "Tambah Utang / Aksi Cepat",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp))
            )
        }
    }
}

// ============================================================================
// Custom Vector/Canvas Icons for the 8 Menu Utama Tiles (matching screenshot)
// ============================================================================

@Composable
private fun InvoiceDollarVectorIcon() {
    Box(modifier = Modifier.size(38.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(34.dp)) {
            val w = size.width
            val h = size.height
            val path = Path().apply {
                moveTo(w * 0.18f, h * 0.08f)
                lineTo(w * 0.62f, h * 0.08f)
                lineTo(w * 0.84f, h * 0.30f)
                lineTo(w * 0.84f, h * 0.92f)
                lineTo(w * 0.18f, h * 0.92f)
                close()
            }
            drawPath(path, color = Color(0xFF67B2FF))
            val fold = Path().apply {
                moveTo(w * 0.62f, h * 0.08f)
                lineTo(w * 0.62f, h * 0.30f)
                lineTo(w * 0.84f, h * 0.30f)
                close()
            }
            drawPath(fold, color = Color(0xFF2877EE))
            drawRoundRect(
                color = NavyCardDark,
                topLeft = Offset(w * 0.28f, h * 0.20f),
                size = Size(w * 0.22f, h * 0.06f),
                cornerRadius = CornerRadius(4f, 4f)
            )
            drawRoundRect(
                color = NavyCardDark,
                topLeft = Offset(w * 0.28f, h * 0.31f),
                size = Size(w * 0.18f, h * 0.06f),
                cornerRadius = CornerRadius(4f, 4f)
            )
        }
        Text(
            text = "$",
            color = NavyCardDark,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 15.sp,
            style = TextStyle(fontFamily = PlusJakartaSans),
            modifier = Modifier.offset(y = 5.dp)
        )
    }
}

@Composable
private fun CalendarCheckVectorIcon() {
    Canvas(modifier = Modifier.size(34.dp)) {
        val w = size.width
        val h = size.height
        drawRoundRect(
            color = MenuTileIconLightBlue,
            topLeft = Offset(w * 0.10f, h * 0.20f),
            size = Size(w * 0.80f, h * 0.70f),
            cornerRadius = CornerRadius(12f, 12f)
        )
        drawRoundRect(
            color = MenuTileIconBlue,
            topLeft = Offset(w * 0.10f, h * 0.20f),
            size = Size(w * 0.80f, h * 0.22f),
            cornerRadius = CornerRadius(12f, 12f)
        )
        drawRoundRect(
            color = MenuTileIconBlue,
            topLeft = Offset(w * 0.26f, h * 0.10f),
            size = Size(w * 0.10f, h * 0.16f),
            cornerRadius = CornerRadius(4f, 4f)
        )
        drawRoundRect(
            color = MenuTileIconBlue,
            topLeft = Offset(w * 0.64f, h * 0.10f),
            size = Size(w * 0.10f, h * 0.16f),
            cornerRadius = CornerRadius(4f, 4f)
        )
        val check = Path().apply {
            moveTo(w * 0.34f, h * 0.62f)
            lineTo(w * 0.46f, h * 0.74f)
            lineTo(w * 0.68f, h * 0.50f)
        }
        drawPath(
            path = check,
            color = MenuTileIconBlue,
            style = Stroke(width = 3.2.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun PieChartVectorIcon() {
    Canvas(modifier = Modifier.size(34.dp)) {
        val w = size.width
        val h = size.height
        drawArc(
            color = MenuTileIconLightBlue,
            startAngle = 15f,
            sweepAngle = 255f,
            useCenter = true,
            topLeft = Offset(w * 0.08f, h * 0.14f),
            size = Size(w * 0.76f, h * 0.76f)
        )
        drawArc(
            color = MenuTileIconBlue,
            startAngle = -85f,
            sweepAngle = 92f,
            useCenter = true,
            topLeft = Offset(w * 0.16f, h * 0.06f),
            size = Size(w * 0.76f, h * 0.76f)
        )
    }
}

@Composable
private fun OpenBookVectorIcon() {
    Canvas(modifier = Modifier.size(34.dp)) {
        val w = size.width
        val h = size.height
        val leftPage = Path().apply {
            moveTo(w * 0.10f, h * 0.22f)
            cubicTo(w * 0.25f, h * 0.16f, w * 0.38f, h * 0.20f, w * 0.48f, h * 0.26f)
            lineTo(w * 0.48f, h * 0.82f)
            cubicTo(w * 0.38f, h * 0.76f, w * 0.25f, h * 0.74f, w * 0.10f, h * 0.80f)
            close()
        }
        drawPath(leftPage, color = MenuTileIconLightBlue)

        val rightPage = Path().apply {
            moveTo(w * 0.90f, h * 0.22f)
            cubicTo(w * 0.75f, h * 0.16f, w * 0.62f, h * 0.20f, w * 0.52f, h * 0.26f)
            lineTo(w * 0.52f, h * 0.82f)
            cubicTo(w * 0.62f, h * 0.76f, w * 0.75f, h * 0.74f, w * 0.90f, h * 0.80f)
            close()
        }
        drawPath(rightPage, color = MenuTileIconBlue)
    }
}

@Composable
private fun TargetBullseyeVectorIcon() {
    Canvas(modifier = Modifier.size(34.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val r = size.minDimension / 2f
        drawCircle(color = MenuTileIconLightBlue.copy(alpha = 0.45f), radius = r * 0.90f, center = center)
        drawCircle(
            color = MenuTileIconBlue,
            radius = r * 0.86f,
            center = center,
            style = Stroke(width = 2.6.dp.toPx())
        )
        drawCircle(
            color = MenuTileIconBlue,
            radius = r * 0.54f,
            center = center,
            style = Stroke(width = 2.6.dp.toPx())
        )
        drawCircle(color = MenuTileIconBlue, radius = r * 0.22f, center = center)
    }
}

@Composable
private fun SnowflakeVectorIcon() {
    Canvas(modifier = Modifier.size(34.dp)) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val arm = size.minDimension * 0.40f
        val stroke = 2.6.dp.toPx()

        for (i in 0 until 6) {
            val angleRad = Math.toRadians((i * 60).toDouble())
            val ex = cx + (arm * cos(angleRad)).toFloat()
            val ey = cy + (arm * sin(angleRad)).toFloat()
            drawLine(
                color = MenuTileIconLightBlue,
                start = Offset(cx, cy),
                end = Offset(ex, ey),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
            val bx = cx + (arm * 0.62f * cos(angleRad)).toFloat()
            val by = cy + (arm * 0.62f * sin(angleRad)).toFloat()
            for (dir in listOf(-35, 35)) {
                val bAngle = Math.toRadians((i * 60 + dir).toDouble())
                val tipX = bx + (arm * 0.28f * cos(bAngle)).toFloat()
                val tipY = by + (arm * 0.28f * sin(bAngle)).toFloat()
                drawLine(
                    color = MenuTileIconBlue,
                    start = Offset(bx, by),
                    end = Offset(tipX, tipY),
                    strokeWidth = stroke * 0.85f,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
private fun RefreshSyncVectorIcon() {
    Canvas(modifier = Modifier.size(34.dp)) {
        val w = size.width
        val h = size.height
        val stroke = 3.5.dp.toPx()
        drawArc(
            color = MenuTileIconBlue,
            startAngle = -155f,
            sweepAngle = 135f,
            useCenter = false,
            topLeft = Offset(w * 0.16f, h * 0.16f),
            size = Size(w * 0.68f, h * 0.68f),
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
        drawArc(
            color = MenuTileIconLightBlue,
            startAngle = 25f,
            sweepAngle = 135f,
            useCenter = false,
            topLeft = Offset(w * 0.16f, h * 0.16f),
            size = Size(w * 0.68f, h * 0.68f),
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
        val topArrow = Path().apply {
            moveTo(w * 0.66f, h * 0.14f)
            lineTo(w * 0.86f, h * 0.30f)
            lineTo(w * 0.64f, h * 0.36f)
            close()
        }
        drawPath(topArrow, color = MenuTileIconBlue)
        val bottomArrow = Path().apply {
            moveTo(w * 0.34f, h * 0.86f)
            lineTo(w * 0.14f, h * 0.70f)
            lineTo(w * 0.36f, h * 0.64f)
            close()
        }
        drawPath(bottomArrow, color = MenuTileIconLightBlue)
    }
}

@Composable
private fun GearSettingsVectorIcon() {
    val holeColor = AppThemeColors.menuTileBackground
    Canvas(modifier = Modifier.size(34.dp)) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val rOuter = size.minDimension * 0.42f
        val rInner = size.minDimension * 0.30f
        for (i in 0 until 8) {
            val angle = Math.toRadians((i * 45).toDouble())
            val tx = cx + (rInner * cos(angle)).toFloat()
            val ty = cy + (rInner * sin(angle)).toFloat()
            drawCircle(
                color = MenuTileIconBlue,
                radius = size.minDimension * 0.11f,
                center = Offset(tx, ty)
            )
        }
        drawCircle(
            color = MenuTileIconLightBlue,
            radius = rOuter * 0.76f,
            center = Offset(cx, cy)
        )
        drawCircle(
            color = holeColor,
            radius = rOuter * 0.34f,
            center = Offset(cx, cy)
        )
    }
}
