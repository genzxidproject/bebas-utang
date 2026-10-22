package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CurrencyFormatter
import com.example.data.DebtEntity
import com.example.ui.AppScreen
import com.example.ui.theme.ActionBlueEnd
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AppThemeColors
import com.example.ui.theme.CreditCardRoyalBlue
import com.example.ui.theme.HeaderBlueEnd
import com.example.ui.theme.HeaderBlueStart
import com.example.ui.theme.NavyCardDark
import com.example.ui.theme.PlusJakartaSans
import com.example.ui.theme.ProgressGreen

@Composable
fun DebtListScreen(
    debts: List<DebtEntity>,
    selectedCategory: String,
    searchQuery: String,
    autoFocusSearch: Boolean = false,
    onCategoryChange: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onPayClick: (DebtEntity) -> Unit,
    onDeleteClick: (DebtEntity) -> Unit,
    onAddDebtClick: () -> Unit,
    onNavigate: (AppScreen) -> Unit,
    decryptNote: (String) -> String,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    var statusFilter by remember { mutableStateOf("Semua") }
    var inspectEncryptedCipher by remember { mutableStateOf(false) }

    val categories = listOf("Semua", "Pinjol", "Kartu Kredit", "Paylater", "Pinjaman Pribadi")

    val filteredDebts = remember(debts, selectedCategory, statusFilter, searchQuery) {
        debts.filter { debt ->
            val matchCat = selectedCategory == "Semua" || debt.category.equals(selectedCategory, ignoreCase = true)
            val matchStatus = when (statusFilter) {
                "Aktif" -> !debt.isPaidOff
                "Lunas" -> debt.isPaidOff
                else -> true
            }
            val matchQuery = searchQuery.isBlank() ||
                debt.creditorName.contains(searchQuery, ignoreCase = true) ||
                debt.category.contains(searchQuery, ignoreCase = true)
            matchCat && matchStatus && matchQuery
        }
    }

    val totalFilteredRemaining = remember(filteredDebts) {
        filteredDebts.filter { !it.isPaidOff }.sumOf { it.remainingAmount }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppThemeColors.screenBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Gradient App Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(HeaderBlueStart, HeaderBlueEnd)
                        )
                    )
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier.testTag("back_button_rincian")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Kembali",
                                    tint = Color.White
                                )
                            }
                            Column {
                                Text(
                                    text = if (autoFocusSearch) "Pencarian & Filter Utang" else "Rincian Utang (${filteredDebts.size})",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 19.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "Sisa Aktif: ${CurrencyFormatter.formatRupiah(totalFilteredRemaining)}",
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.5.sp,
                                    color = Color(0xFFDCEBFF)
                                )
                            }
                        }

                        // Toggle E2EE Inspector Chip
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { inspectEncryptedCipher = !inspectEncryptedCipher }
                                .testTag("toggle_e2e_inspector"),
                            color = if (inspectEncryptedCipher) ProgressGreen else Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Inspeksi Enkripsi E2E",
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (inspectEncryptedCipher) "Ciphertext" else "AES-256",
                                    color = Color.White,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = PlusJakartaSans
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = {
                            Text(
                                "Cari nama aplikasi pinjol, bank, atau kartu kredit...",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.75f),
                                fontFamily = PlusJakartaSans
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Cari",
                                tint = Color.White
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Hapus pencarian",
                                        tint = Color.White
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color.White.copy(alpha = 0.16f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.12f),
                            focusedBorderColor = Color.White.copy(alpha = 0.6f),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.25f),
                            cursorColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("debt_search_input")
                    )
                }
            }

            // Quick Feature Jump Bar for effortless navigation
            QuickFeatureJumpBar(
                currentScreen = AppScreen.RINCIAN_UTANG,
                onNavigate = onNavigate,
                onAddDebtClick = onAddDebtClick
            )

            // Category & Status Filter Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Semua", "Aktif", "Lunas").forEach { status ->
                    FilterChip(
                        selected = statusFilter == status,
                        onClick = { statusFilter = status },
                        label = {
                            Text(
                                text = status,
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NavyCardDark,
                            selectedLabelColor = Color.White
                        )
                    )
                }

                categories.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory.equals(cat, ignoreCase = true),
                        onClick = { onCategoryChange(cat) },
                        label = {
                            Text(
                                text = if (cat == "Semua") "Semua Kategori" else cat,
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CreditCardRoyalBlue,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Debt List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredDebts, key = { it.id }) { debt ->
                    val note = remember(debt.encryptedNotes, inspectEncryptedCipher) {
                        if (inspectEncryptedCipher) debt.encryptedNotes else decryptNote(debt.encryptedNotes)
                    }
                    DebtItemCard(
                        debt = debt,
                        showCiphertext = inspectEncryptedCipher,
                        decryptedNote = note,
                        onPayClick = { onPayClick(debt) },
                        onDeleteClick = { onDeleteClick(debt) }
                    )
                }
            }
        }

        // FAB to Add New Debt
        FloatingActionButton(
            onClick = onAddDebtClick,
            containerColor = CreditCardRoyalBlue,
            contentColor = Color.White,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("fab_add_debt")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Utang")
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Tambah Utang",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun DebtItemCard(
    debt: DebtEntity,
    showCiphertext: Boolean,
    decryptedNote: String,
    onPayClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val timingsLabel = remember(debt.reminderTimingsCsv) {
        debt.parsedTimings.sorted().joinToString("/") { if (it == 0) "Hari-H" else "H-$it" }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("debt_card_${debt.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppThemeColors.cardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = if (debt.category.equals("Pinjol", true)) NavyCardDark else CreditCardRoyalBlue,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = debt.category,
                                color = Color.White,
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        if (debt.isPaidOff) {
                            Surface(
                                color = Color(0xFFDCFCE7),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Lunas",
                                        tint = Color(0xFF15803D),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "LUNAS",
                                        color = Color(0xFF15803D),
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 10.5.sp
                                    )
                                }
                            }
                        } else if (debt.reminderEnabled) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (debt.channelMethod.includesEmail) Icons.Default.Email else Icons.Default.NotificationsActive,
                                    contentDescription = "Pengingat Aktif",
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Tgl ${debt.dueDayOfMonth} ($timingsLabel • ${debt.channelMethod.label})",
                                    color = Color(0xFFF59E0B),
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = debt.creditorName,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp,
                        color = AppThemeColors.textPrimary
                    )
                }

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Hapus Utang",
                        tint = AlertRed.copy(alpha = 0.75f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(AppThemeColors.menuTileBackground)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(debt.progressFraction.coerceIn(0.05f, 1f))
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(if (debt.isPaidOff) ProgressGreen else CreditCardRoyalBlue)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Sisa Utang",
                        fontFamily = PlusJakartaSans,
                        fontSize = 11.5.sp,
                        color = AppThemeColors.textSecondary
                    )
                    Text(
                        text = CurrencyFormatter.formatRupiah(debt.remainingAmount),
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.5.sp,
                        color = if (debt.isPaidOff) ProgressGreen else AppThemeColors.textPrimary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Cicilan / bln (${debt.interestRatePercent}%)",
                        fontFamily = PlusJakartaSans,
                        fontSize = 11.5.sp,
                        color = AppThemeColors.textSecondary
                    )
                    Text(
                        text = CurrencyFormatter.formatRupiah(debt.monthlyInstallment),
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = AppThemeColors.linkBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Encrypted E2EE Note Row
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = AppThemeColors.subtleSurface,
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Terenkripsi E2EE",
                        tint = AppThemeColors.linkBlue,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (showCiphertext) debt.encryptedNotes else decryptedNote,
                        fontFamily = if (showCiphertext) FontFamily.Monospace else PlusJakartaSans,
                        fontSize = 11.5.sp,
                        color = AppThemeColors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (!debt.isPaidOff) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onPayClick,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ActionBlueEnd),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("pay_debt_button_${debt.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Payment,
                        contentDescription = "Bayar Cicilan",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Bayar Cicilan / Lunasi",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp
                    )
                }
            }
        }
    }
}
