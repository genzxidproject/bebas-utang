<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://ai.google.dev/static/site-assets/images/share-ais-513315318.png" />
</div>

# BebasUtang - Catatan Utang Pintar & E2EE Cloud Sync

Aplikasi Android untuk manajemen utang, kalkulator strategi Snowball, pengingat jatuh tempo otomatis (H-7, H-3, H-1), dan sinkronisasi cloud terenkripsi end-to-end (AES-256-GCM).

## Fitur Utama
1. **End-to-End Encryption (E2EE)**: Data catatan privat dan payload cloud dienkripsi menggunakan AES-256-GCM dengan turunan kunci PBKDF2-HMAC-SHA256 langsung di perangkat.
2. **Kalkulator Strategi Snowball**: Pengurutan prioritas pelunasan utang berbasis suku bunga dan pokok untuk meminimalkan beban finansial.
3. **Pengingat Jatuh Tempo (Alarm & Notifikasi)**: Menjadwalkan pengingat otomatis lokal menggunakan `AlarmManager` dan Notifikasi Android.
4. **Laporan & Statistik Bulanan**: Visualisasi ringkasan cicilan dan pengeluaran rutin bulanan.

## Cara Menjalankan di Android Studio

1. Buka **Android Studio** (Koala / Ladybug atau versi terbaru).
2. Pilih **Open** dan arahkan ke folder proyek ini.
3. Sinkronkan Gradle (`Sync Project with Gradle Files`).
4. Jalankan aplikasi pada emulator atau perangkat fisik (Debug mode langsung menggunakan debug keystore default bawaan SDK).
