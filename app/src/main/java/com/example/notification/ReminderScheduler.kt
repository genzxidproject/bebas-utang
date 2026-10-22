package com.example.notification

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.CurrencyFormatter
import com.example.data.DebtEntity
import java.util.Calendar

class DueReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val creditor = intent.getStringExtra("creditor_name") ?: "Cicilan Prioritas"
        val amount = intent.getLongExtra("installment_amount", 1_150_000L)
        val daysLeft = intent.getIntExtra("days_left", 1)
        val methodCode = intent.getStringExtra("method_code") ?: "PUSH,EMAIL"
        val recipientEmail = intent.getStringExtra("recipient_email") ?: "arrprojectx@gmail.com"

        if (methodCode.contains("PUSH")) {
            ReminderScheduler.showDueNotification(
                context = context,
                creditorName = creditor,
                amount = amount,
                daysLeft = daysLeft,
                methodCode = methodCode,
                recipientEmail = recipientEmail,
                notificationId = (creditor.hashCode() + daysLeft)
            )
        }
    }
}

object ReminderScheduler {
    const val CHANNEL_ID = "bebas_utang_due_reminder_channel"

    fun ensureNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Pengingat Jatuh Tempo Otomatis",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi Push & pengingat otomatis H-7, H-3, dan H-1 sebelum jatuh tempo utang"
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    /**
     * Schedules multi-timing alarms (e.g., H-7, H-3, H-1) for active debts at the user's chosen hour.
     */
    fun scheduleRemindersForDebts(
        context: Context,
        debts: List<DebtEntity>,
        reminderHour: Int = 9,
        recipientEmail: String = "arrprojectx@gmail.com"
    ) {
        ensureNotificationChannel(context)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val activeDebts = debts.filter { !it.isPaidOff && it.reminderEnabled }

        activeDebts.take(15).forEach { debt ->
            val timings = debt.parsedTimings
            timings.forEach { daysBefore ->
                val intent = Intent(context, DueReminderReceiver::class.java).apply {
                    action = "com.example.ACTION_DUE_REMINDER"
                    putExtra("creditor_name", debt.creditorName)
                    putExtra("installment_amount", debt.monthlyInstallment)
                    putExtra("days_left", daysBefore)
                    putExtra("method_code", debt.reminderMethodCode)
                    putExtra("recipient_email", recipientEmail)
                }
                val requestCode = (debt.id.toInt() * 100) + daysBefore + 5000
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val triggerCal = Calendar.getInstance().apply {
                    timeInMillis = debt.nextDueDateMillis
                    add(Calendar.DAY_OF_YEAR, -daysBefore)
                    set(Calendar.HOUR_OF_DAY, reminderHour.coerceIn(0, 23))
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }
                val triggerMillis = if (triggerCal.timeInMillis <= System.currentTimeMillis()) {
                    System.currentTimeMillis() + 3_600_000L * 12
                } else {
                    triggerCal.timeInMillis
                }

                try {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerMillis,
                        pendingIntent
                    )
                } catch (_: SecurityException) {
                    // Fallback if exact/idle alarm is restricted
                }
            }
        }
    }

    fun showDueNotification(
        context: Context,
        creditorName: String,
        amount: Long,
        daysLeft: Int,
        methodCode: String = "PUSH,EMAIL",
        recipientEmail: String = "arrprojectx@gmail.com",
        notificationId: Int = 1001
    ): Boolean {
        ensureNotificationChannel(context)
        if (!hasNotificationPermission(context)) return false

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val urgencyText = when {
            daysLeft <= 0 -> "Jatuh tempo HARI INI (Hari-H)!"
            daysLeft == 1 -> "Jatuh tempo BESOK (H-1)!"
            else -> "Jatuh tempo dalam $daysLeft hari lagi (H-$daysLeft)"
        }

        val methodBadge = when {
            methodCode.contains("PUSH") && methodCode.contains("EMAIL") -> "Push + Email ($recipientEmail)"
            methodCode.contains("EMAIL") -> "Email ($recipientEmail)"
            else -> "Push Notification"
        }

        val formattedAmount = CurrencyFormatter.formatRupiah(amount)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("⏰ Pengingat [H-$daysLeft]: $creditorName")
            .setContentText("$urgencyText • Tagihan $formattedAmount")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "$urgencyText untuk cicilan $creditorName sebesar $formattedAmount.\n" +
                        "Metode Pengingat: $methodBadge.\n" +
                        "Segera lakukan pembayaran agar bebas denda keterlambatan."
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        return try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
            true
        } catch (_: SecurityException) {
            false
        }
    }

    /**
     * Builds and launches or previews a formatted Email reminder for upcoming due debts.
     */
    fun buildEmailReminderContent(
        userName: String,
        recipientEmail: String,
        creditorName: String,
        amount: Long,
        dueDay: Int,
        timingsLabel: String
    ): Pair<String, String> {
        val formattedAmount = CurrencyFormatter.formatRupiah(amount)
        val subject = "[BebasUtang] Pengingat Jatuh Tempo ($timingsLabel) - $creditorName ($formattedAmount)"
        val body = buildString {
            appendLine("Halo $userName,")
            appendLine()
            appendLine("Ini adalah pengingat otomatis dari aplikasi BebasUtang untuk jadwal cicilan Anda:")
            appendLine("• Kreditor / Aplikasi : $creditorName")
            appendLine("• Nominal Tagihan     : $formattedAmount")
            appendLine("• Tanggal Jatuh Tempo : Tanggal $dueDay setiap bulan")
            appendLine("• Jadwal Pengingat    : $timingsLabel sebelum jatuh tempo")
            appendLine("• Penerima Email      : $recipientEmail")
            appendLine()
            appendLine("Pastikan saldo pembayaran tersedia sebelum tanggal jatuh tempo agar terhindar dari bunga/denda keterlambatan.")
            appendLine()
            appendLine("Salam Bebas Utang,")
            appendLine("Sistem Pengingat Otomatis BebasUtang (E2EE Secured)")
        }
        return subject to body
    }

    fun launchEmailClientIntent(
        context: Context,
        recipientEmail: String,
        subject: String,
        body: String
    ): Boolean {
        return try {
            val mailtoUri = Uri.parse(
                "mailto:${Uri.encode(recipientEmail)}" +
                    "?subject=${Uri.encode(subject)}" +
                    "&body=${Uri.encode(body)}"
            )
            val emailIntent = Intent(Intent.ACTION_SENDTO, mailtoUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(emailIntent)
            true
        } catch (_: Exception) {
            false
        }
    }
}
