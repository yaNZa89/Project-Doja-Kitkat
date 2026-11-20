# DoJa Emulator KitKat — Target Android 4.4 KitKat (API 19)

Proyek Android ini dikonfigurasi khusus untuk berjalan di **Android 4.4 KitKat (API 19)** tanpa dependensi berat sehingga ringan di perangkat RAM 512MB–1GB.

## Spesifikasi Build
- **Package ID**: `id.doja.kitkat.emulator`
- **Min / Target SDK**: API 19 (Android 4.4 KitKat)
- **Resolusi Default LCD**: `240x240`
- **Tema Keypad**: `foma-graphite`

## Cara 1 — Build APK Otomatis di Cloud (Tanpa Install Android Studio)
1. Buat repositori baru di GitHub dan unggah seluruh isi folder ZIP ini.
2. Buka tab **Actions** di GitHub repositori Anda, lalu jalankan workflow **Build DoJa KitKat APK (API 19)**.
3. Setelah selesai (~2 menit), unduh artefak **DoJa-KitKat-API19-Debug-APK** yang berisi file `app-debug.apk` siap pasang di HP Android 4.4 KitKat!

## Cara 2 — Build Lokal di PC (Android Studio / Gradle)
1. Ekstrak file ZIP ini.
2. Buka folder di **Android Studio** atau jalankan perintah terminal:
   ```bash
   gradle assembleDebug
   ```
3. File APK akan tersedia di:
   `app/build/outputs/apk/debug/app-debug.apk`

## Cara Pasang di Android 4.4 KitKat
- Salin `app-debug.apk` ke HP Android 4.4 Anda.
- Aktifkan **Setelan → Keamanan → Sumber Tidak Dikenal (Unknown Sources)**.
- Ketuk file APK untuk memasang, atau gunakan ADB: `adb install -r app-debug.apk`.
