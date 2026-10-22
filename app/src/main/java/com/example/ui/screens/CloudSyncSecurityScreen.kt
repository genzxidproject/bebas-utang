package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppScreen
import com.example.ui.CloudSyncState
import com.example.ui.theme.AppThemeColors
import com.example.ui.theme.CreditCardRoyalBlue
import com.example.ui.theme.HeaderBlueEnd
import com.example.ui.theme.HeaderBlueStart
import com.example.ui.theme.NavyCardDark
import com.example.ui.theme.PlusJakartaSans
import com.example.ui.theme.ProgressGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CloudSyncSecurityScreen(
    userName: String,
    monthlyIncome: Long,
    e2ePassphrase: String,
    keyFingerprint: String,
    isDarkMode: Boolean,
    onSetDarkMode: (Boolean) -> Unit,
    cloudSyncState: CloudSyncState,
    onSaveProfileAndKey: (String, Long, String) -> Unit,
    onSyncNow: () -> Unit,
    onImportSyncPacket: (String, String, (Boolean, String) -> Unit) -> Unit,
    onResetDemoData: () -> Unit,
    onClearAllData: (() -> Unit)? = null,
    onNavigate: (AppScreen) -> Unit,
    onAddDebtClick: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    var nameInput by remember(userName) { mutableStateOf(userName) }
    var incomeInput by remember(monthlyIncome) { mutableStateOf(monthlyIncome.toString()) }
    var keyInput by remember(e2ePassphrase) { mutableStateOf(e2ePassphrase) }
    var importCipherInput by remember { mutableStateOf("") }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }

    val formattedSyncTime = remember(cloudSyncState.lastSyncedMillis) {
        SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale("id", "ID"))
            .format(Date(cloudSyncState.lastSyncedMillis))
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
                    modifier = Modifier.testTag("back_button_pengaturan")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = Color.White
                    )
                }
                Column {
                    Text(
                        text = "Pengaturan, Tema & Keamanan E2EE",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.5.sp,
                        color = Color.White
                    )
                    Text(
                        text = "Dark/Light Mode • Cloud Sync • AES-256-GCM",
                        fontFamily = PlusJakartaSans,
                        fontSize = 12.5.sp,
                        color = Color(0xFFDCEBFF)
                    )
                }
            }
        }

        QuickFeatureJumpBar(
            currentScreen = AppScreen.PENGATURAN_CLOUD,
            onNavigate = onNavigate,
            onAddDebtClick = onAddDebtClick
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Dark Mode / Light Mode Appearance Card
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = AppThemeColors.cardSurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
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
                                    imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                                    contentDescription = "Tema Tampilan",
                                    tint = if (isDarkMode) Color(0xFFFDE047) else CreditCardRoyalBlue,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Tema Tampilan (Dark / Light Mode)",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.5.sp,
                                        color = AppThemeColors.textPrimary
                                    )
                                    Text(
                                        text = if (isDarkMode) {
                                            "Mode Gelap aktif • Nyaman di mata pada kondisi cahaya rendah"
                                        } else {
                                            "Mode Terang aktif • Tampilan cerah sesuai tema standar"
                                        },
                                        fontFamily = PlusJakartaSans,
                                        fontSize = 12.sp,
                                        color = AppThemeColors.textSecondary
                                    )
                                }
                            }
                            Switch(
                                checked = isDarkMode,
                                onCheckedChange = { onSetDarkMode(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = CreditCardRoyalBlue
                                ),
                                modifier = Modifier.testTag("settings_dark_mode_switch")
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            FilterChip(
                                selected = !isDarkMode,
                                onClick = { onSetDarkMode(false) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.LightMode,
                                        contentDescription = "Light Mode",
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = "Light Mode (Terang)",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CreditCardRoyalBlue,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("chip_light_mode")
                            )

                            FilterChip(
                                selected = isDarkMode,
                                onClick = { onSetDarkMode(true) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.DarkMode,
                                        contentDescription = "Dark Mode",
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = "Dark Mode (Gelap)",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NavyCardDark,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("chip_dark_mode")
                            )
                        }
                    }
                }
            }

            // 2. Live Cloud Sync & E2EE Vault Status Card
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CloudDone,
                                    contentDescription = "Cloud Aktif",
                                    tint = ProgressGreen,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Sinkronisasi Cloud Real-Time Aktif",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.5.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Sinkron terakhir: $formattedSyncTime",
                                        fontFamily = PlusJakartaSans,
                                        fontSize = 12.sp,
                                        color = Color(0xFFA3B8D8)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Surface(
                            color = Color.White.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = "Sidik Jari Kunci",
                                        tint = ProgressGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Sidik Jari Kunci AES-256: $keyFingerprint",
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Paket Terenkripsi E2EE: ${cloudSyncState.encryptedPayloadPreview.take(72)}...",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = Color(0xFF93C5FD),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    onSyncNow()
                                    feedbackMessage = "Sinkronisasi Cloud & Enkripsi ulang AES-256-GCM selesai!"
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ProgressGreen),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("sync_now_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = "Sinkronkan",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Sinkronkan",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }

                            Button(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(
                                        ClipData.newPlainText("BebasUtang E2EE Sync Packet", cloudSyncState.encryptedPayloadPreview)
                                    )
                                    feedbackMessage = "Paket Cloud Terenkripsi (E2E1) disalin ke Papan Klip untuk perangkat lain!"
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CreditCardRoyalBlue),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("copy_sync_packet_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Salin Paket E2EE",
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Salin Paket E2EE",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp
                                )
                            }
                        }

                        if (feedbackMessage != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = feedbackMessage!!,
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = ProgressGreen
                            )
                        }
                    }
                }
            }

            // 3. Profile & End-to-End Encryption Master Key Configuration
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = AppThemeColors.cardSurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profil",
                                tint = AppThemeColors.linkBlue
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Profil Pengguna & Kunci Enkripsi E2EE",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = AppThemeColors.textPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text("Nama Panggilan di Beranda") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("profile_name_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = incomeInput,
                            onValueChange = { incomeInput = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Pendapatan Bulanan (Rp) untuk Rasio Statistik") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = keyInput,
                            onValueChange = { keyInput = it },
                            label = { Text("Kunci Rahasia Enkripsi End-to-End (Passphrase)") },
                            leadingIcon = {
                                Icon(Icons.Default.Key, contentDescription = "Kunci E2E")
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("e2e_key_input")
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                val parsedIncome = incomeInput.toLongOrNull() ?: 18_500_000L
                                onSaveProfileAndKey(nameInput, parsedIncome, keyInput)
                                feedbackMessage = "Profil & Kunci Enkripsi E2EE berhasil disimpan!"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CreditCardRoyalBlue),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("save_profile_button")
                        ) {
                            Text(
                                text = "Simpan Profil & Perbarui Kunci E2EE",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // 4. Multi-Device Cloud Packet Import & Restore
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = AppThemeColors.cardSurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Pulihkan Perangkat Lain",
                                tint = CreditCardRoyalBlue
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Tarik Data dari Perangkat Lain (Multi-Device)",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.5.sp,
                                color = AppThemeColors.textPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Tempelkan paket terenkripsi (E2E1:...) dari ponsel atau tablet Anda yang lain untuk menyinkronkan seluruh data utang & statistik bulanan secara instan.",
                            fontFamily = PlusJakartaSans,
                            fontSize = 12.5.sp,
                            color = AppThemeColors.textSecondary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = importCipherInput,
                            onValueChange = { importCipherInput = it },
                            label = { Text("Tempel Paket Sinkronisasi Terenkripsi (E2E1:...)") },
                            maxLines = 3,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("import_cipher_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = {
                                    val packetToUse = importCipherInput.ifBlank { cloudSyncState.encryptedPayloadPreview }
                                    onImportSyncPacket(packetToUse, keyInput) { _, msg ->
                                        feedbackMessage = msg
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CreditCardRoyalBlue),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Dekripsi & Sinkronkan",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    onResetDemoData()
                                    feedbackMessage = "Berhasil mereset ke data awal Ricky (5/45 Lunas • Rp146.735.070)!"
                                },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.testTag("reset_demo_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Restore,
                                    contentDescription = "Reset",
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Reset Demo",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp
                                )
                            }

                            if (onClearAllData != null) {
                                OutlinedButton(
                                    onClick = {
                                        onClearAllData()
                                        feedbackMessage = "Semua data utang & catatan berhasil dikosongkan!"
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.testTag("clear_data_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Kosongkan",
                                        modifier = Modifier.size(17.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Kosongkan",
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
        }
    }
}
