package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AlarmOn
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CurrencyFormatter
import com.example.data.DebtEntity
import com.example.data.NotificationChannelMethod
import com.example.data.NotificationPreferences
import com.example.data.ReminderDispatchLog
import com.example.notification.ReminderScheduler
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

@Composable
fun ScheduleReminderScreen(
    debts: List<DebtEntity>,
    notificationPrefs: NotificationPreferences,
    dispatchLogs: List<ReminderDispatchLog>,
    onUpdateGlobalPrefs: (
        masterEnabled: Boolean,
        pushEnabled: Boolean,
        emailEnabled: Boolean,
        recipientEmail: String,
        activeTimingsDays: Set<Int>,
        reminderHour: Int,
        applyToAllActiveDebts: Boolean
    ) -> Unit,
    onUpdateSingleDebtReminder: (DebtEntity, Boolean, Set<Int>, String) -> Unit,
    onTriggerTestPush: () -> Boolean,
    onTriggerEmailReminder: (Boolean) -> Pair<String, String>,
    onPayDebt: (DebtEntity) -> Unit,
    onNavigate: (AppScreen) -> Unit,
    onAddDebtClick: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    var selectedDayOfMonth by remember { mutableIntStateOf(0) } // 0 = Semua Tanggal
    var notificationStatusBanner by remember { mutableStateOf<String?>(null) }
    var emailInput by remember(notificationPrefs.recipientEmail) {
        mutableStateOf(notificationPrefs.recipientEmail)
    }
    var emailPreviewPair by remember { mutableStateOf<Pair<String, String>?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val sent = onTriggerTestPush()
            notificationStatusBanner = if (sent) {
                "✅ Push Notification berhasil dikirim ke bilah status Android!"
            } else {
                "Izin diberikan. Pengingat Push otomatis telah dijadwalkan."
            }
        } else {
            notificationStatusBanner = "Izin notifikasi diperlukan untuk Push Notification."
        }
    }

    val activeDebts = remember(debts, selectedDayOfMonth) {
        val base = debts.filter { !it.isPaidOff }.sortedBy { it.dueDayOfMonth }
        if (selectedDayOfMonth == 0) base else base.filter { it.dueDayOfMonth == selectedDayOfMonth }
    }

    val dueDaysMap = remember(debts) {
        debts.filter { !it.isPaidOff }.groupBy { it.dueDayOfMonth }
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
                    modifier = Modifier.testTag("back_button_jadwal")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = Color.White
                    )
                }
                Column {
                    Text(
                        text = "Jadwal Jatuh Tempo & Notifikasi",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.5.sp,
                        color = Color.White
                    )
                    Text(
                        text = "Pengingat Fleksibel (H-1, H-3, H-7) via Push & Email",
                        fontFamily = PlusJakartaSans,
                        fontSize = 12.5.sp,
                        color = Color(0xFFDCEBFF)
                    )
                }
            }
        }

        // Quick-Jump Navigation Bar for effortless navigation
        QuickFeatureJumpBar(
            currentScreen = AppScreen.JADWAL_CICILAN,
            onNavigate = onNavigate,
            onAddDebtClick = onAddDebtClick
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Flexible Notification Configuration Center Card
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyCardDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        // Header Row with Master Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AlarmOn,
                                    contentDescription = "Pengingat Otomatis",
                                    tint = ProgressGreen,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Sistem Pengingat Jatuh Tempo",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Aktif: ${notificationPrefs.timingsSummary} • ${notificationPrefs.methodLabel}",
                                        fontFamily = PlusJakartaSans,
                                        fontSize = 11.5.sp,
                                        color = Color(0xFFA3B8D8)
                                    )
                                }
                            }
                            Switch(
                                checked = notificationPrefs.masterEnabled,
                                onCheckedChange = { enabled ->
                                    onUpdateGlobalPrefs(
                                        enabled,
                                        notificationPrefs.pushEnabled,
                                        notificationPrefs.emailEnabled,
                                        emailInput,
                                        notificationPrefs.activeTimingsDays,
                                        notificationPrefs.reminderHour,
                                        true
                                    )
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = ProgressGreen
                                ),
                                modifier = Modifier.testTag("master_reminder_switch")
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // A. Multi-Select Timing Chips (Hari-H, H-1, H-3, H-7)
                        Text(
                            text = "1. Pilih Waktu Pengingatan (Bisa Pilih Lebih dari Satu):",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                1 to "H-1 (1 Hari Sebelum)",
                                3 to "H-3 (3 Hari Sebelum)",
                                7 to "H-7 (7 Hari Sebelum)",
                                0 to "Hari-H"
                            ).forEach { (days, label) ->
                                val isSelected = notificationPrefs.activeTimingsDays.contains(days)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        val nextSet = if (isSelected && notificationPrefs.activeTimingsDays.size > 1) {
                                            notificationPrefs.activeTimingsDays - days
                                        } else {
                                            notificationPrefs.activeTimingsDays + days
                                        }
                                        onUpdateGlobalPrefs(
                                            notificationPrefs.masterEnabled,
                                            notificationPrefs.pushEnabled,
                                            notificationPrefs.emailEnabled,
                                            emailInput,
                                            nextSet,
                                            notificationPrefs.reminderHour,
                                            false
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = label,
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.5.sp
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ProgressGreen,
                                        selectedLabelColor = Color.White,
                                        labelColor = Color.White.copy(alpha = 0.85f)
                                    ),
                                    modifier = Modifier.testTag("timing_chip_h$days")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // B. Reminder Hour Selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = "Jam Pengingat",
                                    tint = Color(0xFF93C5FD),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Jam Pengiriman:",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(7, 9, 12, 19).forEach { hour ->
                                    val selectedHour = notificationPrefs.reminderHour == hour
                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                onUpdateGlobalPrefs(
                                                    notificationPrefs.masterEnabled,
                                                    notificationPrefs.pushEnabled,
                                                    notificationPrefs.emailEnabled,
                                                    emailInput,
                                                    notificationPrefs.activeTimingsDays,
                                                    hour,
                                                    false
                                                )
                                            },
                                        color = if (selectedHour) CreditCardRoyalBlue else Color.White.copy(alpha = 0.14f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "%02d:00".format(hour),
                                            color = Color.White,
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // C. Notification Method Selector (Push Notification & Email)
                        Text(
                            text = "2. Pilih Metode Notifikasi (Push Notification / Email):",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Push Notification Toggle Card
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        val nextPush = !notificationPrefs.pushEnabled
                                        onUpdateGlobalPrefs(
                                            notificationPrefs.masterEnabled,
                                            nextPush,
                                            if (!nextPush && !notificationPrefs.emailEnabled) true else notificationPrefs.emailEnabled,
                                            emailInput,
                                            notificationPrefs.activeTimingsDays,
                                            notificationPrefs.reminderHour,
                                            false
                                        )
                                    }
                                    .testTag("method_toggle_push"),
                                color = if (notificationPrefs.pushEnabled) CreditCardRoyalBlue else Color.White.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = "Push Notification",
                                        tint = if (notificationPrefs.pushEnabled) ProgressGreen else Color.White.copy(alpha = 0.6f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Push Notif",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 12.5.sp,
                                            color = Color.White
                                        )
                                        Text(
                                            text = if (notificationPrefs.pushEnabled) "Aktif" else "Nonaktif",
                                            fontFamily = PlusJakartaSans,
                                            fontSize = 10.5.sp,
                                            color = Color(0xFFDCEBFF)
                                        )
                                    }
                                }
                            }

                            // Email Notification Toggle Card
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        val nextEmail = !notificationPrefs.emailEnabled
                                        onUpdateGlobalPrefs(
                                            notificationPrefs.masterEnabled,
                                            if (!nextEmail && !notificationPrefs.pushEnabled) true else notificationPrefs.pushEnabled,
                                            nextEmail,
                                            emailInput,
                                            notificationPrefs.activeTimingsDays,
                                            notificationPrefs.reminderHour,
                                            false
                                        )
                                    }
                                    .testTag("method_toggle_email"),
                                color = if (notificationPrefs.emailEnabled) CreditCardRoyalBlue else Color.White.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = "Email Notification",
                                        tint = if (notificationPrefs.emailEnabled) ProgressGreen else Color.White.copy(alpha = 0.6f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Email",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 12.5.sp,
                                            color = Color.White
                                        )
                                        Text(
                                            text = if (notificationPrefs.emailEnabled) "Aktif" else "Nonaktif",
                                            fontFamily = PlusJakartaSans,
                                            fontSize = 10.5.sp,
                                            color = Color(0xFFDCEBFF)
                                        )
                                    }
                                }
                            }
                        }

                        // Recipient Email Address Input (Visible when Email is active)
                        AnimatedVisibility(visible = notificationPrefs.emailEnabled) {
                            Column {
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedTextField(
                                    value = emailInput,
                                    onValueChange = {
                                        emailInput = it
                                        onUpdateGlobalPrefs(
                                            notificationPrefs.masterEnabled,
                                            notificationPrefs.pushEnabled,
                                            notificationPrefs.emailEnabled,
                                            it,
                                            notificationPrefs.activeTimingsDays,
                                            notificationPrefs.reminderHour,
                                            false
                                        )
                                    },
                                    label = {
                                        Text(
                                            "Alamat Email Penerima Pengingat",
                                            color = Color(0xFFDCEBFF),
                                            fontSize = 12.sp
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Email,
                                            contentDescription = "Email",
                                            tint = Color.White
                                        )
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedContainerColor = Color.White.copy(alpha = 0.12f),
                                        unfocusedContainerColor = Color.White.copy(alpha = 0.08f),
                                        focusedBorderColor = ProgressGreen,
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("recipient_email_input")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Action Buttons: Apply to All + Test Push + Test Email
                        Button(
                            onClick = {
                                onUpdateGlobalPrefs(
                                    notificationPrefs.masterEnabled,
                                    notificationPrefs.pushEnabled,
                                    notificationPrefs.emailEnabled,
                                    emailInput,
                                    notificationPrefs.activeTimingsDays,
                                    notificationPrefs.reminderHour,
                                    true
                                )
                                notificationStatusBanner = "✅ Pengaturan (${notificationPrefs.timingsSummary} • ${notificationPrefs.methodLabel}) diterapkan ke seluruh 40 utang aktif!"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ProgressGreen),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("apply_all_reminders_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Terapkan",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Terapkan Jadwal & Metode ke Semua Utang",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.5.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                        !ReminderScheduler.hasNotificationPermission(context)
                                    ) {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        val sent = onTriggerTestPush()
                                        notificationStatusBanner = if (sent) {
                                            "✅ Push Notification dikirim ke bar status Android!"
                                        } else {
                                            "Push Notification terjadwal pada jam %02d:00.".format(notificationPrefs.reminderHour)
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CreditCardRoyalBlue),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("test_notification_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = "Uji Push",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Uji Push Notif",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }

                            Button(
                                onClick = {
                                    val preview = onTriggerEmailReminder(false)
                                    emailPreviewPair = preview
                                    notificationStatusBanner = "📧 Pengingat Email dikirim ke ${emailInput.ifBlank { "arrprojectx@gmail.com" }}!"
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("test_email_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Uji Email",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Uji Kirim Email",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        if (notificationStatusBanner != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = notificationStatusBanner!!,
                                fontFamily = PlusJakartaSans,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ProgressGreen
                            )
                        }
                    }
                }
            }

            // 2. Interactive Monthly Due-Date Calendar Grid (1..28)
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = AppThemeColors.cardSurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Kalender Tanggal Jatuh Tempo",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.5.sp,
                                color = AppThemeColors.textPrimary
                            )
                            if (selectedDayOfMonth != 0) {
                                Text(
                                    text = "Tampilkan Semua",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = AppThemeColors.linkBlue,
                                    modifier = Modifier.clickable { selectedDayOfMonth = 0 }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        for (week in 0 until 4) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                for (col in 1..7) {
                                    val dayNum = week * 7 + col
                                    val countOnDay = dueDaysMap[dayNum]?.size ?: 0
                                    val isSelected = selectedDayOfMonth == dayNum
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    isSelected -> NavyCardDark
                                                    countOnDay > 0 -> AppThemeColors.menuTileBackground
                                                    else -> Color.Transparent
                                                }
                                            )
                                            .clickable {
                                                selectedDayOfMonth = if (isSelected) 0 else dayNum
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "$dayNum",
                                                fontFamily = PlusJakartaSans,
                                                fontWeight = if (countOnDay > 0 || isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                                fontSize = 12.5.sp,
                                                color = if (isSelected) Color.White else AppThemeColors.textPrimary,
                                                textAlign = TextAlign.Center
                                            )
                                            if (countOnDay > 0) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(5.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isSelected) ProgressGreen else AlertRed)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. Recent Notification Dispatch Log (Push & Email)
            if (dispatchLogs.isNotEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = AppThemeColors.cardSurface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = "Log Pengingat",
                                    tint = CreditCardRoyalBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Log Pengiriman Push Notification & Email",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = AppThemeColors.textPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            dispatchLogs.take(4).forEach { log ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 5.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                color = if (log.channel.contains("Email")) Color(0xFFE0F2FE) else Color(0xFFDCFCE7),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = "${log.channel} • ${log.timingTag}",
                                                    color = if (log.channel.contains("Email")) Color(0xFF0369A1) else Color(0xFF15803D),
                                                    fontFamily = PlusJakartaSans,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 10.sp,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = log.creditorName,
                                                fontFamily = PlusJakartaSans,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = AppThemeColors.textPrimary
                                            )
                                        }
                                        Text(
                                            text = log.messageSummary,
                                            fontFamily = PlusJakartaSans,
                                            fontSize = 11.5.sp,
                                            color = AppThemeColors.textSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. Per-Debt Due Date & Custom Reminder List
            item {
                Text(
                    text = if (selectedDayOfMonth == 0) {
                        "Daftar Jatuh Tempo Cicilan Aktif (${activeDebts.size})"
                    } else {
                        "Cicilan Jatuh Tempo Tanggal $selectedDayOfMonth (${activeDebts.size})"
                    },
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = AppThemeColors.textPrimary
                )
            }

            items(activeDebts, key = { it.id }) { debt ->
                val activeTimings = debt.parsedTimings
                val currentMethod = debt.channelMethod

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = AppThemeColors.cardSurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = debt.creditorName,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = AppThemeColors.textPrimary
                                )
                                Text(
                                    text = "Jatuh Tempo Tgl ${debt.dueDayOfMonth} • Cicilan ${CurrencyFormatter.formatRupiah(debt.monthlyInstallment)}",
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 12.5.sp,
                                    color = AppThemeColors.textSecondary
                                )
                            }
                            Surface(
                                color = if (debt.dueDayOfMonth <= 10) AlertRed.copy(alpha = 0.14f) else WarningAmber.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = "Tgl ${debt.dueDayOfMonth}",
                                    color = if (debt.dueDayOfMonth <= 10) AlertRed else Color(0xFFB45309),
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Multi-timing chips per debt (H-1, H-3, H-7)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(1, 3, 7).forEach { days ->
                                    val selected = debt.reminderEnabled && activeTimings.contains(days)
                                    FilterChip(
                                        selected = selected,
                                        onClick = {
                                            val newSet = if (selected && activeTimings.size > 1) {
                                                activeTimings - days
                                            } else {
                                                activeTimings + days
                                            }
                                            onUpdateSingleDebtReminder(
                                                debt,
                                                true,
                                                newSet,
                                                debt.reminderMethodCode
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = "H-$days",
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

                            // Per-debt Notification Method cycling pill (Push / Email / Push+Email)
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        val nextMethod = when (currentMethod) {
                                            NotificationChannelMethod.PUSH_AND_EMAIL -> NotificationChannelMethod.PUSH_ONLY
                                            NotificationChannelMethod.PUSH_ONLY -> NotificationChannelMethod.EMAIL_ONLY
                                            NotificationChannelMethod.EMAIL_ONLY -> NotificationChannelMethod.PUSH_AND_EMAIL
                                        }
                                        onUpdateSingleDebtReminder(
                                            debt,
                                            true,
                                            activeTimings,
                                            nextMethod.code
                                        )
                                    },
                                color = AppThemeColors.menuTileBackground,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (currentMethod.includesEmail) Icons.Default.Email else Icons.Default.NotificationsActive,
                                        contentDescription = currentMethod.label,
                                        tint = AppThemeColors.textPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = currentMethod.label,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = AppThemeColors.textPrimary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { onPayDebt(debt) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CreditCardRoyalBlue),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Payment,
                                contentDescription = "Bayar",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Bayar Cicilan Sekarang",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // Email Reminder Preview & External Email Launch Dialog
    if (emailPreviewPair != null) {
        val (subject, body) = emailPreviewPair!!
        AlertDialog(
            onDismissRequest = { emailPreviewPair = null },
            shape = RoundedCornerShape(24.dp),
            containerColor = AppThemeColors.cardSurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Email Pengingat",
                        tint = CreditCardRoyalBlue
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pengingat Email Terjadwal",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp,
                        color = AppThemeColors.textPrimary
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Kepada: ${emailInput.ifBlank { "arrprojectx@gmail.com" }}",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp,
                        color = AppThemeColors.linkBlue
                    )
                    Text(
                        text = "Subjek: $subject",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = AppThemeColors.textPrimary
                    )
                    Surface(
                        color = AppThemeColors.subtleSurface,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = body,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = AppThemeColors.textPrimary,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onTriggerEmailReminder(true)
                        emailPreviewPair = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CreditCardRoyalBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Buka Aplikasi Email",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { emailPreviewPair = null }) {
                    Text("Tutup", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
